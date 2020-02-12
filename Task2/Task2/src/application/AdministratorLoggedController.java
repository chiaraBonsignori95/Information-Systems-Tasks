package application;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import application.ParametersFactory.helpType;
import backend.DatabaseManager;
import backend.exc.DatabaseManagerException;
import backend.exc.UserNotFoundException;
import backend.exc.UsernameAlreadyPresentException;
import backend.modules.Administrator;
import backend.modules.Customer;
import backend.modules.OpeningHour;
import backend.modules.Restaurant;
import backend.modules.RestaurantOwner;
import backend.modules.Review;
import backend.modules.User;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AdministratorLoggedController extends Controller implements Initializable{

	private Administrator administrator;
	
	//common
	@FXML
	private TabPane tabPane;
	
	//pending request
	private List<Restaurant> listRestaurantNotApproved;
	private Restaurant restaurantNotApproved;

	@FXML
	private Tab tabPendingRequest;
	@FXML
	private TextField nameNotApproved;
	@FXML
	private TextField phoneNumberNotApproved;
	@FXML
	private TextField addressNotApproved;
	@FXML
	private TextField priceRangeNotApproved;
	@FXML
	private ListView<Label> typeOfCookingNotApproved;
	@FXML
	private ListView<Label> optionsNotApproved;
	@FXML
	private ListView<Node> featuresNotApproved;
	@FXML
	private ListView<Label> restaurantViewerNotApproved;
	@FXML	
	private ListView<Node> timetablesRestaurantNotApproved;
	
	//delete user
	@FXML
	private TextField searchedUsername;
	@FXML
	private RadioButton customerRadioButton;
	@FXML
	private RadioButton restaurateursRadioButton;
	
	//insert new administrator
	@FXML
	private TextField newName;
	@FXML
	private TextField newSurname;
	@FXML
	private TextField newUsername;
	@FXML
	private TextField newPassword;
	
	//delete review
	@FXML
	private TextField reviewRestaurantName;
	@FXML
	private TextField reviewUsername;
	@FXML
	private TextArea focusedReviewTitle;
	@FXML
	private TextArea focusedReviewScore;
	@FXML
	private TextArea focusedReviewDate;
	@FXML
	private TextArea focusedReviewText;
	@FXML
	private ListView<Label> reviewViewer;
	@FXML
	private Button buttonDeleteReview;
	
	//insert restaurants
	@FXML
	private ListView<Node> restaurantsViewer;	
	
	private Review focusedReview;
	private List<Review> listReview;
	

	public void initializeScene() {
		restaurantNotApproved =  null;
		
		initializePendingRequest();
		cleanNewAdministratorFields();
		initializeNewRestaurants();
	}
	
	private void initializeNewRestaurants() {
		File folder = new File(".\\data\\");
		
		restaurantsViewer.getItems().clear();
		
		if (folder.listFiles().length == 0) {
			restaurantsViewer.getItems().add(new Label("La cartella è vuota"));
			return;
		}
		
		for (File jsonFile : folder.listFiles()) {
			restaurantsViewer.getItems().add(new CheckBox(jsonFile.getName()));
		}
	}

	private void initializePendingRequest() {
		loadPendingRestaurants();
		loadRestaurantInformation();
	}
	
	private void loadRestaurantInformation() {
		
		cleanPendingRestaurantFields();
		
		if (restaurantNotApproved == null)
			return;
		
		nameNotApproved.setText(restaurantNotApproved.getName());
		phoneNumberNotApproved.setText(restaurantNotApproved.getPhoneNumber());
		addressNotApproved.setText(restaurantNotApproved.getAddress().toString());
		if(restaurantNotApproved.getPriceRange() != null)
			priceRangeNotApproved.setText(	restaurantNotApproved.getPriceRange().getMinPrice() + " - " +
											restaurantNotApproved.getPriceRange().getMaxPrice() + " €");
		
		List<String> list = restaurantNotApproved.getCategories();
		
		for(String string : list) {
			Label restaurantLabel = new Label(string);
			restaurantLabel.setCursor(Cursor.HAND);			
			typeOfCookingNotApproved.getItems().add(restaurantLabel);
		}
			
		list = restaurantNotApproved.getOptions();
		if(list == null || (list != null && list.isEmpty()))
				optionsNotApproved.getItems().add(new Label("Nessun elemento specificato"));
		else 
			for(String string : list) {
				Label restaurantLabel = new Label(string);
				restaurantLabel.setCursor(Cursor.HAND);
				optionsNotApproved.getItems().add(restaurantLabel);
			}
	
			
		list = restaurantNotApproved.getFeatures();
			
		if (list == null || (list != null && list.isEmpty())) 
			featuresNotApproved.getItems().add(new Label("Nessun elemento specificato"));
			else			
				for(String string : list) {
					Label restaurantLabel = new Label(string);
					restaurantLabel.setCursor(Cursor.HAND);
					featuresNotApproved.getItems().add(restaurantLabel);
				}
			
			timetableUpdate();	
			
	}
		
	private void timetableUpdate() {
		
		List<OpeningHour> listOpening = restaurantNotApproved.getOpeningHours();
		
		timetablesRestaurantNotApproved.getItems().clear();
		
		for(OpeningHour h : listOpening) {
			String dayString = h.getDays();
			String[] days = dayString.split(" - ");
			String dayStart = Timetable.parseInCompleteDayName(days[0]);
			String dayEnd = Timetable.parseInCompleteDayName(days[1]);
			List<String> times = h.getTimes();
			for(String time : times) {
				String text = dayStart + " - " + dayEnd + ", " + time;
				timetablesRestaurantNotApproved.getItems().add(new Label(text));
				}
			}
		}
	
	private void loadPendingRestaurants() {
		
		restaurantViewerNotApproved.getItems().clear();
		
		DatabaseManager database = App.getDatabase();
		
		try {
			listRestaurantNotApproved = database.getPendingRequests(1);
		} catch (DatabaseManagerException e) {
			popoutMessage(AlertType.ERROR, "Attenzione", "Si è verificato un errore, l'applicazione verrà chiusa", "");
			App.closeApp();
			return;
		}
		
		boolean arePendingRequestNotAvailable = listRestaurantNotApproved.isEmpty();
		tabPendingRequest.setDisable(arePendingRequestNotAvailable);
		
		if(arePendingRequestNotAvailable) {
			SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
			selectionModel.select(0);
			cleanPendingRestaurantFields();
			return;
		}
			

		int restaurantIdentifier = 0;
		
		for (Restaurant restaurant : listRestaurantNotApproved) {
			String labelText = 	restaurant.getName() + ",\n\t" +
								restaurant.getAddress().toStringSummary();
			Label label = new Label(labelText);
			
			label.setId(Integer.toString(restaurantIdentifier++));
			
			label.setCursor(Cursor.HAND);
			
			// handler when click the the restaurant in the left side
			label.setOnMouseClicked(event -> {
				
				Label item = (Label)event.getSource();
				
				restaurantNotApproved = listRestaurantNotApproved.get(Integer.parseInt(item.getId()));
			
				loadRestaurantInformation();
		        
			    });
			
			restaurantViewerNotApproved.getItems().add(label);
		}
		
		restaurantNotApproved = listRestaurantNotApproved.get(0);
		
	}
	
	private void cleanPendingRestaurantFields() {
		
		typeOfCookingNotApproved.getItems().clear();
		optionsNotApproved.getItems().clear();
		featuresNotApproved.getItems().clear();
		timetablesRestaurantNotApproved.getItems().clear();
		
	}
	
	@FXML
	private void onAcceptRestaurantRequest() {
		
		if (!confirmationPopoutMessage("Attenzione", "Confermi di voler accettare il ristorante nel sistema?", ""))
			return;
		
		DatabaseManager database = App.getDatabase();
		
		database.acceptPendingRequest(restaurantNotApproved);
		
		popoutMessage(AlertType.INFORMATION, "Complimenti", "Il ristorante è stato accettato correttamente dal sistema", "");
		
		listRestaurantNotApproved.remove(restaurantNotApproved);
		
		initializeScene();
	}
	
	@FXML
	private void onRefuseRestaurantRequest() {
		
		if (!confirmationPopoutMessage("Attenzione", "Confermi di voler rifiutare il ristorante nel sistema?", ""))
			return;
		
		DatabaseManager database = App.getDatabase();
		
		database.refusePendingRequest(restaurantNotApproved);
		
		popoutMessage(AlertType.INFORMATION, "Complimenti", "Il ristorante è stato rifiutato correttamente dal sistema", "");
		
		listRestaurantNotApproved.remove(restaurantNotApproved);
		
		initializeScene();
		
	}
	
	@FXML
	private void onDeleteUserRequest() {

		boolean customerSelected 		= customerRadioButton.isSelected();
		boolean restaurateursSelected 	= restaurateursRadioButton.isSelected();
		String usernameToSearch 		= searchedUsername.getText();
		
		if ((customerSelected == false && restaurateursSelected == false ) || usernameToSearch.isEmpty()) {
			popoutMessage(AlertType.WARNING, "Attenzione", "Non hai inserito tutti i campi necessari alla ricerca", "");
			return;
		}
		
		if(!super.confirmationPopoutMessage("Attenzione", "Sei veramente sicuro di voler cancellare l'account?","Questa operazione sarà irreversibile"))
			return;
		
		DatabaseManager database = App.getDatabase();
		User user = customerSelected ? new Customer() : new RestaurantOwner();		
		
		user.setUsername(usernameToSearch);

		try {
			database.deleteAccount(user);	
			popoutMessage(AlertType.INFORMATION, "Complimenti", "L'account è stato eliminato correttamente", "");
			cleanFieldsRemoveAccount();
		} catch (UserNotFoundException e) {
			popoutMessage(AlertType.WARNING, "Attenzione", "L'username inserito non esiste", "");
			cleanFieldsRemoveAccount();
		}
		 
	}
	
	private void cleanFieldsRemoveAccount() {
		
		customerRadioButton.setSelected(false);
		restaurateursRadioButton.setSelected(false);
		searchedUsername.setText("");		
	}
	
	@FXML
	private void onAddNewAdministrator() {
		
		String name 	= newName.getText();
		String surname 	= newSurname.getText();
		String username = newUsername.getText();
		String password = newPassword.getText();
		
		if (name.isEmpty() || surname.isEmpty() || username.isEmpty() || password.isEmpty()) {
			popoutMessage(AlertType.WARNING, "Attenzione", "Non hai completato tutti i campi", "");
			return;
		}
		
		if (!confirmationPopoutMessage("Attenzione", "Sei sicuro di voler inserire un nuovo amministratore?", ""))
			return;
		
		DatabaseManager database = App.getDatabase();
		
		try {
			Administrator adm = new Administrator(name, surname, username, password);
			database.createAccount(adm);
			popoutMessage(AlertType.INFORMATION, "Congratulazioni", "Il nuovo ammistratore è stato inserito correttamente", "");
			cleanNewAdministratorFields();
			
		} catch (UsernameAlreadyPresentException e) {
			popoutMessage(AlertType.ERROR, "Attenzione", "L'username scelto è già utilizzato", "");
		}
	}
	
	@FXML 
	private void onInsertRestaurants() {
		
		Node node = restaurantsViewer.getItems().get(0);
		if (node instanceof Label) {
			popoutMessage(AlertType.ERROR, "Attenzione", "Non ci sono ristoranti da inserire", "");
			return;
		}
		
		ObservableList<Node> restaurantsList = restaurantsViewer.getItems();
		
		List<File> files = new ArrayList<File>();
		
		for (Node n : restaurantsList) {
			CheckBox checkBox = (CheckBox) n;
			if (checkBox.isSelected()) {
				files.add(new File(".\\data\\" + checkBox.getText()));
			}
		}
		
		if (files.isEmpty()) {
			popoutMessage(AlertType.ERROR, "Attenzione", "Non hai selezionato nessun ristorante", "");
			return;
		}
		
		if(!confirmationPopoutMessage("Attenzione", "Sei veramente sicuro di voler inserire i ristoranti?", ""))
			return;
		
		DatabaseManager database = App.getDatabase();
		try {
			database.updateDatabase(files);
			popoutMessage(AlertType.INFORMATION, "Congratulazioni", "I ristoranti sono stati inseriti correttamente nel sistema", "");
		} catch (DatabaseManagerException e) {
			popoutMessage(AlertType.ERROR, "Attenzione", "Si è verificato un errore, l'applicazione verrà chiusa", "");
			App.closeApp();
			return;
		}
		
	}
	
	private void cleanNewAdministratorFields() {
		
		newName.setText("");
		newSurname.setText("");
		newUsername.setText("");
		newPassword.setText("");
	}
	
	@FXML 
	private void onSearchReviewRequest() {
		
		String restaurantName 	= reviewRestaurantName.getText();
		String username 		= reviewUsername.getText();
		
		
		if(restaurantName.isEmpty() || username.isEmpty() ) {
			popoutMessage(AlertType.WARNING, "Attenzione", "Non hai completato tutti i campi necessari", "");
			return;
		}
			
		reviewViewer.getItems().clear();
		
		DatabaseManager database = App.getDatabase();
		
		try {
			listReview = database.getReviews(restaurantName, username);
		} catch (DatabaseManagerException e) {
			popoutMessage(AlertType.ERROR, "Attenzione", "Si è verificato un errore, l'applicazione verrà chiusa", "");
			App.closeApp();
			return;
		}
		
		buttonDeleteReview.setDisable(listReview.isEmpty());
		
		if(listReview.isEmpty()) {
			popoutMessage(AlertType.WARNING, "Attenzione", "Non ci sono recensioni corrispondenti ai parametri inseriti", "");
			reviewViewer.getItems().add(new Label ("Nessuna recensione"));
			return;
		}
		
		int IDReview = 0;
		
		for (Review r : listReview) {
			String labelText = r.getRestaurant() + "\n\t" + r.getTitle();
			Label label = new Label(labelText);
			label.setId(Integer.toString(IDReview++));
			label.setCursor(Cursor.HAND);
			
			// handler when click the the restaurant in the left side
			label.setOnMouseClicked(event -> {
				
				Label item = (Label)event.getSource();
				focusedReview = listReview.get(Integer.parseInt(item.getId()));
				loadReviewInformations();
		        
			    });
			
			reviewViewer.getItems().add(label);
		}
		
		focusedReview = listReview.get(0);
		loadReviewInformations();
	}
	
	@FXML
	private void onDeleteReview() {
		
		if(!confirmationPopoutMessage("Attenzione", "Sei veramente sicuro di voler cancellare la recensione?", "Questa operazione è irreversibile!"))
			return;
		
		DatabaseManager database = App.getDatabase();
		try {
			database.deleteReview(focusedReview);
		} catch (DatabaseManagerException e) {
			popoutMessage(AlertType.ERROR, "Attenzione", "Si è verificato un errore, l'applicazione verrà chiusa", "");
			App.closeApp();
			return;
		}
		
		popoutMessage(AlertType.INFORMATION, "Complimenti", "La recensione è stata eliminata correttamente", "");
		
		listReview.remove(focusedReview);
		
		if(listReview.isEmpty()) {
			buttonDeleteReview.setDisable(true);
			cleanReviewInformations();
			cleanSearchReviewFields();
			focusedReview = null;
			reviewViewer.getItems().clear();
			reviewViewer.getItems().add(new Label ("Nessuna recensione"));
		} else {
			onSearchReviewRequest();
		}
	
	}
	
	private void cleanSearchReviewFields() {
		
		reviewRestaurantName.setText("");
		reviewUsername.setText("");

	}
	
	private void loadReviewInformations() {

		focusedReviewTitle.setText(focusedReview.getTitle());
		focusedReviewScore.setText(Double.toString(focusedReview.getRating()));
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ITALY);
		focusedReviewDate.setText(df.format(focusedReview.getDate()));
		focusedReviewText.setText(focusedReview.getText());
		
	}
	
	private void cleanReviewInformations() {
		
		focusedReviewTitle.setText("");
		focusedReviewScore.setText("");
		focusedReviewDate.setText("");
		focusedReviewText.setText("");
		
	}
	
	public Administrator getAdministrator() {
		return administrator;
	}

	public void setAdministrator(Administrator administrator) {
		this.administrator = administrator;
	}
	
	@FXML
	private void onSetPreviousPage() {
		cleanSearchReviewFields();
		cleanReviewInformations();
		administrator = null;
		App.setAmministratorScene();
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		reviewViewer.getItems().add(new Label ("Nessuna recensione"));
		
	}
	
	@FXML
	public void onHelpRequest() {
		helpWindow(helpType.ADMINISTRATOR_LOGGED_SCENE);
	}
	
	
}
