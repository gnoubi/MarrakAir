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
		return (p1.inPlan(e.p2) && p2.inPlan(e.p1)) || (p1.inPlan(e.p1) && p2.inPlan(e.p2));
	}
	
	public Edge(Point3D p1, Point3D p2) {
		super();
		this.p1 = p1;
		this.p2 = p2;
	}
	
	
	public Point3D getP1() {
		return p1;
	}

	public Point3D getP2() {
		return p2;
	}

	@Override
    public int hashCode() {
		 String res ="-"+Math.min(p1.getX(), p2.getX())+"-"+
				 Math.max(p1.getX(), p2.getX())+"-"+
				 Math.min(p1.getY(), p2.getY())+"-"+
				 Math.max(p1.getY(), p2.getY())+"-";
        return res.hashCode();
    }
	
	
	
	

}
