package gui;

import interfaces.GUIPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import client.Chat;
import client.ChatMain;

/**
 * IntroPanel's purpose is to grab information for the chat program
 *  
 * @author Croller
 *
 */

//Creating JFrame
@SuppressWarnings("serial")
public class IntroPanel extends JPanel implements GUIPanel{
	
	
	private Image background;
	
	private ActionListener a1;
	
	//Text / Check boxes
	private JTextField startRoom = new JTextField("lobby");
	private JTextField serverInfo = new JTextField("localhost:5000");
	private JTextField name = new JTextField("Default Name");
	private JTextField port = new JTextField("21212");
	
	//Labels
	private JLabel title = new JLabel("CRAM");
	private JLabel startRoomLabel = new JLabel("Start Room :");
	private JLabel serverInfoLabel = new JLabel("Server IPP (*) :");
	private JLabel nameLabel = new JLabel("Name (*) :");
	private JLabel portLabel = new JLabel("Local Port (*) :");
	
	
	public final static String rnRegex = "[_A-Za-z][_0-9A-Za-z]*";
	//Button
	private JButton submit;
	//Error JLabels
	private JLabel startRoomError;
	private JLabel serverInfoError;
	private JLabel nameError;
	private JLabel portError;
	
	private String serveripp;
	private int localport;
	private String clientName;
	private String startRoomName;
	
	private int count;
	
	public IntroPanel() throws IOException{
		
		background = Toolkit.getDefaultToolkit().createImage(
				"../res/Brushed Metal by Miatari (5).png");
		
		submit = new JButton("SUBMIT");
		submit.setLocation(250, 375);
		submit.setSize(100,50);
		submit.addActionListener(a1 = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if(getInfo()){
					try {
						ChatMain.setChat(new Chat(serveripp, localport, clientName, startRoomName));			
					} catch (InterruptedException e1) {
						System.out.println("Can't set chat.... Re- run and retry");
					} catch (IOException e1) {
						System.out.println("Can't set chat.... Re- run and retry");
					}
				}
			}
		});
		
		setLayout(null);
		add(submit);
		setTextBoxes();
		setLabels();
	}
	
	//Sets the Text Boxes
	private void setTextBoxes(){
		name.setLocation(300,150);
		name.setSize(150,40);
		name.setBackground(Color.DARK_GRAY);
		name.setForeground(Color.WHITE);
		name.setCaretColor(Color.WHITE);
		
		port.setLocation(300,200);
		port.setSize(150,40);
		port.setBackground(Color.DARK_GRAY);
		port.setForeground(Color.WHITE);
		port.setCaretColor(Color.WHITE);
		
		serverInfo.setLocation(300,250);
		serverInfo.setSize(150,40);
		serverInfo.setBackground(Color.DARK_GRAY);
		serverInfo.setForeground(Color.WHITE);
		serverInfo.setCaretColor(Color.WHITE);
		
		startRoom.setLocation(300,300);
		startRoom.setSize(150,40);
		startRoom.setBackground(Color.DARK_GRAY);
		startRoom.setForeground(Color.WHITE);
		startRoom.setCaretColor(Color.WHITE);
		
		add(name);
		add(port);
		add(serverInfo);
		add(startRoom);
	}
	
	//Sets the Labels 
	private void setLabels(){
		title.setLocation(260, 0);
		title.setSize(300,200);
		title.setFont(new Font("Serif", Font.BOLD, 24));
		//title.setBackground(new Color(0,0,0,0));
		title.setForeground(Color.WHITE);
		
		nameLabel.setLocation(150, 150);
		nameLabel.setSize(150, 40);
		//title.setBackground(new Color(0,0,0,0));
		nameLabel.setForeground(Color.WHITE);
		
		portLabel.setLocation(150,200);
		portLabel.setSize(150,40);
		//title.setBackground(new Color(0,0,0,0));
		portLabel.setForeground(Color.WHITE);
		
		serverInfoLabel.setLocation(150, 250);
		serverInfoLabel.setSize(150,40);
		//title.setBackground(new Color(0,0,0,0));
		serverInfoLabel.setForeground(Color.WHITE);
		
		startRoomLabel.setLocation(150,300);
		startRoomLabel.setSize(150, 40);
		//title.setBackground(new Color(0,0,0,0));
		startRoomLabel.setForeground(Color.WHITE);
		
		add(title);
		add(nameLabel);
		add(portLabel);
		add(serverInfoLabel);
		add(startRoomLabel);
		
	}

	//Gets the info from the Text fields 
	private boolean getInfo(){
		boolean temp = true;
		serveripp = serverInfo.getText();
		if(!serveripp.contains(":")){
			if(count>0){
				remove(serverInfoError);
				validate();
				repaint();
			}
			serverInfoError = new JLabel("Incorrect IPP");
			serverInfoError.setLocation(450, 250);
			serverInfoError.setSize(150,40);
			serverInfoError.setForeground(Color.RED);
			add(serverInfoError);
			validate();
			repaint();
			serverInfo.setText("");
			temp = false;
		}
		try{
			localport = Integer.parseInt(port.getText());
			if(!isAvailable(localport)){
				if(count>0){
					remove(portError);
					validate();
					repaint();
				}
				portError = new JLabel("Incorrect Port");
				portError.setLocation(450, 200);
				portError.setSize(150,40);
				portError.setForeground(Color.RED);
				add(portError);
				validate();
				repaint();
				port.setText("");
				temp = false;
			}
		}catch(Exception e1){
			if(count>0){
				remove(portError);
				validate();
				repaint();
			}
			portError = new JLabel("Not a number");
			portError.setLocation(450, 200);
			portError.setSize(150,40);
			portError.setForeground(Color.RED);
			add(portError);
			validate();
			repaint();
			port.setText("");
			temp = false;
		}
		
		clientName = name.getText();
		if(!checkRegex(clientName)){
			if(count>0){
				remove(nameError);
			}
			nameError = new JLabel("Incorrect Name");
			nameError.setLocation(450, 150);
			nameError.setSize(150,40);
			nameError.setForeground(Color.RED);
			add(nameError);
			name.setText("");
			temp = false;
		}
		
		startRoomName = startRoom.getText();
		if(!checkRegex(startRoomName)){
			if(count>0){
				remove(startRoomError);
			}
			startRoomError = new JLabel("Incorrect Name");
			startRoomError.setLocation(450, 300);
			startRoomError.setSize(150,40);
			startRoomError.setForeground(Color.RED);
			add(startRoomError);
			startRoom.setText("");
			temp = false;
		}
		count++;
		return temp;
	}
	
	/**
	 * TODO check this
	 * 
	 * @param ipp
	 * @return
	 */
	public boolean checkServerConnection(String ipp){
		return false;
	}
	
	//Checks the input Regex
	public boolean checkRegex(String s){
		Pattern r = Pattern.compile(rnRegex);
        Matcher m = r.matcher(s);
        if(m.find())
        	return true;
        return false;
	}
	
	/** Checks to see if a specific port is available.
	*
	* @param port the port to check for availability
	*/
	public static boolean isAvailable(int port) {
	    if (port < 999 || port > 50000) {
	        throw new IllegalArgumentException("Invalid start port: " + port);
	    }
	    DatagramSocket ds = null;
	    try {
	        ds = new DatagramSocket(port);
	        ds.setReuseAddress(true);
	        return true;
	    } catch (IOException e) {
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }
	    }
	    return false;
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
		removeAll();
		repaint();
	}
	
}
 
