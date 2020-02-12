package backend;

import java.util.Comparator;

import backend.modules.Review;

public class IncreasingDateComparator implements Comparator<Review> 
{ 
	
	@Override
	public int compare(Review r1, Review r2) {
		
		return r1.getDate().compareTo(r2.getDate());
	} 
} 