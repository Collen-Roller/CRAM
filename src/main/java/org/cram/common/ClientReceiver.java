package org.cram.common;


import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.cram.client.Chat;

/**
 * 
 * Class ClientReceiver to receive messages from clients and print them to 
 * local clients system
 * 
 * @author Croller
 *
 */
public class ClientReceiver extends Thread {

    //Max buffer size
    public final int bufferSize = 256;
    byte[] receiveData;
    private DatagramSocket socket;

    public ClientReceiver(DatagramSocket socket) {
    this.socket = socket;
    this.receiveData = new byte[bufferSize];
  }
  

  @Override public void run() {
    try {
      while (true) {
    	  
    	  DatagramPacket packet = new DatagramPacket(receiveData,
    				  receiveData.length);
    	  socket.receive(packet);
    	 
    	  /*
    	  Client cc = new Client(packet.getAddress().getHostName(),packet.getPort());
    	  if(!findClient(cc)){
    		  Chat.listOfClients.add(cc);
    	  }
    	  */
    	  String [] response = new String(packet.getData(), 0, packet.getLength()).split(":");
    	  
    	  Client cc = new Client(packet.getAddress().getHostName(),packet.getPort(),response[1]);
    	  if(!findClient(cc)){
    		  Chat.listOfClients.add(cc);
    	  }
    	  
    	  String formattedResponse = ""; 
    	  for(int i=0; i<response.length; i++){
    		  if(i == 0){
    			  formattedResponse += "[" + response[0] + "]";
    		  }else if(i == 1){
    			  formattedResponse += " " + response[1] + " ";
    		  }else if(i == 2){
    			  if(response[2].equals("GOODBYE")){
    				  //remove the client from the buddy list
    				  System.out.println(response[1]);
    				  Chat.removeClient(packet.getAddress().getHostAddress(), packet.getPort());
    			  }else if(response[2].equals("RENAME")){
    				  Chat.getClient(packet.getAddress().getHostAddress(), packet.getPort()).setName(response[1]);
    			  } 
    			  Chat.setClientsOnListArea();
    			  formattedResponse += "- " + response[2];
    		  }else{ 
    			  formattedResponse += ":" + response[i];
    		  }
    	  }
    	  
    	  
    	  //When you send a message send ROOM:NAME:MESSAGE
    	  System.out.println(formattedResponse);
    	  Chat.setOutputLine(formattedResponse);
    	  System.out.print("> ");
    	  Chat.playSound("Chat 3.wav");
      }
      
    }catch (Exception e ){
    	System.out.println("Problem with Client Receiver...");
    }
  }
  
  
  /**
   * Returns True if client is in the set of clients
   * 		 False otherwise
   * 
   * @param c - client to test against
   * @return
   */
  public synchronized boolean findClient(Client c){
 	 for(Client c1 : Chat.listOfClients){
 		 if(c.getIPP().equals(c1.getIPP())){
 			 return true;
 		 }
 	 }
 	 return false;
  }
}