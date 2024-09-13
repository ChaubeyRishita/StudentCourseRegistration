package studentCourseRegistration;

import java.sql.Connection;
import java.sql.DriverManager;

// default access modifier suggests the class can be used within this package only, but not by any sub-package
class DBConnection{
	private static Connection con;
	public static Connection getConnection() {
		
		try {
			// Only create a new connection in case a connection doesn't already exist
			if(con == null) {
				// Load the driver
				Class.forName("com.mysql.cj.jdbc.Driver");
				
				// Create a Connection
				String url = "jdbc:mysql://localhost:3306/StudentCourseRegistration";
				String uname = "root";
				String pw = "RC@MySQL";
				con = DriverManager.getConnection(url, uname, pw);
				if(con.isClosed()) {
					System.out.println("Connection is closed.");
				}else {
					System.out.println("Connection created.");
				}
				
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return con;
	}
}
