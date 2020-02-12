package application;

import java.util.HashMap;
import java.util.Optional;

import application.ParametersFactory.helpType;
import backend.DatabaseManager;

import backend.exc.DatabaseManagerException;
import backend.exc.UserNotFoundException;
import backend.exc.UsernameAlreadyPresentException;
import backend.modules.RestaurantOwner;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class RestaurantOwnerController extends Controller {

	@FXML 
	private TextField username;
	@FXML
	private PasswordField password;
	@FXML
	private Button login;
	@FXML
	private Label register;
	
	@FXML
	public void onLogin() {
		String username = this.username.getText();
		String password = this.password.getText();
		
		if (username.isEmpty() || password.isEmpty()) {
			popoutMessage(AlertType.WARNING, "Attenzione", "Credentiali incomplete", "");
			return;
			
		}
		
		DatabaseManager database = App.getDatabase();
		try {
			RestaurantOwner restaurateur = (RestaurantOwner) database.login(username, password, RestaurantOwner.class);
			RestaurantOwnerLoggedController controller = (RestaurantOwnerLoggedController)App.getController("restaurantOwnerLoggedScene");
			controller.setRestaurateur(restaurateur);
		
			controller.initializeScene();
			cleanFields();
			
			if(! restaurateur.getRestaurants().isEmpty()) {
				App.setRestaurantOwnerLoggedScene();
			}
		} catch (UserNotFoundException nf) {
			popoutMessage(AlertType.ERROR, "Attenzione", "Credentiali errate", "");
			return;
		} catch (DatabaseManagerException e) {
			popoutMessage(AlertType.ERROR, "Errore", "Errore l'applicazione verrà chiusa", "");
			App.closeApp();
		}
		
	}
	
    private void cleanFields() {
		username.setText("");
		password.setText("");
		
	}

	@FXML
    public void onRegister() {
    	
    	Optional<HashMap<String,String>> result = registration().showAndWait();
		
		result.ifPresent(newReview -> {
			
			DatabaseManager database = App.getDatabase();
			
			RestaurantOwner user = new RestaurantOwner(	newReview.get("name"), newReview.get("surname"), 
														newReview.get("username"), newReview.get("password"));
			
			try {
				database.createAccount(user);
				popoutMessage(AlertType.INFORMATION, "Congratulazioni", "La registrazione è avvenuta con successo!", "");
			} catch (UsernameAlreadyPresentException e) {
				popoutMessage(AlertType.WARNING, "Attenzione", "L'username che hai inserito è già presente", "");
			}
		   
		});
	}
	
	@FXML
	public void onHelpRequest() {
		helpWindow(helpType.RESTAURATEUR_SCENE);
	}

}
