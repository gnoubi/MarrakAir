package ummisco.map.shpToStl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;

import com.aspose.threed.FileFormat;
import com.aspose.threed.Mesh;
import com.aspose.threed.PolygonModifier;
import com.aspose.threed.Scene;
import com.aspose.threed.Transform;

public class Conversion {

	private double coupe;
	private String hauteur;
	private double taille;

	// Elevation of the base ground of the map.
	public static double BASE_ELEVATION = 3.0;
	// As we suppose that all the heights are provided in cm, they have to be multiplied by HEIGHT_FACTOR to be in mm.
	public static double HEIGHT_FACTOR = 10;

	/**
	* The main constructor of the class Conversion. The parameters come mainly from the GUI.
	* @param coupe  size of one piece of the map (supposed to be in cm)
	* @param taille size of the map (supposed to be in cm)
	* @param hauteur name of the attribute in the input shapefile representing the height of the geometry (supposed to be in cm)
	*/
	public Conversion(int coupe,int taille, String hauteur){
		this.coupe = coupe * HEIGHT_FACTOR;
		this.hauteur = hauteur;
		this.taille = taille * HEIGHT_FACTOR;
	}


	//Parcours les fichiers shapefiles et decompose les geometry en Polygon et les stock avec leur hauteur
	public void parcoursFichier(ArrayList<File> liste_shapefile) throws IOException{
		Map<Geometry,Double> liste_polygon= new HashMap<Geometry,Double>();
		for(int i=0;i<liste_shapefile.size();i++){
			ShpFile file = new ShpFile(liste_shapefile.get(i));
			ArrayList<SimpleFeature> features = file.readFile();
			for(SimpleFeature feature:features){
				Geometry geom = (Geometry) feature.getAttribute("the_geom");
				if(geom instanceof Polygon){
					if(geom.isValid()){
						Polygon polys = (Polygon) geom;
						if(!hauteur.equals("Error")){
							if(feature.getAttribute(hauteur)!=null)
								liste_polygon.put(polys,(((Number)feature.getAttribute( hauteur)).doubleValue() * HEIGHT_FACTOR));
							else{
								hauteur="Error";
								liste_polygon.put(polys,0.0);
							}
						}
						else
							liste_polygon.put(polys,0.0);
					}
				}
				if(geom instanceof MultiPolygon){
					MultiPolygon mp = (MultiPolygon) geom;
					ArrayList<Polygon> listepoly = decomposeMultiPolygon(mp);
					if(!hauteur.equals("Error")){
						for(Polygon polys:listepoly){
							if(polys.isValid()){
								if(feature.getAttribute(hauteur)!=null)
									liste_polygon.put(polys,(((Number)feature.getAttribute(hauteur)).doubleValue()) * HEIGHT_FACTOR);
								else{
									hauteur="Error";
									liste_polygon.put(polys,0.0);
								}
							}
						}
					}
					else{
						for(Polygon polys:listepoly){
							liste_polygon.put(polys,0.0);
						}		
					}
				}
			}
		}
		Geometry geo = regroupePolygon(liste_polygon);
		Map<Geometry,Double> new_liste_polygon= new HashMap<Geometry,Double>();
		Geometry limite = geo.getEnvelope();
		Coordinate[] coord = limite.getCoordinates();
		System.out.println("[parcoursFichier]");
		System.out.println("Min: "+coord[0]+ " -- Max: "+ coord[2]+" -- Minmax"+coord[1]);
		System.out.println("taille : " + taille );
		new_liste_polygon = redimensionGeometry(coord[0],coord[2],coord[1],liste_polygon, taille);
		Geometry new_geo = regroupePolygon(new_liste_polygon);
		decoupeGeometry(new_geo,new_liste_polygon);
	}


