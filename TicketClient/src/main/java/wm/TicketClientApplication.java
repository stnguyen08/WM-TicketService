package wm;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;



import wm.domain.SeatHold;
import wm.domain.User;

/**
 * 
 * @author ngtson@gmail.com
 *
 */
@SpringBootApplication
public class TicketClientApplication implements CommandLineRunner {
	
	@Autowired
	private RestOperations  restTemplate;

	public static void main(String[] args) {
		SpringApplication.run(TicketClientApplication.class, args);
	}
	
	@Bean
	RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
		return restTemplate;
	}

	@Override
	public void run(String... args) throws Exception {
		String numSeatsUrl = "http://localhost:8080/numSeatsAvailable";
		String findAndHoldSeatsUrl = "http://localhost:8080/findAndHoldSeats/%d/%s";
		String findHoldSeatsUrl = "http://localhost:8080/findHoldSeats";
		String reserveSeatssUrl = "http://localhost:8080/reserveSeats/%d/%s";
		String printUrl = "http://localhost:8080/printSeatMap";
		String storeUrl = "http://localhost:8080/store";
		String restoreUrl = "http://localhost:8080/restore";
		
		String email;
		Scanner sn = new Scanner(System.in);
		int n;
		while (true) {
			try {
				System.out.println("------------------------------------------------------");
				System.out.println("Below is input info for testing Ticket Service. Input:");
				System.out.println("1-numSeatsAvailable 2-findAndHoldSeats 3-reserveSeats 4-printSeatMap 5-forceToStoreData 6-forceToRestoreData 0-exit");
				n = sn.nextInt();
				switch (n) {
					case 1:
						int num = restTemplate.getForObject(numSeatsUrl, Integer.class);
						System.out.println("The number of seats available: " + num);
						break;
					case 2:
						System.out.print("Input number of seats you want to hold: ");
						int seats = sn.nextInt();
						System.out.print("Input your email address: ");
						email = sn.next();
						//SeatHold seatHold = restTemplate.getForObject(String.format(findAndHoldSeatsUrl, seats, email), SeatHold.class);
						User user = new User(seats, email);
						SeatHold seatHold = restTemplate.postForObject(findHoldSeatsUrl, user, SeatHold.class);
						//ResponseEntity<?> seatHold = restTemplate.postForEntity(findHoldSeatsUrl, user, SeatHold.class);
						/*HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.APPLICATION_JSON);
						HttpEntity<Object> entity = new HttpEntity<Object>(headers);
						ResponseEntity<String> seatHold = restTemplate.exchange(findHoldSeatsUrl, HttpMethod.POST, entity, String.class);*/
						System.out.println("Your seat hold information will be expired in some seconds if you will not reserve.");
						System.out.println(seatHold);
						break;
					case 3:
						System.out.print("Input your seat hold Id: ");
						int id = sn.nextInt();
						System.out.print("Input your email address: ");
						email = sn.next();
						String confirm = restTemplate.getForObject(String.format(reserveSeatssUrl, id, email), String.class);
						System.out.println("Your confirmation number: " + confirm);
						break;
					case 4:
						restTemplate.getForObject(printUrl, String.class);
						break;
					case 5:
						restTemplate.getForObject(storeUrl, String.class);
						break;
					case 6:
						restTemplate.getForObject(restoreUrl, String.class);
						break;
					case 0:
						sn.close();
						System.exit(1);
				}
			} catch (Exception e) {
				if (e.toString().contains("400"))
					System.out.println("[ERROR] Bad request. One of inputs invalid");
				else if (e.toString().contains("404"))
					System.out.println("[ERROR] Not found. One of inputs invalid or data not found");
				else
					System.out.println("Some error occurs: " + e.toString());
				sn.next();
			}
		}
		
	}
	
}

