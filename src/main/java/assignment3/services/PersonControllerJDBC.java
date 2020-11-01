package assignment3.services;

import assignment2.model.Person;
import assignment3.database.DBConnect;
import assignment3.database.GatewayException;
import assignment3.database.PersonGatewayMySQL;
import assignment3.database.SessionGatewayMySQL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

@RestController
public class PersonControllerJDBC {
    private static final Logger logger = LogManager.getLogger();
    private Connection connection;

    // create a connection on startup
    @PostConstruct
    public void startup() {
        connection = DBConnect.getInstance().getConnection();
    }

    // close a connection on shutdown
    @PreDestroy
    public void cleanup() {
        try {
            connection.close();
            logger.info("*** MySQL connection closed");
        } catch (SQLException e) {
            logger.error("*** Connection already closed" + e);
        }
    }

    @GetMapping("/people")
    public ResponseEntity<String> fetchPeople(@RequestHeader Map<String, String> headers){
        try{
            if(!new SessionGatewayMySQL(connection).verifyToken(getSessionToken(headers)))
                return new ResponseEntity<>("", HttpStatus.valueOf(401));

            JSONArray peopleJSON = new JSONArray();
            ArrayList<Person> people = new PersonGatewayMySQL(connection).fetchPeople();
            for (Person p : people){
                JSONObject personJSON = new JSONObject();
                personJSON.put("id", p.getId());
                personJSON.put("firstName", p.getFirstName());
                personJSON.put("lastName", p.getLastName());
                personJSON.put("dateOfBirth", p.getDateOfBirth());

                peopleJSON.put(personJSON);
            }
            return new ResponseEntity<>(peopleJSON.toString(), HttpStatus.valueOf(200));
        } catch (SQLException e){
            return new ResponseEntity<>("", HttpStatus.valueOf(500));
        }
    }

    @PostMapping("/people")
    public ResponseEntity<String> insertPerson(@RequestHeader Map<String, String> headers,
                                               @RequestBody Map<String, String> personForm){
        try{
            if(!new SessionGatewayMySQL(connection).verifyToken(getSessionToken(headers)))
                return new ResponseEntity<>("", HttpStatus.valueOf(401));

            int id = new PersonGatewayMySQL(connection).insertPerson(personForm);
            JSONObject personId = new JSONObject();
            personId.put("id", id);
            return new ResponseEntity<>(personId.toString(), HttpStatus.valueOf(200));
        } catch (GatewayException | SQLException e){
            if (e instanceof GatewayException)
                return new ResponseEntity<>(((GatewayException) e).getErrors().toString(), HttpStatus.valueOf(400));
            else
                return new ResponseEntity<>( "", HttpStatus.valueOf(500));
        }
    }

    @PutMapping("/people/{id}")
    public ResponseEntity<String> updatePerson(@RequestHeader Map<String, String> headers,
                                               @RequestBody Map<String, String> personForm,
                                               @PathVariable("id") int personId){
        try {
            if (!new SessionGatewayMySQL(connection).verifyToken(getSessionToken(headers)))
                return new ResponseEntity<>("", HttpStatus.valueOf(401));

            new PersonGatewayMySQL(connection).updatePerson(personForm, personId);
            return new ResponseEntity<>("", HttpStatus.valueOf(200));
        } catch (GatewayException | SQLException e){
            if (e instanceof GatewayException)
                return new ResponseEntity<>(((GatewayException) e).getErrors().toString(), HttpStatus.valueOf(400));
            else
                return new ResponseEntity<>( "", HttpStatus.valueOf(404));
            //TODO SQLException should return 500
        }
    }

    @DeleteMapping("/people/{id}")
    public ResponseEntity<String> deletePerson(@RequestHeader Map<String, String> headers,
                                               @PathVariable("id") int personId){
        try{
            if (!new SessionGatewayMySQL(connection).verifyToken(getSessionToken(headers)))
                return new ResponseEntity<>("", HttpStatus.valueOf(401));

            new PersonGatewayMySQL(connection).deletePerson(personId);
            return new ResponseEntity<>("", HttpStatus.valueOf(200));
        } catch (SQLException e) {
            //TODO SQLException should return 500
            return new ResponseEntity<>( "", HttpStatus.valueOf(404));
        }
    }

    @GetMapping("/people/{id}")
    public ResponseEntity<String> fetchPerson(@RequestHeader Map<String, String> headers,
                                              @PathVariable("id") int personId){
        try {
            if (!new SessionGatewayMySQL(connection).verifyToken(getSessionToken(headers)))
                return new ResponseEntity<>("", HttpStatus.valueOf(401));

            Person person = new PersonGatewayMySQL(connection).fetchPerson(personId);
            JSONObject personJSON = new JSONObject();
            personJSON.put("id", person.getId());
            personJSON.put("firstName", person.getFirstName());
            personJSON.put("lastName", person.getLastName());
            personJSON.put("dateOfBirth", person.getDateOfBirth());

            return new ResponseEntity<>(personJSON.toString(), HttpStatus.valueOf(200));
        } catch (SQLException e) {
            //TODO SQLException should return 500
            return new ResponseEntity<>( "", HttpStatus.valueOf(404));
        }
    }

    private String getSessionToken(Map<String, String> headers) {
        Set<String> keys = headers.keySet();
        for(String key : keys) {
            if(key.equalsIgnoreCase("authorization"))
                return headers.get(key);
        }
        return "";
    }
}
