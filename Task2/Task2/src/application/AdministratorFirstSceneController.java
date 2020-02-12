package application;

import application.ParametersFactory.helpType;
import backend.DatabaseManager;
import backend.exc.DatabaseManagerException;
import backend.exc.UserNotFoundException;
import backend.modules.Administrator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class AdministratorFirstSceneController extends Controller {

	@FXML 
	private TextField username;
	@FXML
	private PasswordField password;
	@FXML
	private Button login;
	
	@FXML
	private void onLogin() {
		String username = this.username.getText();
		String password = this.password.getText();
		
		if (username.isEmpty() || password.isEmpty()) {
			popoutMessage(AlertType.WARNING, "Attenzione", "Credentiali incomplete", "");
			return;
			
		}
		
		DatabaseManager database = App.getDatabase();
		try {
			Administrator administrator = (Administrator) database.login(username, password, Administrator.class);
			AdministratorLoggedController controller = (AdministratorLoggedController)App.getController("administratorLoggedScene");
			controller.setAdministrator(administrator);
			cleanFields();
			App.setAdministatorLoggedScene();
			controller.initializeScene();
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
	public void onHelpRequest() {
		helpWindow(helpType.ADMINISTRATOR_SCENE);
	}
	
}
