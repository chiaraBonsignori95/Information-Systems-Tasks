package backend;

import java.util.Comparator;

import backend.modules.Review;

public class DecreasingDateComparator implements Comparator<Review> {

	@Override
	public int compare(Review r1, Review r2) {
		
		if (r2.getDate().equals(r1.getDate()))
			return -1;
		return r2.getDate().compareTo(r1.getDate());
	} 
	
}
