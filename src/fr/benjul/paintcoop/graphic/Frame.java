package fr.benjul.paintcoop.graphic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import fr.benjul.paintcoop.PaintCoop;
import fr.benjul.paintcoop.Utils;
import fr.benjul.paintcoop.network.Client;
import fr.benjul.paintcoop.network.Server;
import fr.benjul.paintcoop.system.Mouse;
import fr.benjul.paintcoop.system.Packet;
import fr.benjul.paintcoop.system.PacketType;
import fr.benjul.paintcoop.system.Picture;
import fr.benjul.paintcoop.system.Point;
import fr.benjul.paintcoop.system.PointType;

public class Frame extends JFrame implements MouseListener {

	//////////////////// Variables ////////////////////
	private static final long serialVersionUID = 1L;

	//Cr�ation de notre JPanel
	private static Panel panel = new Panel();

	//Variables pour la cr�ation de notre menu
	private JMenuBar menuBar;
	private JMenu fichier, reseau;
	private JMenuItem nouveau, enregistrer, enregistrer_sous, fermer,
			connexion, deconnexion, serveur;
	
	//Permet de r�cup�rer les images dans notre dossier ressource
	ClassLoader classLoader = new PaintCoop().getClass().getClassLoader();

	//Liste des diff�rents boutons utilis�s
	JButton pencil = new JButton(new ImageIcon(
			classLoader.getResource("pencil.png")));

	JButton circle = new JButton(new ImageIcon(
			classLoader.getResource("circle.png")));
	JButton filledCircle = new JButton(new ImageIcon(
			classLoader.getResource("filledcircle.png")));

	JButton rect = new JButton(new ImageIcon(
			classLoader.getResource("rect.png")));
	JButton filledRect = new JButton(new ImageIcon(
			classLoader.getResource("filledrect.png")));

	JButton colors = new JButton(new ImageIcon(
			classLoader.getResource("colors.png")));
	JButton bucket = new JButton(new ImageIcon(
			classLoader.getResource("bucket.png")));

	JButton pipette = new JButton(new ImageIcon(
			classLoader.getResource("pipette.png")));

	//Chemin d'acc�s vers le fichier enregistr�
	private String currentPath = "";

	//Diff�rents champs de texte utilis�s
	private JTextField ip;
	private JTextField port;
	private JTextField pseudo;

	//Diff�rents String qui peuvent �tre affich�s
	private JLabel ipText;
	private JLabel portText;
	private JLabel pseudoText;
	
	//Variable pour la cr�ation de la barre d'outils
	private JToolBar toolBar;

	//Instance de notre class
	public Frame f;

	//Intances de notre client et serveur
	public static Client client;
	public Server server;

	//Permet de savoir si nous sommes connect� � un serveur
	public static boolean isConnected = false;

	//Type d'outils
	private PointType type = PointType.PENCIL;

	//Variable utilis� pour la cr�ation d'un rectangle, rectangle remplie, cercle, disque 
	private int x;
	private int y;

	//Couleur actuelle
	private Color currentColor = new Color(0, 0, 0);

	//Processus secondaire qui �tre utilis� pour l'outils seau
	private Thread t;

	//Constructeur de notre classe avec les param�tres de la fen�tre
	public Frame() {
		//Enregistrement de l'instance de cette classe
		f = this;
		
		//Param�tres de la fen�tre
		this.setSize(800, 800); //Dimension
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //Si nous appuyons sur la croix en haut � droite, rien ne se passera
		this.setContentPane(panel); //JPanel
		this.setTitle("PaintCoop - V0.1"); //Nom de la fen�tre
		this.setLocationRelativeTo(null); // Pour que le logiciel lors de l'ouverture soit au centre
		
		//Permet de mettre une image en icone
		try {
			//Permet d'acc�der aux images de notre logiciel
			ClassLoader classLoader = new PaintCoop().getClass().getClassLoader();
			
			BufferedImage image = ImageIO.read(classLoader.getResource("icon.png"));
			this.setIconImage(image);

		} catch (IOException e) {
			e.printStackTrace();
		}
		this.setVisible(true); // Fen�tre visible

		//Initialisation de la barre de menu, barre d'outils et des �v�nements li�s � nos composants
		initMenuBar();
		initToolBar();
		initEvents();

	}
	
