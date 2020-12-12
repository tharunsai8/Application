package client.controller;

import client.PersonGateway;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
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
    @FXML
    private Button searchButton;
    @FXML
    private Button firstButton;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button lastButton;
    @FXML
    private TextField searchTextField;
    @FXML
    private Label fetchLabel;

    private ObservableList<Person> persons;
    private int currentPageNum;
    private FetchResult fetchResult;
    private boolean searching;

    public PersonListController() {
        currentPageNum = 1;
        fetchResult = PersonGateway.fetchPeople(currentPageNum, "");
        persons = FXCollections.observableArrayList(fetchResult.getPeople());
        searching = false;
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

    @FXML
    void searchPerson(ActionEvent event){
        System.out.println("search");
        fetchResult = PersonGateway.fetchPeople(1, searchTextField.getText());
        personListView.setItems(FXCollections.observableArrayList(fetchResult.getPeople()));
        showFetchLabel();
        handleButtons();
        searching = !searchTextField.getText().equals("");
    }

    private void handleButtons(){
        if(fetchResult.getNumRows() == 0){
            firstButton.setDisable(true);
            prevButton.setDisable(true);
            nextButton.setDisable(true);
            lastButton.setDisable(true);
        }else{
            firstButton.setDisable(false);
            if(currentPageNum == 1)
                prevButton.setDisable(true);
            else
                prevButton.setDisable(false);
            if(currentPageNum*10 > fetchResult.getNumRows())
                nextButton.setDisable(true);
            else
                nextButton.setDisable(false);
            lastButton.setDisable(false);
        }
    }

    @FXML
    void getFirstPage(ActionEvent event){
        System.out.println("first");
        currentPageNum = 1;
        if(searching)
            fetchResult = PersonGateway.fetchPeople(currentPageNum, searchTextField.getText());
        else
            fetchResult = PersonGateway.fetchPeople(currentPageNum, "");
        personListView.setItems(FXCollections.observableArrayList(fetchResult.getPeople()));
        showFetchLabel();
        handleButtons();
    }

    @FXML
    void getPrevPage(ActionEvent event){
        System.out.println("prev");
        if(searching)
            fetchResult = PersonGateway.fetchPeople(--currentPageNum, searchTextField.getText());
        else
            fetchResult = PersonGateway.fetchPeople(--currentPageNum, "");
        personListView.setItems(FXCollections.observableArrayList(fetchResult.getPeople()));
        showFetchLabel();
        handleButtons();
    }

    @FXML
    void getNextPage(ActionEvent event){
        System.out.println("next");
        if(searching)
            fetchResult = PersonGateway.fetchPeople(++currentPageNum, searchTextField.getText());
        else
            fetchResult = PersonGateway.fetchPeople(++currentPageNum, "");
        personListView.setItems(FXCollections.observableArrayList(fetchResult.getPeople()));
        showFetchLabel();
        handleButtons();
    }

    @FXML
    void getLastPage(ActionEvent event){
        System.out.println("last");
        currentPageNum = fetchResult.getNumRows() / 10 + 1;
        if(searching)
            fetchResult = PersonGateway.fetchPeople(currentPageNum, searchTextField.getText());
        else
            fetchResult = PersonGateway.fetchPeople(currentPageNum, "");
        personListView.setItems(FXCollections.observableArrayList(fetchResult.getPeople()));
        showFetchLabel();
        handleButtons();
    }

    private void showFetchLabel(){
        int start = currentPageNum *10 - 9;
        int stop = Math.min(currentPageNum * 10, fetchResult.getNumRows());

        if(fetchResult.getNumRows() == 0)
            fetchLabel.setText("Fetched 0 records");
        else
            fetchLabel.setText("Fetched records " + start + " to " + stop + " out of " + fetchResult.getNumRows() + " records");
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        personListView.setItems(persons);
        showFetchLabel();
        handleButtons();
    }
}