package backend.modules;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

public class Customer extends User {

	private List<Review> reviews;

	public Customer() {

	}

	public Customer(ObjectId id, String name, String surname, String username, String password) {
		super(id, name, surname, username, password, Customer.class.getSimpleName());
		this.reviews = new ArrayList<Review>();
	}

	public Customer(ObjectId id, String name, String surname, String username, String password, List<Review> reviews) {
		super(id, name, surname, username, password, Customer.class.getSimpleName());
		this.reviews = reviews;
	}

	public Customer(String name, String surname, String username, String password) {
		super(name, surname, username, password, Customer.class.getSimpleName());
		this.reviews = new ArrayList<Review>();
	}

	public Customer(String name, String surname, String username) {
		super(name, surname, username, Customer.class.getSimpleName());
		this.reviews = new ArrayList<Review>();
	}

	public Customer(String username) {
		super(username, Customer.class.getSimpleName());
		this.reviews = new ArrayList<Review>();
	}

	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

	@Override
	public String toString() {
		String string = super.toString();
		for (Review r : reviews) {
			string += r.toString();
		}
		return string;
	}
}
