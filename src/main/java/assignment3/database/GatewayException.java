package assignment3.database;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GatewayException extends RuntimeException {

    JSONArray errors;
    boolean needToThrow;

    public GatewayException(Exception e) {
        super(e);
    }

    public GatewayException(String msg) {
        super(msg);
    }

    public GatewayException(){
        errors = new JSONArray();
        needToThrow = false;
    }

    public GatewayException addError(String error){
        errors.put(new JSONObject().put("Error", error));
        needToThrow = true;
        return this;
    }

    public JSONArray getErrors() {
        return errors;
    }
}
