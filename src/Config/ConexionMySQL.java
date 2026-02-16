package Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class ConexionMySQL {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = ConexionMySQL.class.getClassLoader()
                .getResourceAsStream("Config/db.properties")) {

            if (is == null) {
                throw new RuntimeException("No se encontró Config/db.properties");
            }
            props.load(is);

        } catch (IOException e) {
            throw new RuntimeException("Error cargando Config/db.properties", e);
        }
    }

    private ConexionMySQL() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
        );
    }
}
