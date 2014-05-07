package common;

import java.util.concurrent.BlockingQueue;

import client.Chat;

/**
 * This class prepares incoming messages to be sent out
 * 
 * @author Croller
 * 
 */
public class TypingToQueue extends Thread {
  private final BlockingQueue<String> messageQueue;
  private final BlockingQueue<String> commandQueue;
  public static String line;

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
  public TypingToQueue(BlockingQueue<String> messageQueue, 
		  				BlockingQueue<String> commandQueue) {
    this.messageQueue = messageQueue;
    this.commandQueue = commandQueue;
  }

  @Override public void run() {
    String line;
    System.out.print("> ");
    //while ((line = fin.nextLine()) != null) {
    while(true){
      try {
    	  
    	  //Need to use a semaphore here
    	  if(Chat.grabCommand){
    		  line = Chat.getInputLines();
    		  Chat.sem.release();
    	 
	    	  //if line is "" then skip it
	    	  if(line.equals(""))
	    		  continue;
	    	  
	    	  //If the input is \\join, we want to leave the current room
	    	  //Then join the new room (one room at a time)
	    	  else if(line.contains(Chat.CMD_JOIN)){
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
	    			  Chat.setOutputLine("Can't leave the lobby unless you join a new room");
	    			  System.out.println("Can't leave the lobby unless you join a new room");
	    		  }else if(!line.contains(Chat.getCurrentRoom())){
	    			  Chat.setOutputLine("You can't leave a room that you're not in");
	    			  System.out.println("You can't leave a room that you're not in");
	    		  }else{
	    			  commandQueue.put(line);
	    			  commandQueue.put("\\join " + Chat.DEFAULT_ROOM);
	    			  Chat.doubleCommand();
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
	    	Thread.sleep(100);  
    	  }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
