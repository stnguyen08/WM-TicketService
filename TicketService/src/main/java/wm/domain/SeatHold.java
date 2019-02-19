package wm.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author ngtson@gmail.com
 *
 */
public class SeatHold {
	// held Id or reserved Id
	int id;
	int numSeats;
	String customerEmail;
	List<Seat> seatList = new ArrayList<>();
	
	public SeatHold() {
	}
	
	public SeatHold(int id, int numSeats, String customerEmail) {
		this.id = id;
		this.numSeats = numSeats;
		this.customerEmail = customerEmail;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getNumSeats() {
		return numSeats;
	}
	
	public String getCustomerEmail() {
		return customerEmail;
	}

	public List<Seat> getSeatList() {
		return seatList;
	}
	
	public void setSeat(Seat seat) {
		seatList.add(seat);
	}
	
	public synchronized void setState(State newState) {
		for (Seat seat : seatList) {
			seat.setState(newState);
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("SeatHold [id=" + id + ", numSeats=" + numSeats + ", customerEmail=" + customerEmail + "\n");
		sb.append("[" + seatList +"]");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SeatHold other = (SeatHold) obj;
		if (customerEmail == null) {
			if (other.customerEmail != null)
				return false;
		} else if (!customerEmail.equals(other.customerEmail))
			return false;
		/*if (id != other.id)
			return false;*/
		if (numSeats != other.numSeats)
			return false;
		if (seatList == null) {
			if (other.seatList != null)
				return false;
		} else if (!seatList.equals(other.seatList))
			return false;
		return true;
	}
	
	
}
