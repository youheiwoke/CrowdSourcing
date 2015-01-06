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

import dbObject.MicroTask;

public class MicroTaskOperation {
	/**
	 * Insert new micro task record to the database, set the state to initial.
	 * 
	 * @param template
	 *            template XML file path of the task.
	 * @param consumer
	 *            guid of the consumer.
	 * @param address
	 *            address of the consumer agent.
	 * @param expire
	 *            maximum waiting time of the task.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void insertMicroTask(String template, String consumer,
			String deadline) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		Connection conn = ConnectDB.connect();
		String sql = "select * from microtask where consumer=? and template=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, consumer);
		ps.setString(2, template);
		ResultSet rs = ps.executeQuery();
		if (rs.last()) {
			System.out.println("insertMicroTask(): micro task already exists.");
		} else {
			sql = "insert into microtask values(?, ?, ?, ?, ?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, template);
			ps.setString(2, consumer);
			ps.setInt(3, 0);
			ps.setString(4, "initial");
			ps.setString(5, deadline);
			if (ps.executeUpdate() == 0) {
				System.out
						.println("insertMicroTask(): error occured when inserting.");
			}
		}
		rs.close();
		ps.close();
		conn.close();
	}

	/**
	 * 
	 * @param template
	 *            template XML file path of the task.
	 * @param consumer
	 *            guid of the consumer.
	 * @param state
	 *            state of the task that to be updated to(one of the three:
	 *            executing, finished).
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void updateMicroTask(String template, String consumer,
			String state) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		if (!state.equals("creating") && !state.equals("finished")) {
			System.out.println("updateMicroTask(): illegal state.");
			return;
		}

		Connection conn = ConnectDB.connect();
		String sql = "select * from microtask where consumer=? and template=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, consumer);
		ps.setString(2, template);
		ResultSet rs = ps.executeQuery();
		if (rs.last()) {
			sql = "update microtask set state=? where consumer=? and template=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, state);
			ps.setString(2, consumer);
			ps.setString(3, template);
			if (ps.executeUpdate() == 0) {
				System.out
						.println("updateMicroTask(): error occured when updating.");
			}
		} else {
			System.out.println("updateMicroTask(): no such micro task exists.");
		}
		rs.close();
		ps.close();
		conn.close();
	}
	
	/**
	 * 
	 * @param template
	 * @param state
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static void updateMicroTask(String template,
			String state) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		if (!state.equals("creating") && !state.equals("finished")) {
			System.out.println("updateMicroTask(): illegal state.");
			return;
		}

		Connection conn = ConnectDB.connect();
		String sql = "select * from microtask where template=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, template);
		ResultSet rs = ps.executeQuery();
		if (rs.last()) {
			sql = "update microtask set state=? where template=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, state);
			ps.setString(2, template);
			if (ps.executeUpdate() == 0) {
				System.out
						.println("updateMicroTask(): error occured when updating.");
			}
		} else {
			System.out.println("updateMicroTask(): no such micro task exists.");
		}
		rs.close();
		ps.close();
		conn.close();
	}

	/**
	 * 
	 * @return list of tasks whose state are the given state.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static List<MicroTask> queryTask(String state)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		if (!state.equals("initial") && !state.equals("creating")
				&& !state.equals("finished")) {
			System.out.println("queryTask(): illegal argument.");
		}
		List<MicroTask> l = new ArrayList<MicroTask>();
		Connection conn = ConnectDB.connect();
		String sql = "select * from microtask where state=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, state);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			String template = rs.getString(1);
			String consumer = rs.getString(2);
			int cost = rs.getInt(3);
			String deadline = rs.getString(5);
			l.add(new MicroTask(template, consumer, state, cost, deadline));
		}
		return l;
	}

	/**
	 * 
	 * @param date
	 * @return all creating tasks whose deadline are after the given date.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws ParseException
	 */
	public static List<MicroTask> deadlineTask(Date date)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, ParseException {
		List<MicroTask> tasks = queryTask("creating");
		List<MicroTask> result = new ArrayList<MicroTask>();
		for (MicroTask task : tasks) {
			if (task.deadline.contains("not set")) {
				result.add(task);
				continue;
			}

			Date tmpDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.parse(task.deadline);
			if (date.compareTo(tmpDate) > 0) {
				updateMicroTask(task.template, task.consumer, "finished");
			} else {
				result.add(task);
			}
		}
		return result;
	}

}
