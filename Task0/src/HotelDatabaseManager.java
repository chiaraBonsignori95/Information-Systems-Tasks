package task0;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HotelDatabaseManager {
	private final String schema = "hotel_chain";
    private Connection connection;

    /**
     * Open a connection to the database
     * @param ipAddress
     * @param port
     * @param username
     * @param password
     */
	public HotelDatabaseManager(String ipAddress, int port, String username, String password) {
        String url = "jdbc:mysql://" + ipAddress + ":" + port + "/" + schema;
        url += "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
        	System.err.println("SQLException: " + e.getMessage());
			System.err.println("SQLState: " + e.getSQLState());
			System.err.println("Vendor error: " + e.getErrorCode());
        }
    }
	
	public HotelDatabaseManager() {
		this("localhost", 3306, "root", "root");
	}
	
	/**
	 * Close connection to database
	 */
	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
			System.err.println("SQLState: " + e.getSQLState());
			System.err.println("Vendor error: " + e.getErrorCode());
		}
	}
	
	/**
	 * Get a list of future reservations booked by a given customer
	 * @param customer the customer who booked the reservation
	 * @return a list of reservations
	 */
	public List<Reservation> getUpcomingReservations(Customer customer) {
        List<Reservation> upcomingReservations = new ArrayList<>();
        
        String query = ""
        		+ "SELECT R.hotel, R.room AS number, R.`check-in date`, R.`check-out date`, S.capacity \n"
        		+ "FROM reservation R INNER JOIN customer C INNER JOIN room S \n"
        		+ "ON C.id = R.customer AND R.hotel = S.hotel AND R.room = S.number \n"
        		+ "WHERE C.username = ? AND R.`check-in date` >= CURRENT_DATE";
        
        try (
            PreparedStatement statement = connection.prepareStatement(query);
        ) {
        	statement.setString(1, customer.username);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
            	Room room = new Room(
            			resultSet.getInt("hotel"),
            			resultSet.getInt("number"),
            			resultSet.getInt("capacity"));
            	Date checkIn = resultSet.getDate("check-in date");
            	Date checkOut = resultSet.getDate("check-out date");
            	Reservation reservation = new Reservation(customer, room, checkIn, checkOut);
            	upcomingReservations.add(reservation);
            }
        } catch (SQLException e) {
        	System.err.println("SQLException: " + e.getMessage());
			System.err.println("SQLState: " + e.getSQLState());
			System.err.println("Vendor error: " + e.getErrorCode());
        }
        
        return upcomingReservations;
    }
	
	/**
	 * Insert a new reservation
	 * @param reservation is the new reservation to insert
	 * @return 1 if the operation is successful, 0 otherwise
	 */
	public int insertReservation(Reservation reservation, int customerId) {
		int result = 0;
		String query = "" + 
				"INSERT INTO reservation(hotel, room, `check-in date`, `check-out date`, customer) \n" + 
				"VALUES ( \n" + 
				"?, \n" + 
				"?, \n" + 
				"?, \n" + 
				"?, \n" + 
				"?) \n";
		try (
	        PreparedStatement statement = connection.prepareStatement(query);
	    ) {
	        statement.setInt(1, reservation.room.hotel);	
	        statement.setInt(2, reservation.room.number);
	        statement.setDate(3, (Date) reservation.checkInDate);
	        statement.setDate(4, (Date) reservation.checkOutDate);
	        statement.setInt(5, customerId);        
	        result = statement.executeUpdate();
	    } catch (SQLException e) {
	    	System.err.println("SQLException: " + e.getMessage());
			System.err.println("SQLState: " + e.getSQLState());
			System.err.println("Vendor error: " + e.getErrorCode());
	    }
		return result;
	}
	
	/**
	 * Update an existing reservation
	 * @param oldReservation is the reservation to update
	 * @param newReservation is the updated version of the reservation
	 * @return 1 if the operation is successful, 0 otherwise
	 */
	public int updateReservation(Reservation oldReservation, Reservation newReservation, int customerId) {
		int result = deleteReservation(oldReservation);
		if (result != 0)
			result = insertReservation(newReservation, customerId);
		else 
			System.out.println("An error verified in the updating the reservation.");
		return result;
	}
	
	/**
	 * Delete an existing reservation
	 * @param reservation is the reservation to delete
	 * @return 1 if the operation is successful, 0 otherwise
	 */
	public int deleteReservation(Reservation reservation) {
		int result = 0;
		String query = ""
				+ "DELETE FROM reservation WHERE "
				+ "hotel = ? AND "
				+ "room = ? AND "	
				+ "`check-in date` = ?";
		try (
	        PreparedStatement statement = connection.prepareStatement(query);
	    ) {
	        statement.setInt(1, reservation.room.hotel);
	        statement.setInt(2, reservation.room.number);
	        statement.setDate(3, (Date) reservation.checkInDate);
	        result = statement.executeUpdate();
	    } catch (SQLException e) {
	    	System.err.println("SQLException: " + e.getMessage());
			System.err.println("SQLState: " + e.getSQLState());
			System.err.println("Vendor error: " + e.getErrorCode());
	    }
		return result;
	}	
	
	/**
	 * Add a new customer
	 * @param customer is the customer to register
	 * @param password is the inserted password
	 * @return 1 if the operation is successful, 0 otherwise
	 */
	public int addCustomer(Customer customer, String password) {
		int result = 0;
		String query = "" + 
				"INSERT INTO customer(name, surname, username, password) \n" + 
				"VALUES ( \n" + 
				"?, \n" + 
				"?, \n" + 
				"?, \n" + 
				"?) \n";
		try (
	        PreparedStatement statement = connection.prepareStatement(query);
	    ) {
	        statement.setString(1, customer.name);	
	        statement.setString(2, customer.surname);
	        statement.setString(3, customer.username);
	        statement.setString(4, password);
	        
	        result = statement.executeUpdate();
	    } catch (SQLException e) {
	    	System.err.println("SQLException: " + e.getMessage());
			System.err.println("SQLState: " + e.getSQLState());
			System.err.println("Vendor error: " + e.getErrorCode());
	    }
		return result;
	}
	
	/**
	 * Get the username of the customer for a certain reservation
	 * @param reservation
	 * @return the customer
	 */
	public Customer getCustomerOfAReservation(Reservation reservation) {     
		Customer customer = null;
        String query = ""
        		+ "SELECT C.username as username \n" 
        		+ "FROM reservation R inner join customer C ON R.customer = C.id WHERE \n"
        		+ "R.hotel = ? AND "
				+ "R.room = ? AND "	
				+ "R.`check-in date` = ?";
        try (
            PreparedStatement statement = connection.prepareStatement(query);
        ) {
        	statement.setInt(1, reservation.room.hotel);
	        statement.setInt(2, reservation.room.number);
	        statement.setDate(3, (Date) reservation.checkInDate);
	        
	        ResultSet resultSet = statement.executeQuery();
	        if(resultSet.next()) {
	        	String username = resultSet.getString("username");
	        	customer = new Customer(username); 
	        }         
        } catch (SQLException e) {
        	System.err.println("SQLException: " + e.getMessage());
			System.err.println("SQLState: " + e.getSQLState());
			System.err.println("Vendor error: " + e.getErrorCode());
        }
        
        return customer;
    }
	
	/**
	 * Verify if a user is yet registered 
	 * @param username is the username of the user to verify
	 * @return the customer if is yet registered, null otherwise
	 */
	public int getCustomerId(String username) {     
		int customerId = 0;
        String query = ""
        		+ "SELECT * \n"
        		+ "FROM customer WHERE \n"
        		+ "username = ?";
        try (
            PreparedStatement statement = connection.prepareStatement(query);
        ) {
        	statement.setString(1, username);
	        ResultSet resultSet = statement.executeQuery();
	        if(resultSet.next()) 
	        	customerId = resultSet.getInt("id");
     
        } catch (SQLException e) {
        	System.err.println("SQLException: " + e.getMessage());
			System.err.println("SQLState: " + e.getSQLState());
			System.err.println("Vendor error: " + e.getErrorCode());
        }
        return customerId;
    }
	
	
	/**
	 * Delete an existing user
	 * @param username is the username of the user to delete
	 * @return true if the operation is successful, false otherwise
	 */
	public boolean deleteCustomer(String username) {
		int result = 0;
		String query = ""
				+ "DELETE FROM customer "
				+ "WHERE username = ?";
		try (
	        PreparedStatement statement = connection.prepareStatement(query);
	    ) {
	        statement.setString(1, username);
	        result = statement.executeUpdate();
	    } catch (SQLException e) {
	    	System.err.println("SQLException: " + e.getMessage());
			System.err.println("SQLState: " + e.getSQLState());
			System.err.println("Vendor error: " + e.getErrorCode());
	    }
		return (result == 1);
	}	
	
	/**
	 * Get list of existing reservation
	 * @param reservation is the reservation to delete
	 */
	public List<Room> getAvailableRooms(int hotel, Date checkIn, Date checkOut, int roomCapacity) {
		List<Room> rooms = new ArrayList<>();
		String query = ""
				+ "SELECT room.hotel as hotel, room.number as number, room.capacity as capacity \n" + 
				"FROM room LEFT OUTER JOIN (SELECT room.hotel as hotel, room.number as room, reservation.`check-in date`, reservation.`check-out date`, room.capacity \n" + 
				"FROM room INNER JOIN reservation ON room.hotel = reservation.hotel AND room.number = reservation.room \n" + 
				"WHERE (`check-in date` <= ? AND `check-out date` >= ?) OR \n" + 
				"      (`check-in date` <= ? AND `check-out date` >= ?) OR \n" + 
				"      (`check-in date` <= ? AND `check-out date` >= ?) OR \n" + 
				"      (`check-in date` >= ? AND `check-in date` <= ? AND  `check-out date` >= ? AND `check-out date` <= ?)) AS D ON room.hotel = D.hotel AND room.number = D.room \n" + 
				"WHERE room.capacity = ? AND room.hotel = ? AND D.room IS NULL";
		try (
	        PreparedStatement statement = connection.prepareStatement(query);
	    ) {
	        statement.setDate(1, (Date) checkIn);
	        statement.setDate(2, (Date) checkIn);
	        statement.setDate(3, (Date) checkOut);
	        statement.setDate(4, (Date) checkOut);
	        statement.setDate(5, (Date) checkIn);
	        statement.setDate(6, (Date) checkOut);
	        statement.setDate(7, (Date) checkIn);
	        statement.setDate(8, (Date) checkOut);
	        statement.setDate(9, (Date) checkIn);
	        statement.setDate(10, (Date) checkOut);	 
	        statement.setInt(11, roomCapacity);
	        statement.setInt(12, hotel);
	        
	        ResultSet resultSet = statement.executeQuery();
	        
	        while (resultSet.next())
	        	rooms.add(new Room(
	        			resultSet.getInt("hotel"),
	        			resultSet.getInt("number"),
	        			resultSet.getInt("capacity")
	        			));
	    } catch (SQLException e) {
	    	System.err.println("SQLException: " + e.getMessage());
			System.err.println("SQLState: " + e.getSQLState());
			System.err.println("Vendor error: " + e.getErrorCode());
	    }
		
		return rooms;
	}
	
	/**
	 * Authenticate the user
	 * @param user is the user to authenticate
	 * @param password is the inserted password
	 * @return the user
	 */
	public User authenticateUser(User user, String password) {
		String table;
		if (user instanceof Customer) 
			table = "customer";
		else
			table = "receptionist";
		
		String query = ""	
        		+ "SELECT * FROM " + table + " WHERE "
        		+ "username = ? AND "
        		+ "password = ?";        
        try (
            PreparedStatement statement = connection.prepareStatement(query);
        ) {
        	statement.setString(1, user.username);
        	statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
            	// empty result set: authentication failed
            	user = null;
            } else {
            	// successful authentication
                user.name = resultSet.getString("name");
                user.surname = resultSet.getString("surname"); 
            }
                        
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
			System.err.println("SQLState: " + e.getSQLState());
			System.err.println("Vendor error: " + e.getErrorCode());
        }
        return user;
	}
}

