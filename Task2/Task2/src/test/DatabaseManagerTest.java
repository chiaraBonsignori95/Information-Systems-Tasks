package test;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import backend.DatabaseManager;
import backend.exc.*;
import backend.modules.*;

public class DatabaseManagerTest {

	private static DatabaseManager databaseManager;

	@BeforeAll
	public static void start() {
		databaseManager = new DatabaseManager();
	}

	@AfterAll
	public static void end() {
		databaseManager.closeMongoDBConnection();
	}

	/**
	 * Test the following methods: createAccout, login, deleteAccount which are the
	 * same for both Customer and RestaurantOwner
	 */
	@Test
	public void testAccountOperations() {

		// insert a customer
		String name = "Chiara", surname = "Rossi", username = "chiaraRossi", password = "crc";
		Customer customer = new Customer(name, surname, username, password);
		try {
			customer = (Customer) databaseManager.createAccount(customer);
		} catch (UsernameAlreadyPresentException e) {
			fail("Test create new customer account: failed because username already used.");
		}

		// test login with registered customer
		Customer loggedCustomer = null;
		try {
			loggedCustomer = (Customer) databaseManager.login(username, password, Customer.class);
		} catch (UserNotFoundException | DatabaseManagerException e) {
			fail("Test login customer.");
		}
		assertTrue(loggedCustomer.equals(customer));

		// try to insert again the customer
		try {
			databaseManager.createAccount(customer);
			fail("Test create customer account with already used username: failed.");
		} catch (UsernameAlreadyPresentException e) {
			assertTrue("Test create customer account with already used username.", true);
		}

		// test login with unregistered customer
		try {
			databaseManager.login("anyusername", "anypassword", Customer.class);
			fail("Test login unregistered customer.");
		} catch (UserNotFoundException e) {
			assertTrue("Test login unregistered customer.", true);
		} catch (DatabaseManagerException e) {
			fail("Test login unregistered customer.");
		}

		// delete the customer
		try {
			databaseManager.deleteAccount(customer);
		} catch (UserNotFoundException e1) {
			fail("Test delete customer.");
		}

		// test login with deleted customer
		try {
			databaseManager.login(username, password, Customer.class);
			fail("Test login with deleted customer.");
		} catch (UserNotFoundException e) {
			assertTrue("Test login with deleted customer.", true);
		} catch (DatabaseManagerException e) {
			fail("Test login with deleted customer.");
		}
	}

