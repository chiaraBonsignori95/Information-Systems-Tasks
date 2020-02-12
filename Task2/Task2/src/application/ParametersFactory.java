package application;

public class ParametersFactory {
	
	public static enum helpType {
		FIRST_SCENE,
		CUSTOMER_SCENE,
		PAST_REVIEWS_SCENE,
		RESTAURATEUR_SCENE,
		RESTAURATEUR_LOGGED_SCENE,
		ADMINISTRATOR_SCENE,
		ADMINISTRATOR_LOGGED_SCENE,
		NEW_RESTAURANT_SCENE
	};
	
	private static String [] helpText = {
			//FIRST_SCENE
			"Seleziona la città dove vuoi mangiare.\n" + 
			"Attraverso il menu Naviga puoi accedere come ristoratore o come amministratore.",
			
			//CUSTOMER_SCENE
			"Puoi ricercare i ristoranti per nome, per categoria o per entrambi.\n\n" + 
			"Puoi visualizzare la distribuzione delle recensioni di un ristorante da una determinata data " +
			"fino ad oggi selezionandola tramite l'apposito menù.\n\n" +
			"Se non sei ancora registrato puoi registrati tramite l'apposito pulsante situato nel menù Naviga.\n\n" + 
			"Puoi effetture l'accesso tramite il pulsante login collocato nella schermata principale oppure " + 
			"tramite il pulsante nel menù Naviga.\n\n" +
			"Una volta effettuato l'accesso puoi visualizzare le tue recensioni passate.\n\n" +
			"Una volta effettuato l'accesso puoi decidere di eliminare il tuo account.",
			
			//PAST_REVIEWS_SCENE
			"Puoi visualizzare le tue recensioni passate oppure decidere di eliminarle.",
			
			//RESTAURATEUR_SCENE
			"Poi effettuare l'accesso oppure se non sei ancora registrato puoi farlo tramite la schermata principale "+ 
			"o tramite il pulsante che trovi nel menù Naviga.",
			
			//RESTAURATEUR_LOGGED_SCENE
			"Nella sezione \"I miei ristoranti\" puoi visualizzare le recensioni dei clienti ed eventualmente rispondere, " +
			"cancellare un ristorante oppure modificarlo.\n\n"+
			"Nella sezione \"Ristoranti da approvare\" poi controllare i ristoranti che non sono ancora stati approvati.\n\n" +
			"Nella sezione \"Mostra trend\" è presente un grafico che mostra l'andamento dalle recensioni per il proprio ristorante.\n\n" +
			"Nella sezione \"Confronto\" sono forniti dei grafici che mostrano l'andamento del proprio ristorante rispetto alla concorrenza.",
			
			//ADMINISTRATOR_SCENE
			"Puoi effettuare l'accesso come amministratore.",
			
			//ADMINISTRATOR_LOGGED
			"Nella sezione \"Cancella Utente\" puoi cancellare un ristoratore o un cliente.\n\n" +
			"Nella sezione \"Richieste Pendenti\" puoi approvare o rifiutare una richiesta di " +
			"inserimento di un ristorante da parte di un ristoratore.\n\n" +
			"Nella sezione \"Inserisci Nuovo Amministratore\" puoi inserire nuovi amministratori di sistema.\n\n" +
			"Nella sezione \"Cancella Recensione\" è possibile cancellare delle recensioni segnalate.\n\n" +
			"Nella sezione \"Inserisci Ristoranti\" è possibile inserire nuovi ristoranti nel sistema.",
			
			//NEW_RESTAURANT
			"Puoi inserire le informazioni neccessarie all'invio di una nuova richiesta di registrazione del ristorante."
	};
	
	private static String [] categoriesNamePlusNessunaCategoria = {
			
			"Nessuna Categoria",
			"Americana",
			"Araba",
			"Argentina",
			"Asiatica",
			"Bar",
			"Barbecue",
			"Birreria",
			"Caffè",
			"Campana",
			"Centro americana",
			"Cibo di strada",
			"Cinese",
			"Contemporanea",
			"Diner",
			"Emiliana",
			"Europea",
			"Fast food",
			"Francese",
			"Fusion",
			"Gastronomia",
			"Gastropub",
			"Giapponese",
			"Greca",
			"Grill",
			"Indiana",
			"Internazionale",
			"Italiana",
			"Italiana (centro)",
			"Italiana (nord)",
			"Italiana (sud)",
			"Libanese",
			"Ligure",
			"Marocchina",
			"Mediorientale",
			"Mediterranea",
			"Messicana",
			"Napoletana",
			"Pesce",
			"Pizza",
			"Pub",
			"Ristoranti con bar",
			"Salutistica",
			"Steakhouse",
			"Sushi",
			"Toscana",
			"Turca",
			"Wine Bar",
			"Zuppe"
	};
	
