package ch.bfh.blk2.bitcoin.blockchain2database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.bfh.blk2.bitcoin.util.PropertiesLoader;

/**
 * This class handles some of the tedious work when talking to the database. It opens and manages the connection and it provides easy
 * handles for creating new stored procedures. As long as you pass it from object to object rather than creating new ones, you can be sure
 * that only one connection to the database is opened at a time.
 * 
 * @author stefan
 */
public class DatabaseConnection {
	
	private static final Logger logger = LogManager.getLogger("DatabaseConnection");

	private Hashtable<String, PreparedStatement> statements;

	private static final String  DRIVER = "dbdriver", URL = "dburl",
			USER = "user", PASSWORD = "password";

	private String driver, url, user, password;

	private Connection connection;

	/**
	 * Unit tests might want to force the propertiesLoader to load in a non standard configuration file
	 */
	public DatabaseConnection(){
		PropertiesLoader propertiesLoader = PropertiesLoader.getInstance();

		driver = propertiesLoader.getProperty(DRIVER);
		url = propertiesLoader.getProperty(URL);
		user = propertiesLoader.getProperty(USER);
		password = propertiesLoader.getProperty(PASSWORD);
		logger.info("user: " + user + "\tpassword: " + password + "\tdriver: " + driver + "\turl: " + url);
		statements = new Hashtable<>();
		connect();
	}

	/**
	 * Save a PreparedStatment to the hashtable if not exists.
	 * Returns a PreparedStatement from the hashtable by the String you provided.
	 * 
	 * @param sql A String of the SQL statement you want to turn into a prepared statement
	 * @return A PreparedStatement from the provided String sql
	 */
	public PreparedStatement getPreparedStatement(String sql) {

		if(!statements.containsKey(sql)) {
			try {
				statements.put(sql, connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS));
			} catch (SQLException | NullPointerException e) {
				e.printStackTrace();
			}
		}

		return statements.get(sql);
	}

	private void connect() {
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);
			connection.setAutoCommit(false);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * By default, DatabaseConnection does not commit after statements have been executed.
	 * With this function, you force DatabaseConnection to execute a commit.
	 */
	public void commit() {
		try {
			connection.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Closes the database connection.
	 */
	public void closeConnection() {
		try {
			connection.commit();
			for (String key : statements.keySet()){
				statements.get(key).close();
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			for (String key : statements.keySet()){
				statements.get(key).close();
			}
			connection.close();
		} catch (Throwable t) {
			throw t;
		} finally {
			super.finalize();
		}
	}
}
