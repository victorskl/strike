package au.edu.unimelb.tcp.client;

import org.json.simple.parser.ParseException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;

public class Client {

	//String[] args;
    private ComLineValues values;

	MessageSendThread messageSendThread;

	String identity = null;
	private boolean hasStarted = false;
	boolean debug = false;

	State state;

	/**
	 * @deprecated use Client(ComLineValues values) constructor instead
	 */
	@Deprecated
	public Client(String[] args) {
		//this.args = args;
        throw new UnsupportedOperationException("move to: new Client(ComLineValues values)");
	}

	public Client(ComLineValues values) {
        this.values = values;
    }

	public void run(String screenName, SSLSocket authenticatedSocket) throws IOException, ParseException {

		SSLSocket socket = null;
		hasStarted = true;

		try {
			//load command line args
			//ComLineValues values = new ComLineValues();
			//CmdLineParser parser = new CmdLineParser(values);
            //parser.parseArgument(args);

            String hostname = values.getHost();
            identity = screenName;
            int port = values.getPort();
            debug = values.isDebug();

			socket = authenticatedSocket;

            state = new State(identity, "");
			
			// start sending thread
			messageSendThread = new MessageSendThread(socket, state, debug);
			Thread sendThread = new Thread(messageSendThread);
			sendThread.start();
			
			// start receiving thread
			Thread receiveThread = new Thread(new MessageReceiveThread(socket, state, messageSendThread, this, debug));
			receiveThread.start();
			
		} catch (UnknownHostException e) {
			System.out.println("Unknown host");
		} catch (IOException e) {
			System.out.println("Communication Error: " + e.getMessage());
		}
	}

	// Can use this to send messages from the GUI client.
	public void SendMessage(String message) {
		try {
			messageSendThread.MessageSend(messageSendThread.getSocket(), message);
		}
		catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
	}

	public void requestClientListUpdate() {
		SendMessage("#who");
	}

	public void attemptLoginWith(String screenName){
		this.setIdentity(screenName);
		SendMessage("#newidentity " + screenName);
	}

	public boolean isRunning() {
		return this.hasStarted;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
		state.setIdentity(identity);
	}

	// Events which we can hook into for the GUI.

	// Client tries to create a new identity on the server.
	// ####################################################
	public interface INewUserHandler {
		void userWasApproved();
		void userWasDenied();
	}

	public HashMap<String, INewUserHandler> newUserHandlers = new HashMap<>();

	// This is called by the GUI code to register for the event.

	public void onUserApprovalOrDeny(String handlerID, INewUserHandler handler) {
		this.newUserHandlers.put(handlerID, handler);
	}

	// This is called by the client.
	public void userWasApproved() {
		for (String handlerID : this.newUserHandlers.keySet()) {
			INewUserHandler handler = this.newUserHandlers.get(handlerID);
			handler.userWasApproved();
		}
	}

	public void userWasDenied() {
		for (String handlerID : this.newUserHandlers.keySet()) {
			INewUserHandler handler = this.newUserHandlers.get(handlerID);
			handler.userWasDenied();
		}
	}


	// Message received event.
	// #######################

	public interface IMessageReceiveHandler {
		void messageReceive(String sender, String message);
	}
	public HashMap<String, IMessageReceiveHandler> messageReceiveHandlers = new HashMap<>();

	// This is called by the GUI code to register for a message receive event.
	public void onMessageReceive(String handlerID, IMessageReceiveHandler handler) {
		this.messageReceiveHandlers.put(handlerID, handler);
	}

	// This is called by the client in the message receive thread. There is no need to call this anywhere else.
	public void didReceiveMessage(String sender, String message) {
		for (String handlerID : this.messageReceiveHandlers.keySet()) {
			IMessageReceiveHandler handler = this.messageReceiveHandlers.get(handlerID);
			handler.messageReceive(sender, message);
		}
	}

	// Client changed to a new room event.
	// ###################################

	public interface IRoomChangeHandler{
		void roomChange(String from, String to);
	}
	public HashMap<String, IRoomChangeHandler> roomChangeHandlers = new HashMap<>();

	// This is called by the GUI code to register for the event.
	public void onRoomChange(String handlerID, IRoomChangeHandler handler) {
		System.err.println("Registering for room change.");
		this.roomChangeHandlers.put(handlerID, handler);
	}

	// This is called by the client.
	public void didChangeRoom(String from, String to) {

		System.err.println("Did change room, will call: " + this.roomChangeHandlers.keySet().size());
		for (String handlerID : this.roomChangeHandlers.keySet()) {
			IRoomChangeHandler handler = this.roomChangeHandlers.get(handlerID);
			handler.roomChange(from, to);
		}
	}

	// Another user joins or leaves the room the client is in.
	// #######################################################

	public interface IClientListUpdateHandler {
		void userJoined(String userid);
		void userLeft(String userid);
		void userQuit(String userid);
		void receiveInitialClientList(Set<String> clients);
	}
	public HashMap<String, IClientListUpdateHandler> clientListUpdateHandlers = new HashMap<>();

	// This is called by the GUI code to register for the event.
	public void onClientListUpdate(String handlerID, IClientListUpdateHandler handler) {
		this.clientListUpdateHandlers.put(handlerID, handler);
	}

	// This is called by the client.
	public void userDidJoin(String userid) {
		for (String handlerID : this.clientListUpdateHandlers.keySet()) {
			IClientListUpdateHandler handler = this.clientListUpdateHandlers.get(handlerID);
			handler.userJoined(userid);
		}
	}

	public void userDidLeave(String userid) {
		for (String handlerID : this.clientListUpdateHandlers.keySet()) {
			IClientListUpdateHandler handler = this.clientListUpdateHandlers.get(handlerID);
			handler.userLeft(userid);
		}
	}

	public void userDidQuit(String userid) {
		for (String handlerID : this.clientListUpdateHandlers.keySet()) {
			IClientListUpdateHandler handler = this.clientListUpdateHandlers.get(handlerID);
			handler.userQuit(userid);
		}
	}

	public void didReceiveInitialClientList(Set<String> clients) {
		for (String handlerID : this.clientListUpdateHandlers.keySet()) {
			IClientListUpdateHandler handler = this.clientListUpdateHandlers.get(handlerID);
			handler.receiveInitialClientList(clients);
		}
	}
}
