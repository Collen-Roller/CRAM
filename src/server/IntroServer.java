package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * IntroServer Class to keep a set of the existing rooms
 * Also run multiple thread of clients that are connected to the server
 * 
 * @author Croller
 *
 */
public class IntroServer {
	
	//user Request Response Commands
	public final static String RRCMD_JOIN = "\\JOIN";
	public final static String RRCMD_ROOMS = "\\ROOMS";
	public final static String RRCMD_EXIT = "\\EXIT";
	public final static String RRCMD_KILL = "\\KILL";
	public final static String RRCMD_CLIENTS = "\\CLIENTS";
	public static final String RRCMD_RENAME = "\\RENAME";
	
	public static Set<Room> rooms = Collections.synchronizedSet(new LinkedHashSet<Room>());
	
	//Port which server is running on
	private int portNumber;
	
	//Host the server lives on
	public static final String host = "localhost";
	
	//Boolean to tell server to be killed
	public static boolean kill = false;

	//Socket on the current Client
	private Socket currClient;
	
	
	/**
	 * Constructor
	 * 
	 * @param portNumber that server lives on
	 */
	public IntroServer(int portNumber){
		this.portNumber = portNumber;
		kill = false;
	}

	/**
	 * Method to kill this IntroServer
	 */
	public static void kill(){
		kill = true;
		System.out.println("Server is being killed..");
		System.exit(0);
	}
	
	/**
	 * Removes a client from a specific room 
	 * based on @param room,name,port
	 * 
	 * @param room - room to remove client from
	 * @param name - clients machine name
	 * @param port - clients port
	 */
	public synchronized static void removeClientFromRoom(String room, String s, int port){
		Iterator<Room> itr = rooms.iterator();
		while(itr.hasNext()){
			Room r = itr.next();
			if(r.getName().equals(room)){
				r.removeClient(s, port);
				deleteRoom(r);
				return;
			}
		}
	}
	
	public static void deleteRoom(Room r){
		if(r.getClientsToString().length() < 1 && !r.getName().equals("lobby")){
			System.out.println("No one currently in " + r.getName() + ", room being removed");
			rooms.remove(r);
		}
	}

	/**
	 * adds a client to a specific room based on room, and client
	 * 
	 * @param room - room to add client to
	 * @param client - client to be added to a specific room
	 */
	public synchronized static void addClientToRoom(String roomName, String s, int port, String name){
		Iterator<Room> itr = rooms.iterator();
		while(itr.hasNext()){
			Room r = itr.next();
			if(r.getName().equals(roomName)){
				r.addClient(s, port, name);
				return;
			}
		}
		Room newRoom = new Room(roomName);
		newRoom.addClient(s, port, name);
		rooms.add(newRoom);
		
		
	}
	
	/**
	 * Returns the room that corresponds to @param name
	 * If room does not exist, null is returned;
	 * 
	 * @param name
	 * @return
	 */
	
	public synchronized static Room getRoom(String name){
		Iterator<Room> itr = rooms.iterator();
		while(itr.hasNext()){
			Room r = itr.next();
			if(r.getName().equals(name)){
				return r;
			}
		}
		return null;	
	}
	
	
	/**
	 * Runs the IntroServer Thread and connects new clients with the executor
	 */
	@SuppressWarnings("resource")
	public void run() {
		Executor executor = Executors.newCachedThreadPool();
		
	    System.out.println("TCPServer begins " + portNumber);

	    try {
	      ServerSocket server = new ServerSocket(portNumber);
	      System.out.println("Accepting connections on " + portNumber);

	      
	      //keep server socket going always
	      
	      //after client is placed into a room, kill the the currClient socket to allow 
	      //other clients to connect to it
	      while ((currClient = server.accept()) != null) {
	    	  executor.execute(new ServerThread(currClient));
	      }
	      kill();
	        
	      
	    } catch (IOException ioe) {
	    	System.out.println("Could not create a ServerSocket on port " + portNumber);
	      // there was a standard input/output error (lower-level from uhe)
	      System.exit(1);
	    }
	  }
}
