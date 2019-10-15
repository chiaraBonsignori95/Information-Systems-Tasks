package task0;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;

import org.junit.Test;

public class TestApplication {

	private long getTimeInMillis(int year, int month, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		return calendar.getTimeInMillis();
	}
	
	@Test
	public void testManagementReservations() {
		HotelDatabaseManager db = new HotelDatabaseManager();
		String username = "a";
		Customer customer = new Customer(username);
		int customerId = db.getCustomerId(username);
		Receptionist receptionist = new Receptionist("z1");
		Room room = new Room(1, 1, 2);
		
		// java.sql.date month range 0-11
		java.sql.Date arrivalSQL = new java.sql.Date(getTimeInMillis(2020, 10, 1));
		java.sql.Date departureSQL = new java.sql.Date(getTimeInMillis(2020, 10, 3));
		
		Reservation reservation = new Reservation(customer, room, arrivalSQL, departureSQL);
		
		db.insertReservation(reservation, customerId);
		
		List<Reservation> upcomingReservations = db.getUpcomingReservations(customer);		
		if (upcomingReservations.isEmpty()) {
			fail("Impossible to retrieve reservations");
		}
		Reservation dbReservation = upcomingReservations.get(0);
		assertEquals("Insert and Read from DB", dbReservation, reservation);
		
		Room newRoom = new Room(1, 2, 4);
		Reservation newReservation = new Reservation(customer, newRoom, arrivalSQL, departureSQL);
		
		receptionist.updateReservation(reservation, newReservation, customerId);
		
		upcomingReservations = db.getUpcomingReservations(customer);		
		if (upcomingReservations.isEmpty()) {
			fail("Impossible to retrieve reservations");
		}
		dbReservation = upcomingReservations.get(0);
		assertEquals("Update Room", dbReservation, newReservation);
		
		arrivalSQL = new java.sql.Date(getTimeInMillis(2020, 10, 5));
		departureSQL = new java.sql.Date(getTimeInMillis(2020, 10, 7));
		
		reservation = newReservation;
		newReservation = new Reservation(customer, newRoom, arrivalSQL, departureSQL);
		receptionist.updateReservation(reservation, newReservation, customerId);
		upcomingReservations = db.getUpcomingReservations(customer);		
		if (upcomingReservations.isEmpty()) {
			fail("Impossible to retrieve reservations");
		}
		dbReservation = upcomingReservations.get(0);
		assertEquals("Update Date", dbReservation, newReservation);
		
		receptionist.deleteReservation(newReservation);
		upcomingReservations = db.getUpcomingReservations(customer);		
		assertTrue("Delete reservation problem", upcomingReservations.isEmpty());	
		
	}
	
	@Test
	public void testAddCustomer() {
		// user that doesn't exist
		String username = "nonexistinguser";
		String name = "nonexistingname";
		String surname = "nonexistingsurname";
		String password = "password";
		
		// test that user username is not present 
		Customer customer = new Customer(username, name, surname);
		boolean result = customer.areValidCredentials(password);
		assertEquals(false, result);
		
		// test the insertion of the user
		Receptionist receptionist = new Receptionist("z1");
		result = receptionist.addCustomer(customer, password);
		assertEquals(true, result);
		
		// test that user username is present
		result = customer.areValidCredentials(password);
		assertEquals(true, result);
		
		// delete user to repeat the test
		User.db.deleteCustomer(username);
	}
}
