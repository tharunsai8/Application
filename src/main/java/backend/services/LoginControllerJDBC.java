package backend.services;

import backend.database.DBConnect;
import backend.database.SessionGatewayMySQL;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

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
            int userId = sessionGateway.authenticateUser(loginForm.get("username"), loginForm.get("password"));
            if(userId != 0){
                String token = generateToken();
                sessionGateway.insertToken(userId, token);
                return new ResponseEntity<>("{\"session_id\":\"" + token + "\"}", HttpStatus.valueOf(200));
            } else {
                return new ResponseEntity<>("", HttpStatus.valueOf(401));
            }
        } catch (SQLException | NoSuchAlgorithmException e){
            return new ResponseEntity<>("", HttpStatus.valueOf(500));
        }
    }

    private String generateToken() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        StringBuilder token = new StringBuilder();
        for (byte aByte : digest.digest())
            token.append(Integer.toHexString(0xFF & aByte));
        return token.toString();
    }
}
