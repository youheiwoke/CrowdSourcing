package dbObject;

public class AgentInfo {
	public String guid;
	public String address;
	public boolean isOnline;
	public String location;

	public AgentInfo(String guid) {
		this.guid = guid;
		this.address = null;
		this.isOnline = false;
		this.location = null;
	}

	public AgentInfo(String guid, boolean isOnline) {
		this.guid = guid;
		this.address = null;
		this.isOnline = isOnline;
		this.location = null;
	}

	public AgentInfo(String guid, String address, boolean isOnline,
			String location) {
		this.guid = guid;
		this.address = address;
		this.isOnline = isOnline;
		this.location = location;
	}
}
