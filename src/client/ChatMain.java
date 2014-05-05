package client;

import java.io.IOException;

import gui.GUI;


/**
 * Main class to hold the GUI as well as invoke the program
 * 
 * @author Croller
 *
 */
public class ChatMain {

	
	private static GUI mainWindow;
	

	public static void main(String [] args) throws IOException, InterruptedException{
		startChat();
	}	
	
	public static void startChat() throws IOException {
        if (mainWindow == null)
            mainWindow = new GUI();
    }
	
	public static void setChat(Chat c) throws InterruptedException{
		mainWindow.setPanel(c);
	}
	
	public static GUI getGUI(){
		return mainWindow;
	}
}
