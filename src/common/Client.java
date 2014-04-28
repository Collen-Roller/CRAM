package common;

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
	
	public Client(String machineName, int port){
		this.machineName = machineName;
		this.port = port;
		setInetAddress();
	}
	
	public Client(InetAddress address, int port2) {
		this.machineName = address.getHostAddress();
		this.port = port2;
		this.ina = address;
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
