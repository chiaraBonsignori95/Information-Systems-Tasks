package application;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import backend.modules.OpeningHour;

public class Timetable {
	
	private class Range {
		
		LocalTime start;
		LocalTime end;
		
		public Range(LocalTime start, LocalTime end) {

			this.start = start;
			this.end = end;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Range other = (Range) obj;
			
			if (end == null) {
				if (other.end != null)
					return false;
			} else if (!end.equals(other.end))
				return false;
			if (start == null) {
				if (other.start != null)
					return false;
			} else if (!start.equals(other.start))
				return false;
			return true;
		}

		private boolean isOverlapped(Range range) {			
			return !(start.isAfter(range.end) || end.isBefore(range.start));
		}
		
		private String simpleToString() {
			return start + " - " + end;
		}
		
		@Override
		public String toString() {
			return "{ " + simpleToString() + " }";
		}
		
	}

	private int numberOfRecords;
	
	List<List<Range>> week;
	

	public Timetable(){
		numberOfRecords = 0;
		
		week = new ArrayList<>();
		
		for(int index = 0; index < 7; index++ )
			week.add(new ArrayList<>());
	}
	
	public static int toIndex(String day) {
		
		switch(day) {
		case "Lunedì":
			return 0;
		case "Martedì":
			return 1;
		case "Mercoledì":
			return 2;
		case "Giovedì":
			return 3;
		case "Venerdì":
			return 4;
		case "Sabato":
			return 5;
		case "Domenica":
			return 6;
		default:
			return -1;
		}
	}
	
	public static String parseInCompleteDayName(String day) {
		switch(day) {
		case "lun":
			return "Lunedì";
		case "mar":
			return "Martedì";
		case "mer":
			return "Mercoledì";
		case "gio":
			return "Giovedì";
		case "ven":
			return "Venerdì";
		case "sab":
			return "Sabato";
		case "dom":
			return "Domenica";
		default:
			return "err";
		}
	}
	
	private boolean isTimetableFree(List<Range> list, Range range) {
		for (Range r : list) {
			if(r != null && r.isOverlapped(range))
				return false;
		}
		
		return true;
	}
	
	public void addTimetableFromOpeningHour(List<OpeningHour> timetables) throws TimetableNotAvailableException {
		
		if (timetables == null)
			return;
	
		for(OpeningHour opening : timetables) {
			String[] days = opening.getDays().split(" - ");
			List<String> hours = opening.getTimes();
			for(String h : hours) {
				String[] hoursSlitted = h.split(" - ");
				String start = hoursSlitted[0];
				String end = hoursSlitted[1];
			
				if(days.length == 1)
					addTimetable(	parseInCompleteDayName(days[0]), parseInCompleteDayName(days[0]),
								start, end);
				else	
					addTimetable(	parseInCompleteDayName(days[0]), parseInCompleteDayName(days[1]),
									start, end);
			}
		
		}
		
	}
	
	public void addTimetable(String startDay, String endDay, String startHour, String endHours) throws TimetableNotAvailableException  {
		
		int indexStartDay 	= toIndex(startDay);
		int indexEndDay		= toIndex(endDay);
		
		boolean withinWeek = indexStartDay <= indexEndDay;
		
		if ( indexStartDay == -1 || indexEndDay == -1)
			throw new IllegalArgumentException("days not valid");
		
		for(int index = 0; index < 7; index++) {
			
			List<Range> list = week.get(index);
			
			if(withinWeek) {
				if(index >= indexStartDay && index <= indexEndDay) {
					LocalTime start = LocalTime.parse(startHour);
					LocalTime end 	= LocalTime.parse(endHours);
					Range range = new Range(start, end);
					if (!isTimetableFree(list, range)) 
						throw new TimetableNotAvailableException();
					
					list.add(new Range(start, end));
					
				} else {
					list.add(null);
				}
			} else {
				if(index < indexStartDay && index > indexEndDay) {
					list.add(null);
				} else {
					LocalTime start = LocalTime.parse(startHour);
					LocalTime end 	= LocalTime.parse(endHours);
					Range range = new Range(start, end);
					if (!isTimetableFree(list, range)) 
						throw new TimetableNotAvailableException();
					
					list.add(new Range(start, end));
				}
			}
			
		}		
		numberOfRecords++;
	}
	
	public boolean isEmpty() {
		return numberOfRecords == 0;
	}
	
	private String parseString(int index) {
		switch(index) {
			case 0:
				return "lun";
			case 1:
				return "mar";
			case 2:
				return "mer";
			case 3:
				return "gio";
			case 4:
				return "ven";
			case 5:
				return "sab";
			case 6:
				return "dom";
			default:
				return "err";
		}
	}
	
	public void deleteTimetableFromString(String string) {
		
		// first element day range, second element time range
		String[] firstSplit = string.split(", ");
		
		if (firstSplit.length !=2 )
			throw new IllegalArgumentException();
		
		String[] daysSplitted = firstSplit[0].split(" - ");
		String[] timeSplitted = firstSplit[1].split(" - ");
				
		int indexStartDay 	= toIndex(daysSplitted[0]);
		int indexEndDay		= toIndex(daysSplitted[1]);
		
		if (indexStartDay ==-1 || indexEndDay == -1 )
			throw new IllegalArgumentException();
		
		LocalTime startTime = LocalTime.parse(timeSplitted[0]);
		LocalTime endTime 	= LocalTime.parse(timeSplitted[1]);
		
		Range range = new Range(startTime, endTime) ;
				
		int position = week.get(indexStartDay).indexOf(range);
		if (position == -1) 
			throw new RuntimeException("impossible to find date range");

		for (int index = 0; index < 7; index++) 
			week.get(index).remove(position);
		
		numberOfRecords--;
	}
	
	public List<OpeningHour> parseToOpeningHours() {
		
		List<OpeningHour> list = new ArrayList<>();
		Range rangeToInsert = null;
		
		for(int index = 0; index < numberOfRecords; index++ ) {
			int firstDay = -1;
			int lastDay = 6;
			
			for(int day = 0; day < 7; day++) {
				Range range = week.get(day).get(index);
				
				
				if(firstDay == -1 && range != null) {
					rangeToInsert = range;
					firstDay = day;
				}
				
				if(firstDay != -1 && range == null && lastDay == 6)
					lastDay = day - 1;
				
				if(lastDay != 6 && range != null) {
					firstDay = day;
					break;
				}
			}
			
			String rangeDay = parseString(firstDay) + " - " + parseString(lastDay);
			
			OpeningHour opening = null;
			for(OpeningHour o : list) {
				if (rangeDay.equals(o.getDays()))
					opening = o;
			}
			
			if (opening != null) 
				opening.addTime(rangeToInsert.simpleToString());
			else {
				OpeningHour newOpening = new OpeningHour();
				newOpening.setDays(rangeDay);
				newOpening.addTime(rangeToInsert.simpleToString());
				list.add(newOpening);
			}		
		}
		
		return list;
	}

	@Override
	public String toString() {
		String string = "";
		string += "MON: " 	+ week.get(0);
		string += "\nTUE: " + week.get(1);
		string += "\nWED: " + week.get(2);
		string += "\nTHU: " + week.get(3);
		string += "\nFRI: " + week.get(4);
		string += "\nSAT: " + week.get(5);
		string += "\nSUN: " + week.get(6);
		
		return string;
	}

}