	/**
	 * Test the following methods: insertRestaurant, getPendingRequests,
	 * getRestaurants (by name), updateRestaurant and deleteRestaurant
	 */
	@Test
	public void testRestaurantOperations() {

		// insert a restaurant owner
		String name = "Maria", surname = "Bianchi", username = "mariabianchi", password = "mbmbm";
		RestaurantOwner restaurantOwner = new RestaurantOwner(name, surname, username, password);
		try {
			restaurantOwner = (RestaurantOwner) databaseManager.createAccount(restaurantOwner);
		} catch (UsernameAlreadyPresentException e) {
			fail("Test create new restaurant failed: failed in creating restaurant owner.");
		}

		// insert a restaurant
		Restaurant restaurant = insertRestaurantForTesting(restaurantOwner);

		// test if the inserted restaurant is a pending request
		List<Restaurant> pendingRequests = null;
		try {
			pendingRequests = databaseManager.getPendingRequests(1);
		} catch (DatabaseManagerException e) {
			fail("Test get pending requests");
		}
		assertTrue("Test create new restaurant.", pendingRequests.contains(restaurant));

		// accept the request and verify that the restaurant is no more a pending
		// request
		restaurant = databaseManager.acceptPendingRequest(restaurant);
		try {
			pendingRequests = databaseManager.getPendingRequests(1);
		} catch (DatabaseManagerException e) {
			fail("Test get pending requests");
		}
		assertFalse("Test accept new restaurant.", pendingRequests.contains(restaurant));
	
		// try to get the restaurant by name (must be present)
		List<Restaurant> restaurants = new ArrayList<Restaurant>();
		boolean present = false;
		try {
			int i = 1;
			do {
				restaurants = databaseManager.getRestaurants(restaurant.getName(), restaurant.getCategories().get(0),
						restaurant.getAddress().getCity(), i, true);
				i++;
				if (restaurants.contains(restaurant)) {
					present = true;
					break;
				}
			} while (!present && i < 100);
		} catch (DatabaseManagerException e) {
			fail("Test get the inserted restaurant by name");
		}
		assertTrue("Test get the inserted restaurant by name", present);

		// try to get the restaurant by city (must be present)
		restaurants.clear();
		present = false;
		try {
			int i = 1;
			do {
				restaurants = databaseManager.getRestaurants(restaurant.getAddress().getCity(), i, true);
				i++;
				if (restaurants.contains(restaurant)) {
					present = true;
					break;
				}
			} while (!present && i < 100);
		} catch (DatabaseManagerException e) {
			fail("Test get the inserted restaurant by city");
		}
		assertTrue("Test get the inserted restaurant by city", present);

		// try to get the restaurant by category and city (must be present)
		restaurants.clear();
		present = false;
		try {
			int i = 1;
			do {
				restaurants = databaseManager.getRestaurants(restaurant.getCategories().get(0),
						restaurant.getAddress().getCity(), i, true);
				i++;
				if (restaurants.contains(restaurant)) {
					present = true;
					break;
				}
			} while (!present && i < 100);
		} catch (DatabaseManagerException e) {
			fail("Test get the inserted restaurant by category and city");
		}
		assertTrue("Test get the inserted restaurant by category and city", present);

		// update the restaurant
		Restaurant newRestaurant = restaurant;
		newRestaurant.setPhoneNumber("3390000000");
		List<String> times = new ArrayList<String>();
		times.add("18:00 - 23:00");
		if (newRestaurant.getOpeningHours() != null)
			newRestaurant.getOpeningHours().add(new OpeningHour("mar - dom", times));
		else {
			List<OpeningHour> openingHours = new ArrayList<OpeningHour>();
			openingHours.add(new OpeningHour("mar - dom", times));
			newRestaurant.setOpeningHours(openingHours);
		}
		if (newRestaurant.getCategories() != null)
			newRestaurant.getCategories().add("Tipica toscana");
		else {
			List<String> categories = new ArrayList<String>();
			categories.add("Tipica toscana");
			newRestaurant.setCategories(categories);
		}
		if (newRestaurant.getOptions() != null)
			newRestaurant.getOptions().add("Per celiachi");
		else {
			List<String> options = new ArrayList<String>();
			options.add("Per celiachi");
			newRestaurant.setOptions(options);
		}
		if (newRestaurant.getFeatures() != null)
			newRestaurant.getFeatures().add("Aria condizionata");
		else {
			List<String> features = new ArrayList<String>();
			features.add("Aria condizionata");
			newRestaurant.setFeatures(features);
		}
		newRestaurant = databaseManager.updateRestaurant(restaurant, newRestaurant);
		
		// get the updated restaurant by name
		restaurants.clear();
		present = false;
		try {
			int i = 1;
			do {
				restaurants = databaseManager.getRestaurants(newRestaurant.getName(),
						newRestaurant.getCategories().get(1), newRestaurant.getAddress().getCity(), i, true);
				i++;
				if (restaurants.contains(newRestaurant)) {
					present = true;
					break;
				}
			} while (!present && i < 100);
		} catch (DatabaseManagerException e) {
			fail("Test get the updated restaurant by name");
		}
		assertTrue("Test get the updated restaurant by name", present);

		// insert another restaurant for the same owner
		String restaurantName = "Da Maria 2";
		Restaurant restaurant2 = new Restaurant(restaurantName, restaurant.getAddress(), restaurant.getPhoneNumber(),
				restaurant.getOpeningHours(), restaurant.getCategories());
		restaurant2 = databaseManager.insertRestaurant(restaurant2, restaurantOwner);

		// test if the inserted restaurant is a pending request
		try {
			pendingRequests = databaseManager.getPendingRequests(1);
		} catch (DatabaseManagerException e) {
			fail("Test get pending requests");
		}
		assertTrue("Test create new restaurant.", pendingRequests.contains(restaurant2));

		// refuse the request and verify that the restaurant is no more a pending
		// request
		restaurant2 = databaseManager.refusePendingRequest(restaurant2);
		try {
			pendingRequests = databaseManager.getPendingRequests(1);
		} catch (DatabaseManagerException e) {
			fail("Test get pending requests");
		}
		assertFalse("Test create new restaurant.", pendingRequests.contains(restaurant2));
		
		// try to get the refused restaurant (must not be present)
		restaurants.clear();
		present = false;
		try {
			int i = 1;
			do {
				restaurants = databaseManager.getRestaurants(restaurant2.getName(), restaurant2.getCategories().get(0),
						restaurant2.getAddress().getCity(), i, true);
				i++;
				if (restaurants.contains(restaurant2)) {
					present = true;
					break;
				}
			} while (!present && i < 100);
		} catch (DatabaseManagerException e) {
			fail("Test get the refused restaurant");
		}
		assertFalse("Test get the refused restaurant", present);

		// delete one of the restaurant
		databaseManager.deleteRestaurant(restaurant);

		// delete the restaurant owner
		try {
			databaseManager.deleteAccount(restaurantOwner);
		} catch (UserNotFoundException e) {
			fail("Test delete account");
		}
	}

