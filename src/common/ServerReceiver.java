package common;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import client.Chat;

public class ServerReceiver extends Thread {
	
	private Socket serverSocket;
	private Scanner serverIn;
	
	public ServerReceiver(Socket serverSocket, Scanner sc){
		this.serverSocket = serverSocket;
		this.serverIn = sc;	
	}
	
	 /**
		 * This method will grab the first element of the reply from the server
		 * and process it
		 * \rooms - prints an active list of rooms to client
		 * \join - will take all IPP pairs following and add them to the list
		 * \exit - will leave the current room TODO need to figure out how to remove Clients...
		 * \kill - close the connection to the server
		 * 
		 * @param reply - the reply from the server to the client
	 * @throws InterruptedException 
	 */
		
	public void parseCommandFromServer(String reply) throws InterruptedException{
		String line [] = reply.split("\\s+");
		//Kills the Server
		if(line[0].equals(Chat.RRCMD_KILL)){
			//Send a message to everyone that server is killed
			try {
				serverIn.close();
				serverSocket.close();
				System.out.println(Chat.RRCMD_KILL);
				Chat.closeServerConnections();
			}catch (IOException e) {
				System.out.println("Could not close server connections");
			}
		}
		
		//Print out the list of clients in the room
		else if(line[0].equals(Chat.RRCMD_CLIENTS)){
			String clients = "";
			for(int i=1; i<line.length; i++){
				clients += line[i] + " "; 
			}
			System.out.println("Reply from server : " + Chat.RRCMD_CLIENTS + " " + clients);
		}
		
		//Exits the Room
		else if(line[0].equals(Chat.RRCMD_EXIT)){
			long time = 100;
			String room = line[1];
			Chat.sendGoodbye = true;
			Thread.sleep(time); //Sleep to allow the Goodbye to be sent 
			System.out.println("Reply from server : " + Chat.RRCMD_EXIT + " " + room);
			
		}
		
		//Print out the list of rooms
		else if(line[0].equals(Chat.RRCMD_ROOMS)){
			String rooms = "";
			for(int i=1; i<line.length; i++){
				rooms += line[i] + " ";
			}
			System.out.println("Reply from server : " + Chat.RRCMD_ROOMS + " " + rooms);
		}
		//Print out the room you joined followed by all of the addresses
		else if(line[0].equals(Chat.RRCMD_JOIN)){
			String room = line[1];
			String [] temp = new String [line.length-2];
			for(int i=2; i<line.length; i++){
				temp[i-2] = line[i]; 
			}
			
			//Remove all current clients and then add new ones
			Chat.listOfClients.removeAll(Chat.listOfClients);
			
			//Add IPP's of people in room to listOfClients
			for(int i=0; i<temp.length; i++){
				String [] ipp = temp[i].split(":");
				Client tempClient = new Client(ipp[0],Integer.parseInt(ipp[1]));
				findClientAndAdd(tempClient);
			}
			//Get IPP pairs from clients 
			String result = "";
			for(Client c: Chat.listOfClients){
				result += c.getIPP() + " ";
			}
			Chat.setCurrentRoom(room);
			Chat.sendHello = true;
			System.out.println("Reply from server : " + Chat.RRCMD_JOIN + " " + room + " " + result);
			System.out.print("> ");
		}
	}
	/**
	 * Method to find the client, and add to list of clients if necessary.
	 * 
	 * @param c
	 */
	public void findClientAndAdd(Client c){
	 	for(Client c1 : Chat.listOfClients){
	 		if(c.getIPP().equals(c1.getIPP())){
	 			return;
	 		}
	 	}
	 	//Add the client
	 	Chat.listOfClients.add(c);
	}
	
	 @Override public synchronized void run() {
		 try {
			 while (true) {
		    	 
				 while(serverIn.hasNextLine()){
					 parseCommandFromServer(serverIn.nextLine());
		    	 }
				 
		      }
		      
		    
		  }catch (Exception e){
			  System.out.println("Problem with Server Receivers run method...");
		  }
	 }
}
