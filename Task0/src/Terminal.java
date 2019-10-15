package task0;

import java.sql.Date;
import java.time.Year;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

public class Terminal {
	private final static String [] msgCustomer = {"1. Show reservations", "2. Help", "3. Exit"};
	private final static String [] msgReceptionist = {"1. Insert reservation", "2. Update reservation", "3. Delete reservation", "4. Add customer", "5. Help", "6. Exit"};
	private Scanner input;
	private boolean login;
	private User user;
	
	public Terminal() {
		input = new Scanner(System.in);
		login = false;
		user = null;
	}
	
	public boolean getLogin() {
		return login;
	}
	
	public void setLogin(boolean value) {
		login = value;
	}
	
	/**
	 * Handle the insertion of an integer
	 * @param input is the Scanner input
	 * @param is message displayed at the begin
	 * @return the inserted integer
	 */
	private int insertInteger(Scanner input, String text) {
		System.out.print(text);
		int value;
		while (!input.hasNextInt()) {
	    	System.out.print("Invalid value, try again: ");
	        input.next(); 
	    }
		value = input.nextInt();	
		return value;
	}
	
	/**
	 * Handle the insertion of a string
	 * @param input is the Scanner input
	 * @param is message displayed at the begin
	 * @return the inserted string
	 */
	private String insertString(Scanner input, String text) {
		System.out.print(text);
		String value = input.nextLine();
		while (value.length() < 1) {
			value = input.nextLine();
		}
		return value;
	}
	
	/**
	 * Handle the insertion of a date
	 * @param input is the Scanner input
	 * @param is message displayed at the begin
	 * @return the inserted date
	 */
	private Date insertDate(Scanner input, String text) {
		System.out.print(text);
		Date date = null;
		do {
			String checkIn = input.nextLine();
			if (checkIn.length() > 0) {
				date = isValidDate(checkIn);
				if (date == null)
					System.out.print("Invalid date, try again: ");
			}	
		} while(date == null);
		
		return date;
	}
	
