package fr.benjul.paintcoop.graphic;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

import fr.benjul.paintcoop.PaintCoop;
import fr.benjul.paintcoop.system.Point;

public class Panel extends JPanel {

	////// Initialisation des variables //////
	private static final long serialVersionUID = 1L;

	//Liste qui va contenir tout les objets point(cercle, disque, pixel, rectangle, rectangle remplie) 
	public ArrayList<Point> list = new ArrayList<Point>();
	
	//Coordonées de la souris et pseudo d'une personne en ligne
	public int x = -1;
	public int y = -1;
	public String name = "";


	//Fonction de base qui appartient à la classe JPanel qui est activé à chaque fois qu'on repeint la fenêtre
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponents(g);

		//Nous dessinons le fond de la fenêtre en blanc
		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		//Création d'un iterator qui va parcourir la liste pour pouvoir les peindre
		for (ListIterator<Point> iterator = list.listIterator(); iterator.hasNext();) {
			Point p = iterator.next();
			
			switch (p.getType()) {
			case PENCIL:
				g.setColor(p.getColor());
				g.fillRect(p.getX(), p.getY(), 5, 5);
				break;
				
			case CIRCLE:

				g.setColor(p.getColor());
				drawCircle(g, p.getX(), p.getY(), p.getRayon());
				
				break;
				
			case FILLEDCIRCLE:
				g.setColor(p.getColor());
				drawFilledCircle(g, p.getX(), p.getY(), p.getRayon());
				
				break;
				
			case RECT:
				g.setColor(p.getColor());
				drawRect(g, p.getX(), p.getY(), p.getX1(), p.getY1());
				
				break;
				
			case FILLEDRECT:
				g.setColor(p.getColor());
				drawRectFill(g, p.getX(), p.getY(), p.getX1(), p.getY1());
				break;
				
			default:
				break;
				
			}
			
		}

		//Si personne d'autre n'est connecté au serveur, nous ne dessinons pas la souris
		if (!(x < 0 && y < 0)) {
			BufferedImage image;

			try {
				//Permet d'accéder aux images du logiciel
				ClassLoader classLoader = new PaintCoop().getClass().getClassLoader();
				//Dessine la souris
				image = ImageIO.read(classLoader.getResource("pointeur.png"));
				g.drawImage(image, x, y, 16, 16, null);
				
				//Dessine le pseudo de la personne connecté au serveur
				g.setColor(Color.black);
				g.drawString(name, x, y - 10);
				g.setColor(Color.white);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//Permet d'éffacer tout ce qui a était dessiner
	public void reset() {
		list.clear();
		repaint();
	}

	//Permet de rajouter un point à la liste
	public void addPoint(Point p) {
		if (list.contains(p)) {
			return;
		} else {
			list.add(p);
			repaint();
		}

	}

	//Permet de dessiner un cercle
	public void drawCircle(Graphics cg, int xCenter, int yCenter, int r) {
		cg.drawOval(xCenter - r, yCenter - r, 2 * r, 2 * r);
	}

	//Permet de dessiner un disque
	public void drawFilledCircle(Graphics cg, int xCenter, int yCenter, int r) {
		cg.fillOval(xCenter - r, yCenter - r, 2 * r, 2 * r);
	}

	//Permet de dessiner un rectangle
	public void drawRect(Graphics cg, int xa, int ya, int x1, int y1) {
		cg.drawLine(xa, ya, x1, ya);
		cg.drawLine(x1, ya, x1, y1);
		cg.drawLine(xa, ya, xa, y1);
		cg.drawLine(xa, y1, x1, y1);

	}

	//Permet de dessiner un rectangle remplie
	public void drawRectFill(Graphics g, int xa, int ya, int x1, int y1) {

		int xB = x1 - xa;
		int yB = y1 - ya;

		int xA = 0;
		int yA = 0;

		if (xB > xA && yB > yA) {
			g.fillRect(xa, ya, x1 - xa, y1 - ya);

		} else if (xB > 0 && yB < yA) {
			int temp = ya;
			ya = y1;
			y1 = temp;
			g.fillRect(xa, ya, x1 - xa, y1 - ya);

		} else if (xB < xA && yB < yA) {
			int temp = ya;
			int temp1 = xa;
			ya = y1;
			xa = x1;
			y1 = temp;
			x1 = temp1;
			g.fillRect(xa, ya, x1 - xa, y1 - ya);
			
			
		} else if (xB < xA && yB > yA) {
			int temp = x1;
			x1 = xa;
			xa = temp;
			g.fillRect(xa, ya, x1 - xa, y1 - ya);
		}

	}

	public BufferedImage getScreenShot(JPanel panel) {
		BufferedImage bi = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
		panel.paint(bi.getGraphics());
		return bi;
	}

}
