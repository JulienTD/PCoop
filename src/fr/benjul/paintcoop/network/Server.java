package fr.benjul.paintcoop.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import fr.benjul.paintcoop.system.Packet;
import fr.benjul.paintcoop.system.PacketType;
import fr.benjul.paintcoop.system.Picture;

public class Server {
	// Nous cr�ons un identifiant unique � chaque client qui se connectec au serveur
	private static int uniqueId;
	// List qui contient tout les clients qui se connectent
	private ArrayList<ClientThread> al;

	//Permet d'afficher une date
	private SimpleDateFormat sdf;
	
	// Port du serveur
	private int port;
	// Tant que le serveur est allum�
	private boolean keepGoing;
	
	// Processus secondaire
	private Thread t;
		
	//IP du serveur
	public String ip = "";

	//Constructeur de notre Serveur (port)
	public Server(int port) {

		this.port = port;
		
		//Permet d'avoir une date sous la forme Heure:Minute:Seconde
		sdf = new SimpleDateFormat("HH:mm:ss");
		
		// Liste qui va contenir tout les processus qui g�re chaque clients
		al = new ArrayList<ClientThread>();
	}
	
	//Lancement de notre serveur
	public void start() {
		
		t = new Thread(new Runnable(){

			@Override
			public void run() {
				keepGoing = true;
				try 
				{
					// Cr�ation du serveur
					ServerSocket serverSocket = new ServerSocket(port);
					ip = serverSocket.getInetAddress().getHostAddress();

					// Boucle pour �couter chaque client qui souhaite se connecter
					while(keepGoing) 
					{
						// format message saying we are waiting
						println("Attente des clients sur le port " + port + ".");
						
						//Nous acceptons chaque connexion
						Socket socket = serverSocket.accept(); 
						// Si nous devons �teindre le serveur
						if(!keepGoing)
							break;
						
						//Cr�ation du processus qui va �couter chaque client puis nous le stockons dans notre liste
						ClientThread t = new ClientThread(socket);
						al.add(t);
						t.start();
						
					}
					// La boucle est finis donc nous d�connectons chaque clients
					try {
						serverSocket.close();
						for(int i = 0; i < al.size(); ++i) {
							ClientThread tc = al.get(i);
							try {
							tc.sInput.close();
							tc.sOutput.close();
							tc.socket.close();
							}
							catch(IOException ioE) {}
						}
					}
					catch(Exception e) {
						println("Erreur lors de la fermeture du serveur: " + e);
					}
				}
				catch (IOException e) {
					println("Erreur lors de la cr�ation du serveur: " + e);
				}
			}});
		t.start();

	}		

	@SuppressWarnings("resource")
	public void stop() {
		keepGoing = false;
		// Nous nous connectons en local pour stopper le serveur
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
			
		}
	}
	
	//Permet de faire du "Debug" plus d�taill� ([Date] [Server] Message) 
	private void println(String msg) {
			System.out.println("[" + sdf.format(new Date()) + "] [Server] " + msg);

	}
	
	//Permet d'envoyer un packet � tout le monde
	private synchronized void sendPacketToAll(Packet packet)
	{
		//Pour chaque clients ..
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			//Nous essayons de lui envoyer le packet si �a ne marche pas, nous le deconnectons
			if(!ct.writePacket(packet)) {
				al.remove(i);
				println("L'utilisateur '" + ct.username + "' a �tait d�connect� et enlever de la liste");
			}
		}
	}

	// Permet de d�connecter un utilisateur
	synchronized void remove(int id) {
		// Nous recherchons dans la liste son ID
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// Une fois trouver nous le retirons de la liste
			if(ct.id == id) {
				al.remove(i);
				return;
			}
		}
	}
	

	// Processus permettant d'envoyer des packets aux utilisateurs
	class ClientThread extends Thread {
		
		//Permet de cr�� un lien entre deux machines
		Socket socket;
		
		//Permet de lire les objets arrivant et d'�crire les objets � envoyer
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		
		// ID attribu� � chaque utilisateur
		int id;
		
		// Pseudo de l'utilisateur
		String username;
		
		// Type de trame re�u
		Packet packet;
		
		// Date de connexion
		String date;
		
		ClientThread(Socket socket) {
			
			id = ++uniqueId;
			this.socket = socket;
			
			try
			{
				// Intensification de nos variables permettant de lire et d'�crire des objets
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// Lecture du pseudo de l'utilisateur
				try {
					username = (String) sInput.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				println("L'utilisateur '" + username + "' vient de se connecter !");
			}
			catch (IOException e) {
				println("Impossible de cr�er les flux (Ecriture / Lecture): " + e);
				return;
			}

            date = new Date().toString() + "\n";
		}

		// Boucle qui va lire les packets
		public void run() {
			boolean keepGoing = true;
			
			while(keepGoing) {
				// Lecture du packet
				try {
					packet = (Packet) sInput.readObject();
				}
				catch (IOException e) {
					println("Erreur lors de la lecture de packet (Utilisateur: " + username + " " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}

				// En fonction du type de packet
				switch(packet.getPacketType()) {
				case LOGOUT:
					println("L'utilisateur '" + username + "' vient de se deconnecter !");
					keepGoing = false;
					break;
					
				case POINT:
					sendPacketToAll(packet);
					break;
					
				case MOUSE:					
					sendPacketToAll(packet);
					break;
					
				case CLEAR:
					sendPacketToAll(new Packet(PacketType.CLEAR));
					break;
					
				case BUCKET:
					sendPacketToAll(new Packet(PacketType.BUCKET, new Picture(packet.getPicture().getList())));
					break;
				default:
					break;
				}
			}
			
			//La boucle est termin�e donc nous nous d�connectons
			remove(id);
			close();
		}
		
		// Fermeture des flux Ecriture/Lecture et Socket
		private void close() {
			// try to close the connection
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		//Envoie du packet
		private boolean writePacket(Packet packet) {
			//Si l'utilisateur n'est pas connect�
			if(!socket.isConnected()) {
				//Nous fermons tout les flux qu'il utilisait
				close();
				return false;
			}
			// Ecriture du packet
			try {
				sOutput.writeObject(packet);
			}
			catch(IOException e) {
				println("Erreur lors de l'envoie de packet (Utilisateur: " + username + ")");
				println(e.toString());
			}
			return true;
		}
		
	}
}


