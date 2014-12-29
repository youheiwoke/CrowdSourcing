package opration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dbObject.AgentInfo;

public class AgentDBOperation {
	/**
	 * 
	 * @param guid
	 *            the string of JADE agent's name obtained by using
	 *            getAID().getName().
	 * @param address
	 *            address of the agent obtained by using
	 *            getAID().getAddressArray()[0].
	 * @return true if succeed.
	 * @return false if failed.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static boolean register(String guid, String address)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		Connection conn = ConnectDB.connect();
		String sql = "select * from agentinfo where guid=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, guid);
		ResultSet rs = ps.executeQuery();
		if (rs.last()) {
			return false;
		} else {
			sql = "insert into agentinfo values (?, ?, ?, ?, ?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, guid);
			ps.setString(2, address);
			ps.setBoolean(3, true);
			ps.setString(4, "");
			String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date());
			ps.setString(5, time);
			if (ps.executeUpdate() == 0) {
				System.out.println("Error occured in register() insert.");
				return false;
			}
			System.out.println("insert agent info succeed!");
		}
		rs.close();
		ps.close();
		conn.close();
		return true;
	}

	/**
	 * 
	 * @param guid
	 *            the string of JADE agent's name obtained by using
	 *            getAID().getName().
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @return true if succeed.
	 * @return false if failed.
	 */
	public static boolean onLineUpdate(String guid)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		Connection conn = ConnectDB.connect();
		String sql = "select * from agentinfo where guid=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, guid);
		ResultSet rs = ps.executeQuery();
		if (rs.last()) {
			sql = "update agentinfo set isOnline=? where guid=?";
			ps = conn.prepareStatement(sql);
			ps.setBoolean(1, true);
			ps.setString(2, guid);
			if (ps.executeUpdate() == 0) {
				System.out.println("Error occured in onLineUpdate() update.");
				return false;
			}
			System.out.println("update agent info succeed!");
		} else {
			return false;
		}
		rs.close();
		ps.close();
		conn.close();
		return true;
	}

	/**
	 * 
	 * @param guid
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void offLineUpdate(String guid)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		Connection conn = ConnectDB.connect();
		String sql = "select * from agentinfo where guid=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, guid);
		ResultSet rs = ps.executeQuery();
		if (rs.last()) {
			sql = "update agentinfo set isOnline=? where guid=?";
			ps = conn.prepareStatement(sql);
			ps.setBoolean(1, false);
			ps.setString(2, guid);
			if (ps.executeUpdate() == 0) {
				System.out.println("Error occured in offLineUpdate() update.");
			}
		} else {
			System.out.println("Agent info does not exist.");
		}
		rs.close();
		ps.close();
		conn.close();
	}

	/**
	 * 
	 * @return list of all online agents.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static List<AgentInfo> getOnlineAgents()
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		List<AgentInfo> agents = new ArrayList<AgentInfo>();
		Connection conn = ConnectDB.connect();
		String sql = "select * from agentinfo where isOnline=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setBoolean(1, true);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			agents.add(new AgentInfo(rs.getString(1), rs.getString(2), rs
					.getBoolean(3), rs.getString(4)));
		}

		return agents;
	}

	/**
	 * 
	 * @param template
	 *            template XML file path of the task.
	 * @return an AgentInfo object of the consumer of the task.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static AgentInfo getConsumer(String template)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		Connection conn = ConnectDB.connect();
		String sql = "select guid, address, isOnline, location from microtask,agentinfo where guid=consumer and template=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, template);
		ResultSet rs = ps.executeQuery();
		AgentInfo info;
		if (rs.next()) {
			info = new AgentInfo(rs.getString(1), rs.getString(2),
					rs.getBoolean(3), rs.getString(4));
		} else {
			return null;
		}

		return info;
	}

	/**
	 * Set those expired agents' isOnline to false according to the given
	 * maxDiff.
	 * 
	 * @param maxDiff
	 *            max time difference of last active time.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws ParseException
	 */
	public static void checkActive(long maxDiff) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException,
			ParseException {
		Date currentDate = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Connection conn = ConnectDB.connect();
		String sql = "select * from agentinfo where isOnline=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setBoolean(1, true);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			Date activeDate = df.parse(rs.getString(5));
			if (currentDate.getTime() - activeDate.getTime() > maxDiff) {
				sql = "update agentinfo set isOnline=? where guid=?";
				ps = conn.prepareStatement(sql);
				ps.setBoolean(1, false);
				ps.setString(2, rs.getString(1));
				if (ps.executeUpdate() == 0) {
					System.out
							.println("checkActive(): error occured when updating.");
				}
			}
		}
	}
}
