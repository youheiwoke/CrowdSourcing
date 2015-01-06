package aggregator;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import opration.MicroTaskOperation;
import opration.ResponseDBOperation;
import dbObject.WorkerResponse;

public class NaiveAggregator implements Aggregator {
	public NaiveAggregator() {
	}

	@Override
	/**
	 * A naive aggregator implementation which always select the answer with the smallest answer time.
	 */
	public String aggrerator(String taskXML, String deadline) {
		Date cur = new Date();
		Date ddl = null;
		try {
			ddl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(deadline);
		} catch (ParseException e1) {
			e1.printStackTrace();
			return "aggrerator(): error occured when parsing date format.";
		}
		long sleepTime = ddl.getTime() - cur.getTime();
		List<WorkerResponse> responses = null;
		if (sleepTime > 0) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return "aggrerator(): InterruptedException.";
			}
		}

		try {
			responses = ResponseDBOperation.queryResponse(taskXML);
		} catch (InstantiationException e1) {
			e1.printStackTrace();
			return "aggrerator(): InstantiationException.";
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
			return "aggrerator(): IllegalAccessException.";
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			return "aggrerator(): ClassNotFoundException.";
		} catch (SQLException e1) {
			e1.printStackTrace();
			return "aggrerator(): SQLException.";
		}

		if (responses.size() <= 0) {
			return "No answer.";
		}

		WorkerResponse selected = responses.get(0);
		try {
			Date early = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.parse(responses.get(0).answerTime);
			for (WorkerResponse response : responses) {
				Date tmpDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.parse(response.answerTime);
				if (early.compareTo(tmpDate) > 0) {
					early = tmpDate;
					selected = response;
				}
			}
			ResponseDBOperation.acceptResponse(selected.id);

			MicroTaskOperation.updateMicroTask(taskXML, "finished");
		} catch (ParseException e) {
			e.printStackTrace();
			return "aggrerator(): ParseException.";
		} catch (InstantiationException e) {
			e.printStackTrace();
			return "aggrerator(): InstantiationException.";
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return "aggrerator(): IllegalAccessException.";
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return "aggrerator(): ClassNotFoundException.";
		} catch (SQLException e) {
			e.printStackTrace();
			return "aggrerator(): SQLException.";
		}

		return selected.responseString;
	}

}
