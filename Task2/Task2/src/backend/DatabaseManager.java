package backend;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Accumulators.first;
import static com.mongodb.client.model.Accumulators.max;
import static com.mongodb.client.model.Accumulators.min;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.addFields;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Aggregates.unwind;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Projections.computed;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Updates.addToSet;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.pull;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.TransactionOptions;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.TransactionBody;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;

import backend.exc.DatabaseManagerException;
import backend.exc.UserNotFoundException;
import backend.exc.UsernameAlreadyPresentException;
import backend.modules.Administrator;
import backend.modules.ComparisonElement;
import backend.modules.ComparisonResult;
import backend.modules.Customer;
import backend.modules.DistributionElement;
import backend.modules.Reply;
import backend.modules.Restaurant;
import backend.modules.RestaurantDistance;
import backend.modules.RestaurantOwner;
import backend.modules.Review;
import backend.modules.TrendElement;
import backend.modules.User;

public class DatabaseManager {

	private MongoClient mongoClient;

	private MongoDatabase mongoDatabase;

	private ClientSession clientSession;

	private static final int pageSize = 10;

	private static List<String> names, surnames, usernames;

	public int getPagesize() {
		return pageSize;
	}
	
	private enum usageType {APP, TEST};

	public DatabaseManager() {
		openMongoDBConnection(usageType.APP);
		openDatabase();
	}

	/**
	 * Open the connection with MongoDB server/replica set, must be opened once at
	 * the beginning of the application
	 */
	public void openMongoDBConnection(usageType type) {
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
				fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		
		// read preference primary
		if(type == usageType.TEST) {		
			mongoClient = MongoClients.create(MongoClientSettings.builder()
					.applyToClusterSettings(
							builder -> builder.hosts(Arrays.asList(new ServerAddress("localhost", 20000),
									new ServerAddress("localhost", 20001), new ServerAddress("localhost", 20002))))
					.readPreference(ReadPreference.primary()).codecRegistry(pojoCodecRegistry).build());		
			}
		
		// read preference nearest
		if(type == usageType.APP) {		
			mongoClient = MongoClients.create(MongoClientSettings.builder()
					.applyToClusterSettings(
							builder -> builder.hosts(Arrays.asList(new ServerAddress("localhost", 20000),
									new ServerAddress("localhost", 20001), new ServerAddress("localhost", 20002))))
					.readPreference(ReadPreference.nearest()).codecRegistry(pojoCodecRegistry).build());		  
		}
		 
	}

	public MongoDatabase getMongoDatabase() {
		return mongoDatabase;
	}

	/**
	 * Close the connection with MongoDb server
	 */
	public void closeMongoDBConnection() {
		mongoClient.close();
	}

	/**
	 * Get access to the database and to collections, collections are created, of
	 * they do not exist, when a new document is added to them
	 */
	private void openDatabase() {
		mongoDatabase = mongoClient.getDatabase("restaurantAppDB");
	}

	/**
	 * Start a client session, in each session an atomic transaction is performed
	 */
	public void startSession() {
		clientSession = mongoClient.startSession();
	}

	public void closeSession() {
		clientSession.close();
	}