	private static String [] categoriesName = {

			"Americana",
			"Araba",
			"Argentina",
			"Asiatica",
			"Bar",
			"Barbecue",
			"Birreria",
			"Caffè",
			"Campana",
			"Centro americana",
			"Cibo di strada",
			"Cinese",
			"Contemporanea",
			"Diner",
			"Emiliana",
			"Europea",
			"Fast food",
			"Francese",
			"Fusion",
			"Gastronomia",
			"Gastropub",
			"Giapponese",
			"Greca",
			"Grill",
			"Indiana",
			"Internazionale",
			"Italiana",
			"Italiana (centro)",
			"Italiana (nord)",
			"Italiana (sud)",
			"Libanese",
			"Ligure",
			"Marocchina",
			"Mediorientale",
			"Mediterranea",
			"Messicana",
			"Napoletana",
			"Pesce",
			"Pizza",
			"Pub",
			"Ristoranti con bar",
			"Salutistica",
			"Steakhouse",
			"Sushi",
			"Toscana",
			"Turca",
			"Wine Bar",
			"Zuppe"
	};
	
	private static String [] optionsName = {
			
			"Halal",
			"Opzioni senza glutine",
			"Opzioni vegane",
			"Per vegetariani"
	};
	
	private static String [] featuresName = {
			
			"Accessibile in sedia a rotelle",
			"Accetta Mastercard",
			"Accetta Visa",
			"Accetta carte di credito",
			"Area parcheggio gratuita",
			"Bar completo",
			"Connessione Wi-Fi gratuita",
			"Da asporto",
			"Parcheggio disponibile",
			"Parcheggio in strada",
			"Parcheggio validato",
			"Posti a sedere",
			"Riserve",
			"Seggioloni disponibili",
			"Serve alcolici",
			"Servizio al tavolo",
			"Servizio parcheggio",
			"Tavoli all'esterno",
			"Televisore",
			"Vino e birra"

	};
	
	private static String[] days = {
			"Lunedì",
			"Martedì",
			"Mercoledì",
			"Giovedì",
			"Venerdì",
			"Sabato",
			"Domenica"
	};
	
	private static String [] showTrendIntervals = {
			
			"Una settimana",
			"Due settimane",
			"Un mese",
			"Tre mesi",
			"Sei mesi",
			"Un anno"
	};
	
	public static int fromStringToIntInterval(String interval) {
		
		final int week = 7;
		final int month = 30;
		final int year = 365;
		
		if(interval == showTrendIntervals[0])
			return week;
		if(interval == showTrendIntervals[1])
			return week * 2;
		if(interval == showTrendIntervals[2])
			return month;
		if(interval == showTrendIntervals[3])
			return month * 3;
		if(interval == showTrendIntervals[4])
			return month * 6;
		if(interval == showTrendIntervals[5])
			return year;
		return -1;
	}

	public static String [] getCategoriesName() {
		return categoriesName;
	}

	public static String [] getOptionsName() {
		return optionsName;
	}

	public static String [] getFeaturesName() {
		return featuresName;
	}

	public static String[] getDays() {
		return days;
	}

	public static String [] getCategoriesNamePlusNessunaCategoria() {
		return categoriesNamePlusNessunaCategoria;
	}

	public static String [] getShowTrendIntervals() {
		return showTrendIntervals;
	}
	
	public static String getHelpText(helpType type) {
		switch(type) {
			case FIRST_SCENE:
				return helpText[0];
			case CUSTOMER_SCENE:
				return helpText[1];
			case PAST_REVIEWS_SCENE:
				return helpText[2];
			case RESTAURATEUR_SCENE:
				return helpText[3];				
			case RESTAURATEUR_LOGGED_SCENE:
				return helpText[4];
			case ADMINISTRATOR_SCENE:
				return helpText[5];
			case ADMINISTRATOR_LOGGED_SCENE:
				return helpText[6];
			case NEW_RESTAURANT_SCENE:
				return helpText[7];
			default:
				return "";
		
		}
	}

}
