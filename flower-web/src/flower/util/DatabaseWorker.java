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
	
	private static String url = "jdbc:mysql://localhost:3306/flower"; 
	private static String username = "flower";
	private static String password = "computernetworks";
	private static Connection con;
	private static Statement stmt;

	/**
	 * 加载驱动
	 */
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Load class failed!");
			e.printStackTrace();
		}		
	}

	/**
	 * 建立连接
	 */
	public static void connect() {
		try {
			con = DriverManager.getConnection(url, username, password);
			stmt = con.createStatement();
		} catch (SQLException e) {
			System.out.println("Connect failed!");
		}
	}

	/**
	 * 释放连接
	 */
	public static void release() {
		try {
			stmt.close();
			con.close();
		} catch (SQLException e) {
			System.out.println("Release failed!");
			e.printStackTrace();
		}
	}
	
	/**
	 * 执行一条只返回一个结果集的查询操作的SQL语句
	 * @param sql
	 * @return
	 */
	public static List<Object[]> query(String sql) {
		try {
			return getRSList(stmt.executeQuery(sql));
		} catch (Exception e) {
			System.out.println("Query failed!");
			e.printStackTrace();
			return new ArrayList<Object[]>();
		}
	}

	/**
	 * 执行一条插入操作的SQL语句
	 * @param sql
	 */
	public static void insert(String sql) {
		try {
			stmt.execute(sql);
		} catch (Exception e) {
			System.out.println("Insert Failed!");
			e.printStackTrace();
		}
	}

	/**
	 * 执行一条更新操作的SQL语句
	 * @param sql SQL语句
	 */
	public static void update(String sql) {
		try {
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			System.out.println("Update failed!");
			e.printStackTrace();
		}
	}

	/**
	 * 执行一条可能产生多个结果集的SQL语句
	 * @param sql SQL语句
	 * @return 多个结果的对象数组列表组成的列表
	 */
	public static List<List<Object[]>>  execute(String sql) {
		List<List<Object[]>> rsListList = new ArrayList<List<Object[]>>();
		try {
			if (stmt.execute(sql)) {
				rsListList.add(getRSList(stmt.getResultSet()));
				while (stmt.getMoreResults()) {
					rsListList.add(getRSList(stmt.getResultSet()));
				}
			}
		} catch (Exception e) {
			System.out.println("Execute failed!");
			e.printStackTrace();
		}
		return rsListList;
	}
	
	/**
	 * 取出结果集，放入一个对象数组列表中
	 * @param rs 结果集
	 * @return 对象数组列表中
	 */
	private static List<Object[]> getRSList(ResultSet rs) {
		List<Object[]> rsList = new ArrayList<Object[]>();
		try {
			ResultSetMetaData meta_data = rs.getMetaData();// 列名
			int columnNum = meta_data.getColumnCount();
			System.out.println();
			while (rs.next()) {
				Object[] item = new Object[columnNum];
				for (int i_col = 1; i_col <= columnNum; i_col++) {
					item[i_col-1] = rs.getObject(i_col);
				}
				rsList.add(item);
			}
			rs.close();
		} catch (Exception e) {
			System.out.println("Get ResultSet failed!");
			e.printStackTrace();
		}
		return rsList;
	}
}
