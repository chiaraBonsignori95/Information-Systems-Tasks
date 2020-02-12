package application;

import java.util.HashMap;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Controller {
	
	@FXML
	public void onCloseWindow() {
		App.closeApp();		
	}
	
	@FXML
	public void onMainPageRequest() {
		App.setMainScene();
	}
	
	protected void helpWindow(ParametersFactory.helpType type) {
			
		String text = ParametersFactory.getHelpText(type);
		popoutMessage(AlertType.INFORMATION, "Aiuto", "", text);
		
	}
	
	protected void popoutMessage(AlertType type, String title, String header, String content) {
		
		Alert alert = new Alert(type);
		
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(getClass().getResourceAsStream("food.png")));
		
		alert.getDialogPane().getScene().getStylesheets().add("./application/application.css");
		alert.getDialogPane().getStyleClass().add("error-popup");
		
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
	
	protected boolean confirmationPopoutMessage(String title, String header, String content) {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(getClass().getResourceAsStream("food.png")));
		
		alert.getDialogPane().getScene().getStylesheets().add("./application/application.css");
		alert.getDialogPane().getStyleClass().add("error-popup");
		
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
		
		ButtonType button = alert.getResult();
		
		return button.getText().equals("OK");
	}
	
	protected Dialog<HashMap<String, String>> registration() {
		
		Dialog<HashMap<String, String>> dialog = new Dialog<>();
		
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(getClass().getResourceAsStream("food.png")));
		
		dialog.getDialogPane().getStylesheets().add("./application/application.css");
		dialog.getDialogPane().getStyleClass().add("registration-form");
		
		dialog.setTitle("Registrazione");
		dialog.setHeaderText("Per favore completa tutti i campi necessari alla registrazione");
	
		ButtonType registration = new ButtonType("Registrati", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(registration, ButtonType.CANCEL);
	
		GridPane grid = new GridPane();		
		grid.getStyleClass().add("form");
		grid.getStyleClass().add("registration-form");
		
		grid.setVgap(10);
		grid.setHgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
	
		TextField name = new TextField();
		name.setPromptText("Nome");
		name.getStyleClass().add("form-field");
		
		TextField surname = new TextField();
		surname.setPromptText("Cognome");
		surname.getStyleClass().add("form-field");
		
		TextField username = new TextField();
		username.setPromptText("Username");
		username.getStyleClass().add("form-field");
		
		PasswordField password = new PasswordField();
		password.setPromptText("Password");
		password.getStyleClass().add("form-field");
		
		Label l = new Label("Nome:");
		l.getStyleClass().add("l");
		grid.add(l, 0, 0);
		grid.add(name, 1, 0);
		
		l = new Label("Cognome:");
		grid.add(l, 0, 1);
		grid.add(surname, 1, 1);
		
		l = new Label("Username:");
		grid.add(l, 0, 2);
		grid.add(username, 1, 2);
		
		l = new Label("Password:");
		grid.add(l, 0, 3);
		grid.add(password, 1, 3);
	
		Node button = dialog.getDialogPane().lookupButton(registration);
		button.getStyleClass().add("form-button");
		button.setDisable(true);
	
		name.textProperty().addListener((observable, oldValue, newValue) -> {
			boolean check = name.getText().isEmpty() || surname.getText().isEmpty() || 
							username.getText().isEmpty() || password.getText().isEmpty();
		    button.setDisable(check);
		});
		
		surname.textProperty().addListener((observable, oldValue, newValue) -> {
			boolean check = name.getText().isEmpty() || surname.getText().isEmpty() || 
					username.getText().isEmpty() || password.getText().isEmpty();
			button.setDisable(check);
		});
		
		username.textProperty().addListener((observable, oldValue, newValue) -> {
			boolean check = name.getText().isEmpty() || surname.getText().isEmpty() || 
					username.getText().isEmpty() || password.getText().isEmpty();
			button.setDisable(check);
		});
		
		password.textProperty().addListener((observable, oldValue, newValue) -> {
			boolean check = name.getText().isEmpty() || surname.getText().isEmpty() || 
					username.getText().isEmpty() || password.getText().isEmpty();
			button.setDisable(check);
		});
	
		dialog.getDialogPane().setContent(grid);
	
	
		Platform.runLater(() -> name.requestFocus());
	
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == registration) {
		    	
		    	HashMap<String,String> hashMap = new HashMap<String,String>();
		    	
		    	hashMap.put("name", 	name.getText());
		    	hashMap.put("surname", 	surname.getText());
		    	hashMap.put("username", username.getText());
		    	hashMap.put("password", password.getText());
		    	
		        return hashMap;
		    }
		    return null;
		});
		
		return dialog;
	}
}