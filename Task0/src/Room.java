package task0;

public class Room {
	int hotel;
	int number;
	int capacity;
	
	public Room(int hotel, int number, int capacity) {
		this.hotel = hotel;
		this.number = number;
		this.capacity = capacity;
	}
	
	@Override
	public String toString() {
		return "Room " + number + " in Hotel " + hotel + ": capacity " + capacity;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        
        Room other = (Room)obj;
        return 	(other.hotel == hotel) && 
        		(other.number == number) && 
        		(other.capacity == capacity);
	}
}
