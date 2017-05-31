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
	// Nous créons un identifiant unique à chaque client qui se connectec au serveur
	private static int uniqueId;
	// List qui contient tout les clients qui se connectent
	private ArrayList<ClientThread> al;

	//Permet d'afficher une date
	private SimpleDateFormat sdf;
	
	// Port du serveur
	private int port;
	// Tant que le serveur est allumé
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
		
		// Liste qui va contenir tout les processus qui gère chaque clients
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
					// Création du serveur
					ServerSocket serverSocket = new ServerSocket(port);
					ip = serverSocket.getInetAddress().getHostAddress();

					// Boucle pour écouter chaque client qui souhaite se connecter
					while(keepGoing) 
					{
						// format message saying we are waiting
						println("Attente des clients sur le port " + port + ".");
						
						//Nous acceptons chaque connexion
						Socket socket = serverSocket.accept(); 
						// Si nous devons éteindre le serveur
						if(!keepGoing)
							break;
						
						//Création du processus qui va écouter chaque client puis nous le stockons dans notre liste
						ClientThread t = new ClientThread(socket);
						al.add(t);
						t.start();
						
					}
					// La boucle est finis donc nous déconnectons chaque clients
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
					println("Erreur lors de la création du serveur: " + e);
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
	
	//Permet de faire du "Debug" plus détaillé ([Date] [Server] Message) 
	private void println(String msg) {
			System.out.println("[" + sdf.format(new Date()) + "] [Server] " + msg);

	}
	
	//Permet d'envoyer un packet à tout le monde
	private synchronized void sendPacketToAll(Packet packet)
	{
		//Pour chaque clients ..
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			//Nous essayons de lui envoyer le packet si ça ne marche pas, nous le deconnectons
			if(!ct.writePacket(packet)) {
				al.remove(i);
				println("L'utilisateur '" + ct.username + "' a était déconnecté et enlever de la liste");
			}
		}
	}

	// Permet de déconnecter un utilisateur
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
		
		//Permet de créé un lien entre deux machines
		Socket socket;
		
		//Permet de lire les objets arrivant et d'écrire les objets à envoyer
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		
		// ID attribué à chaque utilisateur
		int id;
		
		// Pseudo de l'utilisateur
		String username;
		
		// Type de trame reçu
		Packet packet;
		
		// Date de connexion
		String date;
		
		ClientThread(Socket socket) {
			
			id = ++uniqueId;
			this.socket = socket;
			
			try
			{
				// Intensification de nos variables permettant de lire et d'écrire des objets
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
				println("Impossible de créer les flux (Ecriture / Lecture): " + e);
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
			
			//La boucle est terminée donc nous nous déconnectons
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
			//Si l'utilisateur n'est pas connecté
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