	/**
	 * Test insertReview, insertReply, getReviews and deleteReview
	 */
	@Test
	public void testReviewOperations() {

		// insert a customer
		Customer customer = insertCustomerForTesting();

		// insert a restaurant owner
		RestaurantOwner restaurantOwner = insertRestaurantOwnerForTesting();

		// insert a restaurant
		Restaurant restaurant = insertRestaurantForTesting(restaurantOwner);
		restaurant = databaseManager.acceptPendingRequest(restaurant);

		// insert a review and verify if is present
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		Review review = null;
		List<Review> reviews = new ArrayList<Review>();
		boolean present = false;
		try {
			date = formatter.parse("2019-12-11");
			review = new Review(date, 4.5, restaurant.getName(), restaurant.getId(), "Top!", "Ottimo cibo",
					customer.getUsername());
			restaurant = databaseManager.insertReview(restaurant, review, customer);
			int i = 1;
			do {
				reviews = databaseManager.getReviews(restaurant, i);
				i++;
				if (reviews.contains(review)) {
					present = true;
					break;
				}
			} while (!reviews.isEmpty());
		} catch (Exception e) {
			fail("Test get the inserted review");
		}
		assertTrue("Test get the inserted review", present);

		// insert a reply and verify if is inserted
		Reply reply = null;
		reviews.clear();
		present = false;
		try {
			reply = new Reply(restaurantOwner.getUsername(), "Grazie per la recensione.");
			review = databaseManager.insertReply(review, reply);
			int i = 1;
			do {
				reviews = databaseManager.getReviews(restaurant, i);
				if (reviews.contains(review)) {
					present = true;
					break;
				}
				i++;
			} while (!reviews.isEmpty());
		} catch (DatabaseManagerException e) {
			fail("Test get the inserted review");
		}
		assertTrue("Test get the inserted review", (present && review.getReply().equals(reply)));

		// verify if the review and the reply are inserted in the customer's document
		Customer loggedCustomer = null;
		try {
			loggedCustomer = (Customer) databaseManager.login(customer.getUsername(), customer.getPassword(),
					Customer.class);
		} catch (UserNotFoundException | DatabaseManagerException e) {
			fail("Test get the inserted review in the customer's document");
		}
		assertTrue("Test get the inserted review in the customer's document",
				loggedCustomer.getReviews().contains(review));
		reviews = loggedCustomer.getReviews();
		int index = reviews.indexOf(review);
		Reply insertedReply = reviews.get(index).getReply();
		assertTrue("Test get the reply in the inserted review in the customer's document", (insertedReply != null));
		
		// verify if the review is inserted in the restaurant owner's document
		RestaurantOwner loggedOwner = null;
		try {
			loggedOwner = (RestaurantOwner) databaseManager.login(restaurantOwner.getUsername(),
					restaurantOwner.getPassword(), RestaurantOwner.class);
		} catch (UserNotFoundException | DatabaseManagerException e) {
			fail("Test get the inserted review in the owner's document");
		}
		index = loggedOwner.getRestaurants().indexOf(restaurant);
		assertTrue("Test get the inserted review in the owner's document",
				loggedOwner.getRestaurants().get(index).getReviews().contains(review));
		reviews = loggedOwner.getRestaurants().get(index).getReviews();
		index = reviews.indexOf(review);
		insertedReply = reviews.get(index).getReply();
		assertTrue("Test get the reply in the inserted review in the owner0s document", (insertedReply != null));
		
		// delete the review
		try {
			databaseManager.deleteReview(review);
		} catch (DatabaseManagerException e) {
			fail("Test delete the inserted review");
		}
		restaurant.getReviews().remove(review);
	
		reviews.clear();
		present = false;
		try {
			int i = 1;
			do {
				reviews = databaseManager.getReviews(restaurant, i);
				if (reviews.contains(review)) {
					present = true;
					break;
				}
				i++;
			} while (!reviews.isEmpty());
		} catch (DatabaseManagerException e) {
			fail("Test get the deleted review");
		}
		assertFalse("Test get the deleted review", present);

		// delete the customer and the restaurant owner
		try {
			databaseManager.deleteAccount(customer);
			databaseManager.deleteAccount(restaurantOwner);
		} catch (UserNotFoundException e) {
			fail("Delete account");
		}
	}