	//Cr�ation de la barre d'outils
	private void initToolBar() {
		toolBar = new JToolBar();

		toolBar.add(pencil);

		toolBar.add(circle);
		toolBar.add(filledCircle);

		toolBar.add(rect);
		toolBar.add(filledRect);

		toolBar.add(colors);
		toolBar.add(bucket);

		toolBar.add(pipette);

		this.getContentPane().add(toolBar, BorderLayout.NORTH);

	}

	//Cr�ation des diff�rents �v�nements utilis�
	private void initEvents() {
		//Cette classe impl�mente une autre classe qui r�cup�re tout les �v�nements li�s � la souris
		this.addMouseListener(this);

		//Evenement li� au bouton "Nouveau" de la barre de menu
		nouveau.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				if (JOptionPane.showConfirmDialog(null,
						"Voulez-vous vraiment cr�er un nouveau fichier ?",
						"Nouveau fichier", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					panel.reset();

					if (isConnected) {
						client.sendPacket(new Packet(PacketType.CLEAR));
					}
				}

			}
		});

		//Evenement li� au bouton "Enregistrer" de la barre de menu
		enregistrer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				//Permet d'enregistrer l'image actuelle
				Container content = getContentPane();
				BufferedImage img = new BufferedImage(content.getWidth(),
						content.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g2d = img.createGraphics();
				content.printAll(g2d);
				g2d.dispose();

				try {
					ImageIO.write(img, "png", new File(currentPath));
				} catch (IOException ex) {
					ex.printStackTrace();
				}

			}
		});
		
		//Evenement li� au bouton "Enregistrer" de la barre de menu
		enregistrer_sous.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				//Permet d'enregistrer l'image actuelle
				
				//Cr�ation d'une fen�tre qui nous demande o� placer notre image
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File("C:\\"));

				int result = fileChooser.showOpenDialog(null);

				//Si nous avons appuyer sur le bouton 'Ouvrir'
				if (result == JFileChooser.APPROVE_OPTION) {

					//Enregistre le chemin choisis
					String filename = fileChooser.getSelectedFile().getPath();
					currentPath = filename;
					
					//Enregistre l'image
					Container content = getContentPane();
					BufferedImage img = new BufferedImage(content.getWidth(),
							content.getHeight(), BufferedImage.TYPE_INT_RGB);
					Graphics2D g2d = img.createGraphics();
					content.printAll(g2d);
					g2d.dispose();

					try {
						ImageIO.write(img, "png", new File(filename));
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					
					//Bouton 'Enregistrer' disponible car nous avons enregistrer le chemin d'acc�s que l'utilisateur a sp�cifi�
					enregistrer.setEnabled(true);
				}
			}
		});

		//Evenement li� au bouton "Fermer" de la barre de menu
		fermer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane
						.showConfirmDialog(null,
								"Voulez-vous vraiment quitter ?", "Quitter",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					//Si la personne est connect� � un serveur nous la d�connectons puis nous fermons la fen�tre
					if (isConnected) {
						client.sendPacket(new Packet(PacketType.LOGOUT));
					}
					System.exit(0);
				}
			}
		});

		//Evenement li� au bouton "Connexion" de la barre de menu
		connexion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				JPanel p = new JPanel();
				p.setLayout(null);

				ipText = new JLabel("IP:");
				ipText.setBounds(5, 5, 50, 10);
				p.add(ipText);

				ip = new JTextField("localhost");
				ip.setBounds(25, 0, 100, 25);
				ip.setHorizontalAlignment(SwingConstants.LEFT);
				p.add(ip);

				portText = new JLabel("Port:");
				portText.setBounds(130, 5, 50, 10);
				p.add(portText);

				port = new JTextField();
				port.setBounds(165, 0, 50, 25);
				p.add(port);

				JOptionPane
						.showConfirmDialog(null, p, "Connexion",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.PLAIN_MESSAGE);

				if (ip.getText().length() > 0 && port.getText().length() > 0) {
					int portInt = 0;
					try {
						portInt = Integer.valueOf(port.getText());

						JPanel j = new JPanel();

						j.setLayout(null);

						pseudoText = new JLabel("Pseudo:");
						pseudoText.setBounds(35, 5, 50, 10);
						j.add(pseudoText);

						pseudo = new JTextField("");
						pseudo.setBounds(90, 0, 100, 25);
						pseudo.setHorizontalAlignment(SwingConstants.LEFT);
						j.add(pseudo);

						JOptionPane.showConfirmDialog(null, j, "Connexion",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.PLAIN_MESSAGE);

						//Si la personne a sp�cifi� un pseudo de plus de 1 caract�re
						if (pseudo.getText().length() > 0) {

							client = new Client(ip.getText(), portInt, pseudo
									.getText());
							
							//Si le client s'est bien connect� au serveur
							if (client.start()) {
								connexion.setEnabled(false);
								deconnexion.setEnabled(true);
								serveur.setEnabled(false);
								isConnected = true;
							}
						}
					} catch (NumberFormatException e1) {
						JOptionPane.showMessageDialog(null,
								"Le port doit �tre une valeur num�rique !",
								"Connexion", JOptionPane.ERROR_MESSAGE, null);
					}

				}

			}
		});

		//Evenement li� au bouton "Deconnexion" de la barre de menu
		deconnexion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.sendPacket(new Packet(PacketType.LOGOUT));
				connexion.setEnabled(true);
				deconnexion.setEnabled(false);
				isConnected = false;

				if (!serveur.isEnabled()) {
					server.stop();
					serveur.setEnabled(true);
				}

			}
		});

		//Evenement li� au bouton "Serveur" de la barre de menu
		serveur.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				JPanel p = new JPanel();
				p.setLayout(null);

				portText = new JLabel("Port:");
				portText.setBounds(5, 5, 50, 10);
				p.add(portText);

				port = new JTextField("");
				port.setBounds(34, 0, 50, 25);
				port.setHorizontalAlignment(SwingConstants.LEFT);
				p.add(port);

				pseudoText = new JLabel("Pseudo:");
				pseudoText.setBounds(91, 5, 50, 10);
				p.add(pseudoText);

				pseudo = new JTextField();
				pseudo.setBounds(139, 0, 98, 25);
				p.add(pseudo);

				JOptionPane
						.showConfirmDialog(null, p, "Connexion",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.PLAIN_MESSAGE);

				try {
					int portInt = 0;
					portInt = Integer.valueOf(port.getText());
					
					//Cr�ation du serveur
					server = new Server(portInt);
					server.start();
					
					//Permet de connect� le client � son serveur
					client = new Client("localhost", portInt, pseudo.getText());
					if (client.start()) {
						connexion.setEnabled(false);
						deconnexion.setEnabled(true);
						serveur.setEnabled(false);
						isConnected = true;
					}

				} catch (NumberFormatException e1) {
					JOptionPane.showMessageDialog(null,
							"Le port doit �tre une valeur num�rique !",
							"Connexion", JOptionPane.ERROR_MESSAGE, null);
				}

			}
		});

		//Evenement li� � la fermeture de la fen�tre
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				if (JOptionPane
						.showConfirmDialog(null,
								"Voulez-vous vraiment quitter ?", "Quitter",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					if (isConnected) {
						client.sendPacket(new Packet(PacketType.LOGOUT));
					}
					System.exit(0);
				}
			}
		});

		//Evenement li� � la souris
		this.addMouseMotionListener(new MouseMotionListener() {
			//Bouton press� + d�placement
			public void mouseDragged(MouseEvent e) {
				if (type.equals(PointType.PENCIL)) {
					if (isConnected) {
						client.sendPacket(new Packet(PacketType.POINT,
								new Point(PointType.PENCIL, e.getX() - 8, e
										.getY() - 55, currentColor)));

						client.sendPacket(new Packet(PacketType.MOUSE,
								new Mouse(e.getX() - 8, e.getY() - 55, client
										.getPseudo())));

					} else {
						panel.addPoint(new Point(PointType.PENCIL,
								e.getX() - 8, e.getY() - 55, currentColor));
					}
				}
			}

			//D�placement de la souris
			@Override
			public void mouseMoved(MouseEvent e) {
				if (isConnected) {

					client.sendPacket(new Packet(PacketType.MOUSE, new Mouse(e
							.getX() - 8, e.getY() - 55, client.getPseudo())));
				}


			}
		});

		//Evenement li� au bouton "Pinceau" de la barre d'outils
		pencil.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				setPointType(PointType.PENCIL);
			}
		});

		//Evenement li� au bouton "Cercle" de la barre d'outils
		circle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				setPointType(PointType.CIRCLE);
			}
		});

		//Evenement li� au bouton "Disque" de la barre d'outils
		filledCircle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				setPointType(PointType.FILLEDCIRCLE);
			}
		});

		//Evenement li� au bouton "Rectangle" de la barre d'outils
		rect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				setPointType(PointType.RECT);
			}
		});

		//Evenement li� au bouton "Rectangle remplie" de la barre d'outils
		filledRect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				setPointType(PointType.FILLEDRECT);
			}
		});

		//Evenement li� au bouton "Couleur" de la barre d'outils
		colors.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				Color c = JColorChooser.showDialog(f, "Color", currentColor);
				if (c != null) {
					currentColor = c;
				}
			}
		});

		//Evenement li� au bouton "Seau" de la barre d'outils
		bucket.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				setPointType(PointType.BUCKET);
			}
		});

		//Evenement li� au bouton "Pipette" de la barre d'outils
		pipette.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setPointType(PointType.PIPETTE);

			}
		});

	}

	//Initialisation de la barre de menu
	private void initMenuBar() {
		menuBar = new JMenuBar();

		fichier = new JMenu("Fichier");

		nouveau = new JMenuItem("Nouveau");
		enregistrer = new JMenuItem("Enregistrer");
		enregistrer_sous = new JMenuItem("Enregistrer sous...");
		fermer = new JMenuItem("Fermer");

		fichier.add(nouveau);

		fichier.addSeparator();

		fichier.add(enregistrer);
		fichier.add(enregistrer_sous);

		fichier.addSeparator();

		fichier.add(fermer);

		reseau = new JMenu("R�seau");

		connexion = new JMenuItem("Se connecter");
		deconnexion = new JMenuItem("Se d�connecter");
		serveur = new JMenuItem("Cr�er un serveur");

		reseau.add(connexion);
		reseau.add(deconnexion);

		reseau.addSeparator();

		reseau.add(serveur);

		menuBar.add(fichier);
		menuBar.add(reseau);

		enregistrer.setEnabled(false);
		deconnexion.setEnabled(false);

		this.setJMenuBar(menuBar);

	}

	//Evement de la souris (Clic de la souris)
	@Override
	public void mouseClicked(MouseEvent e) {}

	//Boutton de la souris press�
	@Override
	public void mousePressed(final MouseEvent e) {


		switch (type) {
		case PENCIL:

			if (isConnected) {
				client.sendPacket(new Packet(PacketType.POINT, new Point(type,
						e.getX() - 8, e.getY() - 55, currentColor)));

			} else {
				panel.addPoint(new Point(PointType.PENCIL, e.getX() - 8, e
						.getY() - 55, currentColor));
			}

			break;

		case CIRCLE:
			this.x = e.getX();
			this.y = e.getY();

			break;
		case FILLEDCIRCLE:
			this.x = e.getX();
			this.y = e.getY();

			break;
		case RECT:
			this.x = e.getX();
			this.y = e.getY();

			break;
		case FILLEDRECT:
			this.x = e.getX();
			this.y = e.getY();

			break;

		case BUCKET:

			//Cr�ation du deuxi�me processus pour pouvoir peindre les pixels
			t = new Thread(new Runnable() {

				@Override
				public void run() {

					BufferedImage bi = panel.getScreenShot(panel);

					System.out.println("Filling...");
					long before = System.currentTimeMillis();

					Utils.floodFillImage(bi, e.getX() - 8, e.getY() - 55,
							currentColor, isConnected);

					if(isConnected)
					{
						System.out.println("Sending ...");
						client.sendPacket(new Packet(PacketType.BUCKET, new Picture(Utils.allPoint)));
						System.out.println("Sent !");
					}
					
					panel.list.addAll(Utils.allPoint);
					
					Utils.allPoint.clear();
					
					panel.repaint();
					
					long after = System.currentTimeMillis();
					
					System.out.println("Filled in " + (after - before) + "ms");

				}
			});
			
			//Lancement du processus
			t.start();
			
			//Si le processus est toujours en cours
			if(t.isAlive())
			{
				//Nous l'arr�tons
				t.interrupt();
			}

			break;

		case PIPETTE:
			BufferedImage bi1 = panel.getScreenShot(panel);
			this.currentColor = new Color(bi1.getRGB(e.getX() - 8,
					e.getY() - 55));
			break;

		default:
			break;
		}

	}

	//Relachement de la souris
	@Override
	public void mouseReleased(MouseEvent e) {

		//En fonction du type d'outils choisi
		switch (type) {

		//Si nous avons choisi un cercle alors nous dessinons un cercle
		case CIRCLE:
			if (!isConnected) {
				panel.addPoint(new Point(PointType.CIRCLE, this.x - 8,
						this.y - 55, Utils.distanceBetween2Point(this.x - 8,
								this.y - 55, e.getX() - 8, e.getY() - 55),
						currentColor));
			} else {
				client.sendPacket(new Packet(PacketType.POINT, new Point(
						PointType.CIRCLE, this.x - 8, this.y - 55, Utils
								.distanceBetween2Point(this.x - 8, this.y - 55,
										e.getX() - 8, e.getY() - 55),
						currentColor)));
			}

			break;
			
		//Si nous avons choisi un disque alors nous dessinons un disque
		case FILLEDCIRCLE:
			if (!isConnected) {
				panel.addPoint(new Point(PointType.FILLEDCIRCLE, this.x - 8,
						this.y - 55, Utils.distanceBetween2Point(this.x - 8,
								this.y - 55, e.getX() - 8, e.getY() - 55),
						currentColor));
			} else {
				client.sendPacket(new Packet(PacketType.POINT, new Point(
						PointType.FILLEDCIRCLE, this.x - 8, this.y - 55, Utils
								.distanceBetween2Point(this.x - 8, this.y - 55,
										e.getX() - 8, e.getY() - 55),
						currentColor)));
			}

			break;
			
		//Si nous avons choisi un rectangle alors nous dessinons un rectangle
		case RECT:
			if (!isConnected) {
				panel.addPoint(new Point(PointType.RECT, this.x - 8,
						this.y - 55, e.getX() - 8, e.getY() - 55, currentColor));
			} else {
				client.sendPacket(new Packet(PacketType.POINT, new Point(
						PointType.RECT, this.x - 8, this.y - 55, e.getX() - 8,
						e.getY() - 55, currentColor)));
			}

			break;
			
		//Si nous avons choisi un rectangle remplie alors nous dessinons un rectangle remplie
		case FILLEDRECT:
			if (!isConnected) {
				panel.addPoint(new Point(PointType.FILLEDRECT, this.x - 8,
						this.y - 55, e.getX() - 8, e.getY() - 55, currentColor));
			} else {
				client.sendPacket(new Packet(PacketType.POINT, new Point(
						PointType.FILLEDRECT, this.x - 8, this.y - 55,
						e.getX() - 8, e.getY() - 55, currentColor)));
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
	
	//Recup�ration de l'instance de la class
	public static Panel getPanel() {
		return panel;}
	//Change le type d'outils
	private void setPointType(PointType pointType) {
		this.type = pointType;

	}

}