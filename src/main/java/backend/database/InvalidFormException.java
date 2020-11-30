package backend.database;

import org.json.JSONArray;
import org.json.JSONObject;


public class InvalidFormException extends RuntimeException {

    JSONArray errors;
    boolean needToThrow;

    public InvalidFormException(){
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
