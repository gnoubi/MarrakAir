package ummisco.map.shpToStl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.Geometry;
//import com.vividsolutions.jts.geom.GeometryFactory;
//import com.vividsolutions.jts.geom.MultiPolygon;
//import com.vividsolutions.jts.geom.Polygon;

public class GeometryToTriangle {

	private ArrayList<Triangle> liste_triangle = new ArrayList<Triangle>();
	private HashMap<Edge, ArrayList<Triangle>> edges = new HashMap<Edge, ArrayList<Triangle>>();
	
	public GeometryToTriangle(){}
	

	//Divise le multipolygon en polygon
	public ArrayList<Polygon> decomposeMultiPolygon(MultiPolygon mp){
		Polygon polys;
		ArrayList<Polygon> liste_polygon = new ArrayList<Polygon>();
		for (int i = 0; i < mp.getNumGeometries(); i++) {
			if(mp.getGeometryN(i).isValid()){
				polys = ((Polygon)mp.getGeometryN(i));
				liste_polygon.add(polys);
			}
		}
		return liste_polygon;
	}
	
	
	//Divise un polygone en plusieurs polygones
		public ArrayList<Geometry> decomposePolygon(Geometry p){
			ArrayList<Geometry> liste_polygon = new ArrayList<Geometry>();
			edges = new HashMap<Edge, ArrayList<Triangle>>();
			GeometryFactory fact = new GeometryFactory();
			Coordinate[] coord = p.getCoordinates();
			for(int i=1;i<p.getNumPoints();i++){
				if((coord[0].x==coord[i].x && coord[0].y==coord[i].y) && (p.getNumPoints()-i != 1)){
					Coordinate[] new_coord = new Coordinate[i+1];
					for(int j=0;j<=i;j++){
						new_coord[j]=coord[j];
					}

			        try {
						Polygon polys = fact.createPolygon(new_coord);
						liste_polygon.add(polys);			        	
			        } catch(IllegalArgumentException e) {
			        	System.out.println("IllegalArgumentException in [decomposePolygon]:");
						Stream<Coordinate> stream2 = Arrays.stream(new_coord);
						stream2.forEach(x -> System.out.print(x + " "));
						System.out.println(" ");
						//
						System.out.print("" + new_coord.length);
						System.out.println("  has repeated coordinates: " + CoordinateArrays.hasRepeatedPoints(new_coord));
			        }
			        
					Coordinate[] new_coord2 = new Coordinate[p.getNumPoints()-i];
					int cpt = p.getNumPoints()-i;
					for(int j=0;j<cpt;j++){
						new_coord2[j]=coord[i];
						i++;
					}

			        try {
						Polygon polys = fact.createPolygon(new_coord2);
						liste_polygon.add(polys);			        	
			        } catch(IllegalArgumentException e) {
			        	System.out.println("IllegalArgumentException in [decomposePolygon]:");
						Stream<Coordinate> stream2 = Arrays.stream(new_coord);
						stream2.forEach(x -> System.out.print(x + " "));
						System.out.println(" ");
						//
						System.out.print("" + new_coord.length);
						System.out.println("  has repeated coordinates: " + CoordinateArrays.hasRepeatedPoints(new_coord));
			        }

					return liste_polygon;
				}
			}
			return liste_polygon;
		}


	//Recupere tous les triangles qui composent le polygon et les convertie en Triangle
	public void polygonSTL(Polygon polys,double haut,double bas){
		epaisseurTriangle(polys,haut,bas);
		
		getEdgesOfTriangles(polys);
		
		ArrayList<Polygon> triangles = new ArrayList<Polygon>();
		triangles = trianglePolygon(polys,triangles);
		
		Point3D centroid = new Point3D(0,0,0);
		
		try {
			Point poly_centroid = polys.getCentroid();
			centroid = new Point3D((float) poly_centroid.getX(), (float) poly_centroid.getY(), 0);
		} catch(IllegalStateException e) {
			System.out.println("Empty polygon !!!!! so empty centroid !!!! ");
		}
		
		for(Polygon p:triangles){
			Point3D[] point = new Point3D[3];
			Point3D[] point2 = new Point3D[3];
			Coordinate[] coord_triangle=p.getCoordinates();
			for(int i=0;i<3;i++){
				point[i] = new Point3D((float) coord_triangle[i].x,(float)bas,(float) coord_triangle[i].y);
				if(haut!=0)
					point2[i] = new Point3D((float) coord_triangle[i].x,(float)haut,(float) coord_triangle[i].y);
			}
			Triangle tri = new Triangle(point, centroid);
			liste_triangle.add(tri);
			if(haut!=0){
				Triangle tri2 = new Triangle(point2, centroid);
				liste_triangle.add(tri2);
			}
		}
	}


