package backend.modules;

import org.bson.Document;

public class DistributionElement {

	private int stars;
	private double percentage;
	
	public DistributionElement(int stars) {
		this.stars = stars;
		this.percentage = 0;
	}

	public DistributionElement(Document doc) {
		this.stars = doc.getInteger("_id");
		this.percentage = doc.getDouble("percentage");
	}

	public int getStars() {
		return stars;
	}

	public double getPercentage() {
		return percentage;
	}

	public void setStars(int stars) {
		this.stars = stars;
	}

	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}

	@Override
	public String toString() {
		return "DistributionElement [stars=" + stars + ", percentage=" + percentage + "]";
	}

}