	private Customer insertCustomerForTesting() {
		String name = "Chiara", surname = "Rossi", username = "chiaraRossi", password = "crc";
		Customer customer = new Customer(name, surname, username, password);
		try {
			databaseManager.createAccount(customer);
		} catch (UsernameAlreadyPresentException e) {
			fail("Test create new customer account: failed because username already used.");
		}
		return customer;
	}

	private RestaurantOwner insertRestaurantOwnerForTesting() {
		String name = "Maria", surname = "Bianchi", username = "mariabianchi", password = "mbmbm";
		RestaurantOwner restaurantOwner = new RestaurantOwner(name, surname, username, password);
		try {
			databaseManager.createAccount(restaurantOwner);
		} catch (UsernameAlreadyPresentException e) {
			fail("Test create new owner account: failed because username already used.");
		}
		return restaurantOwner;
	}

	private Restaurant insertRestaurantForTesting(RestaurantOwner restaurantOwner) {
		String restaurantName = "Da Maria", phoneNumber = "3398568868";
		String city = "Pisa", country = "Italia", postcode = "56021", street = "Via Fiorentina 2";
		Address address = new Address(city, country, postcode, street);
		List<String> times = new ArrayList<String>();
		times.add("12:00 - 14:00");
		OpeningHour openingHour = new OpeningHour("lun - sab", times);
		List<OpeningHour> openingHours = new ArrayList<OpeningHour>();
		openingHours.add(openingHour);
		List<String> categories = new ArrayList<String>();
		categories.add("Toscana");
		Restaurant restaurant = new Restaurant(restaurantName, address, phoneNumber, openingHours, categories);
		restaurant = databaseManager.insertRestaurant(restaurant, restaurantOwner);
		return restaurant;
	}
}
