package server;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import common.Client;

/**
 * Room class to hold a set of clients
 * access the name of the room
 * add / remove clients from the room
 * 
 * @author Croller
 *
 */
public class Room {

	private String name;

	//set of clients in the room
	public Set<Client> clients = new HashSet<Client>();
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Constructor
	 * 
	 * @param name - name of room
	 */
	public Room(String name){
		clients = new HashSet<Client>();
		this.name = name;
	}

	public String getClientsToString(){
		String result = "";
		Iterator<Client> itr = clients.iterator();
		while(itr.hasNext()){
			Client c = itr.next();
			result += c.getIPP()+ ":" + c.getName() + " ";
		}
		return result;
	}
	
	/**
	 * Adds a new client to the set of clients in this room
	 * 
	 * @param s
	 * @param n
	 */
	public void addClient(String s, int n, String name) {
		clients.add(new Client(s,n,name));
	}
	
	/**
	 * Removes a clients from the list of clients in this room
	 * 
	 * @param s
	 * @param n
	 */
	public void removeClient(String s, int n){
		String ipp = s + ":" + n;
		Iterator<Client> itr = clients.iterator();
		while(itr.hasNext()){
			Client c = itr.next();
			if(c.getIPP().equals(ipp)){
				itr.remove();
				return;
			}
			
		}
	}

	/**
	 * @return name of this room
	 */
	public String getName(){
		return name;
	}
	
	public boolean contains(String ipp){
		Iterator<Client> itr = clients.iterator();
		while(itr.hasNext()){
			Client c = itr.next();
			if(c.getIPP().equals(ipp))
				return true;
		}
		return false;
	}

	public Client getClient(String s, int n) {
		String ipp = s + ":" + n;
		Iterator<Client> itr = clients.iterator();
		while(itr.hasNext()){
			Client c = itr.next();
			if(c.getIPP().equals(ipp)){
				return c;
			}
		}
		return null;
	}
	
}
