package wm.domain;

import java.util.List;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

/**
 * 
 * @author ngtson@gmail.com
 *
 */
@SuppressWarnings("deprecation")
public class User {
	
	//@Range(min=0, max=${max_range})
	@Min(value=1)
	int numSeats;
	
	@NotBlank
	@Email
	String email;
	
	public User() {
	}
	
	//public User(@Range(min = 0, max = 100) int numSeats, @NotBlank @Email String email) {
	public User(int numSeats, String email) {
		super();
		this.numSeats = numSeats;
		this.email = email;
	}
	
	public int getNumSeats() {
		return numSeats;
	}

	public void setNumSeats(int numSeats) {
		this.numSeats = numSeats;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (numSeats != other.numSeats)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "User [numSeats=" + numSeats + ", email=" + email + "]\n";
	}
	
}
