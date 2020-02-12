package application;


import java.net.URL;
import java.util.ResourceBundle;

import application.ParametersFactory.helpType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


public class FirstSceneController extends Controller implements Initializable {
	
	@FXML
	private VBox resturantViewer;
    
    private void initializeAvailableCity() {
    	String [] cities = {"Pisa","Firenze","Livorno","Siena","Arezzo"};
		
		for ( String city : cities) {
			Label cityLabel = new Label(city);
			cityLabel.getStyleClass().add("general-button");
			cityLabel.getStyleClass().add("city-button");
			cityLabel.setCursor(Cursor.HAND);
			cityLabel.setOnMouseClicked(event -> {
				
				Label item = (Label)event.getSource();
				App.setCustomerScene(item.getText());
				
			});
			resturantViewer.getChildren().add(cityLabel);	
			
		}
    }
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initializeAvailableCity();		
	}
	
    @FXML
    public void onLoginAsRestaurantOwner() {
    	App.setRestaurantOwnerScene();
    }
    
    @FXML
    public void onLoginAsAdministrator() {
    	App.setAmministratorScene();
    }
    
    @FXML
    public void onHelpRequest() {
    	helpWindow(helpType.FIRST_SCENE);
    }
    
}
