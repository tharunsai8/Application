package assignment2.controller;

import assignment2.SessionGateway;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

import assignment2.view.*;

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
        } else {
            logger.info("INVALID CREDENTIALS");
            ViewSwitcher.getInstance().dialogPopup("Invalid Login", "You have failed to login.");
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

    }

}
