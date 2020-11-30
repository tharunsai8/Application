package client.controller;

import client.PersonGateway;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

import client.view.*;
import client.model.*;

public class PersonListController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    @FXML
    private Button addPersonButton;
    @FXML
    private Button deletePersonButton;
    @FXML
    private ListView<Person> personListView;

    private ObservableList<Person> persons;

    public PersonListController() {
        persons = FXCollections.observableArrayList(PersonGateway.fetchPeople());
    }

    @FXML
    void addPerson(ActionEvent event) {
        Person person = personListView.getSelectionModel().getSelectedItem();
        if (person == null){
            PersonParameters.setPersonParam(new Person());
        } else{
            PersonParameters.setPersonParam(person);
            logger.info("READING " + person.getFirstName() + " " + person.getLastName());
        }
        ViewSwitcher.getInstance().switchView(ViewType.PersonDetailView);
    }

    @FXML
    void deletePerson(ActionEvent event) {
        Person person = personListView.getSelectionModel().getSelectedItem();
        if (person != null) {
            logger.info("DELETING " + person.getFirstName() + " " + person.getLastName());
            PersonParameters.setPersonParam(person);
            if(PersonGateway.deletePerson()){
                personListView.getItems().remove(person);
            }
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        personListView.setItems(persons);
    }
}