package ummisco.map.shpToStl;

public class Point3D {
	
	double x,y,z;
	
	public Point3D(float x, float y, float z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point3D(double x, double y, double z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point3D(Point3D p) {
		super();
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
	}
	//Retourne la coordonnees X du point 3D
	public double getX() {
		return x;
	}

	
	//Modifie la coordonnees X du point 3D
	public void setX(double x) {
		this.x = x;
	}

	
	//Retourne la coordonnees Y du point 3D
	public double getY() {
		return y;
	}

	
	//Modifie la coordonnees X du point 3D
	public void setY(double y) {
		this.y = y;
	}

	
	//Retourne la coordonnees Z du point 3D
	public double getZ() {
		return z;
	}

	
	//Modifie la coordonnees X du point 3D
	public void setZ(double z) {
		this.z = z;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof Point3D))
			return false;
		
		Point3D r = (Point3D)o;
		return r.x == this.x && r.z == this.z && r.y == this.y;
	}

	public boolean inPlan(Object o)
	{
		if(!(o instanceof Point3D))
			return false;
		
		Point3D r = (Point3D)o;
		return r.x == this.x && r.y == this.y ;
	}

	
	public double norm()
	{
		return Math.sqrt(x*x+y*y+z*z);
	}
	
	// scalar product between 2 vectors
	public double scalarProduct(Point3D p) {
		return p.x * this.x + p.y * this.y + p.z * this.z;
	}
	
	// scalar product between 2 vectors
	public Point3D scale(float v) {
		return new Point3D(x *v,y*v,z*v );
	}

	
	// opposite vector 
	public Point3D opposite() {
		return new Point3D(-x,-y,-z);
	}
	
	// vector between 2 points
	public Point3D vectorTo(Point3D p) {
		return new Point3D(p.getX() - x, p.getY() - y, p.getZ() - z);
	}
	
	public String toString() {
		return "X: " + getX() + " Y: " + getY() + " Z: " + getZ();
	}

}
