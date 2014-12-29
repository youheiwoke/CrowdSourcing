package aggregator;

import java.util.List;

import dbObject.WorkerResponse;

public interface Aggregator {
	public WorkerResponse aggrerator(List<WorkerResponse> reponses);
}