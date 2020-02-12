package backend.modules;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.Document;

public class TrendElement {

	private double meanRating; 		// mean rating of the reviews in the window
	private Date minDate; 			// minimum date among the reviews dates in the window
	private Date maxDate; 			// maximum date among the reviews dates in the window
	private int reviewsCount; 		// number of reviews in the window

	public final String json;

	public TrendElement(Document doc) {
		this.meanRating = doc.getDouble("meanRating");
		this.minDate = doc.getDate("from");
		this.maxDate = doc.getDate("to");
		this.reviewsCount = doc.getInteger("num");

		this.json = doc.toJson();
	}

	public double getMeanRating() {
		return meanRating;
	}

	public void setMeanRating(double meanRating) {
		this.meanRating = meanRating;
	}

	public Date getMinDate() {
		return minDate;
	}

	public void setMinDate(Date minDate) {
		this.minDate = minDate;
	}

	public Date getMaxDate() {
		return maxDate;
	}

	public void setMaxDate(Date maxDate) {
		this.maxDate = maxDate;
	}

	public int getReviewsCount() {
		return reviewsCount;
	}

	public void setReviewsCount(int reviewsCount) {
		this.reviewsCount = reviewsCount;
	}

	@Override
	public String toString() {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		return "TrendElement [meanRating=" + meanRating + ", minDate=" + f.format(minDate) + ", maxDate="
				+ f.format(maxDate) + ", reviewsCount=" + reviewsCount + "]";
	}

}
