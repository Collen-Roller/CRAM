package client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.Client;
import common.ClientReceiver;
import common.Sender;
import common.ServerReceiver;
import common.TypingToQueue;


/**
 *    Parameters the are required at command line
 * 	  
 *    --local <port> is the local port
 *    --remote <port> the port # of client to communicate with
 *    --machine <name> the name of the machine of other client
 *
 *     Chat Class is intended for a UDP chat between 2 clients
 *
 * @author Croller
 *
 */

public class Chat {
	
	
	/*
	   * Commands to reach server and execute
	   * \rooms
	   * \join
	   * \leave
	   * \name
	   * \quit
	   * \kill
	   */
	public static final String CMD_ROOMS = "\\rooms";
	public static final String CMD_JOIN = "\\join";
	public static final String CMD_LEAVE = "\\leave";
	public static final String CMD_NAME = "\\name";
	public static final String CMD_QUIT = "\\quit";
	public static final String CMD_KILL = "\\kill";
	public static final String CMD_CLIENTS = "\\clients";
	
	//Server RR commands
	public static final String RRCMD_JOIN = "\\JOIN";
	public static final String RRCMD_ROOMS = "\\ROOMS";
	public static final String RRCMD_EXIT = "\\EXIT";
	public static final String RRCMD_KILL = "\\KILL";
	public static final String RRCMD_CLIENTS = "\\CLIENTS";
	
	public final static int DEFAULT_PORT = 21212;
	public final static String DEFAULT_ROOM = "lobby";
	public final static String DEFAULT_MACHINE_NAME = "localhost";
	public final static String DEFAULT_NAME = "DarthVader";
	
	public final static String rnRegex = "[_A-Za-z][_0-9A-Za-z]*";
	
	//Command Line arguments
	public final String ARG_INTRODUCTION_PORT = "--introduction";
	public final String ARG_START_ROOM = "--start_room";
	public final String ARG_NAME = "--name";
	public final String ARG_ROOMS = "--rooms";
	public final String ARG_LOCAL_PORT = "--port";
	public final String ARG_KILL = "--kill";
	
	//Default information
	private static int localPort = DEFAULT_PORT;
	private static String currentRoom = DEFAULT_ROOM;
	private static String name = DEFAULT_NAME;
	private static String machineName = DEFAULT_MACHINE_NAME;
	private static String ipp;
	private static InetAddress clientIP;
	private InetAddress serverIP;
	private int serverPort;
	
	private static Socket serverSocket;
	private static PrintStream serverOut;
	private static Scanner serverIn;
	private static DatagramSocket socket; //Communication socket
	
	public static boolean command = false;
	public static boolean message = false;
	public static boolean doubleCommand = false;
	public static boolean sendHello = false;
	public static boolean sendGoodbye = false;
	public static boolean init = true;
	
	
	//List of clients to connect to
	public static Set<Client> listOfClients = new HashSet<Client>();
	
	//Message Queue
	private static BlockingQueue<String> messageQueue;
	private static BlockingQueue<String> serverCommandQueue;
	
	public static boolean initSendToServerRoom = false;
	public static boolean initSendToServerKill = false;
	
	
	/**
	 * Creates a new thread the will create a new client 
	 * as well as invoke the run method
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {	
		Chat c = new Chat(args);
		c.run();	
	}
	
	/**
	 * This method sets up the command line arguments 
	 * to fields up above. cleans up main method as well
	 * 
	 * @param args
	 * @throws IOException 
	 */
	
	public Chat(String [] args) throws IOException{
		if(args.length < 2){
			System.out.println("Invalid Number of Arguments");
		    System.out.println("Program is exiting.....");
		    printErrorMessageArgs();
		    System.exit(1);
		}
		
		int count = 0; //will keep track of arguments
		
		while(count < args.length) {
			if(args[count].equals(ARG_INTRODUCTION_PORT)) {
				//System.out.println("CHECK POINT INTRODUCTION");
				count++;
				if(checkIPP(args[count])){
					String [] s = args[count].split(":");
					setMachineToServerIP(s[0]);
					serverPort = Integer.parseInt(s[1]);
		 		}else{
					System.out.println("IPP Does not contain a : re-run with correct args");
					printErrorMessageArgs();
					System.exit(1);
				}
			} else if(args[count].equals(ARG_START_ROOM)) {
				count++;
				//System.out.println("CHECK POINT START ROOM, Received Start Room of : " + args[count]);
				if(checkRegex(args[count]))
					setCurrentRoom(args[count]);
			} else if(args[count].equals(ARG_NAME)){
				count++;
				//System.out.println("CHECK POINT NAME " + " Received Name : " + args[count]);
				if(checkRegex(args[count]))
					name = args[count];
			} else if(args[count].equals(ARG_LOCAL_PORT)) {
				count++;
				//System.out.println("CHECK LOCAL PORT " + " Received new local port of " + args[count]);
				localPort = Integer.parseInt(args[count]);
			} else if(args[count].equals(ARG_ROOMS)){
				initSendToServerRoom = true;
			} else if(args[count].equals(ARG_KILL)){
				initSendToServerKill = true;
			}else {
				System.out.println("Unknown parameter \"" + args[count] + "\"");
				printErrorMessageArgs();
		        System.exit(1);
			}
			count++;
		}
		setIPP(machineName);
		//All Arguments are accounted for!
	}
	/**
	 * Error message for Client to re run args
	 * 
	 */
	public void printErrorMessageArgs(){
		
		System.out.println("\n           To Run this server and change arguments...");
		System.out.println("--introduction <IPP> | REQUIRED The machine and port where server is listening");
		System.out.println("--start_room <RM>    | The room to join initially. Default: lobby");
		System.out.println("--name <NM>          | The name to prepend to all outgoing messages.");
		System.out.println("                     | Default: DarthVader");
		System.out.println("--rooms              | Start session with a call to \rooms.");
		System.out.println("--port <port>        | The local port number where this peer expects");
		System.out.println("                     | chat communication. Default: 21212");
		System.out.println("--kill               | Send the \\KILL message to the introduction");
		System.out.println("                     | server and terminate. Default: false  ");
	}
	
