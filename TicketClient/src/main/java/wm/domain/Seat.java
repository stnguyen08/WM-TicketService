package wm.domain;

import java.io.Serializable;

/**
 * 
 * @author ngtson@gmail.com
 *
 */
public class Seat implements Serializable, Comparable<Seat> {
	
	private static final long serialVersionUID = -4868285121888898094L;
	
	int row;
	int col;
	State state;
	
	public Seat() {
	}
	
	public Seat(int row, int col) {
		this.row = row;
		this.col = col;
		state = State.AVAIL;
	}
	
	public Seat(int row, int col, State state) {
		this.row = row;
		this.col = col;
		this.state = state;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	@Override
	public int compareTo(Seat other) {
		return (row < other.row) ? -1 : ((row > other.row) ? 1 : ((col < other.col) ? -1 : ((col > other.col) ? 1 : 0)));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Seat other = (Seat) obj;
		if (col != other.col)
			return false;
		if (row != other.row)
			return false;
		if (state != other.state)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + row + ":" + col + ":" + state + "]";
	}
	
}
