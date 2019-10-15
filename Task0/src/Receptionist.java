package task0;

import java.sql.Date;
import java.util.List;

public class Receptionist extends User {

	public Receptionist(String name, String surname) {
		super(name, surname);
	}
	
	public Receptionist(String username) {
		super(username);
	}

	public List<Room> getAvailableRooms(int hotel, Date checkIn, Date checkOut, int roomCapacity ) {	
		List<Room> availableRooms = db.getAvailableRooms(hotel, checkIn, checkOut, roomCapacity);			
		int counter = 0;
		for (Room r : availableRooms) {
			System.out.println(counter + ") " + r);
			counter++;
		}
		if (availableRooms.size() == 0)
			System.out.println("There are no available rooms with the inserted characteristics.");
		return availableRooms;
	}
	
	public void insertReservation(Reservation reservation, int customerId) {
		int result = db.insertReservation(reservation, customerId);
		if (result == 0)
			System.out.println("An error verified in inserting the reservation.");
		else
			System.out.println("New insertion completed.");
	}
	
	public void updateReservation(Reservation oldReservation, Reservation newReservation, int customerId) {
		int result = db.updateReservation(oldReservation, newReservation, customerId);
		if (result == 0)
			System.out.println("An error verified in updating the reservation.");
		else
			System.out.println("Update completed.");
	}
	
	public void deleteReservation(Reservation reservation) {
		int result = db.deleteReservation(reservation);
		if (result == 0)
			System.out.println("An error verified in deleting the reservation.");
		else
			System.out.println("Delete completed.");
	}
	
	public boolean addCustomer(Customer customer, String password) {
		int result = db.addCustomer(customer, password);
		if (result == 0)
			System.out.println("An error verified in adding the customer.");
		else
			System.out.println("Added customer.");
		return (result > 0);
	}
	
	public Customer getCustomerOfAReservation(Reservation reservation) {
		Customer result = db.getCustomerOfAReservation(reservation);
		return result;
	}
	
	public int getCustomerId(String username) {
		int result = db.getCustomerId(username);
		return result;
	}
}
