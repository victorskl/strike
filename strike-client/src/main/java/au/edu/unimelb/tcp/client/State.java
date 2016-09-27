package au.edu.unimelb.tcp.client;

public class State {

	private String identity;
	private String roomId;
	
	public State(String identity, String roomId) {
		this.identity = identity;
		this.roomId = roomId;
		
	}
	
	public synchronized String getRoomId() {
		return roomId;
	}
	public synchronized void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	
	public String getIdentity() {
		return identity;
	}
	
	
}
