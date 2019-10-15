package task0;

import java.util.List;

public class Customer extends User {
	
	public Customer (String username, String name, String surname) {
		super(username, name, surname);
	}

	public Customer (String name, String surname) {
		super(name, surname);
	}
	
	public Customer(String username) {
		super(username);
	}
	
	public void showUpcomingReservations() {
		List<Reservation> upcomingReservations = db.getUpcomingReservations(this);
		
		if (upcomingReservations.isEmpty())
			System.out.println("You have no upcoming reservations");
		
		for (Reservation r : upcomingReservations)
			System.out.println(r);
	}
	
	@Override
	public String toString() {
		return "Customer " + username;
	}
}