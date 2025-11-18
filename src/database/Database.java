package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.io.FileInputStream;
import java.util.Properties;

public class Database {

    private static String URL;
    private static String USER;
    private static String PASS;

    static {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("config.properties"));

            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASS = props.getProperty("db.pass");

        } catch (Exception e) {
            System.out.println("Failed to load config.properties");
            e.printStackTrace();
        }
    }

    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            System.out.println("Database error");
            e.printStackTrace();
            return null;
        }
    }
}
