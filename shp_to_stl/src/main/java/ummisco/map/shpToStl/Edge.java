package ummisco.map.shpToStl;

public class Edge {
	Point3D p1;
	Point3D p2;
	
	@Override
	public boolean equals(Object c)
	{
		if(!(c instanceof Edge))
			return false;
		
		Edge e = (Edge)c;
		
		return (p1.equals(e.p1) && p2.equals(e.p2)) || (p1.equals(e.p2) && p2.equals(e.p1));
		
	}
	
	public Edge(Point3D p1, Point3D p2) {
		super();
		this.p1 = p1;
		this.p2 = p2;
	}
	
	
	
	
	

}
