package wm.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import wm.domain.Seat;
import wm.domain.SeatHold;
import wm.domain.State;
import wm.service.TicketService;
import wm.repository.TicketRepo;

/**
 * 
 * @author ngtson@gmail.com
 *
 */
@Service
public class TicketServiceImpl implements TicketService {
	@Autowired
	TicketRepo ticketRepo;
	
	static final int ROWS = 10; 
	static final int COLUMNS = 10;
	
	// Assuming that the best seats are from rows 2 to 6 if ROWS is 10 for example.
	static final int BEST_ROW_FROM = 2;
	static final int BEST_ROW_TO = 6;
	
	// Data store timer
	@Value("${data.store.delay}")
	int dataStoreDelay;	// multiple 60*1000 in milliseconds
	@Value("${data.store.period}")
	int dataStorePeriod; 	// multiple 60*1000 in milliseconds

	// timeout for seat hold
	@Value("${seat.hold.timeout}")
	int seatHoldTimeout;
	
	Seat[][] seatMap;
	int numSeatsAvail;
	boolean isInit;
	
	// contain the best available seats.
	PriorityQueue<Seat> bestAvailSeats = new PriorityQueue<>();
	// contain the normal available seats (not the best available seats).
	PriorityQueue<Seat> normalAvailSeats = new PriorityQueue<>();
	
	// held seats: <holdId, SeatHold>
	TreeMap<Integer, SeatHold> heldSeats = new TreeMap<>();
	//  Reserved seats: <reservedId, SeatHold>. Use the same SeatHold data for a reservation.
	TreeMap<Integer, SeatHold> reservedSeats= new TreeMap<>();
	// keep hold and its timer <holdId, Timer>. Used to cancel the timer when the hold is reserved.
	TreeMap<Integer, Timer> holdTimers = new TreeMap<>();
	
	
	public TicketServiceImpl() {
		/*try {
			seatMap = ticketRepo.restoreData();
			checkHeldAndReservedState();
		} catch (Exception e) {
			System.out.println("[TicketRepo] [ERROR] cannot restore seat info from file. Init empty data instead. " + e.getMessage());
			init();
		}*/
		//restore();
	}
	
	@PostConstruct
	void init() {
		seatHoldTimeout *= 1000; // seconds to milliseconds
		dataStoreDelay *= 60*1000; // minutes to milliseconds
		dataStorePeriod *= 60*1000; // minutes to milliseconds
		/*if (BEST_ROW_FROM < 0 || BEST_ROW_FROM >= BEST_ROW_TO || BEST_ROW_FROM > ROWS) {
			System.out.println("[Warning] input BEST_ROW_FROM invalid. Initiate it with default value.");
			BEST_ROW_FROM = (int) (ROWS-ROWS*0.8);
		}
		if (BEST_ROW_TO < 0 || BEST_ROW_TO < BEST_ROW_FROM || BEST_ROW_TO > ROWS) {
			System.out.println("[Warning] input BEST_ROW_TO invalid. Initiate it with default value.");
			BEST_ROW_TO = (int) (ROWS-ROWS*0.2);
		}*/
		numSeatsAvail = ROWS * COLUMNS;
		System.out.println(String.format("[init] ROWS=%d, COLUMNS=%d, BEST_ROW_FROM=%d, BEST_ROW_TO=%d, numSeatsAvail=%d", ROWS, COLUMNS, BEST_ROW_FROM, BEST_ROW_TO, numSeatsAvail));
		System.out.println(String.format("[init] seatHoldTimeout=%d sec, dataStoreDelay=%d min, dataStorePeriod=%d min", seatHoldTimeout/1000, dataStoreDelay/60/1000, dataStorePeriod/60/1000));
		isInit = true;
		restore();
	}
	
	void restore() {
		//System.out.println("[restore] Calling restore() ....");
		restoreData();
		//checkHeldAndReservedState();
		// start and schedule for data store task
		//startDataStoreSchedule();
	}

