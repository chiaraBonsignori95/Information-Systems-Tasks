package task0;

import java.util.Date;

public class Reservation {
	
	Customer customer;
	Room room;
	Date checkInDate;
	Date checkOutDate;
	
	public Reservation(Customer customer, Room room, Date checkIn, Date checkOut) {
		this.customer = customer;
		this.room = room;
		checkInDate = checkIn;
		checkOutDate = checkOut;
	}
	
	@Override
	public String toString() {
		String c = customer.toString();
		String r = room.toString();
		return "Reservation:\n\t" + c + "\n\t" + r;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        
        Reservation other = (Reservation)obj;
        
        return 	other.room.equals(room) && 
        		other.customer.equals(customer) && 
        		other.checkInDate.toString().equals(checkInDate.toString())&&
        		other.checkOutDate.toString().equals(checkOutDate.toString());
	}

}