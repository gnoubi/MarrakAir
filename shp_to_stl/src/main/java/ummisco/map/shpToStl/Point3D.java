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

	
	//Modifit la coordonnees X du point 3D
	public void setX(float x) {
		this.x = x;
	}

	
	//Retourne la coordonnees Y du point 3D
	public float getY() {
		return y;
	}

	
	//Modifit la coordonnees X du point 3D
	public void setY(float y) {
		this.y = y;
	}

	
	//Retourne la coordonnees Z du point 3D
	public float getZ() {
		return z;
	}

	
	//Modifit la coordonnees X du point 3D
	public void setZ(float z) {
		this.z = z;
	}
}
