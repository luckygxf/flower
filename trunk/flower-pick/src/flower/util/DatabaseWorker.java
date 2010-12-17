package flower.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 进行数据库操作的类
 * @author 易建龙
 * @author 郑旭东
 */
public class DatabaseWorker {
	
	public static boolean DEBUG = false;
	
	public static String url = "jdbc:mysql://localhost:3306/flower"; 
	public static String username = "flower";
	public static String password = "computernetworks";
	public static Connection con;
	public static Statement stmt;
	public static ResultSet rs;

	public static void connect() {
		// 加载驱动
		try {
			Class.forName("com.mysql.jdbc.Driver");
			if(DEBUG)System.out.println("successful!");
		} catch (ClassNotFoundException e) {
			if(DEBUG)System.out.println("failed!");
			e.printStackTrace();
		}
		// 建立连接
		try {
			con = DriverManager.getConnection(url, username, password);
			stmt = con.createStatement();
			if(DEBUG)System.out.println("Connect Successful!");
			
		} catch (SQLException e) {
			if(DEBUG)System.out.println("Connect failed!");
		}
	}

	public static List<Object[]> select(String sql) {
		try {
			rs = stmt.executeQuery(sql);
			ResultSetMetaData meta_data = rs.getMetaData();// 列名
			int columnNum = meta_data.getColumnCount();
			System.out.println();
			List<Object[]> rsList = new ArrayList<Object[]>();
			while (rs.next()) {
				Object[] item = new Object[columnNum];
				for (int i_col = 1; i_col <= columnNum; i_col++) {
					item[i_col-1] = rs.getObject(i_col);
				}
				rsList.add(item);
			}
			rs.close();
			return rsList;
		} catch (Exception e) {
			if(DEBUG)System.out.println("Query failed!");
			return null;
		}
	}

	public static void insert(String sql) {
		try {
			stmt.clearBatch();
			stmt.addBatch(sql);
			stmt.executeBatch();
			if(DEBUG)System.out.println("Insert Successful!");
		} catch (Exception e) {
			e.printStackTrace();
			if(DEBUG)System.out.println("Insert Failed!");
		}

	}

	public static void execute(String sql) {
		try {
			stmt.execute(sql);
			if(DEBUG)System.out.println("Execute Successful!");
		} catch (Exception e) {
			if(DEBUG)System.out.println("Execute failed!");
		}
	}
	
	public static void update(String sql) {
		try {
			stmt.executeUpdate(sql);
			if(DEBUG)System.out.println("Update Successful!");
		} catch (Exception e) {
			if(DEBUG)System.out.println("Update failed!");
		}
	}

	public static void release() {
		try {
			stmt.close();
			con.close();
		} catch (SQLException e) { 
			e.printStackTrace();
		}
	}
}
