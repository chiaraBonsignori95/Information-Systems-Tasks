package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;

import backend.DatabaseManager;


public class App extends Application {
	private static final String applicationName = "Ar' tocco!";

	private static DatabaseManager database;
	
	private static Stage stage;
    private static Scene scene;
    private static HashMap<String, Parent> 		parentContainer 		= new HashMap<>();
    private static HashMap<String, Controller> 	controllerContainers  	= new HashMap<>();
    
    private static void addParent(String name, Parent parent){
    	parentContainer.put(name, parent);
    }
    
    private static void addController(String name, Controller controller){
    	controllerContainers.put(name, controller);
    }
    
    public static Controller getController(String name) {
    	return controllerContainers.get(name);
    }
    
    private static Parent getParent(String name) {
    	return parentContainer.get(name);
    }
    
    @Override
    public void init() {
    	String[] sources = {"firstScene",  
    						"customerScene", "restaurantOwnerScene", 
    						"restaurantOwnerLoggedScene", "newRestaurantScene",
    						"historyReviewsScene", "administratorFirstScene",
    						"administratorLoggedScene"};
    	try {
    		for( String name : sources) {
	    		FXMLLoader loader = new FXMLLoader(App.class.getResource(name + ".fxml"));	
	    		
	    		loader.load();
	    
				addParent		(name, 		loader.getRoot());
				addController	(name,		loader.getController());
    		}
    	
    	} catch (IOException e) {
    		Alert alert = new Alert(AlertType.ERROR);
    		
    		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
    		stage.getIcons().add(new Image(getClass().getResourceAsStream("food.png")));
    		
    		alert.getDialogPane().getScene().getStylesheets().add("./application/application.css");
    		alert.getDialogPane().getStyleClass().add("error-popup");
    		
    		alert.setTitle("Errore");
    		alert.setHeaderText("Errore nell'apertura dell'applicazione");
    		alert.showAndWait();
			return;
		}
    	
    	App.database = new DatabaseManager();    	
    }
     
    @Override
    public void start(Stage stage) throws IOException {  
    	App.stage = stage;
    	scene = new Scene(getParent("firstScene"));
    	stage.getIcons().add(new Image(getClass().getResourceAsStream("food.png")));
        stage.setScene(scene);    
        stage.setTitle(applicationName);
        stage.centerOnScreen();
        stage.setMaximized(true);
        stage.show();       
    }
    
   public static void setMainScene() {
	  scene.setRoot(getParent("firstScene"));
   }
    
    public static void setCustomerScene(String city) {
    	CustomerController controller = (CustomerController)App.getController("customerScene");
    	controller.setCity(city);
    	controller.initializeScene();
    	scene.setRoot(getParent("customerScene"));
    }
    
    public static void setRestaurantOwnerScene() {
    	scene.setRoot(getParent("restaurantOwnerScene"));	
    }
    
    public static void setAmministratorScene() {
    	scene.setRoot(getParent("administratorFirstScene"));
    }
    
    public static void setRestaurantOwnerLoggedScene() {
    	scene.setRoot(getParent("restaurantOwnerLoggedScene"));
    }
    
    public static void setRegistrationNewResturantScene() {
    	NewRestaurantController controller = (NewRestaurantController)App.getController("newRestaurantScene");
    	controller.initializeScene();  
    	scene.setRoot(getParent("newRestaurantScene"));
    }
    
    public static void setPastReviewScene() {
    	HistoryReviewsController controller = (HistoryReviewsController)App.getController("historyReviewsScene");
    	scene.setRoot(getParent("historyReviewsScene"));
    	controller.initializeScene();    	
    }
    
    public static void setAdministatorLoggedScene() {
    	scene.setRoot(getParent("administratorLoggedScene"));
    }
    
    public static void closeApp() {
    	stage.close();
    }
    
    @Override
    public void stop() {
    	App.database.closeMongoDBConnection();
    }
    
	public static DatabaseManager getDatabase() {
		return database;
	}
	
    public static void main(String[] args) {
        launch();
   
    }
}