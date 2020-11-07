package assignment3.database;

import assignment2.model.Person;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class PersonGatewayMySQL {
    private Connection connection;

    public PersonGatewayMySQL(Connection connection) {
        this.connection = connection;
    }

    public ArrayList<Person> fetchPeople() throws SQLException {
        ArrayList<Person> people = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rows = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM `People` WHERE 1");
            rows = statement.executeQuery();
            while(rows.next()){
                Person person = new Person(rows.getInt(1), rows.getString(2), rows.getString(3), rows.getString(4));
                people.add(person);
            }
        } finally {
            closeStuff(statement, rows);
        }
        return people;
    }

    public int insertPerson(Map<String, String> personForm) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rows = null;
        int newId;
        try{
            validateInsertForm(personForm);
            statement = connection.prepareStatement("INSERT INTO `People`(`first_name`, `last_name`, `date_of_birth`) VALUES (?,?,?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, personForm.get("first_name"));
            statement.setString(2, personForm.get("last_name"));
            statement.setString(3, personForm.get("date_of_birth"));
            statement.executeUpdate();
            rows = statement.getGeneratedKeys();
            rows.first();
            newId = rows.getInt(1);
        } finally {
            closeStuff(statement, rows);
        }
        return newId;
    }

    private void validateInsertForm(Map<String, String> personForm){
        GatewayException e = new GatewayException();
        if(personForm.get("first_name") == null){
            e.addError("Missing field first_name");
        } else if(personForm.get("first_name").length() < 1 || personForm.get("first_name").length() > 100){
            e.addError("First name must be between 1 and 100 characters");
        }

        if(personForm.get("last_name") == null){
            e.addError("Missing field lastName");
        } else if (personForm.get("last_name").length() < 1 || personForm.get("last_name").length() > 100){
            e.addError("Last name must be between 1 and 100 characters");
        }

        try {
            if (personForm.get("date_of_birth") == null) {
                e.addError("Missing field date_of_birth");
            } else if (LocalDate.parse(personForm.get("date_of_birth")).isAfter(LocalDate.now())) {
                e.addError("Date of birth cannot be after the current date");
            }
        } catch (DateTimeParseException badDate){
            e.addError("Incorrect format for field date_of_birth");
        }

        if (e.needToThrow){
            throw e;
        }
    }

    public void updatePerson(Map<String, String> personForm, int personId) throws SQLException {
        PreparedStatement statement = null;
        try{
            validateUpdateForm(personForm);
            if(personForm.get("first_name") != null){
                statement = connection.prepareStatement("UPDATE `People` SET `first_name`= ? WHERE `person_id` = ?");
                statement.setString(1, personForm.get("first_name"));
                statement.setInt(2, personId);
                if (statement.executeUpdate() == 0)
                    throw new NotFoundException();
            }
            if(personForm.get("last_name") != null){
                statement = connection.prepareStatement("UPDATE `People` SET `last_name`= ? WHERE `person_id` = ?");
                statement.setString(1, personForm.get("last_name"));
                statement.setInt(2, personId);
                if (statement.executeUpdate() == 0)
                    throw new NotFoundException();
            }
            if(personForm.get("date_of_birth") != null){
                statement = connection.prepareStatement("UPDATE `People` SET `date_of_birth`= ? WHERE `person_id` = ?");
                statement.setString(1, personForm.get("date_of_birth"));
                statement.setInt(2, personId);
                if (statement.executeUpdate() == 0)
                    throw new NotFoundException();
            }
        } finally {
            if(statement != null)
                statement.close();
        }
    }

    private void validateUpdateForm(Map<String, String> personForm){
        GatewayException e = new GatewayException();
        Set<String> keys = personForm.keySet();
        for(String key : keys) {
            if (!key.equals("first_name") && !key.equals("last_name") && !key.equals("date_of_birth")){
                e.addError(key + " is an invalid field name");
            }
        }

        if(personForm.get("first_name") != null){
            if(personForm.get("first_name").length() < 1 || personForm.get("first_name").length() > 100){
                e.addError("First name must be between 1 and 100 characters");
            }
        }
        if(personForm.get("last_name") != null){
            if (personForm.get("last_name").length() < 1 || personForm.get("last_name").length() > 100){
                e.addError("Last name must be between 1 and 100 characters");
            }
        }
        try {
            if (personForm.get("date_of_birth") != null) {
                if (LocalDate.parse(personForm.get("date_of_birth")).isAfter(LocalDate.now())) {
                    e.addError("Date of birth cannot be after the current date");
                }
            }
        } catch (DateTimeParseException badDate){
            e.addError("Incorrect format for field date_of_birth");
        }

        if (e.needToThrow){
            throw e;
        }
    }

    public void deletePerson(int personId) throws SQLException{
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM `People` WHERE `person_id` = ?")) {
            statement.setInt(1, personId);
            if (statement.executeUpdate() == 0)
                throw new NotFoundException();
        }
    }

    public Person fetchPerson(int personId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rows = null;
        try {
            statement = connection.prepareStatement("SELECT `person_id`, `first_name`, `last_name`, `date_of_birth` FROM `People` WHERE `person_id` = ?");
            statement.setInt(1, personId);
            rows = statement.executeQuery();
            if(!rows.next())
                throw new NotFoundException();
            return new Person(rows.getInt(1), rows.getString(2), rows.getString(3), rows.getString(4));
        } finally {
            closeStuff(statement, rows);
        }
    }

    private void closeStuff(PreparedStatement statement, ResultSet rows) throws SQLException {
        if(rows != null) {
            rows.close();
        }
        if(statement != null)
            statement.close();
    }
}
