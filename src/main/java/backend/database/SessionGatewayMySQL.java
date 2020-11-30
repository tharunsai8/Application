package backend.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionGatewayMySQL {

    private Connection connection;

    public SessionGatewayMySQL(Connection connection){
        this.connection = connection;
    }

    public int authenticateUser(String username, String password) throws SQLException {
        PreparedStatement authStatement = null;
        ResultSet authResult = null;
        try{
            authStatement = connection.prepareStatement("SELECT * FROM `Users` WHERE username = ? AND password = ?");
            authStatement.setString(1, username);
            authStatement.setString(2, password);
            authResult = authStatement.executeQuery();
            if(authResult.next()){
                return authResult.getInt(1);
            }
            return 0;
        } finally {
            if(authResult != null) {
                authResult.close();
            }
            if(authStatement != null)
                authStatement.close();
        }
    }

    public void insertToken(int userId, String token) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO `Sessions`(`session_id`, `user_id`) VALUES (?,?)")) {
            statement.setString(1, token);
            statement.setInt(2, userId);
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

    public int verifyTokenAndGetUser(String token) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rows = null;
        try{
            statement = connection.prepareStatement("SELECT * FROM `Sessions` WHERE session_id = ?");
            statement.setString(1, token);
            rows = statement.executeQuery();
            if(rows.next()){
                return rows.getInt(2);
            }
            return 0;
        } finally {
            if(rows != null) {
                rows.close();
            }
            if(statement != null)
                statement.close();
        }
    }
}
