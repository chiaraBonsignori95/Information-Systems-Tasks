package application;

import java.net.URL;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import application.ParametersFactory.helpType;
import backend.DatabaseManager;
import backend.exc.DatabaseManagerException;
import backend.exc.UserNotFoundException;
import backend.modules.ComparisonElement;
import backend.modules.ComparisonResult;
import backend.modules.OpeningHour;
import backend.modules.Reply;
import backend.modules.Restaurant;
import backend.modules.RestaurantOwner;
import backend.modules.Review;
import backend.modules.TrendElement;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Pagination;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class RestaurantOwnerLoggedController extends Controller implements Initializable {

	//common fields
	private RestaurantOwner restaurateur;
	private enum Type {APPROVED, NOT_APPROVED};
	@FXML
	private TabPane tabPane;

	// fields for restaurants approved
	private List<Restaurant> listRestaurantApproved;
	private Timetable timetable;
	private Restaurant restaurantApproved;
	private boolean isUpdating;
	private List<Review> listReview;
	private int lastPage = -1;
	private Review focusedReview;
	
	@FXML
	private Label messageRestaurantOwner;
	@FXML
	private TextField nameApproved;
	@FXML
	private TextField phoneNumberApproved;
	@FXML
	private TextField addressApproved;
	@FXML
	private TextField priceRangeApproved;
	@FXML
	private ListView<Node> typeOfCookingApproved;
	@FXML
	private ListView<Node> optionsApproved;
	@FXML
	private ListView<Node> featuresApproved;
	@FXML
	private Pagination paginationApproved;
	@FXML
	private ListView<Label> restaurantViewerApproved;
	@FXML	
	private ListView<Node> timetablesRestaurantApproved;
	@FXML
	private Button modifyButton;
	@FXML
	private Button removeRestaurantButton;
	@FXML
	private Button addButton;
	@FXML
	private Button removeButton;
	@FXML
	private Tab tabApproved;
	
	//fields for restaurants NOT approved
	private List<Restaurant> listRestaurantNotApproved;
	private Restaurant restaurantNotApproved;
	
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
	@FXML
	private Tab tabNotApproved;
	
	//fields analytic show trend
	private Restaurant restaurantShowTrend;
	
	@FXML
	private DatePicker datePickerFromDate;
	@FXML
	private ChoiceBox<String> choiceBoxIntervalWindow;
	@FXML
	private Tab tabShowTrend;
	@FXML
	private ListView<Label> restaurantViewerShowTrend;
	@FXML
	private HBox showTrendViewer;
	
	//fields analytic comparison
	private Restaurant restaurantComparison;
	
	@FXML
	private DatePicker datePickerFromDateComparison;
	@FXML
	private ChoiceBox<String> choiceBoxIntervalWindowComparison;
	@FXML
	private Tab tabComparison;
	@FXML
	private ListView<Label> restaurantViewerComparison;
	@FXML
	private HBox comparisonViewer;
	@FXML
	private HBox gridPaneContainer;

	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		String [] intervals = ParametersFactory.getShowTrendIntervals();
		
		for(String string : intervals) {
			choiceBoxIntervalWindow.getItems().add(string);
			choiceBoxIntervalWindowComparison.getItems().add(string);
		}
		
		choiceBoxIntervalWindow.setValue(intervals[0]);
		choiceBoxIntervalWindowComparison.setValue(intervals[0]);
		
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
        
        datePickerFromDate.setDayCellFactory(callB);
        datePickerFromDate.setShowWeekNumbers(false);
        datePickerFromDate.setValue(LocalDate.now().minusMonths(6));
        
        datePickerFromDateComparison.setDayCellFactory(callB);
        datePickerFromDateComparison.setShowWeekNumbers(false);
        datePickerFromDateComparison.setValue(LocalDate.now().minusMonths(6));
        
        datePickerFromDate.setOnAction(event -> {loadRestaurantShowTrend();});
        choiceBoxIntervalWindow.setOnAction(event -> {loadRestaurantShowTrend();});
        
        datePickerFromDateComparison.setOnAction(event -> {loadRestaurantComparison();});
        choiceBoxIntervalWindowComparison.setOnAction(event -> {loadRestaurantComparison();});
	
	}
	
	private void init() {
		
		listRestaurantApproved = new ArrayList<>();
		listRestaurantNotApproved = new ArrayList<>();
		timetable = new Timetable();
		restaurantApproved = null;
		isUpdating = false;
		listReview	= new ArrayList<>();
		lastPage = -1;
		focusedReview = null;
		restaurantNotApproved = null;
		restaurantShowTrend = null;
		restaurantComparison = null;
	}
	
	private void loadMessage() {
		messageRestaurantOwner.setText("Bentornato, " + restaurateur.getName() + "!");
	}
	
	/**
	 * Used to load the list of restaurants in the left side of the scene (APPROVED/NOT_APPROVED)
	 */
	
	private void loadRestaurants() {
		
		restaurantViewerApproved.getItems().clear();
		restaurantViewerNotApproved.getItems().clear();
		restaurantViewerShowTrend.getItems().clear();
		restaurantViewerComparison.getItems().clear();
		
		List<Restaurant> restaurants = restaurateur.getRestaurants();
		if ( restaurants == null || restaurants.isEmpty()) {
			popoutMessage(AlertType.INFORMATION, "Attenzione", "Non hai ancora nessun ristorante", "Inserisci il tuo primo ristorante!");
			onNewRestaurantRegistration();
			return;
		}
		
		int indexApproved = 0;
		int indexNotApproved = 0;
		
		//initialization of restaurants viewer
		
		for (Restaurant restaurant : restaurants ) {
			
			// initialize the list of restaurants used by the application
			if(restaurant.isApproved())
				listRestaurantApproved.add(restaurant);
			else
				listRestaurantNotApproved.add(restaurant);
			
			Type restaurantType = (restaurant.isApproved()) ? Type.APPROVED : Type.NOT_APPROVED;
			
			String labelText = 	restaurant.getName() + ",\n\t" +
								restaurant.getAddress().toStringSummary();
			
			//this branch initialize restaurants approved and the analytics part
			if(restaurantType == Type.APPROVED) {
				
				Label labelRestaurantApproved 	= new Label(labelText);
				Label labelRestaurantShowTrend 	= new Label(labelText);
				Label labelRestaurantComparison = new Label(labelText);
				
				labelRestaurantApproved.setId(Integer.toString(indexApproved));
				labelRestaurantShowTrend.setId(Integer.toString(indexApproved));
				labelRestaurantComparison.setId(Integer.toString(indexApproved));
				
				indexApproved++;
				
				labelRestaurantApproved.setCursor(Cursor.HAND);
				labelRestaurantShowTrend.setCursor(Cursor.HAND);
				
				labelRestaurantApproved.setOnMouseClicked(event -> {
					
					Label label = (Label)event.getSource();
					restaurantApproved = listRestaurantApproved.get(Integer.parseInt(label.getId()));
						
					listReview	= new ArrayList<>();
					lastPage = -1;
					focusedReview = null;
				
					loadRestaurantInformation(Type.APPROVED);
			        
				});
				
				labelRestaurantShowTrend.setOnMouseClicked(event -> {
					
					Label label = (Label)event.getSource();
					// same list
					restaurantShowTrend = listRestaurantApproved.get(Integer.parseInt(label.getId()));
				
					loadRestaurantShowTrend();
			        
				});
				
				labelRestaurantComparison.setOnMouseClicked(event -> {
					
					Label label = (Label)event.getSource();
					// same list
					restaurantComparison = listRestaurantApproved.get(Integer.parseInt(label.getId()));
				
					loadRestaurantComparison();
			        
				});
				
			restaurantViewerApproved.getItems().add(labelRestaurantApproved);
			restaurantViewerShowTrend.getItems().add(labelRestaurantShowTrend);
			restaurantViewerComparison.getItems().add(labelRestaurantComparison);
			
			} else {	//restaurant NOT approved
				
				Label labelRestaurantNotApproved = new Label(labelText);
				
				labelRestaurantNotApproved.setCursor(Cursor.HAND);
				labelRestaurantNotApproved.setId(Integer.toString(indexNotApproved++));
				
				labelRestaurantNotApproved.setOnMouseClicked(event -> {
					
					Label label = (Label)event.getSource();
					restaurantNotApproved = listRestaurantNotApproved.get(Integer.parseInt(label.getId()));
				
					loadRestaurantInformation(Type.NOT_APPROVED);
			        
				});
				
				restaurantViewerNotApproved.getItems().add(labelRestaurantNotApproved);
			}
			
		}
		
		if(!listRestaurantApproved.isEmpty()) {
			restaurantApproved 		= listRestaurantApproved.get(0);
			restaurantShowTrend 	= listRestaurantApproved.get(0);
			restaurantComparison 	= listRestaurantApproved.get(0);
		}
		if(!listRestaurantNotApproved.isEmpty())
			restaurantNotApproved = listRestaurantNotApproved.get(0);
	}
	
	private void loadRestaurantComparison() {
	
		if(restaurantComparison == null)
			return;
		
		comparisonViewer.getChildren().clear();
		gridPaneContainer.getChildren().clear();
		
		String interval = (String)choiceBoxIntervalWindowComparison.getValue();
		
		int window = ParametersFactory.fromStringToIntInterval(interval);
		
		LocalDate localDate = datePickerFromDateComparison.getValue();
		
		DatabaseManager database = App.getDatabase();
		
		ComparisonResult result = database.getComparisonWithCompetitors(restaurantComparison, 
																		Date.from(localDate.atStartOfDay()
																		.atZone(ZoneId.systemDefault())
																		.toInstant()), window);
		
		if(tabPane.getSelectionModel().getSelectedIndex() != 3 && result == null) {
			tabComparison.setDisable(true);
			return;
		}
		
		if(tabPane.getSelectionModel().getSelectedIndex() == 3 && result == null) {
			popoutMessage(AlertType.WARNING,"Attenzione", "Non ci sono ancora sufficienti recensioni o concorrenti", "");
			return;
		}
		
		List<ComparisonElement> list = result.getComparisonList();
		
		//bar chart
		CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String,Number> bc = new BarChart<String,Number>(xAxis, yAxis);
        bc.getStyleClass().add("score-bar-chart");
        bc.setTitle("Andamento dei concorrenti");
        bc.setLegendVisible(false);
 
        XYChart.Series serie = new XYChart.Series();
        
        serie.getData().add(new XYChart.Data(result.getBest().getName(), result.getBest().getPoints()));
        
        for (ComparisonElement elem : list)
        	if (elem.getName() != result.getBest().getName())
        		serie.getData().add(new XYChart.Data(elem.getName(), elem.getPoints()));
         
        bc.getData().add(serie);
      
        //grid pane
        GridPane pane = new GridPane();
        
        pane.setHgap(50);
        pane.setVgap(20);
        
        pane.add(new Label("Numero totale dei ristoranti concorrenti:"), 0,0 );
        pane.add(new Label("La tua posizione:"), 0, 1);
        pane.add(new Label(Integer.toString(result.getPositionInRank())), 1, 1);
        pane.add(new Label(Integer.toString(result.getNumberOfCompetitors())), 1, 0);
        
        gridPaneContainer.getChildren().add(pane);
        
        //trend
        CategoryAxis xAxisTrend = new CategoryAxis();                     
        NumberAxis yAxisTrend = new NumberAxis(); 
		yAxisTrend.setAutoRanging(false);
        yAxisTrend.setUpperBound(6);
		LineChart<String, Number> showTrendGraph = new LineChart<String, Number>(xAxisTrend, yAxisTrend);
        showTrendGraph.getStyleClass().add("trend-chart");
		
		showTrendGraph.setTitle("Trend delle recensioni con i miei concorrenti");
		showTrendGraph.setMinSize(600, 400);

		DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.ITALY);
		
		XYChart.Series trendBest = new XYChart.Series();
		XYChart.Series mySeries = new XYChart.Series();
		XYChart.Series[] series = new XYChart.Series[5];
			
		double min = 5;
		
		for (TrendElement elem : result.getBest().getTrend()) {
			if (elem.getMeanRating() < min)
				min = elem.getMeanRating();
        	trendBest.getData().add(new XYChart.Data(df.format(elem.getMaxDate()), elem.getMeanRating()));
        	trendBest.setName(result.getBest().getName());
        }
		showTrendGraph.getData().add(trendBest);
		
		for(int i = 0; i < list.size(); i++)
			series[i] = new XYChart.Series();
		
		for(int i = 0; i < list.size(); i++) {
			ComparisonElement elem = list.get(i);
			if (elem.getName() != result.getBest().getName()) {
				for (TrendElement trendElem : elem.getTrend()) {
					if (trendElem.getMeanRating() < min)
						min = trendElem.getMeanRating();
			        series[i].getData().add(new XYChart.Data(df.format(trendElem.getMaxDate()), trendElem.getMeanRating()));
			    }
				series[i].setName(elem.getName());		
				showTrendGraph.getData().add(series[i]);
			}
		}
        
        yAxisTrend.setLowerBound(Math.floor(min - 0.5));
        comparisonViewer.getChildren().addAll( bc, showTrendGraph);		
        
        return;
		
	}

	private void loadRestaurantShowTrend() {
	
		if(restaurantShowTrend == null)
			return;
		
		showTrendViewer.getChildren().clear();
		
		String interval = (String)choiceBoxIntervalWindow.getValue();
		
		int window = ParametersFactory.fromStringToIntInterval(interval);
		
		LocalDate localDate = datePickerFromDate.getValue();
		
		DatabaseManager database = App.getDatabase();
		
		List<TrendElement> list = database.showTrend(restaurantShowTrend, 
																		Date.from(localDate.atStartOfDay()
																		.atZone(ZoneId.systemDefault())
																		.toInstant()), window);
		
		if(tabPane.getSelectionModel().getSelectedIndex() != 2 && list.isEmpty())
			return;
		
		if(tabPane.getSelectionModel().getSelectedIndex() == 2 && list.isEmpty()) {
			popoutMessage(AlertType.WARNING,"Attenzione", "Non ci sono ancora recensioni da poter analizzare", "");
			return;
		}
		
		CategoryAxis xAxis = new CategoryAxis();                     
        NumberAxis yAxis = new NumberAxis(); 
        yAxis.setUpperBound(5);        
        AreaChart<String, Double> showTrendGraph = new AreaChart(xAxis, yAxis);
        showTrendGraph.setLegendVisible(false);
		
		showTrendGraph.setTitle("Trend delle recensioni");
		showTrendGraph.getStyleClass().add("trend-chart");
		showTrendGraph.setMinSize(600, 500);
		
		XYChart.Series trend = new XYChart.Series();
		DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.ITALY);
		for (TrendElement elem : list) {			
        	trend.getData().add(new XYChart.Data(df.format(elem.getMaxDate()), elem.getMeanRating()));
        }
        showTrendGraph.getData().add(trend);
        
        showTrendViewer.getChildren().add(showTrendGraph);

	}

	private void cleanShowTrendFields() {

		 datePickerFromDate.setValue(LocalDate.now());
		 choiceBoxIntervalWindow.setValue(ParametersFactory.getShowTrendIntervals()[0]);
		
	}

	private void loadTimetable() {
		
		try {
			timetable.addTimetableFromOpeningHour(restaurantApproved.getOpeningHours());
		} catch (TimetableNotAvailableException e) {
			popoutMessage(AlertType.ERROR, "Attenzione", "Si è verificato un errore, l'applicazione verrà chiusa", "");
			App.closeApp();
		}
	}
	
	private void loadRestaurantInformation(Type type) {
		
		if(type == Type.APPROVED) {
			
			typeOfCookingApproved.getItems().clear();
			optionsApproved.getItems().clear();
			featuresApproved.getItems().clear();
			timetablesRestaurantApproved.getItems().clear();
			
		} else {
			
			typeOfCookingNotApproved.getItems().clear();
			optionsNotApproved.getItems().clear();
			featuresNotApproved.getItems().clear();
			timetablesRestaurantNotApproved.getItems().clear();
			
		}
		
		if (type == Type.APPROVED) {
			
			nameApproved.setText(restaurantApproved.getName());
			phoneNumberApproved.setText(restaurantApproved.getPhoneNumber());
			addressApproved.setText(restaurantApproved.getAddress().toString());
			if(restaurantApproved.getPriceRange() != null)
				priceRangeApproved.setText(	restaurantApproved.getPriceRange().getMinPrice() + " - " +
											restaurantApproved.getPriceRange().getMaxPrice() + " €");
		} else {
			
			nameNotApproved.setText(restaurantNotApproved.getName());
			phoneNumberNotApproved.setText(restaurantNotApproved.getPhoneNumber());
			addressNotApproved.setText(restaurantNotApproved.getAddress().toString());
			if(restaurantNotApproved.getPriceRange() != null)
				priceRangeNotApproved.setText(	restaurantNotApproved.getPriceRange().getMinPrice() + " - " +
												restaurantNotApproved.getPriceRange().getMaxPrice() + " €");
		}
	
		List<String> list = (type == Type.APPROVED) ? restaurantApproved.getCategories() : restaurantNotApproved.getCategories();
			
		for(String string : list) {
			Label restaurantLabel = new Label(string);
			restaurantLabel.setCursor(Cursor.HAND);
			if(type == Type.APPROVED)
				typeOfCookingApproved.getItems().add(restaurantLabel);
			else 
				typeOfCookingNotApproved.getItems().add(restaurantLabel);
		}
			
		list = (type == Type.APPROVED) ? restaurantApproved.getOptions() : restaurantNotApproved.getOptions();
		if(list == null || (list != null && list.isEmpty())) {
			if(type == Type.APPROVED)
				optionsApproved.getItems().add(new Label("Nessun elemento specificato"));
			else 
				optionsNotApproved.getItems().add(new Label("Nessun elemento specificato"));
		} else {
			for(String string : list) {
				Label restaurantLabel = new Label(string);
				restaurantLabel.setCursor(Cursor.HAND);
				if(type == Type.APPROVED)
					optionsApproved.getItems().add(restaurantLabel);
				else 
					optionsNotApproved.getItems().add(restaurantLabel);
			}
		}
			
		list = (type == Type.APPROVED) ? restaurantApproved.getFeatures() : restaurantNotApproved.getFeatures();
			
		if (list == null || (list != null && list.isEmpty())) {
			if(type == Type.APPROVED)
				featuresApproved.getItems().add(new Label("Nessun elemento specificato"));
			else 
				featuresNotApproved.getItems().add(new Label("Nessun elemento specificato"));
			} else {				
				for(String string : list) {
					Label restaurantLabel = new Label(string);
					restaurantLabel.setCursor(Cursor.HAND);
					if(type == Type.APPROVED)
						featuresApproved.getItems().add(restaurantLabel);
					else 
						featuresNotApproved.getItems().add(restaurantLabel);
				}
			}
			
			timetableUpdate(type);	
			
			if(type == Type.APPROVED)
				loadReviews();
					
	}
		
	private void loadFromDatabase(int index) {
				
		DatabaseManager database  = App.getDatabase();
		
		if(index < lastPage  * database.getPagesize()) 
			return;
		
		lastPage = (lastPage == -1) ? 0 : lastPage; 
		int numberOfIteration = ((index / database.getPagesize()) + 1) - lastPage ;
		
		while (numberOfIteration > 0) {
			try {
				
				List<Review> newReview = database.getReviews(restaurantApproved, ++lastPage);
				listReview.addAll(newReview);
				numberOfIteration--;
				
			} catch (DatabaseManagerException e) {
				popoutMessage(AlertType.ERROR, "Errore", "L'applicazione verra chiusa", "");
				App.closeApp();
			}
		}	
	}
	
	private void onReplyRequest(Button button) {
		
		Dialog<String> dialog = new Dialog<>();
		
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(getClass().getResourceAsStream("food.png")));
		
		dialog.getDialogPane().getStylesheets().add("./application/application.css");
		dialog.getDialogPane().getStyleClass().add("form");
		
		dialog.setTitle("Rispondi al cliente");
		dialog.setHeaderText("Rispondi alla recensione del cliente");

		ButtonType reply = new ButtonType("Rispondi", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(reply, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextArea text = new TextArea();
		text.getStyleClass().add("general-text");
		
		grid.add(text, 0, 0);

		Node replyButton = dialog.getDialogPane().lookupButton(reply);
		replyButton.setDisable(true);

		text.textProperty().addListener((observable, oldValue, newValue) -> {
		    replyButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

	
		Platform.runLater(() -> text.requestFocus());

		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == reply) {
		        return text.getText();
		    }
		    return null;
		});

		Optional<String> result = dialog.showAndWait();

		result.ifPresent(replyText -> {
			
			DatabaseManager database = App.getDatabase();
					
			Reply replyToInsert = new Reply(restaurateur.getUsername(), replyText);
			try {
				database.insertReply(focusedReview, replyToInsert);
				button.setDisable(true);
				popoutMessage(AlertType.INFORMATION, "Congratulationi", "La registrazione della risposta è avvenuta correttamente", "");
				focusedReview.setReply(replyToInsert);
				GridPane parent = (GridPane)button.getParent();
				TextArea replyTextArea = new TextArea();
				replyTextArea.setEditable(false);
				replyTextArea.setWrapText(true);
				replyTextArea.setMaxHeight(150);
				replyTextArea.setText(replyText);
				parent.add(replyTextArea, 1, 1);
				
			} catch (DatabaseManagerException e) {
				popoutMessage(AlertType.ERROR, "Errore", "Si è verificato un errore, l'applicazione verrà chiusa", "");
				App.closeApp();
			} 
		});	
	}
	
	private void loadReviews() {
		
		if(restaurantApproved.getNumberOfReviews() > 0) {
			paginationApproved.setCurrentPageIndex(0);
			paginationApproved.setPageCount(restaurantApproved.getNumberOfReviews());		
		}
		else {
			paginationApproved.setCurrentPageIndex(0);
			paginationApproved.setPageCount(1);
		}

        paginationApproved.setPageFactory(new Callback<Integer, Node>() {
	       public Node call(Integer pageIndex) {
	    	   
	    	   VBox box = new VBox();
	    	   box.getStyleClass().add("review-vbox");
	    	      	   
	    	   if(restaurantApproved.getNumberOfReviews() > 0) {
	    		   
					if(pageIndex > lastPage)
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
					
					GridPane paneReviewReply = new GridPane();
					paneReviewReply.setVgap(10);
					paneReviewReply.setHgap(100);
					
					Button replyButton = new Button("Rispondi");
					replyButton.getStyleClass().add("general-button");
					replyButton.getStyleClass().add("right-panel-button");
					paneReviewReply.add(replyButton, 0, 0);
					
					TextArea reviewText = new TextArea(focusedReview.getText());
					reviewText.setWrapText(true);
					reviewText.setEditable(false);
					reviewText.getStyleClass().add("general-text");
					paneReviewReply.add(reviewText, 0, 1);				
					
					Reply replyToReview = focusedReview.getReply();
					
					replyButton.setOnAction(new EventHandler<ActionEvent>() {
					       @Override
					       public void handle(ActionEvent event) {
					           onReplyRequest((Button)event.getSource());
					       }
					   });
					
					if (replyToReview != null) {						
						replyButton.setDisable(true);
						
						TextArea replyText = new TextArea();
						replyText.setText(replyToReview.getText());
						replyText.setWrapText(true);
						replyText.setEditable(false);
						replyText.getStyleClass().add("general-text");			
						paneReviewReply.add(replyText, 1, 1);
					}
					
					box.getChildren().addAll(pane, paneReviewReply);				
					box.setSpacing(30);
					box.setPadding(new Insets(50, 0, 0, 0));
	
	    	   } else {
	    		   box.getChildren().add(new Label("Non sono ancora presenti delle recensioni!"));
	    	   }
	    	   return box; 
	       }
	   });
	}
	
	public void initializeScene() {
		
		init();
		loadMessage();
		loadRestaurants();
		
		tabApproved.setDisable(restaurantApproved == null);
		tabNotApproved.setDisable(restaurantNotApproved == null);
		tabShowTrend.setDisable((restaurantShowTrend == null) || (restaurantShowTrend != null && restaurantShowTrend.getNumberOfReviews() == 0));
		tabComparison.setDisable((restaurantComparison == null) || (restaurantComparison != null && restaurantComparison.getNumberOfReviews() == 0) );

		 
		if (restaurantApproved != null) {
			loadTimetable();
			loadRestaurantInformation(Type.APPROVED);
			loadRestaurantShowTrend();
			loadRestaurantComparison();
		}
		
		if (restaurantNotApproved != null)
			loadRestaurantInformation(Type.NOT_APPROVED);
		
		if (restaurantNotApproved != null && restaurantApproved == null) {

			SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
			selectionModel.select(1);
		}
		
		if (restaurantApproved != null && restaurantNotApproved == null) {

			SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
			selectionModel.select(0);
		}
				
	}

	@FXML
	private void onNewRestaurantRegistration() {
		App.setRegistrationNewResturantScene();
	}
	
	private void changeFieldsUpdate() {
		
		if(isUpdating) {
			//phone number 
			phoneNumberApproved.setDisable(false);
			phoneNumberApproved.setEditable(true);
			
			//button timetable
			addButton.setDisable(false);
			removeButton.setDisable(false);
			
			// categories
			String[] categoriesName = ParametersFactory.getCategoriesName();
			
			typeOfCookingApproved.getItems().clear();
			
			List<String> text = restaurantApproved.getCategories();
			for(String category : categoriesName) {
				CheckBox item = new CheckBox(category);
				if(text.contains(category))
					item.setSelected(true);
				typeOfCookingApproved.getItems().add(item);
			}
			
			//options
			String[] options = ParametersFactory.getOptionsName();
			
			optionsApproved.getItems().clear();
			
			text = restaurantApproved.getOptions();
			
			for(String option : options) {
				CheckBox item = new CheckBox(option);
				if(text != null && text.contains(option))
					item.setSelected(true);
				optionsApproved.getItems().add(item);
			}
			
			//features
			String[] features = ParametersFactory.getFeaturesName();
			
			featuresApproved.getItems().clear();
			
			text = restaurantApproved.getFeatures();
			
			for(String feature : features) {
				CheckBox item = new CheckBox(feature);
				if(text != null && text.contains(feature))
					item.setSelected(true);
				featuresApproved.getItems().add(item);
			}
			
			//timetables 		
			timetableUpdate(Type.APPROVED);
			
		} else {
		
			// categories
			List<String> categories = new ArrayList<>();
			
			ObservableList<Node> selected = typeOfCookingApproved.getItems();
			for(Node n : selected) {
				CheckBox item = (CheckBox)n;
				if(item.isSelected())
					categories.add(new String(item.getText()));
			}
			
			//options
			List<String> options = new ArrayList<>();
			selected = optionsApproved.getItems();
			for(Node n : selected) {
				CheckBox item = (CheckBox)n;
				if(item.isSelected())
					options.add(new String(item.getText()));
			}
				
			//features			
			List<String> features = new ArrayList<>();
			selected = featuresApproved.getItems();
			for(Node n : selected) {
				CheckBox item = (CheckBox)n;
				if(item.isSelected())
					features.add(new String(item.getText()));
			}
			
			String phoneNumber = phoneNumberApproved.getText();
				
			if(!phoneNumber.matches("^[0-9+ ]*$")) {
				popoutMessage(AlertType.WARNING, "Attenzione", "Il numero di telefono inserito non è valido", "");
				return;
			}
				
			if(categories.isEmpty()) {
				popoutMessage(AlertType.WARNING, "Attenzione", "Deve inserire almeno una categoria di cucina per il tuo ristorante", "");
				return;
			}
			
			restaurantApproved.setCategories(categories);
			restaurantApproved.setFeatures(features);
			restaurantApproved.setOptions(options);
			restaurantApproved.setPhoneNumber(phoneNumber);
			restaurantApproved.setOpeningHours(timetable.parseToOpeningHours());
			phoneNumberApproved.setDisable(true);
			phoneNumberApproved.setEditable(false);
			
			addButton.setDisable(true);
			removeButton.setDisable(true);
			
			loadRestaurantInformation(Type.APPROVED);
			
		}		
	}
	
	@FXML
	private void onModifyRequest() {
		if(!isUpdating) {
			isUpdating = true;
			changeFieldsUpdate();			
			modifyButton.setText("Salva");
			removeRestaurantButton.setDisable(true);
		} else {
			
			if(!confirmationPopoutMessage("Attenzione", "Confermi di voler salvare?", ""))
				return;
			
			isUpdating = false;
			changeFieldsUpdate();			
			modifyButton.setText("Modifica");
			removeRestaurantButton.setDisable(false);
			
			DatabaseManager database = App.getDatabase();
	
			database.updateRestaurant(restaurantApproved, restaurantApproved);

		}
	}
	
	@FXML
	private void onAddTimetableRequest() {
		
		Dialog<List<String>> dialog = new Dialog<>();
		
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(getClass().getResourceAsStream("food.png")));
		
		dialog.getDialogPane().getStylesheets().add("./application/application.css");
		dialog.getDialogPane().getStyleClass().add("form");
		
		dialog.setTitle("Inserisci orario");
		dialog.setHeaderText("Inserisci un nuovo orario di apertura");

		ButtonType insertButtonType = new ButtonType("Inserisci", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(insertButtonType, ButtonType.CANCEL);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(30);
		
		grid.setPadding(new Insets(50, 100, 100, 50));
		
		MenuButton openingDay = new MenuButton();
		openingDay.getStyleClass().add("category-menu");
		MenuButton closingDay = new MenuButton();
		closingDay.getStyleClass().add("category-menu");
		
		openingDay.setText("Giorno di apertura");
		closingDay.setText("Giorno di chiusura");
		
		openingDay.setMinWidth(200);
		closingDay.setMinWidth(200);
		
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
		
		TextField openHour = new TextField();
		TextField closingHour = new TextField();
		openHour.setPromptText("Apertura");
		openHour.getStyleClass().add("form-field");
		closingHour.setPromptText("Chiusura");		
		closingHour.getStyleClass().add("form-field");
		grid.add(new Label("Da:"), 0, 0);
		grid.add(new Label("A:"), 0, 1);
		grid.add(new Label("Orario di apertura:"), 0, 2);
		grid.add(new Label("Orario di chiusura:"), 0, 3);
		
		grid.add(openingDay, 	1, 0);
		grid.add(closingDay, 	1, 1);
		grid.add(openHour, 		1, 2);
		grid.add(closingHour, 	1, 3);
		
		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == insertButtonType) {
		    	if(		openingDay.getText().equals("Giorno di apertura") || closingDay.getText().equals("Giorno di chiusura") ||
		    			openHour.getText().isEmpty() || closingHour.getText().isEmpty()) {
		    		popoutMessage(AlertType.WARNING,"Attenzione", "Non hai completato tutti i campi","");
		    		return null;
		    	}
		    	
		    	String opening = openHour.getText();
		    	String closing = closingHour.getText();
		    	
		    	try {
					if(!opening.contains(":"))
						opening += ":00";
					LocalTime.parse(opening);
					
					if(!closing.contains(":"))
						closing += ":00";
					LocalTime.parse(opening);
					
				} catch(Exception e) {
					popoutMessage(AlertType.WARNING, "Attenzione", "L'orario inserito non è corretto", "");
					return null;
				}
		    	
		    	
		    	List<String> list = new ArrayList<>();
		    	list.add(openingDay.getText());
		    	list.add(closingDay.getText());
		    	list.add(opening);
		    	list.add(closing);
		        return list;
		    }
		    return null;
		});

		Optional<List<String>> result = dialog.showAndWait();

		result.ifPresent(list -> {
			try {
				timetable.addTimetable(list.get(0), list.get(1), list.get(2), list.get(3));
				timetableUpdate(Type.APPROVED);
			} catch (TimetableNotAvailableException e) {
				popoutMessage(AlertType.WARNING, "Attenzione", "Gli orari che hai inserito collidono", "");
				return;
			}
			
		});
		
	}
	
	private void timetableUpdate(Type type) {
		
		List<OpeningHour> listOpening = (type == Type.APPROVED) ? timetable.parseToOpeningHours() : restaurantNotApproved.getOpeningHours();
		
		if(type == Type.APPROVED)
			timetablesRestaurantApproved.getItems().clear();
		else
			timetablesRestaurantNotApproved.getItems().clear();
		
		if(listOpening.isEmpty())
			if (type == Type.APPROVED)
				timetablesRestaurantApproved.getItems().add(new Label("Nessun orario specificato"));
			else
				timetablesRestaurantNotApproved.getItems().add(new Label("Nessun orario specificato"));	
		
		for(OpeningHour h : listOpening) {
			String dayString = h.getDays();
			String[] days = dayString.split(" - ");
			String dayStart = Timetable.parseInCompleteDayName(days[0]);
			String dayEnd = Timetable.parseInCompleteDayName(days[1]);
			List<String> times = h.getTimes();
			for(String time : times) {
				String text = dayStart + " - " + dayEnd + ", " + time;
				if(type == Type.APPROVED) {
					Node item;
					if(isUpdating)
						item = new CheckBox(text);
					else 
						item = new Label(text);
					timetablesRestaurantApproved.getItems().add(item);
				} else {
					timetablesRestaurantNotApproved.getItems().add(new Label(text));
				}
			}
		}
	}

	@FXML
	private void onDeleteTimetableRequest() {
		
		ObservableList<Node> list = timetablesRestaurantApproved.getItems();
		for(Node n : list) {
			CheckBox item = (CheckBox)n;
			if(item.isSelected())
				timetable.deleteTimetableFromString(item.getText());
		}
		
		timetableUpdate(Type.APPROVED);
		
	} 
	
	@FXML
	private void onDeleteRestaurant() {
		
		if(!super.confirmationPopoutMessage("Attenzione", "Sei veramente sicuro di voler eliminare il tuo ristorante?", "Questa operazione è irreversibile!"))
			return;
		
		DatabaseManager database = App.getDatabase();

		database.deleteRestaurant(restaurantApproved);
		
		restaurateur.getRestaurants().remove(restaurantApproved);
		restaurantApproved = null;
		popoutMessage(AlertType.INFORMATION, "Complimenti", "Il ristorante è stato eliminato con successo!", "");
		
		initializeScene();	
	
	}
	
	@FXML
	public void onDeleteAccountRequest() {

		if (!confirmationPopoutMessage("Attenzione", "Sei sicuro di voler cancellare il tuo account?",
				"Questa operazione non può essere annullata"))
			return;

		DatabaseManager database = App.getDatabase();
		try {
			database.deleteAccount(restaurateur);
		} catch (UserNotFoundException e) {
			popoutMessage(AlertType.ERROR, "Attenzione", "Si è verificato un errore, l'applicazione verrà chiusa", "");
			App.closeApp();
			return;
		}

		popoutMessage(AlertType.INFORMATION, "Complimenti", "Il tuo account è stato rimosso con successo!", "");
		onPreviousPageRequest();
	}
	
	@FXML 
	private void onPreviousPageRequest() {		
		restaurateur = null;
		App.setRestaurantOwnerScene();
	}
	
	@FXML
	public void onHelpRequest() {
		helpWindow(helpType.RESTAURATEUR_LOGGED_SCENE);
	}
	
	public RestaurantOwner getRestaurateur() {
		return restaurateur;
	}

	public void setRestaurateur(RestaurantOwner restaurateur) {
		this.restaurateur = restaurateur;
	}

}
