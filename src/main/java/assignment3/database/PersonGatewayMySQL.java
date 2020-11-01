package assignment3.database;

import assignment2.model.Person;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
        int newId = 0;
        try{
            validateInsertForm(personForm);
            statement = connection.prepareStatement("INSERT INTO `People`(`first_name`, `last_name`, `date_of_birth`) VALUES (?,?,?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, personForm.get("firstName"));
            statement.setString(2, personForm.get("lastName"));
            statement.setString(3, personForm.get("dateOfBirth"));
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
        if(personForm.get("firstName") == null){
            e.addError("Missing field firstName");
        } else if(personForm.get("firstName").length() < 1 || personForm.get("firstName").length() > 100){
            e.addError("First name must be between 1 and 100 characters");
        }

        if(personForm.get("lastName") == null){
            e.addError("Missing field lastName");
        } else if (personForm.get("lastName").length() < 1 || personForm.get("lastName").length() > 100){
            e.addError("Last name must be between 1 and 100 characters");
        }

        if(personForm.get("dateOfBirth") == null){
            e.addError("Missing field dateOfBirth");
        } else if(LocalDate.parse(personForm.get("dateOfBirth")).isAfter(LocalDate.now())){
            e.addError("Date of birth cannot be after the current date");
        }

        if (e.needToThrow){
            throw e;
        }
    }

    public void updatePerson(Map<String, String> personForm, int personId) throws SQLException {
        PreparedStatement statement = null;
        try{
            validateUpdateForm(personForm);
            if(personForm.get("firstName") != null){
                statement = connection.prepareStatement("UPDATE `People` SET `first_name`= ? WHERE `person_id` = ?");
                statement.setString(1, personForm.get("firstName"));
                statement.setInt(2, personId);
                if (statement.executeUpdate() == 0)
                    throw new SQLException();
            }
            if(personForm.get("lastName") != null){
                statement = connection.prepareStatement("UPDATE `People` SET `last_name`= ? WHERE `person_id` = ?");
                statement.setString(1, personForm.get("lastName"));
                statement.setInt(2, personId);
                if (statement.executeUpdate() == 0)
                    throw new SQLException();
            }
            if(personForm.get("dateOfBirth") != null){
                statement = connection.prepareStatement("UPDATE `People` SET `date_of_birth`= ? WHERE `person_id` = ?");
                statement.setString(1, personForm.get("dateOfBirth"));
                statement.setInt(2, personId);
                if (statement.executeUpdate() == 0)
                    throw new SQLException();
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
            if (!key.equals("firstName") && !key.equals("lastName") && !key.equals("dateOfBirth")){
                e.addError(key + " is an invalid field name");
            }
        }

        if(personForm.get("firstName") != null){
            if(personForm.get("firstName").length() < 1 || personForm.get("firstName").length() > 100){
                e.addError("First name must be between 1 and 100 characters");
            }
        }
        if(personForm.get("lastName") != null){
            if (personForm.get("lastName").length() < 1 || personForm.get("lastName").length() > 100){
                e.addError("Last name must be between 1 and 100 characters");
            }
        }
        if(personForm.get("dateOfBirth") != null){
            if(LocalDate.parse(personForm.get("dateOfBirth")).isAfter(LocalDate.now())){
                e.addError("Date of birth cannot be after the current date");
            }
        }

        if (e.needToThrow){
            throw e;
        }
    }

    public void deletePerson(int personId) throws SQLException{
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM `People` WHERE `person_id` = ?")) {
            statement.setInt(1, personId);
            if (statement.executeUpdate() == 0)
                throw new SQLException();
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
                throw new SQLException();
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
