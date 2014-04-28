package server;

/**
 * Main to invoke the TCP IntroServer on a specific port #
 * 
 * @author Croller
 *
 */
public class ServerMain {

	public static void main(String [] args){
		
		if (args.length != 2) { // two required command-line arguments
		      System.err.println("TCPServer <port>");
		      System.exit(1);
		}
		if(args[0].equals("--port")){
			int portNumber = Integer.parseInt(args[1]);
			IntroServer server = new IntroServer(portNumber);
			server.run();
		}
	}	
}
