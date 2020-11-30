package backend.database;

import backend.Main;
import com.mysql.cj.jdbc.MysqlDataSource;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnect {

    private Connection connection;
    private static DBConnect instance = null;

    private DBConnect(){}

    public static DBConnect getInstance(){
        if(instance == null){
            instance = new DBConnect();
        }
        return instance;
    }

    public DBConnect connectToDB() throws SQLException, IOException {
        //connect to data source and create a connection instance
        Properties props = getConfig();

        MysqlDataSource ds = new MysqlDataSource();
        ds.setURL(props.getProperty("MYSQL_DB_URL"));
        ds.setUser(props.getProperty("MYSQL_DB_USERNAME"));
        ds.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));

        //create the connection
        connection = ds.getConnection();
        return this;
    }

    private Properties getConfig() throws IOException {
        Properties props = new Properties();

        BufferedInputStream propsFile = (BufferedInputStream) Main.class.getResourceAsStream("/db.properties");
        props.load(propsFile);
        propsFile.close();

        return props;
    }

    public Connection getConnection() {
        return connection;
    }
}
