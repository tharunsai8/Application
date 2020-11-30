package client.controller;

import client.PersonGateway;
import client.model.AuditRecord;
import client.model.PersonParameters;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import client.view.*;
import client.model.Person;

public class PersonDetailController implements Initializable {
    private static Logger logger = LogManager.getLogger();

    @FXML
    private TextField firstNameTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField ageTextField;
    @FXML
    private TextField dateOfBirthTextField;
    @FXML
    private TableView<AuditRecord> auditTrailTableView;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancel;

    private Person person;
    private ArrayList<AuditRecord> auditTrail;

    public PersonDetailController(Person person){
        this.person = new Person(person.getId(), person.getFirstName(), person.getLastName(), person.getDateOfBirth());
        if(this.person.getId() == 0)
            auditTrail = new ArrayList<>();
        else
            auditTrail = PersonGateway.fetchAuditTrail();
    }

    @FXML
    void savePerson(ActionEvent event) {
        if (!checkForValidInput()) return;

        if(person.getId() == 0) {
            logger.info("CREATING " + firstNameTextField.getText() + " " + lastNameTextField.getText());
            createPerson();
        } else {
            logger.info("UPDATING " + this.person.getFirstName() + " " + this.person.getLastName());
            updatePerson();
        }
        ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
    }

    private boolean checkForValidInput() {
        try {
            if ((firstNameTextField.getText().length() < 1 || firstNameTextField.getText().length() > 100) ||
                    (lastNameTextField.getText().length() < 1 || lastNameTextField.getText().length() > 100) ||
                    (LocalDate.parse(dateOfBirthTextField.getText()).isAfter(LocalDate.now()))) {
                ViewSwitcher.getInstance().dialogPopup("Invalid Input", "First and last name must be between "
                        + "1 and 100 characters long. Date of birth must not be after current date");
                return false;
            }
        } catch (DateTimeParseException e){
            ViewSwitcher.getInstance().dialogPopup("Date Error", "Please use yyyy-mm-dd format");
            return false;
        }

        return true;
    }

    private void createPerson() {
        PersonParameters.setPersonParam(new Person(0, firstNameTextField.getText(), lastNameTextField.getText(), dateOfBirthTextField.getText()));
        PersonParameters.getPersonParam().setId(PersonGateway.insertPerson());
    }

    private void updatePerson() {
        Person newPerson = PersonParameters.getPersonParam();
        newPerson.setFirstName(firstNameTextField.getText());
        newPerson.setLastName(lastNameTextField.getText());
        newPerson.setDateOfBirth(dateOfBirthTextField.getText());
        PersonParameters.setPersonParam(newPerson);
        byte bitmap = 0x00;
        if (!newPerson.getFirstName().equals(this.person.getFirstName())){
            bitmap |= 0x01;
        }
        if (!newPerson.getLastName().equals(this.person.getLastName())){
            bitmap |= 0x02;
        }
        if (!newPerson.getDateOfBirth().equals(this.person.getDateOfBirth())){
            bitmap |= 0x04;
        }

        PersonGateway.updatePerson(bitmap);
    }

    @FXML
    void cancel(ActionEvent event) {
        ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        firstNameTextField.setText(person.getFirstName());
        lastNameTextField.setText(person.getLastName());
        dateOfBirthTextField.setText("" + person.getDateOfBirth());

        TableColumn<AuditRecord, String> column1 = new TableColumn<>("Date/Time");
        column1.setCellValueFactory(new PropertyValueFactory<>("whenOccurred"));
        TableColumn<AuditRecord, String> column2 = new TableColumn<>("By");
        column2.setCellValueFactory(new PropertyValueFactory<>("changedBy"));
        TableColumn<AuditRecord, String> column3 = new TableColumn<>("Description");
        column3.setCellValueFactory(new PropertyValueFactory<>("changeMsg"));

        auditTrailTableView.getColumns().add(column1);
        auditTrailTableView.getColumns().add(column2);
        auditTrailTableView.getColumns().add(column3);

        for(AuditRecord r: auditTrail){
            auditTrailTableView.getItems().add(r);
        }
    }
}
