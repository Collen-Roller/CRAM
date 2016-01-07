package org.cram.server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Scanner;

/**
 * ServerThread for each client to communicate to the IntroServer.
 * Incoming commands are parsed using parseServerCommand(String s, PrintStream pout)
 * 
 * Class extends Thread
 * 
 * @author Croller
 *
 */
public class ServerThread extends Thread {

	private Socket currentClient;
	
	public boolean flag;
	
	/**
	 * Constructor to create a new ServerThread object 
	 * using Socket client to communicate
	 * 
	 * @param client
	 */
	public ServerThread(Socket client){
		this.currentClient = client;
		flag = true;
	}
	
	/**
	 * Parses command from clients
	 * \KILL - kills the IntroServer and all connections to it
	 * \EXIT <NM> <RM> - Will remove that specific client from its room
	 * \CLIENTS <RM> - Sends a list of clients in specific room
	 * \JOIN <RM> - Will add clients to a room list if room exists
	 * 				Otherwise will create the new room and add client to it
	 * 
	 * @param s message from client to be parsed
	 * @param pout PrintStream to send info back to the client
	 * @throws SocketException
	 */
	public void parseServerCommand(String s, PrintStream pout) throws SocketException {
		String [] line = s.split("\\s+");
		System.out.println(s);
		//Kills the Server
		if(line[0].equals(IntroServer.RRCMD_KILL)){
			pout.println(IntroServer.RRCMD_KILL);
			pout.flush();
			IntroServer.kill();
		}
			
		//Exits client from <RM>
		else if(line[0].equals(IntroServer.RRCMD_EXIT)){
			//Removing all of the clients in the room from local list to send / receive from
			String room = line[1];
			String [] ipp = line[2].split(":"); 
			IntroServer.removeClientFromRoom(room,ipp[0],Integer.parseInt(ipp[1]));
			pout.println(IntroServer.RRCMD_EXIT + " " + line[1]);

		}
		
		else if(line[0].equals(IntroServer.RRCMD_RENAME)){
			String room = line[1];
			String [] client = line[2].split(":");
			String newName = line[3];
			IntroServer.getRoom(room).getClient(client[0],Integer.parseInt(client[1])).setName(newName);
			pout.println(IntroServer.RRCMD_RENAME + " " + client[2] + " " + line[3]);
		}

		//Sends a list of Clients in a <RM> to the client
		else if(line[0].equals(IntroServer.RRCMD_CLIENTS)){
			String room = line[1];
			Room r = IntroServer.getRoom(room);
			if(r != null)
				pout.println(IntroServer.RRCMD_CLIENTS + " " + room + " " + r.getClientsToString());
			else
				pout.println(IntroServer.RRCMD_CLIENTS + " Could not find room " + room);
			
		}
		
		//Sends a list of rooms to the client
		else if(line[0].equals(IntroServer.RRCMD_ROOMS)){
			String result = "";
			Iterator<Room> itr = IntroServer.rooms.iterator();
			while(itr.hasNext()){
				Room r = itr.next();
				result += " " + r.getName();
				
			}
			pout.println(IntroServer.RRCMD_ROOMS + result);	
		}
		
		//Joins a room
		else if(line[0].equals(IntroServer.RRCMD_JOIN)){
			String room = line[1];
			String [] ipp = line[2].split(":");
			String joinResult = IntroServer.RRCMD_JOIN + " " + room;
			IntroServer.addClientToRoom(room,ipp[0],Integer.parseInt(ipp[1]),ipp[2]);
			Room r = IntroServer.getRoom(room);
			if(r != null){
				joinResult += " " + r.getClientsToString();
			}
			pout.println(joinResult);
			
		//Otherwise
		}else{
			pout.println("Invaild Server command");
		}
		
		System.out.println();
		pout.flush();
	}
	
	
	/**
	 * Runs the Server Thread
	 * and calls the parseServerCommand on every message that is received
	 */
	public void run(){
		System.out.println("Connection from " + currentClient);
		try {
			Scanner cin = new Scanner(currentClient.getInputStream());
			PrintStream cout = new PrintStream(currentClient.getOutputStream());
			while(flag){
				String clientMessage = "";

				while (cin.hasNextLine()) {
					
					clientMessage = cin.nextLine();
					System.out.println("Server saw \"" + clientMessage + "\""); //Debugging
					parseServerCommand(clientMessage,cout);
			
				}
				flag = false;
				cin.close();
				
			}
		} catch (IOException e){
			System.exit(0);
		}
			
	}
}
	

