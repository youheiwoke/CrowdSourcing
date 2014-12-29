package opration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
	private final static String url = "jdbc:mysql://localhost:3306/cs?user=root&password=cloudfdse";

	public static Connection connect() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection conn = DriverManager.getConnection(url);

		return conn;
	}
}
