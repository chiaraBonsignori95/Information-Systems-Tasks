package backend.modules;

import java.util.Date;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import backend.*;

public class Review {
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	@JsonProperty("_id")
	private ObjectId id;
	
	@JsonDeserialize(using = DateDeserializer.class)
	@JsonProperty("date")
	private Date date;
	
    private double rating;
    
    private String restaurant;
    
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId restaurantId;

	private Reply reply;
    
    private String text;
    
	private String title;
    
    private String username;

	public Review() {
	}

	public Review(ObjectId _id, Date date, double rating, String restaurant, ObjectId restaurantId, Reply reply,
			String text, String title, String username) {
		this.id = _id;
		this.date = date;
		this.rating = rating;
		this.restaurant = restaurant;
		this.restaurantId = restaurantId;
		this.reply = reply;
		this.text = text;
		this.title = title;
		this.username = username;
	}

	public Review(Date date, double rating, String restaurant, ObjectId restaurantId, String text, String title,
			String username) {
		this.date = date;
		this.rating = rating;
		this.restaurant = restaurant;
		this.restaurantId = restaurantId;
		this.text = text;
		this.title = title;
		this.username = username;
	}
	
	public Review(ObjectId _id, Date date, double rating, String username) {
		this.date = date;
		this.rating = rating;
		this.username = username;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public String getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(String restaurant) {
		this.restaurant = restaurant;
	}

	public ObjectId getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(ObjectId restaurantId) {
		this.restaurantId = restaurantId;
	}

	public Reply getReply() {
		return reply;
	}

	public void setReply(Reply reply) {
		this.reply = reply;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

    @Override
	public String toString() {
		return "\t{\n\tdate: " + date + ", rating: " + rating + ", restaurant: " + restaurant + ", title=" + title + ", username=" + username +
				"\n\ttext: " + text +
				"\n\treply: " + reply + "\n\t}";
	}
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((restaurantId == null) ? 0 : restaurantId.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Review other = (Review) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (restaurantId == null) {
			if (other.restaurantId != null)
				return false;
		} else if (!restaurantId.equals(other.restaurantId))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
}
