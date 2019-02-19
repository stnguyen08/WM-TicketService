package wm.repository;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;


import wm.domain.Seat;


/**
 * 
 * @author ngtson@gmail.com
 *
 */
@Repository
public class TicketRepo {

	@Value("${seatInfoFile}")
	String seatInfoFileName; // = "SeatInfo.dat";
		
	public TicketRepo() {
		//System.out.println("[TicketRepo] initiating .... ");
	}
	
	/**
	 * Store a seat map to file system
	 * 
	 * @param seatMap 2D array seat map
	 * @throws Exception An exception 
	 */
	public void storeData(Seat[][] seatMap) throws Exception {
		System.out.println("[storeData] Store seat data to file " + seatInfoFileName);
		//printSeatInfo(seatMap);  // testing
		FileOutputStream fos = new FileOutputStream(seatInfoFileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(seatMap);
		fos.close();
	}

	/**
	 * Restore a seat map from stored file system
	 * 
	 * @return 2D array seat map
	 * @throws Exception An exception
	 */
	public Seat[][] restoreData() throws Exception {
		System.out.println("[restoreData] Restore seat data from file " + seatInfoFileName);
		FileInputStream fis = new FileInputStream(seatInfoFileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		Seat[][] seatMap = (Seat[][]) ois.readObject();
		fis.close();
		//printSeatInfo(seatMap);  // testing
		return seatMap;
	}
	
	void printSeatInfo(Seat[][] seatMap) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < seatMap.length; i++) {
			sb.append(i + ": [" + Arrays.toString(seatMap[i]) + "]\n");
		}
		System.out.println(sb.toString());
	}
	
}
