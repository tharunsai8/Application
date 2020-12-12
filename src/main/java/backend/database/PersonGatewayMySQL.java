package backend.database;

import client.model.AuditRecord;
import client.model.FetchResult;
import client.model.Person;

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

    public FetchResult fetchPeople(int pageNum, String lastName) throws SQLException {
        FetchResult fetchResult = new FetchResult();
        PreparedStatement statement = null;
        ResultSet rows = null;
        try {
            statement = connection.prepareStatement("SELECT COUNT(*) FROM `People` WHERE `last_name` LIKE ?");
            if(lastName == null)
                statement.setString(1, "%");
            else
                statement.setString(1, lastName + "%");
            rows = statement.executeQuery();
            rows.next();
            fetchResult.setNumRows(rows.getInt(1));

            statement = connection.prepareStatement("SELECT * FROM `People` WHERE `last_name` LIKE ?");
            if(lastName == null)
                statement.setString(1, "%");
            else
                statement.setString(1, lastName + "%");
            rows = statement.executeQuery();

            rows.absolute((pageNum-1)*10);
            ArrayList<Person> people = new ArrayList<>();
            int i = 0;
            while (rows.next()) {
                if(++i > 10)
                    break;
                people.add(new Person(rows.getInt(1), rows.getString(2), rows.getString(3), rows.getString(4), rows.getString(5)));
            }
            fetchResult.setPeople(people);

        } finally {
            closeStuff(statement, rows);
        }
        return fetchResult;
    }

    public int insertPerson(Map<String, String> personForm, int userId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rows = null;
        int newId;
        try{
            validateInsertForm(personForm);
            connection.setAutoCommit(false);
            statement = connection.prepareStatement("INSERT INTO `People`(`first_name`, `last_name`, `date_of_birth`) VALUES (?,?,?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, personForm.get("first_name"));
            statement.setString(2, personForm.get("last_name"));
            statement.setString(3, personForm.get("date_of_birth"));
            statement.executeUpdate();
            rows = statement.getGeneratedKeys();
            rows.first();
            newId = rows.getInt(1);

            auditStatement(statement, "added", userId, newId);

            connection.setAutoCommit(true);
        } catch (SQLException e){
            connection.rollback();
            connection.setAutoCommit(true);
            throw new SQLException(e);
        } finally {
            closeStuff(statement, rows);
        }
        return newId;
    }

    private void validateInsertForm(Map<String, String> personForm){
        InvalidFormException e = new InvalidFormException();
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

    public void updatePerson(Map<String, String> personForm, int personId, int userId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rows = null;
        try{
            validateUpdateForm(personForm);

            String oldFirstName;
            String oldLastName;
            String oldDOB;

            statement = connection.prepareStatement("SELECT * FROM `People` WHERE `person_id` = ?");
            statement.setInt(1, personId);
            rows = statement.executeQuery();
            if(rows.next()){
                if(!personForm.get("last_modified").equals(rows.getString(5))){
                    throw new UpdateException(new Person(rows.getInt(1), rows.getString(2), rows.getString(3), rows.getString(4), rows.getString(5)));
                }
                oldFirstName = rows.getString(2);
                oldLastName = rows.getString(3);
                oldDOB = rows.getString(4);
            } else {
                throw new NotFoundException();
            }

            connection.setAutoCommit(false);
            if(personForm.get("first_name") != null){
                updateStatement(statement, "first_name", personForm.get("first_name"), personId);
                auditStatement(statement, "First name changed from " + oldFirstName + " to " + personForm.get("first_name"), userId, personId);
            }
            if(personForm.get("last_name") != null){
                updateStatement(statement, "last_name", personForm.get("last_name"), personId);
                auditStatement(statement, "Last name changed from " + oldLastName + " to " + personForm.get("last_name"), userId, personId);
            }
            if(personForm.get("date_of_birth") != null){
                updateStatement(statement, "date_of_birth", personForm.get("date_of_birth"), personId);
                auditStatement(statement, "Date of birth was changed from " + oldDOB + " to " + personForm.get("date_of_birth"), userId, personId);
            }
            connection.setAutoCommit(true);
        } catch (SQLException e){
            connection.rollback();
            connection.setAutoCommit(true);
            throw new SQLException(e);
        }finally {
            closeStuff(statement, rows);
        }
    }

    private void updateStatement(PreparedStatement statement, String field, String value, int personId) throws SQLException {
        statement = connection.prepareStatement("UPDATE `People` SET `" + field + "`= ? WHERE `person_id` = ?");
        statement.setString(1, value);
        statement.setInt(2, personId);
        statement.executeUpdate();
    }

    private void auditStatement(PreparedStatement statement, String message, int userId, int personId) throws SQLException {
        statement = connection.prepareStatement("INSERT INTO `Audit_Trail`(`change_msg`, `changed_by`, `person_id`) VALUES (?,?,?)");
        statement.setString(1, message);
        statement.setInt(2, userId);
        statement.setInt(3, personId);
        statement.executeUpdate();
    }

    private void validateUpdateForm(Map<String, String> personForm){
        InvalidFormException e = new InvalidFormException();
        Set<String> keys = personForm.keySet();
        for(String key : keys) {
            if (!key.equals("first_name") && !key.equals("last_name") && !key.equals("date_of_birth") && !key.equals("last_modified")){
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
            return new Person(rows.getInt(1), rows.getString(2), rows.getString(3), rows.getString(4), rows.getString(5));
        } finally {
            closeStuff(statement, rows);
        }
    }

    public ArrayList<AuditRecord> fetchAuditTrail(int personId) throws SQLException {
        ArrayList<AuditRecord> auditTrail = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rows = null;
        try {
            statement = connection.prepareStatement("SELECT a.change_msg, u.username, a.when_occurred FROM Audit_Trail a INNER JOIN Users u ON a.changed_by = u.id WHERE a.person_id = ?");
            statement.setInt(1, personId);
            rows = statement.executeQuery();

            if(!rows.next())
                throw new NotFoundException();

            do {
                auditTrail.add(new AuditRecord(rows.getString(1), rows.getString(2), rows.getString(3)));
            } while(rows.next());
        } finally {
            closeStuff(statement, rows);
        }
        return auditTrail;
    }

    private void closeStuff(PreparedStatement statement, ResultSet rows) throws SQLException {
        if(rows != null) {
            rows.close();
        }
        if(statement != null)
            statement.close();
    }
}
