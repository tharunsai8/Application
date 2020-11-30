package client.controller;

import client.SessionGateway;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

import client.view.*;

public class LoginController implements Initializable {
    private static Logger logger = LogManager.getLogger();

    @FXML
    private Button loginButton;

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordField;

    public LoginController(){}

    @FXML
    void login(ActionEvent event) {
        SessionGateway sessionGateway = new SessionGateway(usernameTextField.getText(), passwordField.getText());

        if (sessionGateway.getStatusCode() == 200) {
            logger.info(usernameTextField.getText() + " LOGGED IN");
            ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
        } else if(sessionGateway.getStatusCode() == 0){
            logger.info("Unable to connect to server");
            ViewSwitcher.getInstance().dialogPopup("Connection Error", "Unable to connect to server.");
        } else if(sessionGateway.getStatusCode() == 500){
            logger.info("Something when wrong on the server");
            ViewSwitcher.getInstance().dialogPopup("Unknown Error", "Something when wrong on the server.");
        }else {
            logger.info("INVALID CREDENTIALS");
            ViewSwitcher.getInstance().dialogPopup("Invalid Login", "You have failed to login.");
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

    }

}
