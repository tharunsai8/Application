package assignment3.services;

import assignment3.database.DBConnect;
import assignment3.database.SessionGatewayMySQL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@RestController
public class LoginControllerJDBC {
    private static final Logger logger = LogManager.getLogger();

    private Connection connection;

    // create a connection on startup
    @PostConstruct
    public void startup() {
        try {
            connection = DBConnect.getInstance().connectToDB().getConnection();
            logger.info("*** MySQL connection created");
        } catch (SQLException | IOException e) {
            logger.error("*** " + e);
            System.exit(0);
        }
    }

    // close a connection on shutdown
    @PreDestroy
    public void cleanup() {
        try {
            connection.close();
            logger.info("*** MySQL connection closed");
        } catch (SQLException e) {
            logger.error("*** " + e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> loginForm){
        try{
            SessionGatewayMySQL sessionGateway = new SessionGatewayMySQL(connection);
            if(sessionGateway.authenticateUser(loginForm.get("username"), loginForm.get("password"))){
                String token = "i am a session token";
                sessionGateway.insertToken(loginForm.get("username"), token);
                return new ResponseEntity<>("{\"session_id\":\"" + token + "\"}", HttpStatus.valueOf(200));
            } else {
                return new ResponseEntity<>("", HttpStatus.valueOf(401));
            }
        } catch (SQLException e){
            return new ResponseEntity<>("", HttpStatus.valueOf(500));
        }
    }
}
