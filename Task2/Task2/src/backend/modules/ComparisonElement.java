package backend.modules;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

public class ComparisonElement {
	
	private ObjectId id;
	private String name;
	private double points;
	private List<TrendElement> trend;

	public ComparisonElement(Document doc) {
		this.id = doc.getObjectId("_id");
		this.name = doc.getString("name");
		this.points = doc.getDouble("points");
		
		List<Document> array = (List<Document>) doc.get("trend");
		this.trend = new ArrayList<>(array.size());
		for (Document d : array) {
			trend.add(new TrendElement(d));
		}
	}

	public ObjectId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public double getPoints() {
		return points;
	}

	public List<TrendElement> getTrend() {
		return trend;
	}

	@Override
	public String toString() {
		return "ComparisonElement [id=" + id + ", name=" + name + ", points=" + points + "]";
	}

}
