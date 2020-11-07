package assignment3.database;

import org.json.JSONArray;
import org.json.JSONObject;


public class GatewayException extends RuntimeException {

    JSONArray errors;
    boolean needToThrow;

    public GatewayException(){
        errors = new JSONArray();
        needToThrow = false;
    }

    public void addError(String error){
        errors.put(new JSONObject().put("Error", error));
        needToThrow = true;
    }

    public JSONArray getErrors() {
        return errors;
    }
}
