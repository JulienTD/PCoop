package fr.benjul.paintcoop.system;

import java.io.Serializable;
import java.util.ArrayList;

public class Picture implements Serializable{
	//Objet permettant de stocker toutes les points d'une fen�tre

	private static final long serialVersionUID = 1L;
	private ArrayList<Point> list;
	
	//Constructeur avec comme param�tre: une liste contenant que des points
	public Picture(ArrayList<Point> list)
	{
		this.list = list;
	}
	
	//Permet d'avoir acc�s � une liste contenant que des points
	public ArrayList<Point> getList()
	{
		return this.list;
	}
}
