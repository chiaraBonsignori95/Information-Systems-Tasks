package backend.modules;

import org.bson.types.ObjectId;

public class Administrator extends User {

	public Administrator() {
		super();
	}
		
	public Administrator(ObjectId id, String name, String surname, String username, String password) {
		super(id, name, surname, username, password, Administrator.class.getSimpleName());
	}

	public Administrator(String name, String surname, String username) {
		super(name, surname, username, Administrator.class.getSimpleName());
	}
	
	public Administrator(String name, String surname, String username, String password) {
		super(name, surname, username, password, Administrator.class.getSimpleName());
	}
}
