package fr.benjul.paintcoop.network;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;
import fr.benjul.paintcoop.graphic.Frame;
import fr.benjul.paintcoop.system.Packet;

public class Client  {

	//////// Initialisation des variables ////////
	
	//Permet de lire les objets arrivant et d'�crire les objets � envoyer
	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput;
	
	//Permet de cr�� un lien entre deux machines
	private Socket socket;

	//Permet d'afficher une date
	private SimpleDateFormat sdf;
	
	//Addresse du serveur, pseudo utilis� et port
	private String server, username;
	private int port;

	//Constructeur de notre Client (IP du serveur, port et pseudo)
	public Client(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
		
		//Permet d'avoir une date sous la forme Heure:Minute:Seconde
		sdf = new SimpleDateFormat("HH:mm:ss");

	}
	
	//Lancement de notre client
	public boolean start() {
		// Nous essayons de nous connect� au serveur
		try {
			socket = new Socket(server, port);
		} 
		// Fail -> Stop the connection
		catch(Exception ec) {
			println("Impossible de se connecter:" + ec);
			return false;
		}
		
		println("Connexion r�ussie " + socket.getInetAddress() + ":" + socket.getPort());
	
		// Intensification de nos variables permettant de lire et d'�crire des objets
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			println("Impossible de cr�er les flux (Ecriture / Lecture): " + eIO);
			return false;
		}

		// Lancement du processus permettant d'�couter le serveur
		new ListenFromServer().start();
		// Nous envoyer notre premier objet au serveur qui va contenir notre pseudo
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			println("Impossible d'envoyer notre pseudo : " + eIO);
			disconnect();
			return false;
		}
		
		//Nous retournons Vrai car nous avons r�ussi � se connecter au serveur
		return true;
	}

	//Permet de faire du "Debug" plus d�taill� ([Date] [Client] Message) 
	private void println(String msg) {

			System.out.println("[" + sdf.format(new Date()) + "] [Client] " + msg);      // println in console mode
	}
	
	//Permet d'envoyer une trame au serveur contenant notre objet
	public void sendPacket(Packet msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			println("Erreur lors de l'envoie d'un objet: " + e);
			Frame.isConnected = false;
			disconnect();
		}
	}
	
	//Permet de se d�connect� en fermant tout les flux utilis�s (Ecriture, Fermeture, Connexion au serveur)
	public void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} // not much else I can do
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} // not much else I can do
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} // not much else I can do
		
			
	}
	
	//Processus permettant d'�couter le serveur
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					//Lecture des objets envoy� par le serveur | Il ne peut qu'�couter des objets de type Packet
					Packet msg = (Packet) sInput.readObject();
					
					//En fonction du type de packet
					switch(msg.getPacketType())
					{
					case POINT:
						Frame.getPanel().addPoint(msg.getPointPacket());
						break;
						
					case MOUSE:
						Frame.getPanel().x = msg.getMouse().getX();
						Frame.getPanel().y = msg.getMouse().getY();
						Frame.getPanel().name = msg.getMouse().getPseudo();
						Frame.getPanel().repaint();
						break;
						
					case CLEAR:
						Frame.getPanel().reset();
						break;
						
					case BUCKET:
						Frame.getPanel().list.addAll(msg.getPicture().getList());
						Frame.getPanel().repaint();
						System.out.println("Recieved !");
						break;
					
						default:
							
							break;
					}
					
					System.out.println("[" + sdf.format(new Date()) + "] [Client] [Debug] Packet " + msg.getPacketType() + " has been recieved !");
					

				}
				catch(IOException e) {
					println("Le serveur a ferm� la connexion: " + e);
					break;
				}
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
	
	//Permet d'obtenir le pseudo du client
	public String getPseudo()
	{
		return username;
	}
}
