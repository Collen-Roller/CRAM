package common;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

import client.Chat;

/**
 * This class prepares incoming messages to be sent out
 * 
 * @author Croller
 * 
 */
public class TypingToQueue extends Thread {
  private final Scanner fin;
  private final BlockingQueue<String> messageQueue;
  private final BlockingQueue<String> commandQueue;

  /**
   * Construct a new TypingToQueue instance. Set the scanner to scan the
   * keyboard and initialize the server and regular message queue values (these
   * are where server and regular messages are copied after a line is read).
   *
   * @param in
   *          Where to read information from (permits redirection to a file
   *          rather than hard coding the keyboard)
   * @param messageQueue
   *          The regular message queue for messages to be dispatched to all
   *          members of the chat group
   * @param commandQueue
   * 		  The command that will be send to the server to be handled
   * 		
   */
  public TypingToQueue(InputStream in,
    BlockingQueue<String> messageQueue, BlockingQueue<String> commandQueue) {
    this.messageQueue = messageQueue;
    this.commandQueue = commandQueue;
    this.fin = new Scanner(in);
  }

  @Override public void run() {
    String line;
    System.out.print("> ");
    while ((line = fin.nextLine()) != null) {
      try {
   
    	  //If the input is \\join, we want to leave the current room
    	  //Then join the new room (one room at a time)
    	  if(line.contains(Chat.CMD_JOIN)){
    		  if(line.contains(Chat.getCurrentRoom())){
    			  System.out.println("Can't Join a room that you're already in");
    		  }else{
    			  commandQueue.put("\\leave " + Chat.getCurrentRoom());
    			  commandQueue.put(line);
    			  Chat.doubleCommand();
    		  }
    	  }
    	  
    	  //If the input is \\leave, leave the current room
    	  //cant leave a room that your not in, nor can you leave the lobby
    	  else if(line.contains(Chat.CMD_LEAVE)){
    		  if(line.contains(Chat.DEFAULT_ROOM)){
    			  System.out.println("Can't leave the lobby unless you join a new room");
    		  }else if(!line.contains(Chat.getCurrentRoom())){
    			  System.out.println("You can't leave a room that you're not in");
    		  }else{
    			  commandQueue.put(line);
    			  commandQueue.put("\\join " + Chat.DEFAULT_ROOM);
    			  Chat.doubleCommand();
    		  }
    	  }else if(line.contains(Chat.CMD_NAME)){
    		  String [] name = line.split("\\s+");
    		  if(name.length > 1 && Chat.checkRegex(name[1])){
    			  messageQueue.put("\\RENAMED " + Chat.getName() + " To " + name[1]);
    			  Chat.message();
    			  Chat.setName(name[1]);
    		  }else{
    			  System.out.println("Couldent change name");
    		}
    	  }
    	  //Puts Command into Server Command Queue
    	  else if(line.contains("\\")){
    		  commandQueue.put(line);
    		  Chat.command();
    	  }
    	  
    	  //Puts Message into message queue
    	  else{
    		  messageQueue.put(line);
    		  Chat.message();
    	  }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    
      System.out.print("> ");
    }
  }
}