	/**
	 * Load a Restaurant object from a json file
	 * 
	 * @param jsonFile the json file of the restaurant
	 * @return Restaurant the initialized Restaurant object
	 * @throws DatabaseManagerException in case of errors
	 */
	private Restaurant loadRestaurantFromJson(String jsonFile) throws DatabaseManagerException {
		Restaurant restaurant = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			restaurant = objectMapper.readValue(new File(jsonFile), Restaurant.class);
		} catch (Exception e) {
			throw new DatabaseManagerException(e.getMessage());
		}
		return restaurant;
	}

	private Document inizializeReviewDocument(Review review) {
		Document document = new Document();
		document.append("date", review.getDate()).append("rating", review.getRating())
				.append("restaurant", review.getRestaurant()).append("restaurantId", review.getRestaurantId());
		if (review.getReply() != null)
			document.append("reply", review.getReply());
		if (review.getText() != null)
			document.append("text", review.getText());
		if (review.getTitle() != null)
			document.append("title", review.getTitle());
		if (review.getUsername() != null)
			document.append("username", review.getUsername());
		return document;
	}

	private Document iniatializeRestaurantDocument(Restaurant restaurant) {
		Document document = new Document();
		document.append("name", restaurant.getName()).append("address", restaurant.getAddress())
				.append("approved", restaurant.isApproved()).append("rating", restaurant.getRating())
				.append("numberOfReviews", restaurant.getNumberOfReviews());
		if (restaurant.getPhoneNumber() != null)
			document.append("phoneNumber", restaurant.getPhoneNumber());
		if (restaurant.getOpeningHours() != null)
			document.append("openingHours", restaurant.getOpeningHours());
		if (restaurant.getPriceRange() != null)
			document.append("priceRange", restaurant.getPriceRange());
		if (restaurant.getCategories() != null)
			document.append("categories", restaurant.getCategories());
		if (restaurant.getOptions() != null)
			document.append("options", restaurant.getOptions());
		if (restaurant.getFeatures() != null)
			document.append("features", restaurant.getFeatures());
		if (restaurant.getMealtimes() != null)
			document.append("mealtimes", restaurant.getMealtimes());
		return document;
	}

	private Document iniatializeUserDocument(User user) {
		Document document = new Document();
		if (user.getName() != null)
			document.append("name", user.getName());
		if (user.getSurname() != null)
			document.append("surname", user.getSurname());
		document.append("username", user.getUsername()).append("password", user.getPassword()).append("type",
				user.getType());
		return document;
	}

	/**
	 * Populate the database with all restaurants files contained in the folder
	 * 
	 * @param folder the folder that contains files
	 * @throws DatabaseManagerException
	 */
	public void populateDatabase(File folder) throws DatabaseManagerException {
		try {
			names = Files.readAllLines(Paths.get(".\\names.txt"));
			surnames = Files.readAllLines(Paths.get(".\\surnames.txt"));
			usernames = Files.readAllLines(Paths.get(".\\usernames.txt"));
		} catch (IOException e) {
			throw new DatabaseManagerException(e.getMessage());
		}

		int numberName, numberSurname, numberUsername;

		for (File jsonFile : folder.listFiles()) {
			Restaurant restaurant;
			try {
				restaurant = loadRestaurantFromJson(folder + "\\" + jsonFile.getName());
			} catch (DatabaseManagerException e) {
				throw e;
			}

			RestaurantOwner owner = null;
			String ownerUsername = null;
			List<Review> restaurantReviews = restaurant.getReviews();
			restaurant.setReviews(null);

			// try to find the owner's username from reviews
			for (Review review : restaurantReviews) {
				if ((ownerUsername == null) && (review.getReply() != null) && (review.getReply().getOwner() != null)) {
					ownerUsername = review.getReply().getOwner();
					break;
				}
			}
			// get random username if not found
			if (ownerUsername == null) {
				numberUsername = (int) (Math.random() * (usernames.size() - 1));
				ownerUsername = usernames.remove(numberUsername);
			}

			// try to login the owner, if not found insert a new owner
			while (owner == null) {
				try {
					owner = (RestaurantOwner) login(ownerUsername, "pwd", RestaurantOwner.class);
				} catch (UserNotFoundException e) {
					numberName = (int) (Math.random() * (names.size() - 1));
					numberSurname = (int) (Math.random() * (surnames.size() - 1));
					owner = new RestaurantOwner(names.get(numberName), surnames.get(numberSurname), ownerUsername);
					try {
						owner = (RestaurantOwner) createAccount(owner);
					} catch (UsernameAlreadyPresentException ex) {
						owner = null;
						numberUsername = (int) (Math.random() * (usernames.size() - 1));
						ownerUsername = usernames.remove(numberUsername);
					}
					continue;
				} catch (DatabaseManagerException e) {
					throw e;
				}
			}

			restaurant.setRating(0);
			restaurant.setNumberOfReviews(0);
			insertRestaurant(restaurant, owner);

			// sort reviews by increasing date
			Collections.sort(restaurantReviews, new IncreasingDateComparator());
			for (Review review : restaurantReviews) {

				// try to get customer's username
				String customerUsername = review.getUsername();
				Customer customer = null;

				// get random username if not found
				if (customerUsername == null) {
					numberUsername = (int) (Math.random() * (usernames.size() - 1));
					customerUsername = usernames.remove(0);
				}

				while (customer == null) {
					try {
						customer = (Customer) login(customerUsername, "pwd", Customer.class);
					} catch (UserNotFoundException e) {
						numberName = (int) (Math.random() * (names.size() - 1));
						numberSurname = (int) (Math.random() * (surnames.size() - 1));
						customer = new Customer(names.get(numberName), surnames.get(numberSurname), customerUsername);
						try {
							customer = (Customer) createAccount(customer);
						} catch (UsernameAlreadyPresentException ex) {
							customer = null;
							numberUsername = (int) (Math.random() * (usernames.size() - 1));
							customerUsername = usernames.remove(0);
						}
					} catch (DatabaseManagerException e) {
						throw e;
					}
				}

				// insert the review in the collection of reviews
				try {
					review.setUsername(customerUsername);
					if (review.getReply() != null)
						review.getReply().setOwner(ownerUsername);
					insertReview(restaurant, review, customer);

					double rating = (restaurant.getRating() * restaurant.getNumberOfReviews() + review.getRating())
							/ (restaurant.getNumberOfReviews() + 1);
					restaurant.setRating(rating);
					restaurant.setNumberOfReviews(restaurant.getNumberOfReviews() + 1);
				} catch (DatabaseManagerException e) {
					throw e;
				}
			}
		}

		// insert administrators
		try {
			Administrator administrator = new Administrator("Maria", "Zinco", "admin1");
			createAccount(administrator);

			administrator = new Administrator("Matteo", "Rossi", "admin2");
			createAccount(administrator);

			administrator = new Administrator("Andrea", "Lindi", "admin3");
			createAccount(administrator);

			administrator = new Administrator("Giulia", "Bianco", "admin4");
			createAccount(administrator);

			administrator = new Administrator("Elisa", "Azzurri", "admin5");
			createAccount(administrator);
		} catch (Exception e) {
			throw new DatabaseManagerException(e.getMessage());
		}
	}

	/*
	 * Methods for generic User (even if not logged)
	 */

	/**
	 * Returns a page with a list of restaurants of a given category in a given city
	 * and whose name is similar to the given one. Restaurants are ordered by
	 * decreasing score.
	 * 
	 * @param name     the name of the restaurant
	 * @param category the category of restaurants
	 * @param city     the city of restaurants
	 * @param pageNum  the page number
	 * @param approved is true is the restaurant has been already approved, false
	 *                 otherwise
	 * @return the list of retrieved Restaurants in the given page number
	 * @throws DatabaseManagerException in case of unexpected errors
	 */
	public List<Restaurant> getRestaurants(String name, String category, String city, int pageNum, boolean approved)
			throws DatabaseManagerException {

		if (name == null) {
			// name is not specified
			if (category == null) {
				// category is not defined
				try {
					return getRestaurants(city, pageNum, approved);
				} catch (DatabaseManagerException e) {
					throw e;
				}
			} else {
				// category is defined
				try {
					return getRestaurants(category, city, pageNum, approved);
				} catch (DatabaseManagerException e) {
					throw e;
				}
			}
		} else {
			if (category == null) {
				try {
					return getRestaurants(name, city, approved);
				} catch (DatabaseManagerException e) {
					throw e;
				}			
			} else {
				try {
					return getRestaurants(name, category, city, approved);
				} catch (DatabaseManagerException e) {
					throw e;
				}			
			}
		}
	}

	/**
	 * Returns a page with a list of restaurants in a given city whose name is
	 * similar to the given one.
	 * 
	 * @param name     the name of the restaurant
	 * @param city     the city in which restaurants must be searched
	 * @param approved is true is the restaurant has been already approved, false
	 *                 otherwise
	 * @return the list of retrieved Restaurants
	 * @throws DatabaseManagerException in case of unexpected errors
	 */
	public List<Restaurant> getRestaurants(String name, String city, boolean approved)
			throws DatabaseManagerException {
		
		MongoCollection<Document> collection = mongoDatabase.getCollection("restaurants");
		
		// search by name, city is required
		Bson filter = and(eq("address.city", city), eq("approved", approved));
		
		// this can't be limited by page size since the comparison is partly done at the
		// application level
		MongoCursor<Document> cursor = collection.find(filter).iterator();
		List<RestaurantDistance> list = new ArrayList<>();
		List<Restaurant> restaurantList = new ArrayList<>();

		ObjectMapper mapper = new ObjectMapper();
		try {
			while (cursor.hasNext()) {
				if (name != null) {
					JaroWinklerDistance jwd = new JaroWinklerDistance();
					Restaurant restaurant = mapper.readValue(cursor.next().toJson(), Restaurant.class);
					
					String restaurantString = restaurant.getName().toLowerCase();
					if (restaurantString.length() > name.length())
						restaurantString = restaurantString.substring(0, Integer.min(restaurantString.length(), (name.length() + 2)));
					
					double distance = jwd.apply(restaurantString, name.toLowerCase());				
					if (distance >= 0.7) {
						list.add(new RestaurantDistance(restaurant, distance));
					}
					
					// sort by name similarity
					list.sort(new Comparator<RestaurantDistance>() {
						public int compare(RestaurantDistance o1, RestaurantDistance o2) {
							if (o1.getDistance() == o2.getDistance())
								return 0;
							return o1.getDistance() < o2.getDistance() ? 1 : -1;
						}
					});
										
					// swap list, 0 is inclusive and 10 is exlusive
					restaurantList = (List<Restaurant>) (List<?>) list.subList(0, Math.min(list.size(), pageSize));
				} else {
					Restaurant restaurant = mapper.readValue(cursor.next().toJson(), Restaurant.class);
					restaurantList.add(restaurant);
				}
			}	
			
			return restaurantList;
		} catch (Exception e) {
			throw new DatabaseManagerException(e.getMessage());
		} finally {
			cursor.close();
		}
	}
	
	/**
	 * Returns a page with a list of restaurants of a given category in a given city
	 * whose name is similar to the given one.
	 * 
	 * @param name     the name of the restaurant
	 * @param city     the city in which restaurants must be searched
	 * @param category category the category of restaurants
	 * @param approved is true is the restaurant has been already approved, false
	 *                 otherwise
	 * @return the list of retrieved Restaurants
	 * @throws DatabaseManagerException in case of unexpected errors
	 */
	private List<Restaurant> getRestaurants(String name, String category, String city, boolean approved)
			throws DatabaseManagerException {
		MongoCollection<Document> collection = mongoDatabase.getCollection("restaurants");

		// search by name, city is required
		Bson filter = and(eq("address.city", city), eq("categories", category), eq("approved", approved));

		// this can't be limited by page size since the comparison is partly done at the
		// application level
		MongoCursor<Document> cursor = collection.find(filter).iterator();
		List<RestaurantDistance> list = new ArrayList<>();
		List<Restaurant> restaurantList = new ArrayList<>();

		ObjectMapper mapper = new ObjectMapper();
		try {
			while (cursor.hasNext()) {
				if (name != null) {
					JaroWinklerDistance jwd = new JaroWinklerDistance();
					Restaurant restaurant = mapper.readValue(cursor.next().toJson(), Restaurant.class);
					
					String restaurantString = restaurant.getName().toLowerCase();
					if (restaurantString.length() > name.length())
						restaurantString = restaurantString.substring(0, Integer.min(restaurantString.length(), (name.length() + 2)));
					
					double distance = jwd.apply(restaurantString, name.toLowerCase());				
					if (distance >= 0.7) {
						list.add(new RestaurantDistance(restaurant, distance));
					}
					
					// sort by name similarity
					list.sort(new Comparator<RestaurantDistance>() {
						public int compare(RestaurantDistance o1, RestaurantDistance o2) {
							if (o1.getDistance() == o2.getDistance())
								return 0;
							return o1.getDistance() < o2.getDistance() ? 1 : -1;
						}
					});
										
					// swap list, 0 is inclusive and 10 is exlusive
					restaurantList = (List<Restaurant>) (List<?>) list.subList(0, Math.min(list.size(), pageSize));
				} else {
					Restaurant restaurant = mapper.readValue(cursor.next().toJson(), Restaurant.class);
					restaurantList.add(restaurant);
				}
			}		

			return restaurantList;
		} catch (Exception e) {
			throw new DatabaseManagerException(e.getMessage());
		} finally {
			cursor.close();
		}
	}
	
	/**
	 * Returns a page with a list of restaurants of a given category in a given
	 * city, ordered by decreasing score.
	 * 
	 * @param category the category of restaurants
	 * @param city     the city in which restaurants must be searched
	 * @param pageNum  the page number
	 * @param approved is true is the restaurant has been already approved, false
	 *                 otherwise
	 * @return the list of retrieved Restaurants in the given page number
	 * @throws DatabaseManagerException in case of unexpected errors
	 */
	public List<Restaurant> getRestaurants(String category, String city, int pageNum, boolean approved)
			throws DatabaseManagerException {

		MongoCollection<Document> collection = mongoDatabase.getCollection("restaurants");

		Bson filter = and(eq("address.city", city), eq("categories", category), eq("approved", approved));
		MongoCursor<Document> cursor = collection.find(filter).skip(pageSize * (pageNum - 1)).limit(pageSize)
				.iterator();
		List<Restaurant> list = new ArrayList<>(pageSize);
		ObjectMapper mapper = new ObjectMapper();
		try {
			while (cursor.hasNext()) {
				Restaurant restaurant = mapper.readValue(cursor.next().toJson(), Restaurant.class);
				list.add(restaurant);
			}
		} catch (Exception e) {
			throw new DatabaseManagerException(e.getMessage());
		} finally {
			cursor.close();
		}
		return list;
	}

	/**
	 * Returns a page with a list of restaurants in a given city, ordered by
	 * decreasing score.
	 * 
	 * @param city     the city in which restaurants must be searched
	 * @param pageNum  the page number
	 * @param approved when true search for approved restaurants, when false search
	 *                 for restaurants to approve
	 * @return the list of retrieved Restaurants in the given page number
	 * @throws DatabaseManagerException in case of unexpected errors
	 */
	public List<Restaurant> getRestaurants(String city, int pageNum, boolean approved) throws DatabaseManagerException {

		MongoCollection<Document> collection = mongoDatabase.getCollection("restaurants");

		Bson filter = and(eq("address.city", city), eq("approved", approved));
		MongoCursor<Document> cursor = collection.find(filter).skip(pageSize * (pageNum - 1)).limit(pageSize)
				.iterator();
		List<Restaurant> list = new ArrayList<>(pageSize);
		ObjectMapper mapper = new ObjectMapper();
		try {
			while (cursor.hasNext()) {
				Restaurant restaurant = mapper.readValue(cursor.next().toJson(), Restaurant.class);
				list.add(restaurant);
			}
		} catch (Exception e) {
			throw new DatabaseManagerException(e.getMessage());
		} finally {
			cursor.close();
		}
		
		return list;
	}

	/**
	 * Returns a page with a list of reviews of a given restaurant, ordered by
	 * decreasing date.
	 * 
	 * @param restaurant the Restaurant
	 * @param pageNum    the page number
	 * @return a list of Reviews
	 * @throws DatabaseManagerException in case of unexpected errors
	 */
	public List<Review> getReviews(Restaurant restaurant, int pageNum) throws DatabaseManagerException {

		// do not query db for the first page
		if (pageNum == 1) {
			// get all reviews
			List<Review> reviews = restaurant.getReviews();

			// consider the sublist at the end, copy it in another list
			if (reviews == null)
				return new ArrayList<>();

			List<Review> page = new ArrayList<>(
					reviews.subList(Integer.max(0, reviews.size() - pageSize), reviews.size()));

			// sort this list
			Collections.sort(page, new DecreasingDateComparator());

			return page;
		}

		MongoCursor<Document> cursor = null;
		Bson filter = eq("restaurantId", restaurant.getId());

		MongoCollection<Document> reviewsCollection = mongoDatabase.getCollection("reviews");

		cursor = reviewsCollection.find(filter).sort(Sorts.descending("date")).skip(pageSize * (pageNum - 1))
				.limit(pageSize).iterator();

		List<Review> list = new ArrayList<>(pageSize);
		ObjectMapper mapper = new ObjectMapper();

		try {
			while (cursor.hasNext()) {
				Review review = mapper.readValue(cursor.next().toJson(), Review.class);
				list.add(review);
			}
		} catch (Exception e) {
			throw new DatabaseManagerException(e.getMessage());
		} finally {
			cursor.close();
		}

		return list;
	}

	/*
	 * Methods for generic User
	 */

	/**
	 * Login a user.
	 * 
	 * @param username the User's username
	 * @param password the Users's password
	 * @param type     the User's type (can be a Customer, a RestaurantOwnwer or an
	 *                 Administrator)
	 * @return the logged User
	 * @throws UserNotFoundException    if a user with the given username and
	 *                                  password does not exist
	 * @throws DatabaseManagerException in case of other errors
	 */
	public User login(String username, String password, Class type)
			throws UserNotFoundException, DatabaseManagerException {

		String className = type.getSimpleName();

		if (!className.equals("Customer") && !className.equals("RestaurantOwner") && !className.equals("Administrator"))
			throw new IllegalArgumentException("Class type is not correct.");

		MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");

		Bson filter = and(eq("type", className), eq("username", username), eq("password", password));
		MongoCursor<Document> cursor = usersCollection.find(filter).iterator();
		if (!cursor.hasNext())
			throw new UserNotFoundException();

		ObjectMapper mapper = new ObjectMapper();
		User user = null;
		try {
			switch (className) {

			case "Customer":
				user = mapper.readValue(cursor.next().toJson(), Customer.class);
				break;

			case "RestaurantOwner":
				user = mapper.readValue(cursor.next().toJson(), RestaurantOwner.class);
				break;

			case "Administrator":
				user = mapper.readValue(cursor.next().toJson(), Administrator.class);
				break;

			}
		} catch (Exception e) {
			throw new DatabaseManagerException(e.getMessage());
		} finally {
			cursor.close();
		}

		return user;
	}

	/**
	 * Create an account for a new user.
	 * 
	 * @param user can be a Customer or a RestaurantOwner or an Administrator
	 * @return the inserted User
	 * @throws UsernameAlreadyPresentException if the username of the user is
	 *                                         already present
	 */
	public User createAccount(User user) throws UsernameAlreadyPresentException {

		startSession();

		TransactionBody<User> transaction = new TransactionBody<User>() {
			@Override
			public User execute() {

				MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");

				// check if the username is already used
				MongoCursor<Document> cursor = usersCollection.find(clientSession, eq("username", user.getUsername()))
						.iterator();
				if (cursor.hasNext())
					return null;

				// create the document for the user and insert it in the collection of users
				Document userDocument = iniatializeUserDocument(user);
				usersCollection.insertOne(clientSession, userDocument);

				user.setId((ObjectId) userDocument.getObjectId("_id"));
				return user;
			}

		};

		// read preference in a transaction must be primary
		TransactionOptions options = TransactionOptions.builder().readPreference(ReadPreference.primary()).build();

		User insertedUser = clientSession.withTransaction(transaction, options);

		closeSession();

		if (insertedUser == null)
			throw new UsernameAlreadyPresentException();

		return insertedUser;
	}

	/**
	 * Delete a user's account. If is a owner, remove also all their restaurants.
	 * 
	 * @param user the User to delete
	 * @throws DatabaseManagerException in case of errors
	 * @throws UserNotFoundException 
	 */
	public User deleteAccount(User user) throws UserNotFoundException {

		startSession();

		TransactionBody<User> transaction = new TransactionBody<User>() {

			@Override
			public User execute() {

				MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");

				// if the user is a restaurant owner retrieve his/her restaurants
				RestaurantOwner owner = null;

				// if the user is a restaurant owner remove all his/her restaurants
				if (user instanceof RestaurantOwner) {
					try {
						ObjectMapper mapper = new ObjectMapper();
						owner = mapper.readValue(usersCollection.find(clientSession, eq("username", user.getUsername()))
								.cursor().next().toJson(), RestaurantOwner.class);
					} catch (Exception e) {
						return null;
					}

					MongoCollection<Document> restaurantsCollection = mongoDatabase.getCollection("restaurants");
					MongoCollection<Document> reviewsCollection = mongoDatabase.getCollection("reviews");

					// for each restaurant
					for (Restaurant restaurant : owner.getRestaurants()) {

						// delete the restaurant from the collection of restaurants
						restaurantsCollection.deleteOne(clientSession, eq("_id", restaurant.getId()));

						// delete reviews of that restaurant from the collection of reviews
						reviewsCollection.deleteMany(clientSession, eq("restaurantId", restaurant.getId()));
					}
				}

				// delete the user from the collection of users
				DeleteResult res = usersCollection.deleteOne(clientSession, eq("username", user.getUsername()));
				if (res.getDeletedCount() == 0)
					return null;

				return user;
			}
		};

		// read preference in a transaction must be primary
		TransactionOptions options = TransactionOptions.builder().readPreference(ReadPreference.primary()).build();

		User deletedUser = clientSession.withTransaction(transaction, options);

		if (deletedUser == null) {
			throw new UserNotFoundException();
		}

		closeSession();

		return deletedUser;
	}

	/*
	 * Methods for Customer
	 */

	/**
	 * Insert a review written by a customer for a certain restaurant.
	 * 
	 * @param restaurant the Restaurant for which the review is written
	 * @param review     the Review
	 * @param customer   the author of the review
	 * @return the updated Restaurant
	 * @throws DatabaseManagerException in case of unexpected errors
	 */
	public Restaurant insertReview(Restaurant restaurant, Review review, Customer customer)
			throws DatabaseManagerException {

		startSession();

		TransactionBody<Restaurant> transaction = new TransactionBody<Restaurant>() {

			@Override
			public Restaurant execute() {

				MongoCollection<Document> restaurantsCollection = mongoDatabase.getCollection("restaurants");
				MongoCollection<Document> reviewsCollection = mongoDatabase.getCollection("reviews");
				MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");

				// set the name and the id of the restaurant in the review
				review.setRestaurant(restaurant.getName());
				review.setRestaurantId(restaurant.getId());

				// create the document for the review and store it in the collection of reviews
				Document reviewDocument = inizializeReviewDocument(review);
				reviewsCollection.insertOne(clientSession, reviewDocument);

				// get the id of the review which is set by MongoDB
				review.setId((ObjectId) reviewDocument.get("_id"));

				// insert the review as embedded document in the customer
				Bson filter = eq("username", customer.getUsername());
				Bson update = addToSet("reviews", review);
				usersCollection.updateOne(clientSession, filter, update);

				// insert the review as embedded document in the restaurant and update rating
				// and number of reviews
				double newRating = (restaurant.getRating() * restaurant.getNumberOfReviews() + review.getRating())
						/ (restaurant.getNumberOfReviews() + 1);
				filter = eq("_id", restaurant.getId());
				update = combine(addToSet("reviews", review), inc("numberOfReviews", 1), set("rating", newRating));
				restaurantsCollection.updateOne(clientSession, filter, update);

				// add the review and update rating and number of reviews in the restaurant
				// embedded in the owner
				filter = and(eq("type", "RestaurantOwner"), eq("restaurants._id", restaurant.getId()));
				update = combine(addToSet("restaurants.$.reviews", review), inc("restaurants.$.numberOfReviews", 1),
						set("restaurants.$.rating", newRating));
				usersCollection.updateOne(clientSession, filter, update);

				// must read the updated restaurant from the database to perform successive
				// operations
				filter = eq("_id", restaurant.getId());
				ObjectMapper mapper = new ObjectMapper();
				Restaurant readRestaurant;
				try {
					readRestaurant = mapper.readValue(
							restaurantsCollection.find(clientSession, filter).cursor().next().toJson(),
							Restaurant.class);
				} catch (Exception e) {
					return null;
				}

				// only the 10 most recent reviews are entirely embedded in the restaurant, for
				// the others only id, date and rating are stored
				List<Review> restaurantReviews = readRestaurant.getReviews();
				if (restaurantReviews != null && restaurantReviews.size() > 10) {
					Collections.sort(restaurantReviews, new DecreasingDateComparator());

					Review reviewToUpdate = restaurantReviews.get(10);

					// update the review in the restaurant
					filter = eq("reviews._id", reviewToUpdate.getId());
					update = combine(unset("reviews.$.restaurant"), unset("reviews.$.restaurantId"),
							unset("reviews.$.reply"), unset("reviews.$.text"), unset("reviews.$.title"),
							unset("reviews.$.username"));
					restaurantsCollection.updateOne(clientSession, filter, update);

					// delete the same review in the restaurant embedded in the owner
					filter = and(eq("type", "RestaurantOwner"), eq("restaurants._id", restaurant.getId()));
					update = pull("restaurants.$.reviews", new Document("_id", reviewToUpdate.getId()));
					usersCollection.updateOne(clientSession, filter, update);
				}

				return readRestaurant;
			}

		};

		// read preference in a transaction must be primary
		TransactionOptions options = TransactionOptions.builder().readPreference(ReadPreference.primary()).build();

		Restaurant updatedRestaurant = clientSession.withTransaction(transaction, options);

		closeSession();

		if (updatedRestaurant == null)
			throw new DatabaseManagerException();

		return updatedRestaurant;
	}

	public static double approximateRating(double newRating) {
		if ((newRating > 0.5) && (newRating < 1))
			return 0.5;
		if ((newRating > 1) && (newRating < 1.5))
			return 1;
		if ((newRating > 1.5) && (newRating < 2))
			return 1.5;
		if ((newRating > 2) && (newRating < 2.5))
			return 2;
		if ((newRating > 2.5) && (newRating < 3))
			return 2.5;
		if ((newRating > 3) && (newRating < 3.5))
			return 3;
		if ((newRating > 3.5) && (newRating < 4))
			return 3.5;
		if ((newRating > 4) && (newRating < 4.5))
			return 4;
		if ((newRating > 4.5) && (newRating < 5))
			return 4.5;
		return newRating;
	}

	/*
	 * Methods for Restaurant Owner
	 */

	/**
	 * Insert a restaurant in the database.
	 * 
	 * @param restaurant the Restaurant to add
	 * @param owner      the Owner of the restaurant
	 * @return the inserted Restaurant
	 */
	public Restaurant insertRestaurant(Restaurant restaurant, RestaurantOwner owner) {

		startSession();

		TransactionBody<Restaurant> transaction = new TransactionBody<Restaurant>() {

			@Override
			public Restaurant execute() {
				MongoCollection<Document> restaurantsCollection = mongoDatabase.getCollection("restaurants");
				MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");

				// create the document for the restaurant and store it in the collection of
				// restaurants
				Document restaurantDocument = iniatializeRestaurantDocument(restaurant);
				restaurantsCollection.insertOne(clientSession, restaurantDocument);

				// get the id of the restaurant set by MongoDB
				restaurant.setId((ObjectId) restaurantDocument.get("_id"));

				// insert the restaurant as embedded document of the owner
				Bson filter = eq("username", owner.getUsername());
				Bson update = addToSet("restaurants", restaurant);
				usersCollection.updateOne(clientSession, filter, update);
				owner.addRestaurant(restaurant);

				return restaurant;
			}

		};

		Restaurant insertedRestaurant = clientSession.withTransaction(transaction);

		closeSession();

		return insertedRestaurant;
	}

	/**
	 * Delete a restaurant.
	 * 
	 * @param restaurant the Restaurant to delete
	 */
	public Restaurant deleteRestaurant(Restaurant restaurant) {

		startSession();

		TransactionBody<Restaurant> transaction = new TransactionBody<Restaurant>() {

			@Override
			public Restaurant execute() {

				MongoCollection<Document> restaurantsCollection = mongoDatabase.getCollection("restaurants");
				MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");
				MongoCollection<Document> reviewsCollection = mongoDatabase.getCollection("reviews");

				// delete the restaurant from the collection of restaurants
				restaurantsCollection.deleteOne(clientSession, eq("_id", restaurant.getId()));

				// delete the restaurant as embedded document of the restaurant owner
				Bson filter = eq("restaurants._id", restaurant.getId());
				Bson update = pull("restaurants", new Document("_id", restaurant.getId()));
				usersCollection.updateOne(clientSession, filter, update);

				// delete the reviews of the restaurant from the collection of reviews
				filter = eq("restaurantId", restaurant.getId());
				reviewsCollection.deleteMany(clientSession, filter);

				return restaurant;
			}

		};

		Restaurant deletedRestaurant = clientSession.withTransaction(transaction);

		closeSession();

		return deletedRestaurant;
	}

	/**
	 * Updates oldRestaurant with the information in newRestaurant. Only phone
	 * number, opening hours, options, categories and features can be modified.
	 * 
	 * @param oldRestaurant
	 * @param newRestaurant
	 * @return an updated Restaurant reference
	 */
	public Restaurant updateRestaurant(Restaurant oldRestaurant, Restaurant newRestaurant) {

		startSession();

		TransactionBody<Restaurant> transaction = new TransactionBody<Restaurant>() {

			@Override
			public Restaurant execute() {

				MongoCollection<Document> restaurantsCollection = mongoDatabase.getCollection("restaurants");

				restaurantsCollection.updateOne(clientSession, eq("_id", oldRestaurant.getId()),
						combine(set("phoneNumber", newRestaurant.getPhoneNumber()),
								set("openingHours", newRestaurant.getOpeningHours()),
								set("categories", newRestaurant.getCategories()),
								set("options", newRestaurant.getOptions()),
								set("features", newRestaurant.getFeatures())));

				// update the restaurant embedded in the owner
				MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");
				Bson filter = eq("restaurants._id", oldRestaurant.getId());
				Bson update = combine(set("restaurants.$.phoneNumber", newRestaurant.getPhoneNumber()),
						set("restaurants.$.openingHours", newRestaurant.getOpeningHours()),
						set("restaurants.$.categories", newRestaurant.getCategories()),
						set("restaurants.$.options", newRestaurant.getOptions()),
						set("restaurants.$.features", newRestaurant.getFeatures()));
				usersCollection.updateOne(clientSession, filter, update);

				return newRestaurant;
			}

		};

		Restaurant updatedRestaurant = clientSession.withTransaction(transaction);

		closeSession();

		return updatedRestaurant;
	}

	/**
	 * Insert a reply to a review.
	 * 
	 * @param review the Review to which the reply is related
	 * @param reply  the Reply to add
	 * @return the updated Review
	 * @throws DatabaseManagerException
	 */
	public Review insertReply(Review review, Reply reply) throws DatabaseManagerException {

		startSession();

		TransactionBody<Review> transaction = new TransactionBody<Review>() {

			@Override
			public Review execute() {

				MongoCollection<Document> restaurantsCollection = mongoDatabase.getCollection("restaurants");
				MongoCollection<Document> reviewsCollection = mongoDatabase.getCollection("reviews");
				MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");

				// insert the reply as embedded in the review
				Bson filter = eq("_id", review.getId());
				Bson update = set("reply", reply);
				reviewsCollection.updateOne(clientSession, filter, update);
				review.setReply(reply);

				// insert the reply in the review embedded in the customer
				filter = and(eq("username", review.getUsername()), eq("reviews._id", review.getId()));
				update = set("reviews.$.reply", reply);
				usersCollection.updateOne(clientSession, filter, update);

				// return if the review is not one of the 10 most recent
				filter = eq("_id", review.getRestaurantId());
				Restaurant restaurant = null;
				ObjectMapper mapper = new ObjectMapper();
				try {
					restaurant = mapper.readValue(
							restaurantsCollection.find(clientSession, filter).cursor().next().toJson(),
							Restaurant.class);
				} catch (Exception e) {
					return null;
				}

				List<Review> restaurantReviews = restaurant.getReviews();
				Collections.sort(restaurantReviews, new DecreasingDateComparator());
				if (restaurantReviews.size() > 10)
					restaurantReviews = restaurantReviews.subList(0, 9);

				if (!restaurantReviews.contains(review))
					return review;

				// if the review is one of the 10 most recent

				// update the review as embedded in the restaurant
				filter = and(eq("reviews._id", review.getId()));
				update = set("reviews.$.reply", reply);
				restaurantsCollection.updateOne(clientSession, filter, update);

				// update the review in the restaurant in the owner's document (only if is one
				// of the 10 most recent)
				filter = and(eq("restaurants._id", review.getRestaurantId()));
				update = set("restaurants.$[outer].reviews.$[inner].reply", reply);
				List<Bson> filters = new ArrayList<Bson>();
				filters.add(eq("outer._id", review.getRestaurantId()));
				filters.add(eq("inner._id", review.getId()));
				UpdateOptions options = new UpdateOptions().arrayFilters(filters);
				usersCollection.updateOne(clientSession, filter, update, options);

				return review;
			}

		};

		// read preference in a transaction must be primary
		TransactionOptions options = TransactionOptions.builder().readPreference(ReadPreference.primary()).build();

		Review updatedReview = clientSession.withTransaction(transaction, options);

		closeSession();

		if (updatedReview == null)
			throw new DatabaseManagerException();

		return updatedReview;
	}

	/**
	 * Computes the trend of the reviews of a given restaurant from a given date to
	 * the current date. In particular, it aggregates the reviews in windows and
	 * then it computes the mean value. The trend is the sequence of this values.
	 * 
	 * @param restaurant the restaurant
	 * @param from       the starting date for the trend
	 * @param window     the number of days for the aggregation
	 * @return a list of TrendElement, sorted by date
	 */
	public List<TrendElement> showTrend(Restaurant restaurant, Date from, int window) {
		Date to = new Date();
		MongoCollection<Document> restaurants = mongoDatabase.getCollection("restaurants");

		AggregateIterable<Document> documents = restaurants.aggregate(Arrays.asList(
				match(eq("_id", restaurant.getId())), project(include("reviews")), unwind("$reviews"),
				match(gte("reviews.date", from)),
				addFields(new Field("diffMillis", new Document("$subtract", Arrays.asList("$reviews.date", from)))),
				addFields(new Field("diffDays", new Document("$divide", Arrays.asList("$diffMillis", 86400000L))),
						new Field("window", window)),
				group(eq("$floor", eq("$divide", Arrays.asList("$diffDays", "$window"))),
						avg("meanRating", "$reviews.rating"), min("from", "$reviews.date"), max("to", "$reviews.date"),
						sum("num", 1)),
				sort(ascending("from"))));

		List<TrendElement> trend = new ArrayList<>();
		for (Document doc : documents) {
			trend.add(new TrendElement(doc));
		}

		return trend;
	}

	/**
	 * Computes the comparison of a restaurant with its competitors. Competitors are
	 * restaurants in the same city with at least three categories in common.
	 * Results are sorted according to "points", i.e. the average rating of reviews
	 * written from the given date. The result object specifies the position in this
	 * rank and the number of competitors, as well as a list of ComparisonElement.
	 * This list contains information about the four closest competitors according
	 * to this rank (two below and two above). Each ComparisonElement contains the
	 * trend of a restaurant, starting from the given date. The result also provides
	 * the ComparisonElement relative to the best competitor (outside of the list).
	 * 
	 * @param restaurant the restaurant
	 * @param from       the starting date for the trend and for the computation of
	 *                   points
	 * @param window     the number of days for the aggregation
	 * @return a ComparisonResult object
	 */
	public ComparisonResult getComparisonWithCompetitors(Restaurant restaurant, Date from, int window) {
		MongoCollection<Document> restaurants = mongoDatabase.getCollection("restaurants");

		int commonCategoriesThreshold = 3;
		String city = restaurant.getAddress().getCity();
		List<String> categories = restaurant.getCategories();

		AggregateIterable<Document> documents = restaurants
				.aggregate(Arrays.asList(
						new Document("$match",
								new Document("$and", Arrays.asList(new Document("address.city", city),
										new Document("categories", new Document("$in", categories))))),
						new Document("$project", new Document("name", 1)
								.append("reviews",
										new Document("$filter",
												new Document("input", "$reviews").append("as", "review").append("cond",
														new Document("$gte", Arrays.asList("$$review.date", from)))))
								.append("commonCategories",
										new Document("$size",
												new Document("$setIntersection",
														Arrays.asList("$categories", categories))))),
						new Document("$match",
								new Document("commonCategories", new Document("$gte", commonCategoriesThreshold))),
						new Document("$unwind", new Document("path", "$reviews")),
						new Document("$group",
								new Document("_id", new Document("restaurantId", "$_id").append("name", "$name"))
										.append("points", new Document("$avg", "$reviews.rating"))
										.append("reviewsCounter", new Document("$sum", 1))
										.append("reviews", new Document("$push", "$reviews"))),
						new Document("$unwind", new Document("path", "$reviews")),
						new Document("$addFields",
								new Document("diffDays", new Document("$divide",
										Arrays.asList(new Document("$subtract", Arrays.asList("$reviews.date", from)),
												86400000L))).append("window", window)),
						new Document("$group", new Document("_id",
								new Document("windowIndex",
										new Document("$floor",
												new Document("$divide", Arrays.asList("$diffDays", "$window"))))
														.append("restaurantId", "$_id.restaurantId")
														.append("name", "$_id.name").append("points", "$points"))
																.append("meanRating",
																		new Document("$avg", "$reviews.rating"))
																.append("from", new Document("$min", "$reviews.date"))
																.append("to", new Document("$max", "$reviews.date"))
																.append("num", new Document("$sum", 1))),
						new Document("$sort", new Document("from", 1)),
						new Document("$group", new Document("_id", "$_id.restaurantId")
								.append("trend",
										new Document("$push",
												new Document("meanRating", "$meanRating").append("from", "$from")
														.append("to", "$to").append("num", "$num")))
								.append("name", new Document("$first", "$_id.name"))
								.append("points", new Document("$first", "$_id.points"))),
						new Document("$project", new Document("name", 1).append("points", 1).append("trend", 1)),
						new Document("$sort", new Document("points", -1))));

		if (!documents.iterator().hasNext())
			return null;

		List<ComparisonElement> comparison = new ArrayList<>();
		for (Document doc : documents) {
			comparison.add(new ComparisonElement(doc));
		}

		// filter the list "comparison"

		final int closeCompetitors = 2;
		int indexOfRestaurant = -1;

		for (int i = 0; i < comparison.size(); i++)
			if (comparison.get(i).getId().equals(restaurant.getId())) {
				indexOfRestaurant = i;
				break;
			}

		int l = indexOfRestaurant - closeCompetitors; // from this index (included)
		int h = indexOfRestaurant + closeCompetitors; // to this index (included)

		if (l < 0)
			l = 0;
		if (h >= comparison.size())
			h = comparison.size() - 1;

		// create result object

		int positionInRank = indexOfRestaurant + 1;
		int numberOfCompetitors = comparison.size();
		List<ComparisonElement> comparisonList = new ArrayList<>(comparison.subList(l, h + 1));
		ComparisonElement best = comparison.get(0);

		return new ComparisonResult(restaurant, positionInRank, numberOfCompetitors, comparisonList, best);
	}

	/**
	 * Computes the distribution of reviews of a given restaurant. It returns a list
	 * of DistributionElement, i.e. a list of star-percentage pairs.
	 * 
	 * @param restaurant the restaurant
	 * @param from       consider only reviews written after this date
	 * @return a list of DistributionElement, orderer from star 1 to star 5
	 */
	public List<DistributionElement> getReviewsDistribution(Restaurant restaurant, Date from) {
		MongoCollection<Document> restaurants = mongoDatabase.getCollection("restaurants");

		AggregateIterable<Document> documents = restaurants
				.aggregate(Arrays.asList(match(eq("_id", restaurant.getId())),
						project(computed("reviews",
								eq("$filter", and(eq("input", "$reviews"), eq("as", "review"),
										gte("cond", Arrays.asList("$$review.date", from)))))),
						addFields(new Field("total", new Document("$size", "$reviews"))), unwind("$reviews"),
						group("$reviews.rating", sum("counter", 1L), first("total", "$total")), sort(ascending("_id")),
						project(computed("percentage", eq("$divide",
								Arrays.asList(eq("$multiply", Arrays.asList(100L, "$counter")), "$total"))))));
		
		if(!documents.iterator().hasNext())
			return null;
		
		final int maxStars = 5;
		List<DistributionElement> distribution = new ArrayList<>(maxStars);

		// add elements (for each possible star) with percentage equal to 0
		for (int i = 1; i <= maxStars; i++)
			distribution.add(new DistributionElement(i));

		// set percentage of existing elements
		for (Document doc : documents) {
			int star_i = doc.getDouble("_id").intValue();
			double percentage_i = doc.getDouble("percentage");

			distribution.get(star_i - 1).setPercentage(percentage_i);
		}

		return distribution;
	}

	/*
	 * Methods for Administrator
	 */

	/**
	 * Return the list of requests to add a new restaurant.
	 * 
	 * @param pageNum the number of the page
	 * @return the list of Restaurants requests
	 * @throws DatabaseManagerException in case of errors
	 */
	public List<Restaurant> getPendingRequests(int pageNum) throws DatabaseManagerException {

		MongoCollection<Document> restaurantsCollection = mongoDatabase.getCollection("restaurants");

		Bson filter = eq("approved", false);
		MongoCursor<Document> cursor = restaurantsCollection.find(filter).skip(pageSize * (pageNum - 1)).limit(pageSize)
				.iterator();
		List<Restaurant> list = new ArrayList<>();

		// object mapper that does not throw exceptions if it finds properties in
		// the JSON that do not have a match in the POJO
		ObjectMapper mapper = new ObjectMapper();
		try {
			while (cursor.hasNext()) {
				// cast to java object here
				Restaurant restaurant = mapper.readValue(cursor.next().toJson(), Restaurant.class);
				list.add(restaurant);
			}
		} catch (Exception e) {
			throw new DatabaseManagerException(e.getMessage());
		} finally {
			cursor.close();
		}

		return list;
	}

	/**
	 * Accept a request to insert a restaurant.
	 * 
	 * @param request the Restaurant to accept
	 * @return the accepted Restaurant
	 */
	public Restaurant acceptPendingRequest(Restaurant request) {

		startSession();

		TransactionBody<Restaurant> transaction = new TransactionBody<Restaurant>() {

			@Override
			public Restaurant execute() {

				MongoCollection<Document> restaurantsCollection = mongoDatabase.getCollection("restaurants");
				MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");

				// set approved equal to true in the document of the restaurant
				Bson filter = eq("_id", request.getId());
				Bson update = set("approved", true);
				restaurantsCollection.updateOne(clientSession, filter, update);

				// set approved equal to true in the restaurant embedded in the owner
				filter = eq("restaurants._id", request.getId());
				update = set("restaurants.$.approved", true);
				usersCollection.updateOne(clientSession, filter, update);

				request.setApproved(true);
				return request;
			}

		};

		Restaurant updatedRestaurant = clientSession.withTransaction(transaction);

		closeSession();

		return updatedRestaurant;
	}

	/**
	 * Refuse a request to insert a restaurant.
	 * 
	 * @param request the Restaurant to refuse
	 * @return the refused Restaurant
	 */
	public Restaurant refusePendingRequest(Restaurant request) {
		deleteRestaurant(request);
		return request;
	}

	/**
	 * Find a list of reviews
	 * 
	 * @param restaurantName
	 * @param customerUsername
	 * @param titleReview
	 * @return the list of reviews
	 * @throws DatabaseManagerException 
	 */
	
	public List<Review> getReviews(String restaurantName, String customerUsername) throws DatabaseManagerException {
		
		MongoCollection<Document> reviewsCollection = mongoDatabase.getCollection("reviews");

		
		Bson filter = and(	eq("restaurant", 	restaurantName), 
							eq("username", 		customerUsername)
						);

		MongoCursor<Document> cursor = reviewsCollection.find(filter).iterator();

		List<Review> list = new ArrayList<>();

		// object mapper that does not throw exceptions if it finds properties in
		// the JSON that do not have a match in the POJO
		ObjectMapper mapper = new ObjectMapper();
		try {
			while (cursor.hasNext()) {
				// cast to java object here
				Review review = mapper.readValue(cursor.next().toJson(), Review.class);
				list.add(review);
			}
		} catch (Exception e) {
			throw new DatabaseManagerException();
		} finally {
			cursor.close();
		}

		return list;

	}

	/*
	 * Shared methods
	 */

	/**
	 * Delete a review (can be performed by both customer and administrator).
	 * 
	 * @param review the review to delete
	 * @return the deleted review
	 * @throws DatabaseManagerException in case of errors
	 */
	public Review deleteReview(Review review) throws DatabaseManagerException {

		startSession();

		TransactionBody<Review> transaction = new TransactionBody<Review>() {

			@Override
			public Review execute() {

				MongoCollection<Document> restaurantsCollection = mongoDatabase.getCollection("restaurants");
				MongoCollection<Document> reviewsCollection = mongoDatabase.getCollection("reviews");
				MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");

				// remove the review from the collection of reviews
				Bson filter = eq("_id", review.getId());
				reviewsCollection.deleteOne(clientSession, filter);

				// remove the review as embedded in the customer
				filter = eq("username", review.getUsername());
				Bson update = pull("reviews", new Document("_id", review.getId()));
				usersCollection.updateOne(clientSession, filter, update);

				// retrieve the restaurant
				ObjectMapper mapper = new ObjectMapper();
				filter = eq("_id", review.getRestaurantId());
				Restaurant restaurant;
				try {
					restaurant = mapper.readValue(
							restaurantsCollection.find(clientSession, filter).cursor().next().toJson(),
							Restaurant.class);
				} catch (Exception e) {
					return null;
				}
				List<Review> reviews = restaurant.getReviews();

				// remove the review as embedded in the restaurant and update number of reviews
				// and global rating
				double newRating;
				if (restaurant.getNumberOfReviews() == 1)
					newRating = 0;
				else
					newRating = (restaurant.getRating() * restaurant.getNumberOfReviews() - review.getRating())
							/ (restaurant.getNumberOfReviews() - 1);
				update = combine(pull("reviews", new Document("_id", review.getId())), inc("numberOfReviews", -1),
						set("rating", newRating));
				restaurantsCollection.updateOne(clientSession, filter, update);

				// remove the review as embedded in the restaurant embedded in the owner and
				// update number of reviews and global rating
				filter = and(eq("type", "RestaurantOwner"), eq("restaurants._id", restaurant.getId()));
				update = combine(inc("restaurants.$.numberOfReviews", -1), set("restaurants.$.rating", newRating),
						pull("restaurants.$.reviews", new Document("_id", review.getId())));
				usersCollection.updateOne(clientSession, filter, update);

				// if the review is one of the most 10 recent, make another review replacing it
				Collections.sort(reviews, new DecreasingDateComparator());
				if (reviews.size() < 10)
					return review;
				List<Review> recentReviews = reviews.subList(0, Integer.min(reviews.size(), pageSize));
				if (!recentReviews.contains(review))
					return review;

				Review reviewToAdd = reviews.get(pageSize);
				try {
					reviewToAdd = mapper.readValue(
							reviewsCollection.find(eq("_id", reviewToAdd.getId())).cursor().next().toJson(),
							Review.class);
				} catch (Exception e) {
					return null;
				}

				filter = eq("reviews._id", reviewToAdd.getId());
				update = combine(set("reviews.$.restaurant", reviewToAdd.getRestaurant()),
						set("reviews.$.restaurantId", reviewToAdd.getRestaurantId()),
						set("reviews.$.text", reviewToAdd.getText()), set("reviews.$.title", reviewToAdd.getTitle()),
						set("reviews.$.username", reviewToAdd.getUsername()),
						set("reviews.$.reply", reviewToAdd.getReply()));
				restaurantsCollection.updateOne(clientSession, filter, update);

				filter = and(eq("type", "RestaurantOwner"), eq("restaurants._id", restaurant.getId()));
				update = addToSet("restaurants.$.reviews", reviewToAdd);
				usersCollection.updateOne(clientSession, filter, update);

				return review;
			}

		};

		// read preference in a transaction must be primary
		TransactionOptions options = TransactionOptions.builder().readPreference(ReadPreference.primary()).build();

		Review deletedReview = clientSession.withTransaction(transaction, options);

		closeSession();

		if (deletedReview == null) {
			throw new DatabaseManagerException();
		}

		return deletedReview;

	}
	
	/**
	 * Update the database with the restaurants contained in a set of files
	 * 
	 * @param restaurantFiles the list of files
	 * @throws DatabaseManagerException in case of unexpected errors
	 */
	public void updateDatabase(List<File> restaurantFiles) throws DatabaseManagerException {
		
		for (File file : restaurantFiles) {
			Restaurant restaurant;
			try {
				restaurant = loadRestaurantFromJson(".\\data\\" + "\\" + file.getName());
			} catch (DatabaseManagerException e) {
				throw e;
			}

			RestaurantOwner owner = null;
			String ownerUsername = null;
			List<Review> restaurantReviews = restaurant.getReviews();
			restaurant.setReviews(null);

			// try to find the owner's username from reviews
			for (Review review : restaurantReviews) {
				if ((ownerUsername == null) && (review.getReply() != null) && (review.getReply().getOwner() != null)) {
					ownerUsername = review.getReply().getOwner();
					break;
				}
			}
			
			// if not found set the owner username equal to the name of the restaurant
			if (ownerUsername == null) 
				ownerUsername = restaurant.getName();

			// try to login the owner, if not found insert a new owner
			int i = 0;
			while (owner == null) {
				i++;
				try {
					owner = (RestaurantOwner) login(ownerUsername, "pwd", RestaurantOwner.class);
				} catch (UserNotFoundException e) {
					owner = new RestaurantOwner(ownerUsername);
					try {
						owner = (RestaurantOwner) createAccount(owner);
					} catch (UsernameAlreadyPresentException ex) {
						owner = null;
						ownerUsername = restaurant.getName() + i;
					}
					continue;
				} catch (DatabaseManagerException e) {
					throw e;
				}
			}

			restaurant.setRating(0);
			restaurant.setNumberOfReviews(0);
			insertRestaurant(restaurant, owner);

			// sort reviews by increasing date
			Collections.sort(restaurantReviews, new IncreasingDateComparator());
			for (Review review : restaurantReviews) {

				// try to get customer's username
				String customerUsername = review.getUsername();
				Customer customer = null;

				// if the user is null, insert only the review
				if (customerUsername != null) {
					try {
						customer = (Customer) login(customerUsername, "pwd", Customer.class);
					} catch (UserNotFoundException e) {
						customer = new Customer(customerUsername);
						try {
							customer = (Customer) createAccount(customer);
						} catch (UsernameAlreadyPresentException ex) {
							throw new DatabaseManagerException(ex.getMessage());
						}
					}
				}

				// insert the review in the collection of reviews
				try {
					review.setUsername(customerUsername);
					if (review.getReply() != null)
						review.getReply().setOwner(ownerUsername);
					insertReview(restaurant, review, customer);

					double rating = (restaurant.getRating() * restaurant.getNumberOfReviews() + review.getRating())
							/ (restaurant.getNumberOfReviews() + 1);
					restaurant.setRating(rating);
					restaurant.setNumberOfReviews(restaurant.getNumberOfReviews() + 1);
				} catch (DatabaseManagerException e) {
					throw e;
				}
			}
		}
	}
	
	private User getUserByUsername(String username, Class userClass)
			throws UserNotFoundException, DatabaseManagerException {
		MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");
		MongoCursor<Document> cursor = usersCollection.find(eq("username", username)).iterator();

		if (!cursor.hasNext())
			throw new UserNotFoundException();

		ObjectMapper mapper = new ObjectMapper();
		try {
			return (User) mapper.readValue(cursor.next().toJson(), userClass);
		} catch (Exception e) {
			throw new DatabaseManagerException(e.getMessage());
		} finally {
			cursor.close();
		}
	}

	public void createCollectionsAndIndexes() {
		mongoDatabase.createCollection("restaurants");
		mongoDatabase.createCollection("reviews");
		mongoDatabase.createCollection("users");

		MongoCollection<Document> restaurantsCollection = mongoDatabase.getCollection("restaurants");
		restaurantsCollection.createIndex(Indexes.compoundIndex(Indexes.descending("approved"),
				Indexes.descending("address.city"), Indexes.descending("categories"), Indexes.descending("rating")));
		restaurantsCollection.createIndex(Indexes.compoundIndex(Indexes.descending("approved"),
				Indexes.descending("address.city"), Indexes.descending("rating")));
		
		MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");
		usersCollection.createIndex(Indexes.compoundIndex(Indexes.descending("type"), Indexes.descending("username")));
	}
	
}
