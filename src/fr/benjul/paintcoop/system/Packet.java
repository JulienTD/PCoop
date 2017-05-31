package fr.benjul.paintcoop.system;

import java.io.Serializable;
import java.util.ArrayList;

public class Packet implements Serializable{
	//Objet qui peut contenir tout les types d'objets (Message, Souris, Point)
	private static final long serialVersionUID = 1L;
	
	private PacketType packetT = null;
		
	private Point point = null;
		
	private Mouse mouse = null;
	
	private Picture picture = null;
	
	private ArrayList<Point> list;
	
	//////////// Constructeurs ////////////
	//Il y a plusieurs types de constructeurs avec des paramètres différents pour pouvoir envoyer des packets personnalisés
	
	public Packet(PacketType packetT, Point point, Mouse mouse, ArrayList<Point> list)
	{
		this.packetT = packetT;
		this.point = point;
		this.mouse = mouse;
		this.list = list;
	}
	
	public Packet(PacketType packetT) {
		this.packetT = packetT;
	}

	public Packet(PacketType packetT, Point point) {
		this.packetT = packetT;
		this.point = point;
	}

	public Packet(PacketType packetT, Mouse mouse) {
		this.packetT = packetT;
		this.mouse = mouse;
	}
	
	public Packet(PacketType packetT, Picture picture)
	{
		this.packetT = packetT;
		this.picture = picture;
	}
	
	public Packet(PacketType packetT, ArrayList<Point> list)
	{
		this.packetT = packetT;
		this.list = list;
	}

	//Permet de savoir le type de packet
	public PacketType getPacketType()
	{
		return packetT;
	}
	
	//Permet d'avoir accés à l'objet Point
	public Point getPointPacket()
	{
		return point;
	}
	
	//Permet d'avoir accés à l'objet Mouse
	public Mouse getMouse()
	{
		return mouse;
	}
	
	//Permet d'avoir accés à l'objet Picture
	public Picture getPicture()
	{
		return this.picture;
	}
	
	//Permet d'avoir accés à une liste contenant des points
	public ArrayList<Point> getList()
	{
		return this.list;
	}
	
}

