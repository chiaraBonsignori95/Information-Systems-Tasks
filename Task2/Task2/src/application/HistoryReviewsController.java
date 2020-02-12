package application;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import application.ParametersFactory.helpType;
import backend.DatabaseManager;
import backend.exc.DatabaseManagerException;
import backend.modules.Customer;
import backend.modules.Reply;
import backend.modules.Review;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class HistoryReviewsController extends Controller {
	
	@FXML
	private TextField title;
	@FXML
	private TextField restaurantName;
	@FXML
	private TextField score;
	@FXML
	private TextField date;
	@FXML
	private TextArea textReview;
	@FXML
	private TextField restaurateurUsername;
	@FXML
	private TextArea textReply;
	@FXML
	ListView<Label> reviewsViewer;
	
	private String city;
	private Customer customer;
	private List<Review> listReviews;
	private Review review;
	
	public void initializeScene() {
		initializeCustomer();
		cleanScene();
		if(customer.getReviews() == null) {
			popoutMessage(AlertType.INFORMATION, "Attenzione", "Non hai ancora nessuna recensione", "");
			App.setCustomerScene(city);
			return;
		} else if(customer.getReviews().isEmpty()) {
			popoutMessage(AlertType.INFORMATION, "Attenzione", "Non hai ancora nessuna recensione", "");
			App.setCustomerScene(city);
			return;
		}
		
		initializeReviewsViewer();
		loadReviewInformation();
	}

	private void cleanScene() {
		title.setText("");
		restaurantName.setText("");
		score.setText("");
		date.setText("");
		textReview.setText("");
		restaurateurUsername.setText("");
		textReply.setText("");
		reviewsViewer.getItems().clear();
		
	}

	private void initializeCustomer() {
		CustomerController controller = (CustomerController)App.getController("customerScene");
		customer = controller.getCustomer();
		city = controller.getCity();
		listReviews = customer.getReviews();
	}

	private void loadReviewInformation() {
		
		title.setText(review.getTitle());
		restaurantName.setText(review.getRestaurant());
		score.setText(Double.toString(review.getRating()));
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ITALY);
		date.setText(df.format(review.getDate()));
		textReview.setText(review.getText());
		
		Reply reply = review.getReply();
		
		if (reply != null) {
			restaurateurUsername.setText(reply.getOwner());
			textReply.setText(reply.getText());
		} else {
			restaurateurUsername.setText("");
			textReply.setText("");
		}
	}

	private void initializeReviewsViewer() {
		
		reviewsViewer.getItems().clear();
		
		int idLabel = 0;
		
		for (Review r : listReviews) {
			Label label = new Label(r.getTitle() + "\n\t" + r.getRestaurant());
			label.setId(Integer.toString(idLabel++));
			label.setCursor(Cursor.HAND);
			label.setOnMouseClicked(event -> {
				
				Label item = (Label)event.getSource();
				int index = Integer.parseInt(item.getId());
				review = listReviews.get(index);
				
				loadReviewInformation();
				 
			});
			
			reviewsViewer.getItems().add(label);
		}
		
		review = listReviews.get(0);
	}
	
	@FXML
	public void onDeleteReviewRequest() {
		
		if (!confirmationPopoutMessage("Cancellazione", "Sei veramente sicuro di voler cancellare la recensione?", ""))
			return;
		
		DatabaseManager database = App.getDatabase();
		
		try {
			database.deleteReview(review);
		} catch (DatabaseManagerException e) {
			popoutMessage(AlertType.ERROR, "Attenzione", "Si è verificato un errore, l'applicazione verrà chiusa", "");
			App.closeApp();
			return;
		}
		
		customer.getReviews().remove(review);
		
		popoutMessage(AlertType.INFORMATION, "Complimenti", "La tua recensione è stata eliminata con successo", "");
		initializeScene();
		
	}		

	@FXML
	public void onPreviousPageRequest() {
		App.setCustomerScene(city);
	}
	
	@FXML
	public void onHelpRequest() {
		helpWindow(helpType.PAST_REVIEWS_SCENE);
	}

}
