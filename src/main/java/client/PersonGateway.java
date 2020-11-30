package client;

import client.model.AuditRecord;
import client.model.Person;
import client.model.PersonParameters;
import client.view.ViewSwitcher;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PersonGateway {
    private static final String WS_URL = "http://localhost:8080";
    private static final Logger logger = LogManager.getLogger();
    private static CloseableHttpResponse response;
    private static CloseableHttpClient httpclient;

    public PersonGateway(){
    }

    public static ArrayList<Person> fetchPeople(){
        ArrayList<Person> personList = new ArrayList<>();
        try{
            httpclient = HttpClients.createDefault();
            HttpGet personRequest = new HttpGet(WS_URL + "/people");
            personRequest.setHeader("Authorization", ViewSwitcher.getSessionToken());
            personRequest.setHeader("Accept", "application/json");

            // make the request and check the responses
            response = httpclient.execute(personRequest);
            if (handleErrors(response)){
                closeStuff();
                return personList;
            }

            String responseString = getResponseAsString(response);
            if(responseString != null)
                makePersonList(personList, responseString);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStuff();
        }
        return personList;
    }


    private static void makePersonList(ArrayList<Person> personList, String responseString) {
        JSONArray jsonArray = new JSONArray(responseString);
        for(Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            personList.add(new Person(jsonObject.getInt("id"), jsonObject.getString("first_name"), jsonObject.getString("last_name"), jsonObject.getString("date_of_birth")));
        }
    }

    public static Boolean deletePerson(){
        try{
            httpclient = HttpClients.createDefault();
            HttpDelete deleteRequest = new HttpDelete(WS_URL + "/people/" + PersonParameters.getPersonParam().getId());
            deleteRequest.setHeader("Authorization", ViewSwitcher.getSessionToken());
            deleteRequest.setHeader("Accept", "application/json");

            // make the request and check the responses
            response = httpclient.execute(deleteRequest);
            if (handleErrors(response)){
                closeStuff();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStuff();
        }
        return true;
    }

    public static int insertPerson(){
        int newPersonId = 0;
        try{
            httpclient = HttpClients.createDefault();
            HttpPost insertRequest = new HttpPost(WS_URL + "/people");

            Person person = PersonParameters.getPersonParam();
            JSONObject newPerson = new JSONObject();
            newPerson.put("first_name", person.getFirstName());
            newPerson.put("last_name", person.getLastName());
            newPerson.put("date_of_birth", person.getDateOfBirth());
            String newPersonString = newPerson.toString();
            logger.info("new person: " + newPersonString);

            StringEntity reqEntity = new StringEntity(newPersonString);

            insertRequest.setEntity(reqEntity);
            insertRequest.setHeader("Authorization", ViewSwitcher.getSessionToken());
            insertRequest.setHeader("Accept", "application/json");
            insertRequest.setHeader("Content-type", "application/json");

            response = httpclient.execute(insertRequest);
            String responseString = getResponseAsString(response);
            if (responseString.charAt(0) == '{') {
                JSONObject responseJSON = new JSONObject(responseString);
                newPersonId = Integer.parseInt(responseJSON.getString("id"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStuff();
        }
        return newPersonId;
    }


    public static void updatePerson(byte bitmap){
        try{
            Person person = PersonParameters.getPersonParam();
            httpclient = HttpClients.createDefault();
            HttpPut updateRequest = new HttpPut(WS_URL + "/people/" + person.getId());

            JSONObject personJSON = new JSONObject();
            if((bitmap & 0x01) == 0x01)
                personJSON.put("first_name", person.getFirstName());
            if((bitmap & 0x02) == 0x02)
                personJSON.put("last_name", person.getLastName());
            if((bitmap & 0x04) == 0x04)
                personJSON.put("date_of_birth", person.getDateOfBirth());

            String updatePersonString = personJSON.toString();
            logger.info("update person: " + updatePersonString);

            StringEntity reqEntity = new StringEntity(updatePersonString);

            updateRequest.setEntity(reqEntity);
            updateRequest.setHeader("Authorization", ViewSwitcher.getSessionToken());
            updateRequest.setHeader("Accept", "application/json");
            updateRequest.setHeader("Content-type", "application/json");

            response = httpclient.execute(updateRequest);
            handleErrors(response);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStuff();
        }
    }

    public static ArrayList<AuditRecord> fetchAuditTrail(){
        ArrayList<AuditRecord> auditTrail = new ArrayList<>();
        httpclient = HttpClients.createDefault();
        HttpGet auditRequest = new HttpGet(WS_URL + "/people/" + PersonParameters.getPersonParam().getId() + "/audittrail");
        auditRequest.setHeader("Authorization", ViewSwitcher.getSessionToken());
        auditRequest.setHeader("Accept", "application/json");

        try {
            response = httpclient.execute(auditRequest);

            if (handleErrors(response)){
                closeStuff();
                return auditTrail;
            }

            String responseString = getResponseAsString(response);
            if(responseString != null){
                makeAuditTrail(auditTrail, responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStuff();
        }
        return auditTrail;
    }

    private static void makeAuditTrail(ArrayList<AuditRecord> auditTrail, String responseString){
        JSONArray jsonArray = new JSONArray(responseString);
        for(Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            auditTrail.add(new AuditRecord(jsonObject.getString("change_msg"), jsonObject.getString("changed_by"), jsonObject.getString("when_occurred")));
        }
    }

    private static String getResponseAsString(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        EntityUtils.consume(entity);

        logger.info("Fetch response: " + responseString);
        return responseString;
    }

    private static boolean handleErrors(CloseableHttpResponse response){
        if(response.getStatusLine().getStatusCode() == 400) {
            logger.error("400: Bad Request");
            return true;
        }
        if(response.getStatusLine().getStatusCode() == 401) {
            logger.error("401: Unauthorized");
            return true;
        }
        if(response.getStatusLine().getStatusCode() != 200) {
            logger.error("Non-200 status code returned: " + response.getStatusLine());
            return true;
        }
        return false;
    }

    private static void closeStuff() {
        try {
            if(response != null)
                response.close();
            if(httpclient != null)
                httpclient.close();
        } catch(IOException e) {
            // ignore
        }
    }
}
