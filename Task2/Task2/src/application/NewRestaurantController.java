package application;

import java.net.URL;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import application.ParametersFactory.helpType;
import backend.DatabaseManager;
import backend.modules.Address;
import backend.modules.PriceRange;
import backend.modules.Restaurant;
import backend.modules.RestaurantOwner;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class NewRestaurantController extends Controller implements Initializable {
	
	private Timetable timetables = new Timetable();
	
	@FXML
	private TextField name;
	@FXML
	private TextField phoneNumber;
	@FXML
	private TextField country;
	@FXML
	private TextField city;
	@FXML
	private TextField street;
	@FXML
	private TextField postcode;
	@FXML
	private ListView<CheckBox> categories;
	@FXML
	private ListView<CheckBox> options;
	@FXML
	private ListView<CheckBox> extras;
	@FXML
	private ListView<CheckBox> openingViewer;
	@FXML
	private TextField minPrice;
	@FXML
	private TextField maxPrice;
	@FXML
	private MenuButton openingDay;
	@FXML
	private MenuButton closingDay;
	@FXML
	private TextField openingHour;
	@FXML
	private TextField closingHour;
	
	public void initializeScene() {
		cleanForm();
	}

	private void initializeCategories() {
		
		String[] categoriesName = ParametersFactory.getCategoriesName();
		
		for (String category : categoriesName ) {
			CheckBox item = new CheckBox(category);
			categories.getItems().add(item);
		}
	}
	
	private void initializePriceRange() {
		minPrice.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
		    {
		        if (!newPropertyValue)
		        	checkPrice();        
		    }		         
		});
		
		maxPrice.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
		    {
		        if (!newPropertyValue)
		        	checkPrice();        
		    }		         
		});
		
	}
	
	private void initalizeOptions() {
		
	String[] optionsName = ParametersFactory.getOptionsName();
		
		for (String option : optionsName ) {
			CheckBox item = new CheckBox(option);
			options.getItems().add(item);
		}
	}
	
	private void initializeExtra() {
		String[] features = ParametersFactory.getFeaturesName();
		
		for (String extra : features ) {
			CheckBox item = new CheckBox(extra);
			extras.getItems().add(item);
		}
	}
	
	private void initializeMenuDayOfWeek() {
		
		String[] days = ParametersFactory.getDays();
		
		ToggleGroup toggleOpening = new ToggleGroup();
		ToggleGroup toggleClosing = new ToggleGroup();
		
		for (String day : days) {			

			RadioMenuItem itemOpening = new RadioMenuItem(day);
			RadioMenuItem itemClosing = new RadioMenuItem(day);
			
			itemOpening.setOnAction(e -> {
				RadioMenuItem item = (RadioMenuItem)e.getSource();
				openingDay.setText(item.getText());				
			});
			
			itemClosing.setOnAction(e -> {
				RadioMenuItem item = (RadioMenuItem)e.getSource();
				closingDay.setText(item.getText());				
			});
		
			itemOpening.setToggleGroup(toggleOpening);
			itemClosing.setToggleGroup(toggleClosing);
			openingDay.getItems().add(itemOpening);
			closingDay.getItems().add(itemClosing);
		}
	}
	
	private void initializePhoneNumberField() {
		phoneNumber.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
		    {
		        if (!newPropertyValue)
		        	checkPhoneNumber();        
		    }		         
		});
	}
	
	private void initializePostCode() {
		postcode.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
		    {
		        if (!newPropertyValue)
		        	checkPostcodeNumber();        
		    }		         
		});
	}
	
	private void initializeTimeFields() {
		openingHour.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
		    {
		        if (!newPropertyValue)
		        	checkTime();        
		    }		         
		});
		
		closingHour.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
		    {
		        if (!newPropertyValue)
		        	checkTime();        
		    }		         
		});

	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		initializeCategories();	
		initializePriceRange();
		initalizeOptions();
		initializeExtra();
		initializeMenuDayOfWeek();
		initializeTimeFields();
		initializePhoneNumberField();
		initializePostCode();
	}
	
	private void checkTime() {
				
		String opening = openingHour.getText();
		String closing = closingHour.getText();
		
		if (opening.isEmpty() && closing.isEmpty())
			return;
		
		if (!opening.isEmpty()) {
			try {
				if(!opening.contains(":"))
					opening += ":00";
				LocalTime.parse(opening);
				
			} catch(Exception e) {
				popoutMessage(AlertType.WARNING, "Attenzione", "L'orario inserito non è corretto", "");
				openingHour.setText("");
			}
		}
		
		if (!closing.isEmpty()) {
			try {
				if(!closing.contains(":"))
					closing += ":00";
				LocalTime.parse(closing);
				
			} catch(Exception e) {
				popoutMessage(AlertType.ERROR, "Attenzione", "L'orario inserito non è corretto", "");
				closingHour.setText("");
			}
		}
	}
	
	private void checkPrice() {
		
		String minPriceString = minPrice.getText();
		String maxPriceString = maxPrice.getText();
		
		if (maxPriceString.isEmpty() && minPriceString.isEmpty())
			return;
				
		try {
			
			if(maxPriceString.isEmpty()) {
				Double.parseDouble(minPriceString);
				return;
			}
			if(minPriceString.isEmpty()) {
				Double.parseDouble(maxPriceString);
				return;
			}
			
			double min = Double.parseDouble(minPriceString);
			double max = Double.parseDouble(maxPriceString);
			
			if ( min <= 0 || max <= 0 || min > max) {
				popoutMessage(AlertType.WARNING, "Attenzione", "Intervallo di prezzi inserito non è corretto", "");
				minPrice.requestFocus();
			}	
			
		} catch (Exception e) {
			popoutMessage(AlertType.WARNING, "Attenzione", "Devi inserire un valore numerico", "");	
			minPrice.requestFocus();
		}
	}
	
	private void checkPhoneNumber() {
		String regex = "^[0-9]*$";
		
		String phone = phoneNumber.getText();
		
		if (phone.isEmpty())
			return;
		
		if(!phone.matches(regex)) {
			popoutMessage(AlertType.WARNING, "Attenzione", "Numero di telefono non valido", "");
			phoneNumber.requestFocus();
		}
	}
	
	private void checkPostcodeNumber() {
		String regex = "^[0-9]*$";
		
		String postcode = this.postcode.getText();
		
		if (postcode.isEmpty())
			return;
		
		if(!postcode.matches(regex)) {
			popoutMessage(AlertType.WARNING, "Attenzione", "Il CAP inserito non è valido", "");
			this.postcode.requestFocus();
		}
	}

	private void clearFieldTimetable(RadioMenuItem item1, RadioMenuItem item2){
		if (item1 != null)
			item1.setSelected(false);
		if (item2 != null)
			item2.setSelected(false);
		
		openingDay.setText("Giorni di apertura");
		closingDay.setText("Giorno di chiusura");
		
		openingHour.setText("");
		closingHour.setText("");

	}
	
	private void cleanForm() {
		
		name.setText("");
		phoneNumber.setText("");
		country.setText("");
		city.setText("");
		street.setText("");
		postcode.setText("");
		
		ObservableList<CheckBox> nodes = categories.getItems();
		for(CheckBox item : nodes) {			
			item.setSelected(false);
		}
		
		openingViewer.getItems().clear();
		
		ObservableList<MenuItem> menu = openingDay.getItems();
		
		for(MenuItem  m : menu) {
			RadioMenuItem item = (RadioMenuItem)m;
			item.setSelected(false);
		}
		
		menu = closingDay.getItems();
		
		for(MenuItem  m : menu) {
			RadioMenuItem item = (RadioMenuItem)m;
			item.setSelected(false);
		}
		
		openingDay.setText("Giorni di apertura");
		closingDay.setText("Giorno di chiusura");
		
		openingHour.setText("");
		closingHour.setText("");
		
		timetables = new Timetable();
		
		minPrice.setText("");
		maxPrice.setText("");
		
		nodes = options.getItems();
		
		for(CheckBox item : nodes) {
			item.setSelected(false);
		}
		
		nodes = extras.getItems();
		
		for(CheckBox item : nodes) {
			item.setSelected(false);
		}
		
	}
	
	@FXML
	private void addTimetable() {
		
		String openDaySelected = null ;
    	String closeDaySelected = null;
    	RadioMenuItem radioItemOpen = null;
    	RadioMenuItem radioItemClose = null;
    	
    	String openHour 	= openingHour.getText();
		String closeHour 	= closingHour.getText();
    		
		ObservableList<MenuItem> listOpening = openingDay.getItems();
		for(MenuItem item : listOpening) {
			RadioMenuItem radioItem = (RadioMenuItem)item;
			if (radioItem.isSelected()) {
				openDaySelected = radioItem.getText();
				radioItemOpen = radioItem;
			}
		}
		
		ObservableList<MenuItem> listClosing = closingDay.getItems();
		for(MenuItem item : listClosing) {
			RadioMenuItem radioItem = (RadioMenuItem)item;
			if (radioItem.isSelected()) {
				closeDaySelected = radioItem.getText();	
				radioItemClose = radioItem;
			}
		}
	
		if (openDaySelected 	== null ||
			closeDaySelected 	== null ||
			openHour.isEmpty() ||
			closeHour.isEmpty()) {
			
			popoutMessage(AlertType.WARNING, "Attenzione", "Non hai completato tutti i campi necessari", "");
			return;
			
		}
	
		try {
			
			if(!openHour.contains(":"))
				openHour += ":00";
			if(!closeHour.contains(":"))
				closeHour += ":00";
			
			timetables.addTimetable(openDaySelected, closeDaySelected, openHour, closeHour);
			
		} catch (TimetableNotAvailableException e) {
			popoutMessage(AlertType.WARNING, "Attenzione", "Gli orari che hai inserito collidono", "");
			return;
		}
		
		clearFieldTimetable(radioItemOpen, radioItemClose);
		
		String text = openDaySelected + " - " + closeDaySelected + ", " + openHour + " - " + closeHour;
		CheckBox item = new CheckBox (text);
		
		openingViewer.getItems().add(item);	
		
	}
	
	@FXML
	private void deleteTimetable() {
		ObservableList<CheckBox> nodes = openingViewer.getItems();
		List<CheckBox> toDelete = new ArrayList<>();
		
		for(CheckBox item : nodes) {
			if(item.isSelected()) {
				toDelete.add(item);			
				timetables.deleteTimetableFromString(item.getText());
			}
		}
		
		nodes.removeAll(toDelete);
		
	}
	
	@FXML
	private void addNewRestaurant() {
		
		String name 		= this.name.getText();
		String phoneNumber 	= this.phoneNumber.getText();
		String country 		= this.country.getText();
		String city 		= this.city.getText();
		String street		= this.street.getText();
		String postcode		= this.postcode.getText();
		
		String minPrice		= this.minPrice.getText();
		String maxPrice		= this.maxPrice.getText();
		
		List<String> typeOfCooking 	= new ArrayList<>();
		List<String> options 		= new ArrayList<>();
		List<String> extras			= new ArrayList<>();
		
		ObservableList<CheckBox> nodes = categories.getItems();
		for (CheckBox item : nodes ) {
			if (item.isSelected()) {
				typeOfCooking.add(item.getText());
			}
		}
		
		if (	name.isEmpty() 		|| phoneNumber.isEmpty() 	||
				country.isEmpty()	|| street.isEmpty()			||
				postcode.isEmpty()	|| typeOfCooking.isEmpty()	||
				city.isEmpty()		|| timetables.isEmpty()) {
			
			popoutMessage(AlertType.WARNING, "Attenzione", "Non tutti i campi necessari sono stati completati", "");
			return;
		}
		
		Restaurant restaurant = new Restaurant();
		restaurant.setName(name);
		restaurant.setAddress(new Address(city, country, postcode, street));
		restaurant.setApproved(false);
		restaurant.setPhoneNumber(phoneNumber);
		restaurant.setCategories(typeOfCooking);		
		restaurant.setOpeningHours(timetables.parseToOpeningHours());
		
		if(		(minPrice.isEmpty() 	&& 	!maxPrice.isEmpty()) ||
				(!minPrice.isEmpty() 	&& 	maxPrice.isEmpty())) {	
			
			popoutMessage(AlertType.WARNING, "Attenzione", "Specificare entrambi i valori dell'intervallo di prezzo", "");
			return;
		}
		
		if (!minPrice.isEmpty() && !maxPrice.isEmpty())
			restaurant.setPriceRange(new PriceRange(Double.parseDouble(minPrice), Double.parseDouble(maxPrice)));
		
		nodes = this.options.getItems();
		for (CheckBox item : nodes ) {
			if (item.isSelected()) {
				options.add(item.getText());
			}
		}
		
		nodes = this.extras.getItems();
		for (CheckBox item : nodes ) {
			if (item.isSelected()) {
				extras.add(item.getText());
			}
		}
		
		if (!options.isEmpty())
			restaurant.setOptions(options);
		if (!extras.isEmpty())
			restaurant.setFeatures(extras);
				
		popoutMessage(AlertType.CONFIRMATION, "Conferma", "Confermi di voler inviare una nuova richiesta di registrazione?", "");
		
		DatabaseManager database = App.getDatabase();
		RestaurantOwnerLoggedController controller = (RestaurantOwnerLoggedController)App.getController("restaurantOwnerLoggedScene");
		database.insertRestaurant(restaurant, controller.getRestaurateur());
		popoutMessage(AlertType.INFORMATION, "Complimenti", "La richiesta di registrazione di un nuovo ristorante è avvenuta con successo", "");
		
		cleanForm();
		
		setPreviousPage();

	}
	
	@FXML
	private void onLogoutRequest() {
		App.setRestaurantOwnerScene();		
	}

	@FXML
	private void setPreviousPage() {
		RestaurantOwnerLoggedController controller = (RestaurantOwnerLoggedController)App.getController("restaurantOwnerLoggedScene");
		RestaurantOwner restaurateur = controller.getRestaurateur();
		if(restaurateur.getRestaurants() == null || restaurateur.getRestaurants().isEmpty()) {
			popoutMessage(AlertType.INFORMATION, "Attenzione", "Non hai ancora nessun ristorante", "Inserisci il tuo primo ristorante!");
			return;
		}
		controller.initializeScene();
		App.setRestaurantOwnerLoggedScene();
	}
	
	@FXML
	public void onHelpRequest() {
		helpWindow(helpType.NEW_RESTAURANT_SCENE);
	}
	
}
