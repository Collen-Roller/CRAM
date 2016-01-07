package org.cram.client;


import org.cram.interfaces.GUIPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
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

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.cram.common.Client;
import org.cram.common.ClientReceiver;
import org.cram.common.Sender;
import org.cram.common.ServerReceiver;
import org.cram.common.TypingToQueue;


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

@SuppressWarnings("serial")
public class Chat extends JPanel implements GUIPanel, Runnable{
	
	//Threads 
	TypingToQueue keyboard;
	ClientReceiver cr;
	ServerReceiver sr;
	public static Sender sender = null;
	
	
	//COMMANDS & DEFAULT INFO
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
	public static final String RRCMD_RENAME = "\\RENAME";
	
	//Default information
	public static int DEFAULT_PORT = 21212;
	public static String DEFAULT_ROOM = "lobby";
	public static String DEFAULT_MACHINE_NAME = "localhost";
	public static String DEFAULT_NAME = "DarthVader";
	
	//REGEX that Names & Rooms must abide by
	public static String rnRegex = "[_A-Za-z][_0-9A-Za-z]*";
	
	//Command Line arguments
	public static final String ARG_INTRODUCTION_PORT = "--introduction";
	public static final String ARG_START_ROOM = "--start_room";
	public static final String ARG_NAME = "--name";
	public static final String ARG_ROOMS = "--rooms";
	public static final String ARG_LOCAL_PORT = "--port";
	public static final String ARG_KILL = "--kill";
	
	//Default information
	private static int localPort = DEFAULT_PORT;
	private static String currentRoom = DEFAULT_ROOM;
	private static String name = DEFAULT_NAME;
	private static String machineName = DEFAULT_MACHINE_NAME;
	private static String ipp;
	private static InetAddress clientIP;
	private static InetAddress serverIP;
	private static int serverPort;
	         
	//Communications
	private static Socket serverSocket;
	private static PrintStream serverOut;
	private static Scanner serverIn;
	private static DatagramSocket socket;
	
	public static boolean grabCommand;
	public static boolean sendHello = false;
	public static boolean sendGoodbye = false;
	public static boolean sendRename = false;
	public static boolean init = true;

	//List of clients to connect to
	public static Set<Client> listOfClients = new HashSet<Client>();
	
	//Message Queue
	private BlockingQueue<String> messageQueue;
	private BlockingQueue<String> serverCommandQueue;
	
	/////////////GUI FIELDS\\\\\\\\\\\\\\\\
	private Image background;
	
	public static String text;
	
	private static JTextField inputArea = new JTextField();
	private static JTextArea outputArea = new JTextArea(30,30);
	private static JTextArea clientListArea = new JTextArea(30,30);
	
	private ActionListener a1;
	private KeyAdapter a2;
	
	private JButton submit;
	
	//SOUND METHOD
	public synchronized static void playSound(final String url) {
		
		//TODO : Find out why sound won't work on Linux
		try{
			File soundFile = new File("src/main/resources/" + url);
		    AudioInputStream sound = AudioSystem.getAudioInputStream(soundFile);
	
		    // load the sound into memory (a Clip)
		    DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
		    Clip clip = (Clip) AudioSystem.getLine(info);
		    clip.open(sound);
		    
		    clip.start();
		}catch(Exception e){}
	}
	
	//*********************GUI CODE**********************\\
	
	//Basically a constructor to construct submit button / action listeners / ect..
	public void guiSetUp(){
		background = Toolkit.getDefaultToolkit().createImage(
				"src/main/resources/Brushed Metal by Miatari (5).png");
		setLayout(null);
		submit = new JButton("SUBMIT");
		submit.setLocation(435, 365);
		submit.setSize(100,50);
		submit.addActionListener(a1 = new ActionListener() {
	
			
			public synchronized void actionPerformed(ActionEvent e) {
				String s = inputArea.getText();
				text = s;
				inputArea.setText("");
				grabCommand = true;
				synchronized(keyboard){
					keyboard.notify();
				}
			}
		});
			
		inputArea.addKeyListener(a2 = new KeyAdapter() {
			
			@Override
	         public void keyPressed(KeyEvent e) {
	           int key = e.getKeyCode();
	           if (key == KeyEvent.VK_ENTER) {
	        	   submit.doClick();   
	              }
	         	}
	         }
	      );
		setLayout(null);
		add(submit);
		setTypingTextArea();
		setOutputTextArea();
		setClientList();
	}
	
