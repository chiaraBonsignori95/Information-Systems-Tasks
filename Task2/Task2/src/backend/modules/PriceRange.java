package backend.modules;

public class PriceRange {
	
	private double minPrice;
	
	private double maxPrice;
	
	public PriceRange() {		
	}
	
	public PriceRange(double minPrice, double maxPrice) {
		this.minPrice = minPrice;
		this.maxPrice = maxPrice;
	}

	public double getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(double minPrice) {
		this.minPrice = minPrice;
	}

	public double getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(double maxPrice) {
		this.maxPrice = maxPrice;
	}

	@Override
	public String toString() {
		return "Minimum price: " + minPrice + " € " + "  Maximum price: " + maxPrice + " €";
	}

}
