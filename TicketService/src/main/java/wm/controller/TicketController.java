package wm.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.MethodArgumentNotValidException;

import wm.domain.SeatHold;
import wm.domain.User;
import wm.service.TicketService;

/**
 *
 * 
 * @author ngtson@gmail.com
 */
@RestController
public class TicketController {
	@Autowired
	TicketService ticketService;
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	//@ResponseStatus(HttpStatus.BAD_REQUEST)
	//@ResponseBody
	public ResponseEntity<?> validationError(MethodArgumentNotValidException ex) {
		BindingResult result = ex.getBindingResult();
	    //final List<FieldError> fieldErrors = result.getFieldErrors();
	    final FieldError fieldError = result.getFieldError();
	    System.out.println("[TicketController] validationError() ERROR: " + fieldError);
	    return new ResponseEntity<CustomErrorType>(new CustomErrorType(fieldError.toString()), HttpStatus.BAD_REQUEST);

	}
	
	@GetMapping("/numSeatsAvailable")
	public ResponseEntity<?> getNumAvail() {
		//System.out.println("[TicketController] calling getNumAvail() .... ");
		int nums = ticketService.numSeatsAvailable();
		return new ResponseEntity<Integer>(new Integer(nums), HttpStatus.OK);
	}
	
	@GetMapping("/findAndHoldSeats/{numSeats}/{email}")
	public ResponseEntity<?> findAndHoldSeats(@PathVariable String numSeats, @PathVariable String email) {
		int nSeats = Integer.parseInt(numSeats);
		if (nSeats <= 0 || nSeats > ticketService.numSeatsAvailable()) {
			//System.out.println("[TicketController.findAndHoldSeats] [ERROR] Seats not available or no of seats input invalid");
			return new ResponseEntity<CustomErrorType>(new CustomErrorType("Seats not available or no of seat input invalid"), HttpStatus.NOT_FOUND);
		}
		SeatHold seatHold = ticketService.findAndHoldSeats(nSeats, email);
		if (seatHold == null)
			return new ResponseEntity<CustomErrorType>(new CustomErrorType("Cannot find and hold seats"), HttpStatus.NOT_FOUND);
		return new ResponseEntity<SeatHold>(seatHold, HttpStatus.OK);
	}
	
	// Testing
	@PostMapping("/findHoldSeats")
    //@ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> findHoldSeats(@RequestBody @Valid User user) {
		//System.out.println("[TicketController.findHoldSeats] User: " + user);
		SeatHold seatHold = ticketService.findAndHoldSeats(user.getNumSeats(), user.getEmail());
		if (seatHold == null)
			return new ResponseEntity<CustomErrorType>(new CustomErrorType("Cannot find and hold seats"), HttpStatus.NOT_FOUND);
		return new ResponseEntity<SeatHold>(seatHold, HttpStatus.OK);
    }
	
	@GetMapping("/reserveSeats/{seatHoldId}/{email}")
	public ResponseEntity<?> reserveSeats(@PathVariable String seatHoldId, @PathVariable String email) {
		//System.out.println("[TicketController] calling reserveSeats() .... seatHoldId: " + seatHoldId);
		String confirmNo = ticketService.reserveSeats(Integer.parseInt(seatHoldId), email);
		if (confirmNo == null)
			return new ResponseEntity<CustomErrorType>(new CustomErrorType(
					"Cannot reserve seats because the hold is timeout or hold id is invalid"), 
					HttpStatus.NOT_FOUND);
		return new ResponseEntity<String>(confirmNo, HttpStatus.OK);
	}
	
	// for testing
	@GetMapping("/printSeatMap")
	public ResponseEntity<?> printSeatMap() {
		ticketService.printSeatMap();
		return new ResponseEntity<String>("Print Seat Map", HttpStatus.OK);
	}
	
	// for testing
	@GetMapping("/store")
	public ResponseEntity<?> storeData() {
		ticketService.storeData();
		return new ResponseEntity<String>("store data", HttpStatus.OK);
	}
	
	// for testing
	@GetMapping("/restore")
	public ResponseEntity<?> restoreData() {
		ticketService.restoreData();
		return new ResponseEntity<String>("restore data", HttpStatus.OK);
	}

}
