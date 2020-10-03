package assignment2.controller;

import assignment2.PersonGateway;
import assignment2.model.PersonParameters;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import assignment2.view.*;
import assignment2.model.Person;

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
    private Button saveButton;
    @FXML
    private Button cancel;

    private Person person;

    public PersonDetailController(Person person){
        this.person = new Person(person.getId(), person.getFirstName(), person.getLastName(), person.getDateOfBirth());
    }

    @FXML
    void savePerson(ActionEvent event) {
        if (checkForValidInput()) return;

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
        if ((firstNameTextField.getText().length() < 1 || firstNameTextField.getText().length() > 100) ||
        (lastNameTextField.getText().length() < 1 || lastNameTextField.getText().length() > 100) ||
        (LocalDate.parse(dateOfBirthTextField.getText()).isAfter(LocalDate.now()))){
            ViewSwitcher.getInstance().dialogPopup("Invalid Input", "First and last name must be between "
                    + "1 and 100 characters long. Date of birth must not be after current date");
            return true;
        }
        return false;
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
        newPerson.setId(0);
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
    }
}
