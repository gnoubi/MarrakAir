package ummisco.map.shpToStl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import com.aspose.threed.Mesh;
import com.aspose.threed.PolygonBuilder;
import com.aspose.threed.Vector4;

public class GeometryToMesh {
	
	private Mesh mesh;
	private Map<Point3D, ArrayList<Polygon3D>> polygons;
	private Map<Edge, ArrayList<Polygon3D>> ePolygons;
	private Map<Point3D, Vector4> controlPoints;
	
	public double LOWER_HEIGHT = 0.0;
	
	
	
	
	public GeometryToMesh(double minElevation) {
		super();
		polygons = new HashMap<Point3D, ArrayList<Polygon3D>>();
		ePolygons = new HashMap<Edge, ArrayList<Polygon3D>>();
		controlPoints = new HashMap<Point3D, Vector4>();
		LOWER_HEIGHT = minElevation;
	}


	public void loadPolygon(Polygon p, double height)
	{
		Polygon3D poly3D = extractPolygonFromGeometry(p,height);
		storePolygon3D(poly3D);
		extractEdgeFromPolygon3D(poly3D);
	}
	
	
	public void loadPolygons(ArrayList<Polygon> polys)
	{
		double height  = 12;
		for(Polygon p:polys)
		{
			Polygon3D poly3D = extractPolygonFromGeometry(p,height);
			storePolygon3D(poly3D);
			extractEdgeFromPolygon3D(poly3D);
		}
		extractBorderFromEdges();
	}
	
	private void extractBorderFromEdges()
	{
		Set<Edge> edges = this.ePolygons.keySet();
		for(Edge e:edges) {
			ArrayList<Polygon3D> pp = this.ePolygons.get(e);
			double minHeight = Polygon3D.minHeight(pp);
			double maxHeight = Polygon3D.maxHeight(pp);
			minHeight = minHeight<maxHeight?minHeight:LOWER_HEIGHT;
			Point3D p1 = new Point3D(e.getP1());
			p1.setZ(maxHeight);
			Point3D p2 = new Point3D(e.getP1());
			p2.setZ(minHeight);
			Point3D p3 = new Point3D(e.getP2());
			p3.setZ(minHeight);
			Point3D p4 = new Point3D(e.getP2());
			p4.setZ(maxHeight);
			Polygon3D poly = new Polygon3D();
			poly.addPoint(p1);
			poly.addPoint(p2);
			poly.addPoint(p3);
			poly.addPoint(p4);
			
			System.out.println("P1 "+ p1);
			System.out.println("P2 "+ p2);
			System.out.println("P3 "+ p3);
			System.out.println("P4 "+ p4);
			storePolygon3D(poly);
		}
		
	}
	
	
	private void extractEdgeFromPolygon3D(Polygon3D poly)
	{
		ArrayList<Point3D> pp = poly.getPoints();
		for(int i = 0; i< pp.size();i++) {
			Edge e = storeEdge(pp.get(i),pp.get((i+1)%pp.size()),poly);
			poly.addEdge(e);
		}
	}
	
	private Edge storeEdge(Point3D p1, Point3D p2,Polygon3D poly )
	{
		Edge e = new Edge(p1,p2);
		
		ArrayList<Polygon3D> subList = this.ePolygons.get(e);
		if(subList == null) {
			subList = new ArrayList<Polygon3D>();
			this.ePolygons.put(e,subList);
		}
		subList.add(poly);
		return e;
	}
	
	private Polygon3D extractPolygonFromGeometry(Polygon polys,double z){
		Coordinate[] coord_polys=polys.getCoordinates();
		Polygon3D res = new Polygon3D();
		for(int i=0;i<coord_polys.length-1;i++){
			Point3D p = new Point3D(coord_polys[i].x,coord_polys[i].y,z);
			res.addPoint(p);
		}
		return res;
	}
	
	
	private void storePolygon3D(Polygon3D poly3D)
	{
		ArrayList<Point3D> points = poly3D.getPoints();
		for(Point3D pp:points) {
			ArrayList<Polygon3D> subList = this.polygons.get(pp);
			if(subList == null) {
				subList = new ArrayList<Polygon3D>();
				this.polygons.put(pp,subList);
				this.controlPoints.put(pp, new Vector4( pp.getX(), pp.getY(), pp.getZ(), 1.0));
			}
			subList.add(poly3D);
		}
	}
	public void prepareAndBuildData()
	{
		extractBorderFromEdges();
	}
	public Mesh buildMesh()
	{
		int nbPolygon = this.polygons.values().size();
		mesh = new Mesh();
		ArrayList<Vector4> orderedList = new ArrayList<Vector4>();
		orderedList.addAll(controlPoints.values());
		
		// Add control points to the mesh
		mesh.getControlPoints().addAll(orderedList);
		//create facet
		PolygonBuilder builder = new PolygonBuilder(mesh);
		for(Point3D pp:this.polygons.keySet()) {
			ArrayList<Polygon3D> polys = this.polygons.get(pp);
			for(Polygon3D poly: polys)
			{
				if(!poly.isDrawn())
					{
						drawPolygon(poly,orderedList,builder);
						poly.setDrawn();
					}
				nbPolygon --;
				System.out.println("groupe restant " + nbPolygon);
			}
		}
		
		return mesh;
	}
	
	private void drawPolygon(Polygon3D poly,ArrayList<Vector4> orderedList, PolygonBuilder builder)
	{
		builder.begin();
			for(Point3D p:poly.getPoints()) {
				builder.addVertex(orderedList.indexOf(this.controlPoints.get(p)));
			}
		builder.end();
	}

}
