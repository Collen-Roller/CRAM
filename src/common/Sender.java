package common;



import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import client.Chat;


/**
 * Class Sender to send messages to clients and commands to the server
 * 
 * @author Croller
 *
 */
public class Sender extends Thread {

  private BlockingQueue<String> messageQueue; //Queue of messages for clients
  private BlockingQueue<String> commandQueue; //Queue of commands for the server
  private byte[] sendData;//byte [] of data 
  private DatagramSocket socket; //Datagram socket to receive messages
  private PrintStream serverOut; //PrintStream to server
  
  public Sender(DatagramSocket socket,Socket serverSocket, PrintStream serverOut,
      BlockingQueue<String> messageQueue, BlockingQueue<String> commandQueue) throws InterruptedException {
    this.messageQueue = messageQueue;
    this.commandQueue = commandQueue;
    this.socket = socket;
    this.serverOut = serverOut;
    
    try {
		serverOut = new PrintStream(serverSocket.getOutputStream());
		sendCommandToServer(Chat.CMD_JOIN + " " + Chat.getCurrentRoom());
		/*if(Chat.initSendToServerRoom){
			sendCommandToServer(Chat.CMD_ROOMS);
		}if(Chat.initSendToServerKill){
			sendCommandToServer(Chat.RRCMD_KILL);
		}
		*/
	} catch (IOException e) {
		System.out.println("Can't Set OutPutStream with serverSocket");
	}
    
  }
  
  /**
   * Method to send a command to the server
   * 
   * @param command
   * @throws IOException
 * @throws InterruptedException 
   */
  public synchronized void sendCommandToServer(String command) throws IOException, InterruptedException{
		//send server command
		String [] line = command.split("\\s+");

		//ROOMS
		if(line[0].equals(Chat.CMD_ROOMS)){
			serverOut.println(Chat.RRCMD_ROOMS);
		}
		
		//Clients
		else if(line[0].equals((Chat.CMD_CLIENTS)) && line.length > 1){
			serverOut.println(Chat.RRCMD_CLIENTS + " " + line[1]);
		}
		
		//JOIN
		else if(line[0].equals(Chat.CMD_JOIN) && line.length > 1){
			if(Chat.checkRegex(line[1])){
				//String info = Chat.RRCMD_JOIN + " " + line[1] + " " + Chat.getIPP();
				//***********NEW STUFF
				String info = Chat.RRCMD_JOIN + " " + line[1] + " " + Chat.getIPP()+":"+Chat.getClientName();
				//***********
				serverOut.println(info);
			}else{
				Chat.setOutputLine("Invalid room name, " + line[1] + " Could not process request... ");
				System.out.println("Invalid room name, " + line[1] + " Could not process request... ");
			}
		}
		
		//LEAVE
		else if(line[0].equals((Chat.CMD_LEAVE)) && line.length > 1){
			String info = line[1] + " " + Chat.getIPP();
			serverOut.println(Chat.RRCMD_EXIT + " " + info);
			
		}
		
		//NAME
		else if(line[0].equals((Chat.CMD_NAME)) && line.length > 1){
			if(Chat.checkRegex(line[1])){
				String info = Chat.getCurrentRoom() + " " + Chat.getIPP() + ":" + Chat.getClientName() + " " + line[1];
				serverOut.println(Chat.RRCMD_RENAME + " " + info);
			}else{
				Chat.setOutputLine("Could not change name from " + Chat.getClientName() + " to " + line[1]);
				System.out.println("Could not change name from " + Chat.getClientName() + " to " + line[1]);
			}
		}
		
		
		//QUIT
		else if(line[0].equals((Chat.CMD_QUIT))){
			String info = Chat.getCurrentRoom() + " " + Chat.getIPP();
			serverOut.println(Chat.RRCMD_EXIT + " " + info);
			Chat.closeServerConnections();
			
		}
		
		//KILL
		else if(line[0].equals((Chat.CMD_KILL))){
			serverOut.println(Chat.RRCMD_KILL);
			serverOut.close();
		}
		
		//Unknown Server Command
		else{
			Chat.setOutputLine("Unknown Command");
			System.out.println("unknown command");
			
		}	
		serverOut.flush();
	}
  
  
  /**
   * Will send message to the list of clients to send to
   * 
   * @param message
   */
  public synchronized void sendMessageToClients(String message){
	  
	//Sends Messages to Clients
	  String info = Chat.getCurrentRoom() + ":" + Chat.getClientName() + ":" + message;
	  sendData = info.getBytes();
	  Iterator<Client> itr = Chat.listOfClients.iterator();
	  while(itr.hasNext()){
		  Client c = itr.next();
		  //if(c.getIPP().equals(Chat.getIPP()))
		  //continue;
		  DatagramPacket packet = new DatagramPacket(sendData,
				  sendData.length, c.getInetAddress(), c.getPort());
		  try {
			socket.send(packet);
		} catch (IOException e) {
			System.out.println("Could not send message to " + c.getIPP());
		}
	  }
	  
  }
  
  @Override
  public void run() {
    while (true) {
      try {
    	  
    	  //Flag to send an Initial message to clients
    	  if(Chat.sendHello){
    		  sendMessageToClients("HELLO I AM YOUR LEADER");
    		  Chat.resetHello();
    	  }
    	  
    	  else if(Chat.sendRename){
    		  sendMessageToClients("RENAME");
    		  Chat.resetRename();
    	  }
    	  
    	  //Flag so that when a client leaves a room, the other clients wont receive
    	  //Messages from them anymore
    	  else if(Chat.sendGoodbye){
    		  sendMessageToClients("GOODBYE");
    		  Chat.resetGoodbye();
    		  Chat.resetHello();
    		  Chat.removeCurrentClients();
    		  Chat.setCurrentRoom("none");
    		  
    	  }
    	  
    	  else if(Chat.doubleCommand){
    		  sendCommandToServer(commandQueue.take());
    		  Chat.resetDCM();
    	  }
    	  
    	  //Sends Command To Server
    	  else if(Chat.command){
    		  sendCommandToServer(commandQueue.take()); 
    		  Chat.resetCM();
    	  }
    	  
    	  //Sends MEssage to Clients
    	  else if(Chat.message){
    		  sendMessageToClients(messageQueue.take());
    		  Chat.resetCM();
    	  }

    	  Thread.sleep(100);
    	  //Didn't Send Anything
    	 
      } catch (Exception e){
    	  System.out.println("Problem sending command to server on client side");
      }
    }

  }
}