	//Construit les triangles pour l'épaisseur
	public void epaisseurTriangle(Polygon polys, double hauteur,double bas){
		Point poly_centroid = polys.getCentroid();
		Point3D centroid = new Point3D((float) poly_centroid.getX(), (float) poly_centroid.getY(), 0);
				
		for(int i=0;i<polys.getNumPoints()-1;i++){
			Point3D[] point = new Point3D[3];
			Point3D[] point2 = new Point3D[3];
			Coordinate[] coord_polys=polys.getCoordinates();
			point[0]= new Point3D ((float) coord_polys[i].x,(float)bas,(float) coord_polys[i].y);
			point2[0]= new Point3D ((float) coord_polys[i].x,(float)hauteur,(float) coord_polys[i].y);
			point[1]= new Point3D ((float) coord_polys[i+1].x,(float)bas,(float) coord_polys[i+1].y);
			point2[1]= new Point3D ((float) coord_polys[i+1].x,(float)hauteur,(float) coord_polys[i+1].y);
			point[2]= point2[1];
			point2[2]= point[0];
			
			System.out.println("Centroid : " + centroid);
			Triangle tri = new Triangle(point,centroid);
			Triangle tri2 = new Triangle(point2,centroid);
			liste_triangle.add(tri);
			liste_triangle.add(tri2);
		}
	}
	
	//Construit les triangles pour l'épaisseur
	public void getEdgesOfTriangles(Polygon polys){
		Coordinate[] coord_polys=polys.getCoordinates();
		for(int i=0;i<coord_polys.length-1;i++){
			Point3D p1 = new Point3D((float) coord_polys[i].x,0,(float) coord_polys[i].y);
			Point3D p2 = new Point3D((float) coord_polys[i+1].x,0,(float) coord_polys[i+1].y);
			Edge edg = new Edge(p1, p2);
			if(!this.edges.containsKey(edg))
			{
				System.out.println("ADD NEW EDGE" + p1 + "  "+ p2);
				this.edges.put(edg, new ArrayList<Triangle>());
			}
		}
	}


	/*//Recupere tous les triangles de la geometrie
	public ArrayList<Polygon> trianglePolygon(Polygon polys){
		ArrayList<Polygon> allTriangle = new ArrayList<Polygon>();
		return trianglePolygon(polys,allTriangle);
	}*/	


	//Trouve une oreille la stock dans liste des triangles et la soustrait au polygone du debut
	private ArrayList<Polygon> trianglePolygon(Polygon polys,ArrayList<Polygon> allTriangle){
		int longueur = polys.getNumPoints();
		if(longueur == 4 ){
			allTriangle.add(polys);
			return allTriangle;
		}
		Coordinate[] coord_polys=polys.getCoordinates();
		for(int i = 0 ; i< longueur;i++){
			Polygon triangle = generateTriangle(coord_polys[i],coord_polys[(i+1)%longueur],coord_polys[(i+2)%longueur]);
			if(isHear(polys,triangle)){
				allTriangle.add(triangle);
				polys = (Polygon)polys.difference(triangle);
				return trianglePolygon(polys, allTriangle);
			}
		}
		return allTriangle;
	}	


	//Verifie si le triangle est une oreille
	public boolean isHear(Polygon polys,Polygon triangle ){
		if(!polys.contains(triangle))
			return false;
		Geometry geom = polys.difference(triangle);
		if(geom instanceof Polygon)
			return true;
		return false;
	}


	//Genere un triangle avec 3 coordonnees
	public Polygon generateTriangle(Coordinate a, Coordinate b, Coordinate c){
		GeometryFactory fact = new GeometryFactory();
		Coordinate[] coords = {a,b,c,a};
	
		// BEN 
	//	if(!Orientation.isCCW(coords)) {
	//		CoordinateArrays.reverse(coords);
	//	}
		// 
		Polygon newpolys =fact.createPolygon(coords);

		return newpolys;
	}


	//Vide la liste des triangles
	public void videListe(){
		liste_triangle.clear();
	}


	//Renvoie la liste des triangles
	public ArrayList<Triangle> getListeTriangle(){
		return liste_triangle;
	}
}