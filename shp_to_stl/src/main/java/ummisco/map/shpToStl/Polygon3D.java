package ummisco.map.shpToStl;

import java.util.ArrayList;

public class Polygon3D {
	
	ArrayList<Point3D> points;
	ArrayList<Edge> edges;
	boolean isDrawn;
	
	public Polygon3D()
	{
		this.points = new ArrayList<Point3D>();
		this.edges = new ArrayList<Edge>();
		isDrawn = false;
	}
	
	public boolean isDrawn()
	{
		return this.isDrawn;
	}
	public void setDrawn()
	{
		this.isDrawn = true;
	}
	
	public double getHeight() {
		return points.get(0).getZ();
	}
	
	public void addPoint(Point3D p)
	{
		this.points.add(p);
	}
	public void addEdge(Edge p)
	{
		this.edges.add(p);
	}
	
	public ArrayList<Point3D> getEdges()
	{
		return this.getEdges();
	}
	public ArrayList<Point3D> getPoints()
	{
		return this.points;
	}
	public static double minHeight(ArrayList<Polygon3D> lpoly)
	{
		double res = Double.MAX_VALUE;
		for(Polygon3D p:lpoly) if(p.getHeight()<res) res = p.getHeight();
		return res;
	}
	public static double maxHeight(ArrayList<Polygon3D> lpoly)
	{
		double res = Double.MIN_VALUE;
		for(Polygon3D p:lpoly) if(p.getHeight()>res) res = p.getHeight();
		return res;
	}
}