	//Redimensionne la geometry
	public Map<Geometry,Double> redimensionGeometry(Coordinate min,Coordinate max,Coordinate minmax, Map<Geometry,Double> liste_polygon,double taillle){
		Map<Geometry,Double> new_liste_polygon= new HashMap<Geometry,Double>();
		GeometryFactory fact = new GeometryFactory();
		Point minp = fact.createPoint(min);
		Point maxp = fact.createPoint(max);
		Point minmaxp = fact.createPoint(minmax);
		// System.out.println("Min: "+minp+ " -- Max: "+ maxp+" -- Minmax"+minmaxp);		
		// System.out.println("Distance : minp to mimaxp: " + minp.distance(minmaxp) + " -- maxp to minmaxp: " + maxp.distance(minmaxp));
//		double mulx = taillle/(minp.distance(minmaxp));
//		double muly = taillle/(maxp.distance(minmaxp));
		double sizeX = maxp.distance(minmaxp);
		double mulx = taillle/sizeX;
		
		System.out.println("Multiply " + " Mulx: "+mulx  );		
		for(Entry<Geometry, Double> entry : liste_polygon.entrySet()) {
			Coordinate[] coord = entry.getKey().getCoordinates();
			Coordinate[] new_coord = new Coordinate[entry.getKey().getNumPoints()];
			for(int j=0;j<entry.getKey().getNumPoints();j++){
				Coordinate att = new Coordinate();
//				att.x=(minp.getX() + minmaxp.getX() - coord[j].x)*mulx;
//				att.y=coord[j].y*muly;

//				att.x=(minp.getX() + minmaxp.getX() - coord[j].x)*mulx;				
//				att.y = (coord[j].y) * muly ;
//				att.x = (coord[j].x - minp.getX()) * mulx + minp.getX();
				System.out.println(maxp.getX()+" " + minp.getX()+" " + coord[j].x);
				
				att.x = (maxp.getX() + minp.getX() - coord[j].x) * mulx ;
				att.y = (coord[j].y - minp.getY()) * mulx + minp.getY();
				
				new_coord[j]=att;
			}
			new_coord[entry.getKey().getNumPoints()-1]=new_coord[0];
			Geometry geo = fact.createPolygon(new_coord);
			new_liste_polygon.put(geo, entry.getValue());
		}
		return new_liste_polygon;
	}


	//Regroupe tous les polygons valide dans un MultiPolygon puis le met en Geometry
	public Geometry regroupePolygon(Map<Geometry,Double> liste_polygon) throws IOException{
		System.out.println("\n[regroupePolygon]");
		int ii = 0;
		Polygon[] tab_polys = new Polygon[liste_polygon.keySet().size()];
		for(Geometry p:liste_polygon.keySet()){
			tab_polys[ii] = (Polygon)p;
			ii++;
		}
		GeometryFactory factory = new GeometryFactory();
		Geometry geo = factory.createMultiPolygon(tab_polys);
		return geo;
	}


