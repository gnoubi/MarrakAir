package ummisco.map.shpToStl;

public class Triangle {
	
	Point3D[] points;
	
	public Triangle(Point3D[] point) {
		this.points = point;
	}


	//Retourne un tableau de Point3D qui sont les coordonnees du troangle
	public Point3D[] getPoint3D(){
		return points;
	}
}
