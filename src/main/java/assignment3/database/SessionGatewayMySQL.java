package assignment3.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionGatewayMySQL {

    private Connection connection;

    public SessionGatewayMySQL(Connection connection){
        this.connection = connection;
    }

    public boolean authenticateUser(String username, String password) throws SQLException {
        PreparedStatement authStatement = null;
        ResultSet authResult = null;
        try{
            authStatement = connection.prepareStatement("SELECT * FROM `Users` WHERE username = ? AND password = ?");
            authStatement.setString(1, username);
            authStatement.setString(2, password);
            authResult = authStatement.executeQuery();
            return authResult.next();
        } finally {
            if(authResult != null) {
                authResult.close();
            }
            if(authStatement != null)
                authStatement.close();
        }
    }

    public void insertToken(String username, String token) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO `Sessions`(`username`, `session_id`) VALUES (?,?)")) {
            statement.setString(1, username);
            statement.setString(2, token);
            statement.executeUpdate();
        }
    }

    public boolean verifyToken(String token) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rows = null;
        try{
            statement = connection.prepareStatement("SELECT * FROM `Sessions` WHERE session_id = ?");
            statement.setString(1, token);
            rows = statement.executeQuery();
            return rows.next();
        } finally {
            if(rows != null) {
                rows.close();
            }
            if(statement != null)
                statement.close();
        }
    }
}
