package wm;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import wm.controller.CustomErrorType;
import wm.domain.Seat;
import wm.domain.SeatHold;
import wm.domain.State;
import wm.domain.User;

//@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TicketServiceApplicationTests {
	@Autowired
	private TestRestTemplate restTemplate;
	
	String numSeatsUrl = "http://localhost:8080/numSeatsAvailable";
	String findAndHoldSeatsUrl = "http://localhost:8080/findAndHoldSeats/%d/%s";
	String findHoldSeatsUrl = "http://localhost:8080/findHoldSeats";
	String reserveSeatssUrl = "http://localhost:8080/reserveSeats/%d/%s";
	String printUrl = "http://localhost:8080/printSeatMap";
	String storeUrl = "http://localhost:8080/store";
	String restoreUrl = "http://localhost:8080/restore";
	int totalSeats = 100;
	
	@BeforeClass
	public static void setup() {
		System.out.println("setup() ....");
		File file = new File("SeatInfo.dat");
		if(file.delete()) 
			System.out.println("Data file deleted successfully"); 
		else
			System.out.println("Failed to delete the file"); 
	}
	
	/**
	 * First check if the number of available seats is 100
	 */
	@Test
	public void T1_getNumSeatsAvailable() {
		int  numSeats = restTemplate.getForObject(numSeatsUrl, Integer.class);
		System.out.println("\n[T1_getNumSeatsAvailable] Num of available seats: " + numSeats);
		assertThat(numSeats).isEqualTo(totalSeats);
	}

	/**
	 * Hold and reserve 5 seats successfully
	 */
	@Test
	public void T2_findAndHoldSeats_Reserve() {
		int numSeats = 5;
		String email = "a@b.com";
		User user = new User(numSeats, email);
		SeatHold seatHold = restTemplate.postForObject(findHoldSeatsUrl, user, SeatHold.class);
		//SeatHold seatHold = restTemplate.getForObject(String.format(findAndHoldSeatsUrl, numSeats, email), SeatHold.class);
		int holdId = seatHold.getId();
		SeatHold expected = new SeatHold(0, numSeats, email);
		expected.setSeat(new Seat(2, 0, State.HELD));
		expected.setSeat(new Seat(2, 1, State.HELD));
		expected.setSeat(new Seat(2, 2, State.HELD));
		expected.setSeat(new Seat(2, 3, State.HELD));
		expected.setSeat(new Seat(2, 4, State.HELD));
		System.out.println("\n[T2_findAndHoldSeats_Reserve] SeatHold: " + seatHold);
		assertThat(seatHold).isEqualTo(expected);
		//System.out.println("[T2_findAndHoldSeats_Reserve] holdId: " + holdId);
		String confirmId = restTemplate.getForObject(String.format(reserveSeatssUrl, holdId, email), String.class);
		System.out.println("[T2_findAndHoldSeats_Reserve] confirmation Id: " + confirmId);
		assertThat(confirmId).doesNotContain("Cannot reserve seats");
	}
	
	/**
	 * Hold another 6 seats, wait for timeout, cannot reserve
	 */
	@Test
	public void T3_findAndHoldSeats_Timeout() throws Exception {
		String email = "ab@c.com";
		int numRequestedSeats = 6;
		SeatHold seatHold = restTemplate.getForObject(String.format(findAndHoldSeatsUrl, numRequestedSeats, email), SeatHold.class);
		int holdId = seatHold.getId();
		SeatHold expected = new SeatHold(0, numRequestedSeats, email);
		expected.setSeat(new Seat(2, 5, State.HELD));
		expected.setSeat(new Seat(2, 6, State.HELD));
		expected.setSeat(new Seat(2, 7, State.HELD));
		expected.setSeat(new Seat(2, 8, State.HELD));
		expected.setSeat(new Seat(2, 9, State.HELD));
		expected.setSeat(new Seat(3, 0, State.HELD));
		System.out.println("\n[T3_findAndHoldSeats_Timeout] SeatHold: " + seatHold);
		assertThat(seatHold).isEqualTo(expected);
		System.out.println("[T3_findAndHoldSeats_Timeout] Waiting for timeout ...");
		Thread.sleep(20000);
		// Reserve failed after timeout
		String confirmId = restTemplate.getForObject(String.format(reserveSeatssUrl, holdId, email), String.class);
		System.out.println("[T3_findAndHoldSeats_Timeout] confirmation Id: " + confirmId);
		assertThat(confirmId).contains("errorMessage");
	}
	
	/**
	 * After reserving 5 seats and timing out 6 seats. Total seats remains 95
	 */
	@Test
	public void T4_getNumSeatsAvailable() {
		int  numSeats = restTemplate.getForObject(numSeatsUrl, Integer.class);
		System.out.println("\n[T4_getNumSeatsAvailable] Num of available seats: " + numSeats);
		assertThat(numSeats).isEqualTo(95);
	}
	
	/**
	 * Check invalid inputs of number of seats to find and hold.
	 */
	@Test
	public void T5_findAndHoldSeats_Invalid() {
		// numSeats invalid
		User user = new User(0, "a@b.com");
		String seatHold = restTemplate.postForObject(findHoldSeatsUrl, user, String.class);
		System.out.println("\n[T5_findAndHoldSeats_Invalid] numSeats invalid: " + seatHold);
		assertThat(seatHold).contains("errorMessage");
		// email invalid
		user = new User(5, "ab.com");
		seatHold = restTemplate.postForObject(findHoldSeatsUrl, user, String.class);
		System.out.println("[T5_findAndHoldSeats_Invalid] email invalid: " + seatHold);
		assertThat(seatHold).contains("errorMessage");
		
	}
	
	/**
	 * Test print, store and restore
	 */
	@Test
	public void T6_printStoreAndRestore() {
		System.out.println("\n[T6] print the seat map info");
		String res = restTemplate.getForObject(printUrl, String.class);
		assertThat(res).contains("Print Seat Map");
		
		System.out.println("[T6] store the seat map info to file");
		res = restTemplate.getForObject(storeUrl, String.class);
		assertThat(res).contains("store data");
		
		System.out.println("[T6] restore the seat map info from file");
		res = restTemplate.getForObject(restoreUrl, String.class);
		assertThat(res).contains("restore data");
	}
	
	@AfterClass
	public static void tearDown() {
		File file = new File("SeatInfo.dat");
		if(file.delete()) 
			System.out.println("Data file deleted successfully"); 
		else
			System.out.println("Failed to delete the file"); 
	}
}

