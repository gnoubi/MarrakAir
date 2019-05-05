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
	
	
	public Point3D getNormal() {
		
		Point3D U = new Point3D(
				points[1].getX() - points[0].getX(), 
				points[1].getY() - points[0].getY(), 
				points[1].getZ() - points[0].getZ());

		Point3D V = new Point3D(
				points[2].getX() - points[0].getX(), 
				points[2].getY() - points[0].getY(), 
				points[2].getZ() - points[0].getZ());

		Point3D normal = new Point3D(
				U.getY() * V.getZ() - U.getZ() * V.getY(), 
				U.getZ() * V.getX() - U.getX() * V.getZ(), 
				U.getX() * V.getY() - U.getY() * V.getX());
//		Set Normal.x to (multiply U.y by V.z) minus (multiply U.z by V.y)
//		Set Normal.y to (multiply U.z by V.x) minus (multiply U.x by V.z)
//		Set Normal.z to (multiply U.x by V.y) minus (multiply U.y by V.x)

		return normal;
		
	}
}
