package backend.modules;

import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import backend.ObjectIdDeserializer;

public class Restaurant {
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	@JsonProperty("_id")
	private ObjectId id;

	private String name;

	private Address address;

	private boolean approved;

	private String phoneNumber;

	private List<OpeningHour> openingHours;

	private PriceRange priceRange;

	private List<String> categories;

	private List<String> options;

	private List<String> features;

	private List<String> mealtimes;

	private int numberOfReviews;

	private double rating;

	private List<Review> reviews;

	public Restaurant() {
		
	}

	public Restaurant(ObjectId id, String name, Address address, boolean approved, String phoneNumber,
			List<OpeningHour> openingHours, PriceRange priceRange, List<String> categories, List<String> options,
			List<String> features, List<String> mealtimes, int numberOfReviews, double rating, List<Review> reviews) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.approved = approved;
		this.phoneNumber = phoneNumber;
		this.openingHours = openingHours;
		this.priceRange = priceRange;
		this.categories = categories;
		this.options = options;
		this.features = features;
		this.mealtimes = mealtimes;
		this.numberOfReviews = numberOfReviews;
		this.rating = rating;
		this.reviews = reviews;
	}

	public Restaurant(String name, Address address, String phoneNumber, List<OpeningHour> openingHours, List<String> categories) {
		this.name = name;
		this.address = address;
		this.approved = false;
		this.phoneNumber = phoneNumber;
		this.openingHours = openingHours;
		this.categories = categories;
		this.numberOfReviews = 0;
		this.rating = 0.0;
		this.reviews = new ArrayList<Review>();
	}
	
	/**
	  * Sort of a copy constructor. Creates a new Restaurant with the same references
	  * as the argument.
	  * 
	  * @param restaurant the restaurant to copy
	  */	
	 public Restaurant(Restaurant restaurant) {
	  this.id = restaurant.id;
	  this.name = restaurant.name;
	  this.address = restaurant.address;
	  this.approved = restaurant.approved;
	  this.phoneNumber = restaurant.phoneNumber;
	  this.openingHours = restaurant.openingHours;
	  this.priceRange = restaurant.priceRange;
	  this.categories = restaurant.categories;
	  this.options = restaurant.options;
	  this.features = restaurant.features;
	  this.mealtimes = restaurant.mealtimes;
	  this.numberOfReviews = restaurant.numberOfReviews;
	  this.rating = restaurant.rating;
	  this.reviews = restaurant.reviews;
	 }

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public List<OpeningHour> getOpeningHours() {
		return openingHours;
	}

	public void setOpeningHours(List<OpeningHour> openingHours) {
		this.openingHours = openingHours;
	}

	public PriceRange getPriceRange() {
		return priceRange;
	}

	public void setPriceRange(PriceRange priceRange) {
		this.priceRange = priceRange;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	public List<String> getFeatures() {
		return features;
	}

	public void setFeatures(List<String> features) {
		this.features = features;
	}

	public List<String> getMealtimes() {
		return mealtimes;
	}

	public void setMealtimes(List<String> mealtimes) {
		this.mealtimes = mealtimes;
	}

	public int getNumberOfReviews() {
		return numberOfReviews;
	}

	public void setNumberOfReviews(int numberOfReviews) {
		this.numberOfReviews = numberOfReviews;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

	@Override
	public String toString() {
		return "\t{\n\tname: " + name + "\n\taddress: " + address + "\n\tphoneNumber: " + phoneNumber + "\n\topeningHours: "
				+ openingHours + "\n\tpriceRange: " + priceRange + "\n\tcategories: " + categories + "\n\toptions: " + options
				+ "\n\tfeatures: " + features + "\n\tmealtimes: " + mealtimes + "\n\tnumberOfReviews: " + numberOfReviews
				+ "\n\trating: " + rating + "\n\t}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Restaurant other = (Restaurant) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
