package backend;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class DateDeserializer extends JsonDeserializer<Date> {
	
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.readValueAsTree();

        JsonNode date = node.get("$date");         
        if (date != null) {
        	return new Date(date.asLong());
        }
       
    	try {
			return formatter.parse(node.asText());
		} catch (ParseException e) {
			e.printStackTrace();
		}   
		
        return null;
    }
}