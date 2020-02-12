package backend.modules;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

public class RestaurantOwner extends User {

	private List<Restaurant> restaurants;

	public RestaurantOwner() {		
		super();
		this.restaurants = new ArrayList<Restaurant>();
	}

	public RestaurantOwner(ObjectId id, String name, String surname, String username, String password) {
		super(id, name, surname, username, password, RestaurantOwner.class.getSimpleName());
		this.restaurants = new ArrayList<Restaurant>();
	}

	public RestaurantOwner(ObjectId id, String name, String surname, String username, String password,
			List<Restaurant> restaurants) {
		super(id, name, surname, username, password, RestaurantOwner.class.getSimpleName());
		this.restaurants = restaurants;
	}

	public RestaurantOwner(String name, String surname, String username, String password) {
		super(name, surname, username, password,  RestaurantOwner.class.getSimpleName());
		this.restaurants = new ArrayList<Restaurant>();
	}

	public RestaurantOwner(String name, String surname, String username) {
		super(name, surname, username, RestaurantOwner.class.getSimpleName());
		this.restaurants = new ArrayList<Restaurant>();
	}

	public RestaurantOwner(String username) {
		super(username, RestaurantOwner.class.getSimpleName());
		this.restaurants = new ArrayList<Restaurant>();
	}

	public List<Restaurant> getRestaurants() {
		return restaurants;
	}

	public void setRestaurants(List<Restaurant> restaurants) {
		this.restaurants = restaurants;
	}

	public void addRestaurant(Restaurant restaurant) {
		restaurants.add(restaurant);
	}

	@Override
	public String toString() {
		String string = super.toString();
		for (Restaurant r : restaurants) {
			string += r.toString();
		}

		return string;
	}
}