	//Sets input typing area
	public void setTypingTextArea(){
		JPanel consolePane = new JPanel();
		consolePane.setSize(new Dimension(350, 100));
		consolePane.setLocation(20,350);
		consolePane.setLayout(null);
		inputArea.setLocation(0, 0);
		inputArea.setSize(new Dimension(350, 100));
		inputArea.setForeground(Color.WHITE);
		inputArea.setBackground(Color.DARK_GRAY.darker());
		inputArea.setText("");
		inputArea.setCaretColor(Color.WHITE);
		consolePane.add(inputArea);
		add(consolePane);
	}
	
	//Sets the Output area on GUI
	public void setOutputTextArea(){
		JPanel consolePane = new JPanel();
		consolePane.setSize(new Dimension(350, 300));
		consolePane.setLocation(20,20);
		consolePane.setLayout(null);
		outputArea.setEditable(false);
		outputArea.setLocation(0, 0);
		outputArea.setSize(new Dimension(350, 300));
		outputArea.setForeground(Color.WHITE);
		outputArea.setBackground(Color.BLACK);
		outputArea.setText("");
		JScrollPane scrollPane = new JScrollPane(outputArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBackground(Color.darkGray);
		scrollPane.setForeground(Color.darkGray);
		scrollPane.setLocation(0, 0);
		scrollPane.setSize(new Dimension(350, 300));
		scrollPane.setBorder(BorderFactory.createBevelBorder(10, Color.BLUE, Color.WHITE));
		consolePane.add(scrollPane);
		add(consolePane);
	}
	
	//Sets the Buddy List on GUI
	public void setClientList(){
		JPanel consolePane = new JPanel();
		consolePane.setSize(new Dimension(170, 300));
		consolePane.setLocation(400,20);
		consolePane.setLayout(null);
		clientListArea.setEditable(false);
		clientListArea.setLocation(0, 0);
		clientListArea.setSize(new Dimension(170, 300));
		clientListArea.setForeground(Color.WHITE);
		clientListArea.setBackground(Color.BLACK);
		clientListArea.setText("");
		JScrollPane scrollPane = new JScrollPane(clientListArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBackground(Color.darkGray);
		scrollPane.setForeground(Color.darkGray);
		scrollPane.setLocation(0, 0);
		scrollPane.setSize(new Dimension(170, 300));
		scrollPane.setBorder(BorderFactory.createBevelBorder(5, Color.BLUE.darker().darker(), Color.WHITE));
		consolePane.add(scrollPane);
		add(consolePane);
	}
	
	public static String getInputLines() throws InterruptedException {
		grabCommand = false;
		return text;
	}
	
	public static void setOutputLine(String line){
		//clear the area if there are too many lines
		if(outputArea.getLineCount() > 100){
			outputArea.setText("");
		}
		for(int i=0; i<line.length(); i += 45){
			if(line.length() > i+45){
				outputArea.append(line.substring(i,i+45) + "\n");
			}else{
				outputArea.append(line.substring(i) + "\n");
			}
			outputArea.setCaretPosition(outputArea.getDocument().getLength());
		}
	}
	
	public static void setClientsOnListArea(){
		clientListArea.setText("");
		clientListArea.append("Room : " + getCurrentRoom() + "\n");
		clientListArea.append("\n" + "Buddies" + "\n");
		for(Client c : listOfClients){
			clientListArea.append(c.getName() + "\n");
		}
	}
	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(background, 0, 0, 600, 500, this);
	}

	@Override
	public void update() {
		
	}

	@Override
	public void kill() {
		submit.removeActionListener(a1);
		a1 = null;
		inputArea.removeKeyListener(a2);
		a2 = null;
	}
	
	//*********************END OF GUI CODE*********************\\

	
	//*********************START OF CHAT CODE*******************\\
	
	/**
	 * Constructor, sets up Chat object, and GUI 
	 * Invokes the run method which makes the Receiver's & Sender
	 * 
	 * @param s
	 * @param port
	 * @param name
	 * @param room
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Chat(String s, int port, String name, String room) throws IOException, InterruptedException{
		Chat.setLocalPort(port);
		String [] temp = s.split(":");
		Chat.setMachineToServerIP(temp[0]);
		serverPort = Integer.parseInt(temp[1]);
		Chat.setClientName(name);
		Chat.setCurrentRoom(room);
		Chat.setIPP(machineName);
		guiSetUp();
		
		Thread t = new Thread(this);
		t.start();
		//Set up the gui
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
	public static void setMachineToServerIP(String s){
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
	public static void setIPP(String s){
		try{
			clientIP = InetAddress.getLocalHost();
			ipp =  clientIP.getHostAddress() + ":" + getLocalPort();
			
		}catch (Exception e){
			System.out.println("Could not set client IPP");
		}
	}
	
	/**
	 * Removes a client from the list that maps to 
	 * 
	 * @param s IP
	 * @param n port
	 */
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
	 * Closes the connections to the server and kills the thread
	 * 
	 */
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
	 * Gets the client object
	 * 
	 * @param s - IP
	 * @param n - port
	 * @return Client Object associated with s & n
	 */
	public static Client getClient(String s, int n) {
		String ipp = s + ":" + n;
		Iterator<Client> itr = listOfClients.iterator();
		while(itr.hasNext()){
			Client c = itr.next();
			if(c.getIPP().equals(ipp)){
				return c;
			}
		}
		return null;
		
	}
	
	//**********FLAGS FOR THEADS TO SEND / RECEIVE***************\\
	public synchronized static void resetHello(){
		sendHello = false;
	}
	
	public synchronized static void resetGoodbye(){
		sendGoodbye = false;
	}
	
	public synchronized static void resetRename(){
		sendRename = false;
	}
	
	//*************SETTERS AND GETTERS****************\\
	public static void setClientName(String name){
		Chat.name = name;
		
	}
	private static void setLocalPort(int port){
		localPort = port;
	}
	public static void setCurrentRoom(String room) {
		currentRoom = room;
	}
	//Getters for Chat
	public static String getMachineName(){
		return machineName;
	}
	public static String getClientName(){
		return name;
	}
	private static int getLocalPort(){
		return localPort;
	}
	public static String getCurrentRoom() {
		return currentRoom;
	}

	public static String getIPP(){
		return ipp;
	}

	
	/**
	 * Run the client and use 
	 * ClientReceiver - Thread to receive messages from clients
	 * ServerReceiver - Thread to receive messages from the server
	 * Sender - Thread to send messages to the clients and the server
	 * @throws InterruptedException 
	 */
	public void run() {
		// Linked List implementation of a queue for
		// messages
		messageQueue = new LinkedBlockingQueue<String>();
		serverCommandQueue = new LinkedBlockingQueue<String>();

		try {
			keyboard = new TypingToQueue(messageQueue, serverCommandQueue);
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
			
			cr = new ClientReceiver(socket);
			sr = new ServerReceiver(serverSocket, serverIn);
			try {
				sender = new Sender(socket, serverSocket, serverOut, messageQueue, serverCommandQueue);
				sender.start();
			} catch (InterruptedException e) {
				System.out.println("Can't invoke sender thread....re-run");
				System.out.println("If no luck... then contact me");
			}

			cr.start();
			sr.start();
			
		} catch(SocketException e) {
			System.err.println("Couldn't establish local or distant connection");
			System.exit(2);
		}
	}
	
	//*********END OF CHAT CODE**********\\
}






