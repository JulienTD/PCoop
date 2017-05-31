package fr.benjul.paintcoop.system;

import java.io.Serializable;

public class Mouse implements Serializable{
	//Objet permettant de déplacer la souris des autres utilisateur sur notre fenêtre

	private static final long serialVersionUID = 1L;
	
	int x = 0;
	int y = 0;
		
	String pseudo = "";
	
	//Constructeur avec comme paramètres: les coordonées de la souris (x, y) et le pseudo de la personne qui l'a déplacé
	public Mouse(int x, int y, String pseudo)
	{
		this.x = x;
		this.y = y;
		this.pseudo = pseudo;
	}
	
	//Permet d'obtenir la position X de la souris
	public int getX()
	{
		return x;
	}
	
	//Permet d'obtenir la position Y de la souris
	public int getY()
	{
		return y;
	}
	
	//Permet d'obtenir le pseudo de la personne qui déplacé la souris
	public String getPseudo()
	{
		return pseudo;
	}
}