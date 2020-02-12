package backend.modules;

import java.util.List;

public class ComparisonResult {

	private Restaurant restaurant;

	private int positionInRank;
	private int numberOfCompetitors;

	private List<ComparisonElement> comparisonList;
	private ComparisonElement best;

	public ComparisonResult(Restaurant restaurant, int positionInRank, int numberOfCompetitors,
			List<ComparisonElement> comparisonList, ComparisonElement best) {

		this.restaurant = restaurant;
		this.positionInRank = positionInRank;
		this.numberOfCompetitors = numberOfCompetitors;
		this.comparisonList = comparisonList;
		this.best = best;
	}

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}

	public int getPositionInRank() {
		return positionInRank;
	}

	public void setPositionInRank(int positionInRank) {
		this.positionInRank = positionInRank;
	}

	public int getNumberOfCompetitors() {
		return numberOfCompetitors;
	}

	public void setNumberOfCompetitors(int numberOfCompetitors) {
		this.numberOfCompetitors = numberOfCompetitors;
	}

	public List<ComparisonElement> getComparisonList() {
		return comparisonList;
	}

	public void setComparisonList(List<ComparisonElement> comparisonList) {
		this.comparisonList = comparisonList;
	}

	public ComparisonElement getBest() {
		return best;
	}

	public void setBest(ComparisonElement best) {
		this.best = best;
	}

	@Override
	public String toString() {
		return "ComparisonResult [restaurant=" + restaurant + ", positionInRank=" + positionInRank
				+ ", numberOfCompetitors=" + numberOfCompetitors + ", comparisonList=" + comparisonList + ", best="
				+ best + "]";
	}

}
