package org.cram.common;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Class Client to hold client information
 * -inetaddress
 * -machineName
 * -port
 * 
 * @author Croller
 *
 */
public class Client implements Comparable<Client> {


	private InetAddress ina;
	private String machineName;
	private int port;
	private String name;
	
	public Client(String machineName, int port, String name){
		this.machineName = machineName;
		this.port = port;
		this.name = name;
		setInetAddress();
	}

	//Setter for InetAddress among construction
	public void setInetAddress(){
		try {
			ina = InetAddress.getByName(machineName);
		} catch (UnknownHostException e) {
			System.out.println("Can't set InetAddress for client " + machineName+ ":" + port);
		}
	}
	
	//Getter for IPP
	public String getIPP(){
		return ina.getHostAddress()+":"+port;
	}
	
	//Getter for name
	public String getMachineName(){
		return machineName;
	}
	
	//Getter for port # of client
	public int getPort(){
		return port;
	}
	
	//Setter for Name
	public void setName(String s){
		this.name = s;
	}
	
	//Getter for Name
	public String getName(){
		return name;
	}
	
	//Getter for inet address of client
	public InetAddress getInetAddress(){
		return ina;
	}
	
	@Override
	public int hashCode() {
		return getIPP().hashCode();
	}

	public int compareTo(Client o) {
		return this.getIPP().compareTo(o.getIPP());
	}
	
	
}
