package dbObject;

public class MicroTask {
	public String template;
	public String consumer;
	public int cost;
	public String state;
	public String deadline;

	public MicroTask(String template, String consumer, String state) {
		this.template = template;
		this.consumer = consumer;
		this.state = state;
		this.cost = 0;
		this.deadline = "not set";
	}

	public MicroTask(String template, String consumer, String state, int cost,
			String deadline) {
		this.template = template;
		this.consumer = consumer;
		this.state = state;
		this.cost = cost;
		this.deadline = deadline;
	}
}