	void initSeatMap() {
		seatMap = new Seat[ROWS][COLUMNS];
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++) {
				seatMap[i][j] = new Seat(i, j);
				if (isTheBestRow(i))
					bestAvailSeats.offer(seatMap[i][j]);
				else
					normalAvailSeats.offer(seatMap[i][j]);
			}
		}
	}
	
	boolean isTheBestRow(int row) {
		return (BEST_ROW_FROM <= row && row <= BEST_ROW_TO);
	}
	
	/**
	 * Start a schedule for seat map store
	 * 
	 */
	void startDataStoreSchedule() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				/*try {
					ticketRepo.storeData(seatMap);
				} catch (Exception e) {
					System.out.println("[storeDataTask] [ERROR] Store seat info from file has an error. " + e.getMessage());
				}*/
				storeData();
			}
	    }, dataStoreDelay, dataStorePeriod);
	}
	
	/**
	 * after restore, check and reset held state to available, and add available states to bestAvailSeats and normalAvailSeats
	 */
	void checkHeldAndReservedState() {
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++) {
				// reset Held states after restore
				if (seatMap[i][j].getState() == State.HELD) {
					seatMap[i][j].setState(State.AVAIL);
					//System.out.println(String.format("[cleanUpHeldAndRestoreReserved] Set the held state back to Avail of seat[%d,%d]", i, j));
				}
				// update num of avail seats
				if (seatMap[i][j].getState() == State.RESERVED) {
					numSeatsAvail--;
				}
				if (seatMap[i][j].getState() == State.AVAIL)
					if (isTheBestRow(i))
						bestAvailSeats.offer(seatMap[i][j]);
					else
						normalAvailSeats.offer(seatMap[i][j]);
			}
		}
	}
	
	@Override
	public int numSeatsAvailable() {
		return numSeatsAvail;
	}

	@Override
	public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
		if (numSeats <= 0 || numSeats > numSeatsAvail)
			return null;
		int holdId = generateUniqueId();
		SeatHold seatHold = new SeatHold(holdId, numSeats, customerEmail);
		// find the best seats available first
		int remainingSeats = findAvailableSeats(numSeats, bestAvailSeats, seatHold);
		// continue to find normal seats available
		findAvailableSeats(remainingSeats, normalAvailSeats, seatHold);
		heldSeats.put(holdId, seatHold);
		// start a schedule to check if it is expired, set the Held state back to Avail
		Timer timer = new Timer();
	    timer.schedule(new SeatHoldExpired(holdId), seatHoldTimeout);
	    holdTimers.put(holdId, timer);
		return seatHold;
	}
	
	/**
	 * find and remove seats available from queue, set the seat to held state and assign it to the seat hold
	 * 
	 * @param numSeats number of requested seats
	 * @param queue the best or normal queue
	 * @param seatHold a seat hold
	 * @return number of remaining seats (not hold yet)
	 */
	synchronized int findAvailableSeats(int numSeats, Queue<Seat> queue, SeatHold seatHold) {
		while (numSeats > 0) {
			Seat seat = queue.poll();
			if (seat == null) // no seat available
				break;
			seat.setState(State.HELD);
			seatHold.setSeat(seat);
			numSeats--;
			numSeatsAvail--;
		}
		return numSeats;
	}
	
	int generateUniqueId() {
		return (int) (System.currentTimeMillis() & 0xFFFFFFF);
	}

	@Override
	public String reserveSeats(int seatHoldId, String customerEmail) {
		System.out.println("[reserveSeats] reserve seats with hold id: " + seatHoldId);
		SeatHold seatHold = heldSeats.remove(seatHoldId);
		if (seatHold == null)
			return null;
		
		// Cancel the hold timer thread
		Timer timer = holdTimers.remove(seatHoldId);
		if (timer != null) {
			System.out.println("[reserveSeats] cancel the hold timer thread because the seats are reserved");
			timer.cancel();
			timer = null; // help GC
		}
		
		int reservedId = generateUniqueId();
		seatHold.setId(reservedId);
		seatHold.setState(State.RESERVED);
		reservedSeats.put(reservedId, seatHold);
		storeData();
		return String.valueOf(reservedId);
	}

	@Override
	public void printSeatMap() {
		System.out.println(toString());
	}
	
	@Override
	public void storeData() {
		try {
			synchronized (seatMap) {
				//System.out.println("[storeData] store seat map info to file system");
				ticketRepo.storeData(seatMap);
			}
		} catch (Exception e) {
			System.out.println("[storeData] [ERROR] Store seat info from file has an error. " + e.getMessage());
		}
	}

	@Override
	public void restoreData() {
		try {
			//synchronized (seatMap) {
				//System.out.println("[restoreData] restore seat map info from file system");
				if (isInit) {
					// the system starts up, get seat data from file.
					seatMap = ticketRepo.restoreData();
					checkHeldAndReservedState();
					isInit = false;
				} else {
					// Expose this API at runtime for TESTING ONLY. Because data from the file
					// might be obsolete to current running data. For TESTING, only read and print it
					Seat[][] seatMapTemp = ticketRepo.restoreData();
					StringBuffer sb = new StringBuffer();
					sb.append("[TicketServiceImpl] Seat map info is restored from file:\n");
					for (int i = 0; i < ROWS; i++) {
						sb.append(i + ": [" + Arrays.toString(seatMapTemp[i]) + "]\n");
					}
					System.out.println(sb.toString());
				}
			//}
		} catch (Exception e) {
			System.out.println("[restoreData] [ERROR] cannot restore seat info from file. Init empty data instead. " + e.getMessage());
			isInit = false;
			initSeatMap();
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("TicketServiceImpl [numAvail=" + numSeatsAvail + "\n");
		for (int i = 0; i < ROWS; i++) {
			sb.append(i + ": [" + Arrays.toString(seatMap[i]) + "]\n");
		}
		return sb.toString();
	}
	
	/**
	 * Timer task checks if a seat hold is expired. 
	 * It will then cancel the timer thread and reset Help seats back to Available states.
	 */
	class SeatHoldExpired extends TimerTask {
		int holdId;
		
		SeatHoldExpired(int holdId) {
			this.holdId = holdId;
		}

		@Override
		public void run() {
			System.out.println(String.format("[SeatHoldExpired] the seat hold with Id [%d] is expired. Set the Held state back to Available", holdId));
			SeatHold seatHold = heldSeats.remove(holdId);
			if (seatHold != null) {
				List<Seat> seats = seatHold.getSeatList();
				synchronized (seatMap) {
					for (Seat seat : seats) {
						if (seat.getState() == State.HELD) {
							seat.setState(State.AVAIL);
							// put the seat back to best available queue
							if (isTheBestRow(seat.getRow()))
								bestAvailSeats.offer(seat);
							else // put the seat back to normal available queue
								normalAvailSeats.offer(seat);
							numSeatsAvail++;
						}
					}
				}
				Timer timer = holdTimers.remove(holdId);
				if (timer != null) {
					//System.out.println("[SeatHoldExpired] cancel the hold timer thread because the seats are expired");
					timer.cancel();
					timer = null; // help GC
				}
				seatHold = null; // help GC
			}
			else 
				System.out.println(String.format("[SeatHoldExpired] [ERROR] Cannot set state of the seat hold with Id [%d] to Available because of some error", holdId));
		}
	}

}