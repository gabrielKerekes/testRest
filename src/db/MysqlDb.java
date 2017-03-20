package db;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class MysqlDb {
	private MysqlDataSource mysqlDataSource;
	
	public MysqlDb() {
		mysqlDataSource = null;
		
		init();
	}
	
	public void init() {
		Properties props = new Properties();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/config/db.conf")));
			props.load(br);
			mysqlDataSource = new MysqlDataSource();
			mysqlDataSource.setURL(props.getProperty("MYSQL_DB_URL"));
			mysqlDataSource.setUser(props.getProperty("MYSQL_DB_USERNAME"));
			mysqlDataSource.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean addAccountNumberToken(String username, String accountNumber, String token) {
		String query = String.format(
				"INSERT INTO AccountNumberToken (Username, AccountNumber, Token)" + 
				"VALUES('%s', '%s', '%s')",
				username, accountNumber, token);
				
		return executePreparedStatement(query);
	}
	
	public boolean isAccountNumberTokenValid(String username, String accountNumber, String token) {
		String query = String.format(
				"SELECT * FROM AccountNumberToken " + 
				"WHERE username='%s' AND accountNumber='%s' AND token='%s'",
				username, accountNumber, token);

		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = mysqlDataSource.getConnection();
			statement = connection.createStatement();

			resultSet = statement.executeQuery(query);
			if (resultSet != null)
				return resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) statement.close();
				if (connection != null) connection.close();
				if (resultSet != null) resultSet.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public boolean removeAccountNumberToken(String username, String accountNumber, String token) {
		String query = String.format(
				"DELETE FROM AccountNumberToken " + 
				"WHERE username='%s' AND accountNumber='%s' AND token='%s'",
				username, accountNumber, token);
		
		return executePreparedStatement(query);
	}
	
	private ResultSet executeQuery(String query) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = mysqlDataSource.getConnection();
			statement = connection.createStatement();
			
			resultSet = statement.executeQuery(query);

			return resultSet;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) statement.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	private boolean executePreparedStatement(String query) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = mysqlDataSource.getConnection();
			statement = connection.prepareStatement(query);
			
			statement.execute();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) statement.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public void testDb() {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = mysqlDataSource.getConnection();
			statement = connection.createStatement();
			
			resultSet = statement.executeQuery("SELECT accountNumber FROM AccountNumberToken");
			
			while (resultSet.next()) {
				System.out.println(resultSet.getString("accountNumber"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (resultSet != null) resultSet.close();
				if (statement != null) statement.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
