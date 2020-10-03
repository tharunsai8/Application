package assignment2.view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import assignment2.controller.LoginController;
import assignment2.controller.PersonDetailController;
import assignment2.controller.PersonListController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;

import assignment2.model.*;

public class ViewSwitcher implements Initializable{
    private static ViewSwitcher instance = null;
    private static String sessionToken;

    @FXML
    private BorderPane rootPane;

    private ViewSwitcher() {
    }

    public static ViewSwitcher getInstance() {
        if(instance == null)
            instance = new ViewSwitcher();
        return instance;
    }

    public void switchView(ViewType viewType) {
        FXMLLoader loader = null;

        try {
            switch(viewType) {
                case LoginView:
                    loader = new FXMLLoader(ViewSwitcher.class.getResource("/loginview.fxml"));
                    loader.setController(new LoginController());
                    break;

                case PersonDetailView:
                    loader = new FXMLLoader(ViewSwitcher.class.getResource("/persondetailview.fxml"));
                    loader.setController(new PersonDetailController(PersonParameters.getPersonParam()));
                    break;

                case PersonListView:
                    loader = new FXMLLoader(ViewSwitcher.class.getResource("/personlistview.fxml"));
                    loader.setController(new PersonListController());
                    break;
                default:
                    break;
            }
            Parent rootNode = loader.load();
            rootPane.setCenter(rootNode);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dialogPopup(String title, String message){
        Dialog<String> dialog = new Dialog<String>();
        dialog.setTitle(title);
        ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(type);
        dialog.setContentText(message);
        dialog.showAndWait();
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        switchView(ViewType.LoginView);
    }

    public static String getSessionToken() {
        return sessionToken;
    }

    public static void setSessionToken(String sessionToken) {
        ViewSwitcher.sessionToken = sessionToken;
    }
}
