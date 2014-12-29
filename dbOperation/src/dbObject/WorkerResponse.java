package dbObject;

public class WorkerResponse {
	public int id;
	public String worker;
	public String template;
	public String responseString;
	public boolean isAccepted;
	public String answerTime;

	public WorkerResponse(int id, String worker, String template,
			String responseString, boolean isAccepted, String answerTime) {
		this.id = id;
		this.worker = worker;
		this.template = template;
		this.responseString = responseString;
		this.isAccepted = isAccepted;
		this.answerTime = answerTime;
	}
}
