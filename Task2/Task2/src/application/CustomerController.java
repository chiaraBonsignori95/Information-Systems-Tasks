package application;

import java.net.URL;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import application.ParametersFactory.helpType;
import backend.DatabaseManager;
import backend.exc.DatabaseManagerException;
import backend.exc.UserNotFoundException;
import backend.exc.UsernameAlreadyPresentException;
import backend.modules.Customer;
import backend.modules.DistributionElement;
import backend.modules.OpeningHour;
import backend.modules.Restaurant;
import backend.modules.Review;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CustomerController extends Controller implements Initializable {

	private String city;
	private List<Restaurant> restaurants;
	private Restaurant restaurant;
	private int pageNumRestaurants;
	private int pageNumReviews;
	private int indexResturant;
	private List<Review> listReview;
	private Review focusedReview;

	private Customer customer;

	@FXML
	private Label messageCustomer;
	@FXML
	private Label login;
	@FXML
	private TextField searchName;
	@FXML
	private MenuButton searchCategory;
	@FXML
	private ListView<Label> restaurantViewer;
	@FXML
	private TextField name;
	@FXML
	private TextField phoneNumber;
	@FXML
	private TextField address;
	@FXML
	private ListView<Label> timetables;
	@FXML
	private ListView<Label> typeOfCooking;
	@FXML
	private ListView<Label> options;
	@FXML
	private ListView<Label> features;
	@FXML
	private Pagination reviewsViewer;
	@FXML
	private Button addReviewButton;
	@FXML
	private MenuItem loginLogoutMenuItem;
	@FXML
	private MenuItem pastReviewMenuItem;
	@FXML
	private MenuItem deleteMenuItem;
	@FXML
	private MenuItem registerMenuItem;
	@FXML
	private DatePicker datePicker;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		pageNumRestaurants = 1;

		String[] categories = ParametersFactory.getCategoriesNamePlusNessunaCategoria();

		int index = 0;

		for (String category : categories) {
			MenuItem item = new MenuItem(category);

			item.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					MenuItem item = (MenuItem) e.getSource();
					searchCategory.setText(item.getText());
					onChangeDetected();
				}
			});

			searchCategory.getItems().add(item);

			if (index == 0)
				searchCategory.getItems().add(new SeparatorMenuItem());

			index++;
		}

		Callback<DatePicker, DateCell> callB = new Callback<DatePicker, DateCell>() {
			@Override
			public DateCell call(final DatePicker param) {
				return new DateCell() {
					@Override
					public void updateItem(LocalDate item, boolean empty) {
						super.updateItem(item, empty);
						LocalDate today = LocalDate.now();
						setDisable(empty || item.compareTo(today) > 0);
					}

				};
			}

		};

		datePicker.setDayCellFactory(callB);
		datePicker.setShowWeekNumbers(false);
		datePicker.setValue(LocalDate.now().minusMonths(12));

	}

	private void loadRestaurants() {

		String nameSearched = searchName.getText().isEmpty() ? null : searchName.getText();
		String category;
		if (searchCategory.getText().equals("Nessuna Categoria"))
			category = null;
		else
			category = searchCategory.getText();

		DatabaseManager database = App.getDatabase();

		try {
			restaurants = database.getRestaurants(nameSearched, category, city, pageNumRestaurants, true);
		} catch (DatabaseManagerException e) {
			popoutMessage(AlertType.ERROR, "Attenzione", "Si è verificato un errore l'applicazione verrà chiusa", "");
			App.closeApp();
		}

		if (!restaurants.isEmpty())
			restaurant = restaurants.get(0);
	}

	private void updateRestaurantsViewer() {

		restaurantViewer.getItems().clear();

		if (restaurants.isEmpty()) {
			restaurantViewer.getItems().add(new Label("Nessun risultato"));
			return;
		}

		for (Restaurant r : restaurants) {

			String text = r.getName() + "\n\t" + r.getAddress().toStringSummary();
			Label item = new Label(text);
			item.setId(Integer.toString(indexResturant++));
			item.setCursor(Cursor.HAND);
			item.setOnMouseClicked(event -> {
				Label label = (Label) event.getSource();
				int index = Integer.parseInt(label.getId());
				restaurant = restaurants.get(index);

				updateRestaurantInformations();

			});
			restaurantViewer.getItems().add(item);
		}
	}

	@FXML
	public void onChangeDetected() {
		initializeScene();
	}

	public void initializeScene() {
		indexResturant = 0;
		loadRestaurants();
		updateRestaurantsViewer();
		if (restaurant != null)
			updateRestaurantInformations();
	}

	private void updateRestaurantInformations() {

		name.setText(restaurant.getName());
		phoneNumber.setText(restaurant.getPhoneNumber());
		address.setText(restaurant.getAddress().toString());

		timetables.getItems().clear();

		List<OpeningHour> opening = restaurant.getOpeningHours();

		if (opening != null)
			for (OpeningHour h : opening) {

				String[] days = h.getDays().split(" - ");

				String dayStart = Timetable.parseInCompleteDayName(days[0]);
				String dayEnd = days.length != 2 ? dayStart : Timetable.parseInCompleteDayName(days[1]);
				String text = dayStart + " - " + dayEnd + ", ";
				List<String> hours = h.getTimes();
				for (String hour : hours) {
					timetables.getItems().add(new Label(text + hour));
				}

			}
		else
			timetables.getItems().add(new Label("Il ristoratore non ha specificato gli orari"));

		typeOfCooking.getItems().clear();
		options.getItems().clear();
		features.getItems().clear();

		List<String> list = restaurant.getCategories();
		if (list != null && list.isEmpty())
			list = null;

		if (list == null)
			typeOfCooking.getItems().add(new Label("Non specificato"));
		else
			for (String text : list) {
				Label label = new Label(text);
				typeOfCooking.getItems().add(label);
			}

		list = restaurant.getOptions();
		if (list != null && list.isEmpty())
			list = null;

		if (list == null)
			options.getItems().add(new Label("Non specificato"));
		else
			for (String text : list) {
				Label label = new Label(text);
				options.getItems().add(label);
			}

		list = restaurant.getFeatures();
		if (list != null && list.isEmpty())
			list = null;

		if (list == null)
			features.getItems().add(new Label("Non specificato"));
		else
			for (String text : list) {
				Label label = new Label(text);
				features.getItems().add(label);
			}

		loadReviews();
	}

	public Customer getCustomer() {
		return customer;
	}

	private void loadFromDatabase(int index) {

		DatabaseManager database = App.getDatabase();

		if (index < pageNumReviews * database.getPagesize())
			return;

		pageNumReviews = (pageNumReviews == -1) ? 0 : pageNumReviews;
		int numberOfIteration = ((index / database.getPagesize()) + 1) - pageNumReviews;

		while (numberOfIteration > 0) {
			try {

				List<Review> newReview = database.getReviews(restaurant, ++pageNumReviews);
				listReview.addAll(newReview);
				numberOfIteration--;

			} catch (DatabaseManagerException e) {
				popoutMessage(AlertType.ERROR, "Errore", "L'applicazione verrà chiusa", "");
				App.closeApp();
			}
		}
	}

	private void loadReviews() {

		pageNumReviews = -1;
		listReview = new ArrayList<>();

		int numberOfReviews = restaurant.getNumberOfReviews();

		if (numberOfReviews == 0)
			reviewsViewer.setPageCount(1);
		else
			reviewsViewer.setPageCount(numberOfReviews);

		reviewsViewer.setPageFactory(new Callback<Integer, Node>() {
			public Node call(Integer pageIndex) {

				VBox box = new VBox();
				box.getStyleClass().add("review-vbox");

				if (restaurant.getNumberOfReviews() > 0) {

					if (pageIndex > pageNumReviews)
						loadFromDatabase(pageIndex);

					focusedReview = listReview.get(pageIndex);

					DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.ITALY);
					GridPane pane = new GridPane();
					pane.setVgap(20);
					pane.setHgap(40);

					Label l = new Label("Titolo:");
					pane.add(l , 0, 0); 
					
					l = new Label(focusedReview.getTitle());
					pane.add(l, 1, 0); 
					
					l = new Label("Recensito da:");
					pane.add(l, 0, 1); 
					
					l = new Label(focusedReview.getUsername());
					pane.add(l, 1, 1); 
					
					l = new Label("In data:");
					pane.add(l, 2, 1); 
					
					l = new Label(df.format(focusedReview.getDate()));
					pane.add(l, 3, 1); 
					
					l = new Label("Punteggio:");
					pane.add(l, 2, 0); 
					
					l = new Label(Double.toString(focusedReview.getRating()));
					pane.add(l, 3, 0);
					
					if (focusedReview.getText() != null) {
						TextArea text = new TextArea(focusedReview.getText());
						text.setWrapText(true);
						text.setEditable(false);
						text.getStyleClass().add("general-text");
						box.getChildren().addAll(pane, text);
					} else
						box.getChildren().addAll(pane);
					
					box.setSpacing(30);
					box.setPadding(new Insets(50, 0, 0, 0));

				} else
					box.getChildren().add(new Label("Non sono ancora presenti delle recensioni"));

				return box;
			}
		});
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@FXML
	public void onLoginLogoutRequest() {

		if (login.getText().equals("Logout")) {
			onLogoutRequest();
			return;
		}

		Dialog<Pair<String, String>> dialog = new Dialog<>();
		
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(getClass().getResourceAsStream("food.png")));
		
		dialog.getDialogPane().getStylesheets().add("./application/application.css");
		dialog.getDialogPane().getStyleClass().add("form");
		dialog.getDialogPane().getStyleClass().add("login-form");
		
		dialog.setTitle("Login");
		dialog.setHeaderText("Per favore inserisci le tue credentiali");

		ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.getStyleClass().add("form");
		grid.getStyleClass().add("login-form");
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField username = new TextField();
		username.setPromptText("Username");
		username.getStyleClass().add("form-field");
		
		PasswordField password = new PasswordField();
		password.setPromptText("Password");
		password.getStyleClass().add("form-field");

		grid.add(new Label("Username:"), 0, 0);
		grid.add(username, 1, 0);
		grid.add(new Label("Password:"), 0, 1);
		grid.add(password, 1, 1);

		Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
		loginButton.setDisable(true);

		username.textProperty().addListener((observable, oldValue, newValue) -> {
			loginButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> username.requestFocus());

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == loginButtonType) {
				return new Pair<>(username.getText(), password.getText());
			}
			return null;
		});

		Optional<Pair<String, String>> result = dialog.showAndWait();

		result.ifPresent(usernamePassword -> {
			DatabaseManager database = App.getDatabase();

			try {
				customer = (Customer) database.login(usernamePassword.getKey(), usernamePassword.getValue(),
						Customer.class);

				addReviewButton.setDisable(false);
				messageCustomer.setText("Bentornato, " + customer.getName() + "!");
				login.setText("Logout");
				loginLogoutMenuItem.setText("Logout");
				pastReviewMenuItem.setDisable(false);
				deleteMenuItem.setDisable(false);
				registerMenuItem.setDisable(true);
			} catch (UserNotFoundException e) {
				popoutMessage(AlertType.WARNING, "Attenzione", "Le credenziali che hai inserito non sono corrette", "");
			} catch (DatabaseManagerException e) {
				popoutMessage(AlertType.ERROR, "Attenzione", "Si è verificato un errore, l'aplicazione verrà chiusa",
						"");
				App.closeApp();
			}

		});

	}

	private void onLogoutRequest() {

		customer = null;

		messageCustomer.setText("Ciao!");
		addReviewButton.setDisable(true);
		login.setText("Login");
		loginLogoutMenuItem.setText("Login");
		pastReviewMenuItem.setDisable(true);
		deleteMenuItem.setDisable(true);
		registerMenuItem.setDisable(false);

	}

	@FXML
	public void onAddNewReview() {

		Dialog<List<Object>> dialog = new Dialog<>();
		
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(getClass().getResourceAsStream("food.png")));
		
		dialog.getDialogPane().getStylesheets().add("./application/application.css");
		dialog.getDialogPane().getStyleClass().add("form");
		
		dialog.setTitle("Aggiungi una recensione");
		dialog.setHeaderText("Facci sapere cosa ne pensi!");

		ButtonType addButton = new ButtonType("Aggiungi", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.getStyleClass().add("form");
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextArea text = new TextArea();
		TextField title = new TextField();	
		title.setPromptText("Titolo");
		title.getStyleClass().add("form-field");
		
		Slider slider = new Slider(0, 5, 0.25);
		DatePicker date = new DatePicker();

		Callback<DatePicker, DateCell> callB = new Callback<DatePicker, DateCell>() {
			@Override
			public DateCell call(final DatePicker param) {
				return new DateCell() {
					@Override
					public void updateItem(LocalDate item, boolean empty) {
						super.updateItem(item, empty);
						LocalDate today = LocalDate.now();
						setDisable(empty || item.compareTo(today) > 0);
					}

				};
			}

		};

		date.setDayCellFactory(callB);
		date.setShowWeekNumbers(false);

		grid.add(new Label("Titolo della recensione:"), 0, 0);
		grid.add(title, 0, 1);
		grid.add(new Label("Valutazione generale della recensione:"), 0, 3);
		grid.add(slider, 0, 4);
		grid.add(new Label("Quando hai mangiato?"), 0, 5);
		grid.add(date, 0, 6);
		grid.add(new Label("Scrivi una breve recensione:"), 0, 7);
		text.getStyleClass().add("general-text");
		grid.add(text, 0, 8);

		Node button = dialog.getDialogPane().lookupButton(addButton);
		button.setDisable(true);

		text.textProperty().addListener((observable, oldValue, newValue) -> {
			boolean check = title.getText().isEmpty() || text.getText().isEmpty() || date.getValue() == null;
			button.setDisable(check);
		});

		title.textProperty().addListener((observable, oldValue, newValue) -> {
			boolean check = title.getText().isEmpty() || text.getText().isEmpty() || date.getValue() == null;
			button.setDisable(check);
		});

		date.setOnAction(e -> {
			boolean check = title.getText().isEmpty() || text.getText().isEmpty() || date.getValue() == null;
			button.setDisable(check);
		});

		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == addButton) {
				List<Object> list = new ArrayList<>();
				list.add(title.getText());
				list.add(DatabaseManager.approximateRating(slider.getValue()));
				list.add(date.getValue());
				list.add(text.getText());

				return list;
			}
			return null;
		});

		Optional<List<Object>> result = dialog.showAndWait();

		result.ifPresent(newReview -> {

			DatabaseManager database = App.getDatabase();

			String titleText = (String) newReview.get(0);
			double scoreValue = (double) newReview.get(1);
			LocalDate dateValue = (LocalDate) newReview.get(2);
			String reviewText = (String) newReview.get(3);

			Date dateForReview = Date.from(dateValue.atStartOfDay(ZoneId.systemDefault()).toInstant());

			Review review = new Review(dateForReview, scoreValue, restaurant.getName(), restaurant.getId(), reviewText,
					titleText, customer.getUsername());

			try {
				database.insertReview(restaurant, review, customer);
				popoutMessage(AlertType.INFORMATION, "Complimenti",
						"La tua recensione è stata registrata correttamente", "");
				if (customer.getReviews() == null) {
					List<Review> listNewReviews = new ArrayList<>();
					listNewReviews.add(review);
					customer.setReviews(listNewReviews);
				} else
					customer.getReviews().add(review);

				if (restaurant.getReviews() == null)
					restaurant.setReviews(new ArrayList<Review>());
				restaurant.getReviews().add(review);
				restaurant.setNumberOfReviews(restaurant.getNumberOfReviews() + 1);
				loadReviews();
			} catch (DatabaseManagerException e) {
				popoutMessage(AlertType.ERROR, "Attenzione", "Si è verificato un errore, l'applicazione verrà chiusa",
						"");
				App.closeApp();
			}

		});
	}

	@FXML
	public void onRegistration() {

		Optional<HashMap<String, String>> result = registration().showAndWait();

		result.ifPresent(newReview -> {

			DatabaseManager database = App.getDatabase();

			Customer user = new Customer(newReview.get("name"), newReview.get("surname"), newReview.get("username"),
					newReview.get("password"));

			try {
				database.createAccount(user);
				popoutMessage(AlertType.INFORMATION, "Congratulazioni", "La registrazione è avvenuta con successo!",
						"");
			} catch (UsernameAlreadyPresentException e) {
				popoutMessage(AlertType.WARNING, "Attenzione", "L'username che hai inserito è già presente", "");
			}

		});
	}

	@FXML
	public void onDeleteAccountRequest() {

		if (!confirmationPopoutMessage("Attenzione", "Sei sicuro di voler cancellare il tuo account?",
				"Questa operazione non può essere annullata"))
			return;

		DatabaseManager database = App.getDatabase();
		try {
			database.deleteAccount(customer);
		} catch (UserNotFoundException e) {
			popoutMessage(AlertType.ERROR, "Attenzione", "Si è verificato un errore, l'applicazione verrà chiusa", "");
			App.closeApp();
			return;
		}

		popoutMessage(AlertType.INFORMATION, "Complimenti", "Il tuo account è stato rimosso con successo!", "");
		onLogoutRequest();
	}

	@FXML
	private void onShowReviewDistribution() {

		DatabaseManager database = App.getDatabase();

		LocalDate localDate = datePicker.getValue();

		List<DistributionElement> list = database.getReviewsDistribution(restaurant,
				Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

		if (list == null) {
			popoutMessage(AlertType.WARNING, "Attenzione",
					"Non ci sono ancora sufficenti recensioni da poter essere analizzate", "");
			return;
		}

		Dialog<String> dialog = new Dialog<>();
		
		dialog.getDialogPane().getScene().getStylesheets().add("./application/application.css");
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(getClass().getResourceAsStream("food.png")));
		dialog.getDialogPane().getStyleClass().add("form");
		
		dialog.setTitle("Distribuzione delle recensioni");
		dialog.setHeaderText("Distribuzione delle recensioni per il ristorante \"" + restaurant.getName() + "\"");

		ButtonType addButton = new ButtonType("OK", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

		VBox box = new VBox();
		box.getStyleClass().add("form");
		
		ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
		if (list.get(0).getPercentage() != 0) 
			pieChartData.add(new PieChart.Data("Uno", list.get(0).getPercentage()));
		if (list.get(1).getPercentage() != 0) 
			pieChartData.add(new PieChart.Data("Due", list.get(1).getPercentage()));
		if (list.get(2).getPercentage() != 0) 
			pieChartData.add(new PieChart.Data("Tre", list.get(2).getPercentage()));
		if (list.get(3).getPercentage() != 0) 
			pieChartData.add(new PieChart.Data("Quattro", list.get(3).getPercentage()));
		if (list.get(4).getPercentage() != 0) 
			pieChartData.add(new PieChart.Data("Cinque", list.get(4).getPercentage()));
		

		PieChart pieChart = new PieChart(pieChartData);

		box.getChildren().add(pieChart);

		dialog.getDialogPane().setContent(box);

		dialog.setResultConverter(dialogButton -> {
			return "OK";
		});

		Optional<String> result = dialog.showAndWait();
	}

	@FXML
	public void onPastReviews() {
		if (customer.getReviews() == null || customer.getReviews().isEmpty()) {
			popoutMessage(AlertType.INFORMATION, "Attenzione", "Non hai ancora nessuna recensione", "");
			return;
		}

		App.setPastReviewScene();
	}
	
	@FXML
	public void onHelpRequest() {
		helpWindow(helpType.CUSTOMER_SCENE);
	}

}
