package backend.modules;

import java.util.ArrayList;
import java.util.List;

public final class OpeningHour {

	private String days;
	
	private List<String> times;

	public OpeningHour() {
		
		times = new ArrayList<>();
	}

	public OpeningHour(String days, List<String> times) {
		this.days = days;
		this.times = times;
	}
	
	public String getDays() {
		return days;
	}

	public void setDays(String day) {
		this.days = day;
	}

	public List<String> getTimes() {
		return times;
	}

	public void setTimes(List<String> times) {
		this.times = times;
	}

	/**
	 * Add an opening time for certain days
	 * 
	 * @param time
	 */
	public void addTime(String time) {
		times.add(time);
	}

	/**
	 * Delete an opening time for certain days
	 * 
	 * @param time
	 * @return Returns true if time is contained in the list of opening time
	 */
	public boolean deleteTime(String time) {
		return times.remove(time);
	}

	@Override
	public String toString() {
		return "[" + days + "] : " + times;
	}

}
