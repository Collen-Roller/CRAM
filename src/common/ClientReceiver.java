package common;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import client.Chat;

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
    	  Client cc = new Client(packet.getAddress().getHostName(),packet.getPort());
    	  if(!findClient(cc)){
    		  Chat.listOfClients.add(cc);
    	  }
    	  String [] response = new String(packet.getData(), 0, packet.getLength()).split(":");
    	  String formattedResponse = ""; 
    	  for(int i=0; i<response.length; i++){
    		  if(i == 0){
    			  formattedResponse += "[" + response[0] + "]";
    		  }else if(i == 1){
    			  formattedResponse += " " + response[1] + " ";
    		  }else if(i == 2){
    			  if(response[2].equals("GOODBYE")){
    				  Chat.removeClient(packet.getAddress().getHostAddress(), packet.getPort());
    			  }
    			  formattedResponse += "- " + response[2];
    		  }else{ 
    			  formattedResponse += ":" + response[i];
    		  }
    	  }
    	  
    	  
    	  //When you send a message send ROOM:NAME:MESSAGE
    	  System.out.println(formattedResponse);
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
  public boolean findClient(Client c){
 	 for(Client c1 : Chat.listOfClients){
 		 if(c.getIPP().equals(c1.getIPP())){
 			 return true;
 		 }
 	 }
 	 return false;
  }
}