	//Divise la Geometry avec le quadrillage et l'ecrit le fichier STL
	public void decoupeGeometry(Geometry geo,Map<Geometry,Double> liste_polygon) throws IOException{
		System.out.println("\n[decoupeGeometry]");
		int cpt=0;
		Map<Geometry, Double> myMap = new HashMap<Geometry,Double>();
		Map<Geometry, Double> valide2 = new HashMap<Geometry,Double>();
		Geometry limite = geo.getEnvelope();
		Coordinate[] coord = limite.getCoordinates();
		ArrayList<Geometry> liste = quadrillage(coord[0],coord[2],coord[1],coupe);
		for(Entry<Geometry, Double> current:liste_polygon.entrySet()){
			if(!current.getKey().isValid()){
				ArrayList<Geometry> valide =decomposePolygon(current.getKey());
				for(int i=0;i<valide.size();i++){
					if(!valide.get(i).isValid()){
						
					}
				}
				for(int i=0;i<valide.size();i++){
					valide2.put(valide.get(i), current.getValue());
				}
			}
			else{
				valide2.put(current.getKey(), current.getValue());
			}
		}
		for(Geometry cell:liste){
			GeometryToMesh msh = new GeometryToMesh(BASE_ELEVATION);
			for(Entry<Geometry, Double> current:valide2.entrySet()){
				if(current.getKey().isValid()){
					Geometry res =cell.intersection(current.getKey());
					if(!res.equals(cell)){
					ArrayList<Geometry> tempRes = new ArrayList<Geometry>();
					if(res != null)
						if(res instanceof MultiPolygon){
							MultiPolygon resmul = (MultiPolygon) res;
							ArrayList<Polygon> listepolys = decomposeMultiPolygon(resmul);
							tempRes.addAll(listepolys);
						}
						else 
							tempRes.add(res);
					for(Geometry g:tempRes)
						myMap.put(g, current.getValue());
					}
				}
			}
			for(Entry<Geometry, Double> entry : myMap.entrySet()){
				
				if(entry.getKey() instanceof Polygon) {
					msh.loadPolygon((Polygon)entry.getKey(), entry.getValue());
				} else {
					System.out.println("" + entry.getKey().getGeometryType());
				}
			}
			msh.loadPolygon((Polygon)cell, BASE_ELEVATION);
			msh.loadPolygon((Polygon)cell, 0);
			
			System.out.println("Data preparation ....");
			
			msh.prepareAndBuildData();
			
			
			
			System.out.println("Mesh generating ....");
			
			
			Mesh mesh = msh.buildMesh();
	         // Triangulate the mesh
            Mesh newMesh = PolygonModifier.triangulate(mesh);
            // Replace the old mesh
             // Triangulate the mesh
            // Replace the old mesh
          	Scene scene = new Scene();
			
			Transform tr = scene.getRootNode().createChildNode(newMesh).getTransform();
			System.out.println("Mesh saving ...."+ "/tmp/toto"+cpt+".stl");
	         
			scene.save("/tmp/mesh"+cpt+".stl", FileFormat.STL_BINARY);
			System.out.println("Next part");

		//	gtt.polygonSTL((Polygon)cell, BASE_ELEVATION, 0.0);
		//	WriteSTL write = new WriteSTL();
		//	WriteSTLA writea = new WriteSTLA();
			
		//	writea.ecrireSTL(gtt.getListeTriangle(), cpt);
		//	gtt.videListe();
			cpt++;
			myMap.clear();
		}	
	}
	
	
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



	//Retourne le quadrillage de la Geometry
	public ArrayList<Geometry> quadrillage(Coordinate min, Coordinate max,Coordinate minmax, double coupe){
		System.out.println("\n[Quadrillage]");
		ArrayList<Geometry> quadri = new ArrayList<Geometry>();
		GeometryFactory fact = new GeometryFactory();
		Point minp = fact.createPoint(min);
		Point maxp = fact.createPoint(max);
		Point minmaxp = fact.createPoint(minmax);
		System.out.println("Min: "+minp+ " -- Max: "+ maxp+" -- Minmax"+minmaxp);		
		System.out.println("Distance : minp to mimaxp: " + minp.distance(minmaxp) + " -- maxp to minmaxp: " + maxp.distance(minmaxp));
		
		double width = Math.round(((minp.distance(minmaxp))/coupe));
		double height = Math.round(((maxp.distance(minmaxp))/coupe));
		
		System.out.println("Width : " +width+ " - height: " + height + " -- width*coupe= " + width*coupe + " -- height*coupe= " + height*coupe);
		if(width*coupe<minp.distance(minmaxp))
			width++;
		if(height*coupe<maxp.distance(minmaxp))
			height++;
		System.out.println("Width : " +width+ " - height: " + height + " -- width*coupe= " + width*coupe + " -- height*coupe= " + height*coupe);
		
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				Coordinate coord1 = new Coordinate(min.x+coupe*i,min.y+coupe*j);
				Coordinate coord2 = new Coordinate(min.x+coupe*(i+1),min.y+coupe*j);
				Coordinate coord3 = new Coordinate(min.x+coupe*(i+1),min.y+coupe*(j+1));
				Coordinate coord4 = new Coordinate(min.x+coupe*i,min.y+coupe*(j+1));
				Coordinate[] cooord = {coord1,coord2,coord3,coord4,coord1};
				Polygon polys = fact.createPolygon(cooord);
				quadri.add(polys);
			}
		}
		return quadri;
	}


	//Parcours tous les polygons pour retrouver la hauteur du polygon donne
	public double hauteurPolygon(Map<Geometry, Double> decoupe,Polygon polys){
		for(Entry<Geometry, Double> entry : decoupe.entrySet()) {
			if(entry.getKey().equals(polys)){
				return entry.getValue();
			}
		}
		return 0;
	}
}