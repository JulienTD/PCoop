package fr.benjul.paintcoop.system;

import java.awt.Color;
import java.io.Serializable;

public class Point implements Serializable{
	//Objet peut contenir des rectangles, rectangles remplie, cercles, disques, des pixels

	private static final long serialVersionUID = 1L;
	

	private PointType pointType;
	
	private int x;
	private int y;
	
	private int rayon;
	
	private int x1;
	private int y1;
	
	private Color color;
	
	//////////// Constructeurs ////////////
	//Il y a plusieurs types de constructeurs avec des paramètres différents pour pouvoir envoyer des packets personnalisés
	
	//Constructeur pour un pixel
	public Point(PointType pointT,int x, int y, Color color)
	{
		this.pointType = pointT;
		this.x = x;
		this.y = y;
		this.color = color;
	}
	
	//Construceur pour un cercle / disque
	public Point(PointType pointT,int x, int y, int rayon, Color color)
	{
		this.pointType = pointT;
		this.x = x;
		this.y = y;
		this.rayon = rayon;
		this.color = color;
	}
	
	//Constructeur pour un rectangle, rectangle rempli
	public Point(PointType pointT,int x, int y, int x1, int y1, Color color)
	{
		this.pointType = pointT;
		this.x = x;
		this.y = y;
		this.x1 = x1;
		this.y1 = y1;
		this.color = color;
	}
	
	
	//Permet d'obtenir la position X du point
	public int getX()
	{
		return x;
	}
	
	//Permet d'obtenir la position Y du point
	public int getY()
	{
		return y;
	}
	
	//Permet d'obtenir le rayon du cercle / disque
	public int getRayon()
	{
		return rayon;
	}
	
	//Permet d'obtenir la position X1 du point
	public int getX1()
	{
		return x1;
	}
	
	//Permet d'obtenir la position Y1 du point
	public int getY1()
	{
		return y1;
	}
	
	//Permet de connaitre le type de point
	public PointType getType()
	{
		return pointType;
	}
	
	//Permet d'obtenir la couleur du point
	public Color getColor()
	{
		return color;
	}
}
