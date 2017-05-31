package fr.benjul.paintcoop;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import fr.benjul.paintcoop.system.Point;
import fr.benjul.paintcoop.system.PointType;

public class Utils {

	//Liste qui contient tout les pixels qui ont chang� de couleur
	public static ArrayList<Point> allPoint = new ArrayList<Point>();

	// Permet de connaitre la distance entre 2 points
	public static int distanceBetween2Point(int xA, int yA, int xB, int yB) {
		return (int) Math.sqrt(Math.pow((xB - xA), 2) + Math.pow((yB - yA), 2));
	}

	//Permet de peindre les pixels
	public static void floodFillImage(BufferedImage image, int x, int y, Color color, boolean isConnected) {

		//Couleur du pixel s�lectionn�
		int srcColor = image.getRGB(x, y);
		
		//Si la couleur du pixel s�lectionn� est �gale � la couleur voulue alors nous arr�tons de peindre
		if (new Color(srcColor).equals(color)) {
			return;
		}
		
		//Tableau � 2 dimmensions (Param�tre: x, y) qui permet de savoir si nous avons d�j� peint un pixel pour pouvoir �viter de le repeindre 
			//de perdre du temps et des performances
		boolean[][] hits = new boolean[image.getHeight()][image.getWidth()];

		//Queue est une type de liste qui est utilis� quand nous manipulons rapidement des listes
		Queue<java.awt.Point> queue = new LinkedList<java.awt.Point>();
		
		//Rajout du pixel quand la liste
		queue.add(new java.awt.Point(x, y));

		//Tant que la liste n'est pas vide
		while (!queue.isEmpty()) {
			
			
			//Permet d'obtenir le premier �l�ment de la liste et de le supprimer
			java.awt.Point p = queue.remove();
			
			//Si le pixel n'est pas en dehors de la zone voulue, n'a pas �tait d�j� peint et que le pixel � une couleur diff�rente de la couleur voulue
			if (floodFillImageDo(image, hits, p.x, p.y, srcColor, color.getRGB(), isConnected)) {
				
				//Rajout des pixels adjacents � ce pixel dans la liste
				queue.add(new java.awt.Point(p.x, p.y - 1));
				queue.add(new java.awt.Point(p.x, p.y + 1));
				queue.add(new java.awt.Point(p.x - 1, p.y));
				queue.add(new java.awt.Point(p.x + 1, p.y));
			}
		}
	}

	private static boolean floodFillImageDo(BufferedImage image, boolean[][] hits, int x, int y, int srcColor,
			int tgtColor, boolean isConnected) {
		//Si le pixel est dans la zone
		if (y < 0)
			return false;
		if (x < 0)
			return false;
		if (y >= image.getHeight())
			return false;
		if (x >= image.getWidth())
			return false;

		//Si le pixel n'a pas d�j� �tait peint
		if (hits[y][x])
			return false;

		//Si le pixel a une couleur diff�rent de la couleur voulue
		if (image.getRGB(x, y) != srcColor)
			return false;

		//Nous peignons le pixel
		image.setRGB(x, y, tgtColor);
		
		//Puis nous le rajoutons dans notre seconde liste
		allPoint.add(new Point(PointType.PENCIL, x, y, new Color(tgtColor)));
		
		//Ce pixel est consid�r� comme peint
		hits[y][x] = true;
		return true;
	}

}
