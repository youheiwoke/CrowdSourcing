package aggregator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import dbObject.WorkerResponse;

public class NaiveAggregator implements Aggregator {
	public NaiveAggregator() {
	}

	@Override
	/**
	 * A naive aggregator implementation which always select the answer with the smallest answer time.
	 */
	public WorkerResponse aggrerator(List<WorkerResponse> responses) {
		if (responses.size() <= 0) {
			return null;
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
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return selected;
	}

}
