package au.edu.unimelb.tcp.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Set;

import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class Client {

	String[] args;

	MessageSendThread messageSendThread;

	String identity = null;

	private boolean hasStarted = false;

	public Client(String[] args) {
		this.args = args;
	}

	public void run() throws IOException, ParseException {
		Socket socket = null;

		boolean debug = false;
		hasStarted = true;

		try {
			//load command line args
			ComLineValues values = new ComLineValues();
			CmdLineParser parser = new CmdLineParser(values);
			try {
				parser.parseArgument(args);
				String hostname = values.getHost();
				//identity = values.getIdeneity(); Have to set it now on the client before running.
				int port = values.getPort();
				debug = values.isDebug();
				socket = new Socket(hostname, port);
			} catch (CmdLineException e) {
				e.printStackTrace();
			}
			
			State state = new State(identity, "");
			
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

	public boolean isRunning() {
		return this.hasStarted;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	// Events which we can hook into for the GUI.

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
		this.roomChangeHandlers.put(handlerID, handler);
	}

	// This is called by the client.
	public void didChangeRoom(String from, String to) {
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

	public void didReceiveInitialClientList(Set<String> clients) {
		for (String handlerID : this.clientListUpdateHandlers.keySet()) {
			IClientListUpdateHandler handler = this.clientListUpdateHandlers.get(handlerID);
			handler.receiveInitialClientList(clients);
		}
	}
}
