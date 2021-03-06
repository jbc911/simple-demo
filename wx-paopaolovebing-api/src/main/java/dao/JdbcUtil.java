package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.PropertiesUtil;

public class JdbcUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcUtil.class);

	public static Connection getConn() {
		try {
			LOGGER.info("Class.forName");
			Class.forName(PropertiesUtil.getValue("jdbc.driver"));
		} catch (ClassNotFoundException e) {
			LOGGER.error("找不到jdbc.driver类", e);
		}
		LOGGER.info("得到连接");
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(PropertiesUtil.getValue("jdbc.url"),
					PropertiesUtil.getValue("jdbc.user"), PropertiesUtil.getValue("jdbc.password"));
		} catch (SQLException e) {
			LOGGER.error("得到连接出错", e);
		}
		return conn;
	}

	public static String delete(String question) {
		Connection conn = null;
		try {
			conn = getConn();
			String[] questions = question.substring(question.indexOf("-") + 1, question.length()).split(",");
			StringBuffer sql = new StringBuffer("update msg_info set deleted = 1 where id in( ");
			for (int i = 0; i < questions.length; i++) {
				if (i > 0) {
					sql.append(",");
				}
				sql.append("?");
			}
			sql.append(")");
			PreparedStatement prepareStatement = conn.prepareStatement(sql.toString());
			int id = 0;
			for (int i = 0; i < questions.length; i++) {
				id = 0;
				try {
					id = Integer.parseInt(questions[i]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				prepareStatement.setInt(i + 1, id);
			}
			prepareStatement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConn(conn);
		}
		return list(question.substring(0, question.indexOf("-")));
	}

	public static void save(String question) {
		Connection conn = null;
		try {
			conn = getConn();
			PreparedStatement ps = conn.prepareStatement("insert into msg_info(question,answer) values(?,?)");
			int index = question.indexOf("=");
			if (index > -1) {
				ps.setString(1, question.substring(0, index));
				ps.setString(2, question.substring(index + 1, question.length()));
			} else {
				ps.setString(1, question);
				ps.setString(2, "");
			}
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConn(conn);
		}
	}

	public static String list(String question) {
		StringBuffer result = new StringBuffer();
		Connection conn = null;
		try {
			conn = getConn();
			String[] questions = question.split(" ");
			StringBuffer sql = new StringBuffer("select * from msg_info where deleted = 0 and(");
			for (int i = 0; i < questions.length; i++) {
				if (i > 0) {
					sql.append(" or ");
				}
				sql.append("question like ? ");
			}
			sql.append(")");
			PreparedStatement prepareStatement = conn.prepareStatement(sql.toString());
			for (int i = 0; i < questions.length; i++) {
				prepareStatement.setString(i + 1, "%" + questions[i] + "%");
			}
			ResultSet rs = prepareStatement.executeQuery();
			while (rs.next()) {
				result.append(rs.getInt(1)).append("\t");
				result.append(rs.getString(2)).append("\t");
				result.append(rs.getString(3)).append("\t");
				result.append("\r\n");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConn(conn);
		}
		return result.toString();
	}

	public static void closeConn(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		save("风范=dfd");
		System.out.println(list("风范"));
	}

	public static String getById(int i) {
		String result = null;
		Connection conn = null;
		try {
			conn = getConn();
			LOGGER.info("getById:" + i);
			PreparedStatement prepareStatement = conn.prepareStatement("select answer from msg_info where id = " + i);
			ResultSet rs = prepareStatement.executeQuery();
			if (rs.next()) {
				result = rs.getString(1);
			}
		} catch (SQLException e) {
			LOGGER.error("查询出错了!", e);
		} finally {
			closeConn(conn);
		}
		LOGGER.info("getById-result:" + result);
		return result;
	}
}
