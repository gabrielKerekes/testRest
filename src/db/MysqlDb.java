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
import java.sql.Timestamp;

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
	
	public boolean addAccountNumberToken(String accountNumber, String token) {
		String query = String.format(
				"INSERT INTO AccountNumberToken (AccountNumber, Token) " + 
				"VALUES('%s', '%s')",
				accountNumber, token);
				
		return executePreparedStatement(query);
	}
	
	public boolean isAccountNumberTokenValid(String accountNumber, String token) {
		String query = String.format(
				"SELECT * FROM AccountNumberToken " + 
				"WHERE accountNumber='%s' AND token='%s'",
				accountNumber, token);

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
				if (resultSet != null) resultSet.close();
				if (statement != null) statement.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public boolean isAccountNumberAuthenticated(String accountNumber, Timestamp timestamp, long authenticationPeriod) {
		System.out.println("DB: isAccountNumberAuthenticated - " + accountNumber + " " + timestamp);
		
		String query = String.format(
				"SELECT * FROM AccountNumberAuthentication " + 
				"WHERE accountNumber='%s'",
				accountNumber);

		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = mysqlDataSource.getConnection();
			statement = connection.createStatement();

			resultSet = statement.executeQuery(query);
			if (resultSet != null) {
				if (resultSet.next()) {
					Timestamp dbTimestamp = resultSet.getTimestamp("timestamp");					
					return timestamp.getTime() - dbTimestamp.getTime() < authenticationPeriod;
				}
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
		
		return false;
	}
	
	public boolean setAccountNumberAuthenticated(String accountNumber, Timestamp timestamp) {
		System.out.println("DB: setAccountNumberAuthenticated - " + accountNumber + " " + timestamp);
		
		String query = String.format(
				"REPLACE INTO AccountNumberAuthentication " + 
				"VALUES('%s', '%s')",
				accountNumber, timestamp);
		
		return executePreparedStatement(query);
	}
	
	public boolean removeAccountNumberToken(String accountNumber, String token) {
		String query = String.format(
				"DELETE FROM AccountNumberToken " + 
				"WHERE accountNumber='%s' AND token='%s'",
				accountNumber, token);
		
		return executePreparedStatement(query);
	}
	
	public boolean addPendingIdentityConfirmation(String key, String accountNumber, String username, String timestamp, String guid) {
		System.out.println("DB: addPendingIdentityConfirmation - key: " + key + " accountNumber: " + accountNumber);
		
		String query = String.format(
				"INSERT INTO PendingIdentityConfirmation (Token, AccountNumber, Username, Timestamp, Guid) " + 
				"VALUES('%s', '%s', '%s', '%s', '%s')",
				key, accountNumber, username, timestamp, guid);
				
		return executePreparedStatement(query);
	}
	
	public String getPendingIdentityConfirmation(String key) {
		System.out.println("DB: getPendingIdentityConfirmation - key: " + key);
		
		// token used in db, because Key can't be used
		String query = String.format(
				"SELECT * FROM PendingIdentityConfirmation " + 
				"WHERE Token='%s'",
				key);

		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = mysqlDataSource.getConnection();
			statement = connection.createStatement();

			resultSet = statement.executeQuery(query);
			if (resultSet != null) {
				if (resultSet.next()) {
					return resultSet.getString("AccountNumber");					
				}
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
		
		return null;
	}
	
	public boolean addPendingTransaction(String key, String paymentId) {
		System.out.println("DB: addPendingTransaction - key: " + key + " paymentId: " + paymentId);

		// token used in db, because Key can't be used
		String query = String.format(
				"INSERT INTO PendingTransaction (Token, PaymentId) " + 
				"VALUES('%s', '%s')",
				key, paymentId);
				
		return executePreparedStatement(query);
	}
	
	public String getPendingTransaction(String key)	{
		System.out.println("DB: getPendingTransaction - key: " + key);

		// token used in db, because Key can't be used
		String query = String.format(
				"SELECT * FROM PendingTransaction " + 
				"WHERE Token='%s'",
				key);

		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = mysqlDataSource.getConnection();
			statement = connection.createStatement();

			resultSet = statement.executeQuery(query);
			if (resultSet != null) {
				if (resultSet.next()) {
					return resultSet.getString("PaymentId");					
				}
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
		
		return null;
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
				if (resultSet != null) resultSet.close();
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
