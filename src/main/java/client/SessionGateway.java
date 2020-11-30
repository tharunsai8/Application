package client;

import client.view.ViewSwitcher;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

public class SessionGateway {
    private static final String WS_URL = "http://localhost:8080";
    private static final Logger logger = LogManager.getLogger();

    private int statusCode = 401;

    public SessionGateway(String username, String password){
        authenticate(username, password);

    }

    private void authenticate(String username, String password){
        CloseableHttpResponse response = null;
        CloseableHttpClient httpclient = null;
        try{
            httpclient = HttpClients.createDefault();
            HttpPost loginRequest = getLoginRequest(username, password);

            response = httpclient.execute(loginRequest);
            String responseString = getResponseAsString(response);
            statusCode = response.getStatusLine().getStatusCode();

            if (handleErrors(response)){
                response.close();
                httpclient.close();
                return;
            }

            getSessionToken(responseString);

        } catch (ConnectException e){
            statusCode = 0;
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null)
                    response.close();
                if(httpclient != null)
                    httpclient.close();
            } catch(IOException e) {
                // ignore
            }
        }
    }

    private HttpPost getLoginRequest(String username, String password) throws UnsupportedEncodingException {
        // assemble credentials into a JSON encoded string
        JSONObject credentials = new JSONObject();
        credentials.put("username", username);
        credentials.put("password", password);
        String credentialsString = credentials.toString();
        logger.info("credentials: " + credentialsString);

        HttpPost loginRequest = new HttpPost(WS_URL + "/login");
        // this requires setting it up as a request entity where we can describe what the text is
        StringEntity reqEntity = new StringEntity(credentialsString);
        loginRequest.setEntity(reqEntity);
        loginRequest.setHeader("Accept", "application/json");
        loginRequest.setHeader("Content-type", "application/json");
        return loginRequest;
    }

    private String getResponseAsString(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        // use org.apache.http.util.EntityUtils to read json as string
        String responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        EntityUtils.consume(entity);

        logger.info("Login response as a string: " + responseString);
        return responseString;
    }

    private static boolean handleErrors(CloseableHttpResponse response){
        // a special response for invalid credentials
        if(response.getStatusLine().getStatusCode() == 401) {
            logger.error("401: I DON'T KNOW YOU!!!");
            return true;
        }
        if(response.getStatusLine().getStatusCode() != 200) {
            logger.error("Non-200 status code returned: " + response.getStatusLine());
            return true;
        }
        return false;
    }

    private void getSessionToken(String responseString){
        JSONObject responseJSON = new JSONObject(responseString);
        String token = responseJSON.getString("session_id");

        logger.info("Session token: " + token);

        ViewSwitcher.setSessionToken(token);
    }

    public int getStatusCode() {
        return statusCode;
    }
}
