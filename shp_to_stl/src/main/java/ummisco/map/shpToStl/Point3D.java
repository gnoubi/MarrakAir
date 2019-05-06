package ummisco.map.shpToStl;

public class Point3D {
	
	float x,y,z;
	
	public Point3D(float x, float y, float z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	
	//Retourne la coordonnees X du point 3D
	public float getX() {
		return x;
	}

	
	//Modifie la coordonnees X du point 3D
	public void setX(float x) {
		this.x = x;
	}

	
	//Retourne la coordonnees Y du point 3D
	public float getY() {
		return y;
	}

	
	//Modifie la coordonnees X du point 3D
	public void setY(float y) {
		this.y = y;
	}

	
	//Retourne la coordonnees Z du point 3D
	public float getZ() {
		return z;
	}

	
	//Modifie la coordonnees X du point 3D
	public void setZ(float z) {
		this.z = z;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof Point3D))
			return false;
		
		Point3D r = (Point3D)o;
		return r.x == this.x && r.y == this.y && r.z == this.z;
	}
	
	
	// scalar product between 2 vectors
	public float scalarProduct(Point3D p) {
		return p.x * this.x + p.y * this.y + p.z * this.z;
	}
	
	// opposite vector 
	public Point3D opposite() {
		return new Point3D(-x,-y,-z);
	}
	
	// vector between 2 points
	public Point3D vectorBetween(Point3D p) {
		return new Point3D(p.getX() - x, p.getY() - y, p.getZ() - z);
	}
	
	public String toString() {
		return "X: " + getX() + " Y: " + getY() + " Z: " + getZ();
	}

}
