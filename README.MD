#CRAM

Peer-to-Peer UDP Chat & TCP Server

##Description

This piece of software was written for an undergraduate Computer Networks course.
The initial assignment sparked my interest to create graphics for the project which
eventually led to the birth of CRAM (Collen Roller's Awesome Messenger)

##The Server (TCP)

The Server runs on a specified port and listens for connections to be made. 
ServerMain runs and creates a new instance of IntroServer which holds a set of rooms 
that clients can be in. The purpose of the server is to hold information of clients, 
so that other clients can connect to the server and send commands. 

Command List

	\JOIN <RM> <IPP>    	:	 Introduction/join room request
	\JOIN <RM> (<IPP>)+ 	:	 Room member response; includes the member joining

	\ROOMS              	:	 List of active rooms request
	\ROOMS (<RM>)+      	:	 Room list response; all non-empty rooms

	\EXIT <RM> <IPP>    	:	 Leave room request
	\EXIT  <RM>         	:	 Room client left, leaving a non-existent room or a room the client is not in is NOT AN ERROR

	\KILL               	:	 Server kill command
	\KILL               	:	 Server kill response, server terminates after sending this response

	\CLIENTS <RM>       	:	 List of active clients in room request
	\CLIENTS (<IPP>)+   	:	 Clients response; list of all clients in room

Each command thats parsed by the server will send back some information, which is
the second listing of each command.

##Running The Server

1) Compile the ServerMain as well as ServerThread from inside the src directory

	$ javac server/ServerMain.java server/ServerThread.java"

2) Then you must run the ServerMain which will create a new instance of IntroServer

he argument that is expected is the port number

	$ java server/ServerMain --port <port>"


##The Client (UDP)

The Client attempts to connect to the server using a IPP, and is added to a room,
(lobby, if other not specified in args) using a default port of 21212 or a 
specific port specified in the IntroPanel

##Running The Client

1) Compile the client from inside the src directory

	"javac */*.java"

2) Run the client with necessary arguments

	"java client/ChatMain"
					
##List of Client Commands
	 
How Does the Client Connect to the Server?

###\rooms      		
	Contacts the introduction server and asks for a list of the rooms known to the server. Displays the results for 
###\clients <RM> 
	Contacts the introduction server and asks for a list of clients in an existing room, if room doesn't exist the 
	server will send back \CLIENTS <RM> with nothing
###\join <RM>     
	Leave current room, and join a new or existing room contacts the introduction server asking for
	a list of room members from the introduction server When the reply comes back, HELLO goes out to
        all members of the room other than this peer. Note that this request results in the creation of a new room if
        the server has never heard of it
###\leave <RM>    
	Leave the given room. Send \EXIT to the server and sends GOODBYE to all other peers in the room which will
	cause each recipient to remove them from their list and the sender will stop displaying any more messages
	from that room. The peer should set the current room to the start_room specified on the command-line. 
	This implies, correctly, that you cannot \leave the start room. Trying should give a reasonable error message
###\name <NM>
	Set <NM> to be the "handle" used by this peer in front of all messages sent until it is reset. A message will
	send to all clients in room that clients name changed
###\quit
	This peer runs \leave for the current room and then terminate
###\kill
	Send \KILL to the introduction server and terminate.
