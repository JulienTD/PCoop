package fr.benjul.paintcoop.system;

import java.io.Serializable;

public class Mouse implements Serializable{
	//Objet permettant de d�placer la souris des autres utilisateur sur notre fen�tre

	private static final long serialVersionUID = 1L;
	
	int x = 0;
	int y = 0;
		
	String pseudo = "";
	
	//Constructeur avec comme param�tres: les coordon�es de la souris (x, y) et le pseudo de la personne qui l'a d�plac�
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
	
	//Permet d'obtenir le pseudo de la personne qui d�plac� la souris
	public String getPseudo()
	{
		return pseudo;
	}
}