	/**
	 * Checks a String to a REGEX for a room or client name
	 * that is trying to be instantiated. 
	 * 
	 * @param s
	 * @return
	 * 	true - if a match is found
	 *  false - otherwise
	 */
	public static boolean checkRegex(String s){
		Pattern r = Pattern.compile(rnRegex);
        Matcher m = r.matcher(s);
        if(m.find())
        	return true;
        return false;
	}
	
	/**
	 * Checks to see if s contains : for a IPP pair
	 * 
	 * @param s
	 * @return
	 * true - if s contains a :
	 * false - otherwise
	 *
	 */
	public boolean checkIPP(String s){
		if(s.contains(":"))
			return true;
		return false;
	}
	
	/**
	 * sets the serverIP to a Inetaddress based on
	 * a host name s
	 * 
	 * @param s
	 */
	public void setMachineToServerIP(String s){
		try {
			serverIP = InetAddress.getByName(s);
		} catch (UnknownHostException e) {
			System.out.println("Could not set ServerIP");
		}
	}
	
	/**
	 * Set IPP for this
	 * @param s
	 */
	public void setIPP(String s){
		try{
			clientIP = InetAddress.getLocalHost();
			ipp =  clientIP.getHostAddress() + ":" + Chat.getLocalPort();
			
		}catch (Exception e){
			System.out.println("Could not set client IPP");
		}
	}
	
	public synchronized static void removeClient(String s, int n){
		Iterator<Client> itr = listOfClients.iterator();
		while(itr.hasNext()){
			Client c = itr.next();
			if(c.getMachineName().equals(s) && c.getPort() == n || 
					c.getInetAddress().getHostAddress().equals(s) && c.getPort() == n){
				itr.remove();
			}
			
		}
	}
	
	/**
	 * removes all of the clients in the local listOfClients
	 */
	public synchronized static void removeCurrentClients(){
		listOfClients.removeAll(listOfClients);
	}
	
	/**
	 * closes the connections to the server and kills the thread
	 * 
	 */
	//Closes connection for Server
	public static void closeServerConnections(){
		try {
			serverOut.close();
			serverIn.close();
			serverSocket.close();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Connect to Server closed (Client Side)");
		}
	}
	
	/**
	 * 
	 * This set of methods is to reset or change the various 
	 * flags used in this program
	 * {resetHello(),resetGoodbye(),command(),message(),resetCM()}
	 * 
	 */
	public synchronized static void resetHello(){
		sendHello = false;
	}
	
	public synchronized static void resetGoodbye(){
		sendGoodbye = false;
	}
	
	public static void command(){
		Chat.command = true;
		Chat.message = false;
	}
	public static void doubleCommand(){
		Chat.doubleCommand = true;
		command();
	}
	
	public static void message(){
		Chat.message = true;
		Chat.command = false;
	}
	
	public static void resetCM(){
		Chat.message = false;
		Chat.command = false;
	}
	public static void resetDCM(){
		Chat.doubleCommand = false;
	}
	
	
	/**
	 * sets clients name to name
	 * 
	 * @param name to change to
	 */
	public static void setName(String name){
		Chat.name = name;
		
	}
	
	/**
	 * @return machine name
	 */
	public static String getMachineName(){
		return machineName;
	}
	
	/**
	 * 
	 * @return name of client
	 */
	public static String getName(){
		return name;
	}
	
	/**
	 * 
	 * @return local port
	 */
	public static int getLocalPort(){
		return localPort;
	}
	
	/**
	 * 
	 * @return current room
	 */
	public static String getCurrentRoom() {
		return currentRoom;
	}

	/**
	 *
	 * @return IPP
	 */
	public static String getIPP(){
		return ipp;
	}
	
	/**
	 * changes current room to room
	 * @param room to change current room to
	 */
	public static void setCurrentRoom(String room) {
		Chat.currentRoom = room;
	}

	
	/**
	 * Run the client and use 
	 * ClientReceiver - Thread to receive messages from clients
	 * ServerReceiver - Thread to receive messages from the server
	 * Sender - Thread to send messages to the clients and the server
	 * @throws InterruptedException 
	 */
	public void run() throws InterruptedException {
		
		// Linked List implementation of a queue for
		// messages
		
		messageQueue = new LinkedBlockingQueue<String>();
		serverCommandQueue = new LinkedBlockingQueue<String>();
		

		try {
			// Reads keyboard information and places it into queue
			TypingToQueue keyboard = new TypingToQueue(System.in, messageQueue, serverCommandQueue);
			keyboard.start();
			
			// Start reader thread
			socket = new DatagramSocket(localPort);
			try {
				serverSocket = new Socket(serverIP, serverPort);
				serverOut = new PrintStream(serverSocket.getOutputStream(), true);
				serverIn = new Scanner(serverSocket.getInputStream());
			} catch (IOException e) {
				System.out.println("Couldent connect to sever...");
				System.exit(2);
			}
			
			ClientReceiver cr = new ClientReceiver(socket);
			ServerReceiver sr = new ServerReceiver(serverSocket, serverIn);
			Sender sender = new Sender(socket, serverSocket, serverOut, messageQueue, serverCommandQueue);
			
			sender.start();
			cr.start();
			sr.start();

			while(true) {}
		} catch(SocketException e) {
			System.err.println("Couldn't establish local or distant connection");
			System.exit(2);
		}
	}
}






