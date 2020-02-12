package backend.modules;

public class RestaurantDistance extends Restaurant {

	private double distance;

	public RestaurantDistance(Restaurant restaurant, double distance) {
		super(restaurant.getId(), restaurant.getName(), restaurant.getAddress(), restaurant.isApproved(),
				restaurant.getPhoneNumber(), restaurant.getOpeningHours(), restaurant.getPriceRange(),
				restaurant.getCategories(), restaurant.getOptions(), restaurant.getFeatures(),
				restaurant.getMealtimes(), restaurant.getNumberOfReviews(), restaurant.getRating(),
				restaurant.getReviews());
		this.distance = distance;

	}
	
	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getDistance() {
		return this.distance;
	}

}
