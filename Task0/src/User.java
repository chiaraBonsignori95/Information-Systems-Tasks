package task0;

public abstract class User {
	
	static HotelDatabaseManager db = new HotelDatabaseManager();
	
	String username;
	String name;
	String surname;
	
	User(String username, String name, String surname) {
		this.username = username;
		this.name = name;
		this.surname = surname;	
	}
	
	User(String name, String surname) {
		this.name = name;
		this.surname = surname;	
	}
	
	User(String username){
		this.username = username;
	}
	
	@Override
	public String toString() {
		return "User " + username;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        
        User other = (User)obj;
        return username == other.username;
	}
	
	public boolean areValidCredentials(String password) {
		if (db.authenticateUser(this, password) == null)
			return false;
		return true;
	}
}