	/**
	 * Check the inserted date
	 * @param dateString
	 * @return the date if it is valid, null otherwise
	 */
	private Date isValidDate(String dateString) {
		String [] tmp = dateString.split("-");
		if (tmp.length < 3)
			return null;
		
		Date date = null;		
		
		Calendar calendar = Calendar.getInstance();
		try {
			int year = Integer.parseInt(tmp[0]), month = Integer.parseInt(tmp[1]) - 1, day = Integer.parseInt(tmp[2]);
			int currentYear = calendar.get(Calendar.YEAR);
			int currentMonth = calendar.get(Calendar.MONTH);
			int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
			
			//System.out.println("Year = " + year + " month = " +  month + " day " + day);
			//System.out.println("Current year = " + year + " current month = " +  month + " current day " + day);
			
			if (year < currentYear) 
				return null;
			
			if ((year == currentYear && month < currentMonth) || month > 11) 
				return null;
			
			if (year == currentYear && month == currentMonth && day < currentDay) 
				return null;
			
			if (month == 1) // february
				if (Year.isLeap(year) && day > 29) // leap year	
					return null;
				else if (day > 28) 
					return null;
			if ((month == 10 || month == 3 || month == 5 || month == 8) && day > 30) // november, april, june, septempber
				return null;
			
			if (day > 31) 
				return null;
				
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, month);
			calendar.set(Calendar.DAY_OF_MONTH, day);	
			date = new Date(calendar.getTimeInMillis());
		} catch(NumberFormatException e) {
			date = null;
		}
		return date;
	}
	
	/**
	 * Login the user
	 */
	public void login() {
		String cmd, username, password;
		boolean validCredentials = false;
		
		do {
			cmd = insertString(input, "Who are you? Type C if you are a custumer or R if you are a receptionist: ");
		} while (!cmd.equals("c") && !cmd.equals("C") && !cmd.equals("r") && !cmd.equals("R"));
		
	
		do {			
			username = insertString(input, "Please, insert your username: ");
			password = insertString(input, "Please, insert your password: ");
			
			if (cmd.equals("C") || cmd.equals("c"))
				user = new Customer(username);
			else
				user = new Receptionist(username);
			
			validCredentials = user.areValidCredentials(password);
			if (!validCredentials) {
				System.out.println("Wrong credentials, try again!");
			} else {
				setLogin(true);
				System.out.println("Hi " + user.username + "!");
				help();
			}				
		} while (!validCredentials);
	}
	
	/**
	 * Logout the user
	 */
	public void logout() {
		setLogin(false);
		System.out.println("Bye " +  user.username + "!");
		
		User.db.closeConnection();
	}
	
	/**
	 * Handle the insertion of a command 
	 */
	public void waitCommand() {			
		int cmd = insertInteger(input, "Select a command: ");
		if(user instanceof Customer)
			handleCustomerCommand(cmd);
		else
			handleReceptionistCommand(cmd);	
	}
	
	/**
	 * Display the list of commands for the specific user (customer or receptionist)
	 */
	private void help() {		
		System.out.println("You can choose one of the following command: ");
		if (user instanceof Customer) 
			for (String msg : msgCustomer)
				System.out.println(msg);
		else
			for (String msg : msgReceptionist)
				System.out.println(msg);
	}
	
	/**
	 * Handle the insertion of a command by a customer
	 */
	private void handleCustomerCommand(int cmd) {
		if (cmd < 0 || cmd > msgCustomer.length) {
			System.out.println("Command not valid.");
			return;
		}
		
		switch (cmd) {
			case 1:
				((Customer) user).showUpcomingReservations();
				break;
				
			case 2: 
				help();
				break;
				
			case 3: 
				logout();
				break;
				
			default:
				break;
		}		
	}
	
	/**
	 * Handle the insertion of a command by a receptionist
	 */
	private void handleReceptionistCommand(int cmd) {
		if (cmd < 0 || cmd > msgReceptionist.length) {
			System.out.println("Command not valid.");
			help();
			return;
		}
		
		switch (cmd) {
			case 1:
				handleInsertReservation();
				break;
				
			case 2: 
				handleUpdateReservation();
				break;
			
			case 3: 
				handleDeleteReservation();
				break;
				
			case 4:
				addCustomer();
				break;
				
			case 5:
				help();
				break;
				
			case 6:
				logout();
				break;
			
			default:
				break;
		}	
	}	
	
	/**
	 * Handle the insertion of a new reservation
	 */
	private void handleInsertReservation() {
		System.out.println("Insert details of the new reservation");
		
		// insert the hotel
		int hotel = insertInteger(input, "Hotel: ");
		
		// insert the username
		String username = insertString(input, "Username: ");
		int customerId = ((Receptionist) user).getCustomerId(username);
		if (customerId == 0) {
			System.out.println("Customer is a new user, first register it.");
			return;
		}
		Customer customer = new Customer(username);
		
		// insert check-in and check-out dates 
		Date checkInDate = insertDate(input, "Insert check-in date (in format YYYY-MM-DD): " );
		Date checkOutDate = insertDate(input, "Insert check-out date (in format YYYY-MM-DD): " );		
		if (!(checkOutDate.toString().compareTo(checkInDate.toString())> 0)) {
			System.out.println("Check-out date must be greater that chek-in date");
			return;
		}
		
		// insert the number of guests
		int roomCapacity = insertInteger(input, "Number of guests: ");
		
		// displays the list of available rooms 
		List<Room> availableRooms = ((Receptionist) user).getAvailableRooms(hotel, checkInDate, checkOutDate, roomCapacity);
		if(availableRooms.size() == 0)
			return;
		
		// select one of the available room
		int room;
		do {
			room = insertInteger(input, "Select room: ");
		} while(room > availableRooms.size());
		
		// insert the new reservation
		Reservation reservation = new Reservation(customer, availableRooms.get(room), checkInDate, checkOutDate);
		((Receptionist) user).insertReservation(reservation, customerId);
	}
	
	
	/**
	 * Handle the update of an existing reservation
	 */
	private void handleUpdateReservation() {
		System.out.println("Insert hotel, room and check-in data of the reservation to delete");
		
		// insert the hotel
		int hotel = insertInteger(input, "Hotel: ");
		
		// insert number of the room
		int room = insertInteger(input, "Room: ");
		
		// insert check-in date
		Date checkInDate = insertDate(input, "Check-in date (in format YYYY-MM-DD): " );
		
		Reservation oldReservation = new Reservation(null, new Room(hotel, room, 0), checkInDate, null);
		
		Customer customer = ((Receptionist) user).getCustomerOfAReservation(oldReservation);
		if (customer == null) {
			System.out.println("Error in updating reservation.");
			return;
		}
		int customerId = ((Receptionist) user).getCustomerId(customer.username);
		
		System.out.println("Insert information of the new reservation");
		
		// insert check-in and check-out dates 
		checkInDate = insertDate(input, "New check-in date (in format YYYY-MM-DD): " );
		Date checkOutDate = insertDate(input, "New check-out date (in format YYYY-MM-DD): " );
				
		if (!(checkOutDate.toString().compareTo(checkInDate.toString())> 0)) {
			System.out.println("Check-out date must be greater that chek-in date");
			return;
		}
				
		// insert the number of guests
		int roomCapacity = insertInteger(input, "New number of guests: ");
		
		// displays the list of available rooms 
		List<Room> availableRooms = ((Receptionist) user).getAvailableRooms(hotel, checkInDate, checkOutDate, roomCapacity);
		if(availableRooms.size() == 0)
			return;
		
		// select one of the available room
		do {
			room = insertInteger(input, "Select room: ");
		} while(room > availableRooms.size());
		
		Reservation newReservation = new Reservation(customer, availableRooms.get(room), checkInDate, checkOutDate);

		((Receptionist) user).updateReservation(oldReservation, newReservation, customerId);
	}
		
	/**
	 * Handle the delete of an existing reservation
	 */
	private void handleDeleteReservation() {
		System.out.println("Insert hotel, room and check-in data of the reservation to delete");
		
		// insert the hotel
		int hotel = insertInteger(input, "Hotel: ");
		
		// insert number of the room
		int room = insertInteger(input, "Room: ");
		
		// insert check-in date
		Date checkInDate = insertDate(input, "Check-in date (in format YYYY-MM-DD): " );
		
		Reservation reservation = new Reservation(null, new Room(hotel, room, 0), checkInDate, null);
		((Receptionist) user).deleteReservation(reservation);		
	}
	
	/**
	 * Handle the registration of a new customer
	 */
	private void addCustomer() {
		System.out.println("Insert name, surname, username and passoword of the new customer");
		
		// insert the name
		String name = insertString(input, "Name: ");
		
		// insert the surname
		String surname = insertString(input, "Surname: ");
		
		// insert the username
		String username;
		int customerId;
		do {
			username = insertString(input, "Username: ");
			customerId = ((Receptionist) user).getCustomerId(username);
			if (customerId != 0) {
				System.out.print("Username is yet used by another customer, try another one. ");
			}
		} while (customerId != 0);
		
		Customer customer = new Customer(username, name, surname);
		
		// insert the password
		String password = insertString(input, "Password: ");
		
		((Receptionist) user).addCustomer(customer, password);
	}
}
