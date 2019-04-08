package org.geotools.tutorial.bis;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.GeometryFactory;
//import com.vividsolutions.jts.geom.MultiPolygon;
//import com.vividsolutions.jts.geom.Polygon;

public class Bis {

	public static void main(String[] args) throws Exception {

		//Creer fichier STL
		try{
			FileOutputStream fos = new FileOutputStream("zimbabwe.stl");
			DataOutputStream dos = new DataOutputStream(fos);

			//Recupere le fichier shp
			File file = new File("ne_50m_admin_0_sovereignty.shp");
			//ne_50m_admin_0_sovereignty.shp
			Map<String, Object> map = new HashMap<>();
			map.put("url", file.toURI().toURL());

			DataStore dataStore = DataStoreFinder.getDataStore(map);
			String typeName = dataStore.getTypeNames()[0];
			FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore
					.getFeatureSource(typeName);
			Filter filter = Filter.INCLUDE;
			FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);

			
			//Parcours toute la structure
			try (
					FeatureIterator<SimpleFeature> features = collection.features()) {

				while (features.hasNext()) {
					SimpleFeature feature = features.next();
					
					//System.out.println(feature.getAttributes());
					if(feature.getID().equals("ne_50m_admin_0_sovereignty.200")){
						
						/*
						 * Verification de la figure geometrique et recuperation des points
						 * */
						
						
						//System.out.println(feature.getAttribute("the_geom"));
						MultiPolygon mp = (MultiPolygon) feature.getAttribute("the_geom");
						Polygon[] polys = new Polygon[mp.getNumGeometries()];
						 for (int i = 0; i < mp.getNumGeometries(); i += 1) {
					            polys[i] = ((Polygon)mp.getGeometryN(i));
					            System.out.println(polys[i]);
					        }
						//apply(filter) enleve coord
						 System.out.println(polys[0].within(polys[0]));
			
						 polys[0]=polys[0];
						 Coordinate[] coord = null;
						 System.out.println(coord);
						 coord=polys[0].getCoordinates();
						 double test = coord[0].x;
						 System.out.println(test);
						 System.out.println(coord[0].x);
						 float n = (float) coord[0].x;
						 System.out.println(n);
						 
						
						
						
						
						
						
						String s = feature.getAttribute("the_geom").toString();
						int taille = s.length();
						boolean bool;
						float x,z;
						int cpt=0;
						int fin_mot=s.indexOf(" ");
						String type_geo="";
						type_geo=s.substring(0,fin_mot+1);
						s=s.substring(fin_mot+1,taille);
						taille = s.length();

						if(type_geo.equals("POINT")){
							if(s.substring(0,1).equals("Z")){
								s=s.substring(4,taille);
								taille = s.length();
							}
							if(s.substring(0,1).equals("M")){
								s=s.substring(3,taille);
								taille = s.length();
							}
							if(s.substring(0,1).equals("E")){
								s=s.substring(7,taille);
								taille = s.length();
							}
						}

						if(type_geo.equals("LINESTRING ")){
							s=s.substring(1,taille);
							taille = s.length();
						}

						if(type_geo.equals("POLYGON ")){
							s=s.substring(2,taille);
							taille = s.length();
						}

						if(type_geo.equals("MULTIPOINT ")){
							s=s.substring(2,taille);
							taille = s.length();
						}

						if(type_geo.equals("MULTILINESTRING ")){
							s=s.substring(2,taille);
							taille = s.length();
						}

						if(type_geo.equals("MULTIPOLYGON ")){
							if(s.substring(0,1).equals("E")){
								s=s.substring(7,taille);
								taille = s.length();

							}else{
								s=s.substring(3,taille);
								int verif=0;
								while(s.length()>2){
									//s=Polygone(s,dos);
									taille = s.length();
									if(s.indexOf(',')==0){
										s=s.substring(4,taille);
										taille = s.length();
									}
									if(s.indexOf('(')==0){
										s=s.substring(1,taille);
										taille = s.length();
									}
									if(s.indexOf(' ')==0){
										s=s.substring(2,taille);
										taille = s.length();
									}
								}
							}
						}
						//if(test.equals("GEOMETRYCOLLECTION")){} en attente
					}
				}
			}
		}catch(IOException re){
			System.out.println("Erreur creation fichier");
		}
	}


	public static String Polygone(String s, DataOutputStream dos){
		ArrayList<Float> liste = new ArrayList<Float>();
		int taille = s.length();
		int fin_mot, fin2;
		float x,z;
		boolean bool = true;
		while(bool==true){
			fin_mot=s.indexOf(' ');
			x = Float.valueOf(s.substring(0,fin_mot+1));
			liste.add(x);
			s=s.substring(fin_mot,taille);
			taille = s.length();
			fin2=s.indexOf(")");
			fin_mot=s.indexOf(',');
			if(fin2<fin_mot){
				fin_mot=fin2;
				bool=false;
			}

			if(fin_mot==-1){
				fin_mot=s.indexOf(')');
				bool=false;
			}
			z = Float.valueOf(s.substring(0,fin_mot));
			liste.add(z);
			s=s.substring(fin_mot+2,taille);
			taille = s.length();
			if(s.length()<3){
				bool=false;
			}
		}
		STLPolygone(liste, dos);
		return s;
	}

	public static void STLPolygone(ArrayList<Float> liste, DataOutputStream dos){
		ArrayList<Float> triangle = new ArrayList<Float>();
		ArrayList<Float> liste2 = new ArrayList<Float>();

		try{
			for(int i=0;i<20;i++){
				dos.writeInt(0);
			}

			int nb_tri=(liste.size()/2)-2;
			writeIntLE(dos,nb_tri);

			while(liste.size()!=6){
				triangle=divisionTriangle(liste);

				for(int j=0;j<3;j++){
					liste2.add(0.0f);
				}
				liste.remove(liste.indexOf(triangle.get(2)));
				liste.remove(liste.indexOf(triangle.get(3)));

				liste2.add(triangle.get(0));
				liste2.add(0.0f);
				liste2.add(triangle.get(1));
				liste2.add(triangle.get(2));
				liste2.add(0.0f);
				liste2.add(triangle.get(3));
				liste2.add(triangle.get(4));
				liste2.add(0.0f);
				liste2.add(triangle.get(5));
				for(int j=0;j<12;j++){
					writeFloatLE(dos,liste2.get(j));
				}
				dos.writeShort(0);
			}


			/*int x=2,z=3;
			while(z!=liste.size()-1){
				ArrayList<Float> liste2 = new ArrayList<Float>();
				for(int j=0;j<3;j++){
					liste2.add(0.0f);
				}
				liste2.add(liste.get(0));
				liste2.add(0.0f);
				liste2.add(liste.get(1));
				liste2.add(liste.get(x));
				liste2.add(0.0f);
				liste2.add(liste.get(z));
				x=x+2;
				z=z+2;
				liste2.add(liste.get(x));
				liste2.add(0.0f);
				liste2.add(liste.get(z));
				for(int j=0;j<12;j++){
					writeFloatLE(dos,liste2.get(j));
				}
				dos.writeShort(0);
			}*/

		}catch(IOException e){
			System.out.println("Erreur d'ecriture");
		}
	}

	public static void writeFloatLE(DataOutputStream out, float value) throws IOException{
		writeIntLE(out,Float.floatToRawIntBits(value));
	}

	public static void writeIntLE(DataOutputStream out, int value) throws IOException{

		out.writeByte(value & 0xFF);
		out.writeByte((value >> 8) & 0xFF);
		out.writeByte((value >> 16) & 0xFF);
		out.writeByte((value >> 24) & 0xFF);

	}


	public static void dessinerTriangle(ArrayList<Float> liste,DataOutputStream dos) throws IOException{
		for(int i=0;i<3;i++){
			writeFloatLE(dos,0.0f);
		}
		for(int i=0;i<6;i++){
			writeFloatLE(dos,liste.get(i));
		}
		dos.writeShort(0);
	}

	public static ArrayList<Float> divisionTriangle(ArrayList<Float> liste){

		if(liste.size()==6){

			return liste;
		}
		ArrayList<Float> liste2 = new ArrayList<Float>();

		int point_fin=4;
		int num_point=6;
		float point_A_x;
		float point_A_y;
		float point_B_x;
		float point_B_y;
		int cpt_intersection=0,cpt=0,fail=0,cpt_tour=0;
		float point_C_x;
		float point_C_y;
		float point_D_x;
		float point_D_y;

		while(true){
			point_A_x = liste.get(0);
			point_A_y = liste.get(1);
			point_B_x = liste.get(point_fin);
			point_B_y = liste.get(point_fin+1);
			cpt=0;
			while(cpt!=liste.size()-4){
				point_C_x = liste.get(num_point);
				point_C_y = liste.get(num_point+1);
				point_D_x = liste.get(num_point+2);
				point_D_y = liste.get(num_point+3);
				
				//Verification intersection entre segment + pas parallele
				if(segmentCroisement(point_A_x,point_A_y,point_B_x,point_B_y,point_C_x,point_C_y,point_D_x,point_D_y)){
					cpt++;
					num_point=num_point+2;
				}
			}
			
		}
	}


	/*	while(true){
			//System.out.println("aaa");
			point_C_x = liste.get(num_point);
			point_C_y = liste.get(num_point+1);
			point_D_x = liste.get(num_point+2);
			point_D_y = liste.get(num_point+3);
			//Verification intersection entre segment + pas parallele
			if(segmentCroisement(point_A_x,point_A_y,point_B_x,point_B_y,point_C_x,point_C_y,point_D_x,point_D_y)){
				point_fin=point_fin+2;
				num_point=2;
				point_B_x=liste.get(point_fin);
				point_B_y=liste.get(point_fin+1);
				fail++;
			}else{
				cpt_intersection=0;
				cpt_tour=0;
				while(cpt_tour!=liste.size()/2){
					//Verification si le segment se trouve dans le polygone + parallele				
					if(dansPolygone(point_A_x,point_A_y,point_B_x,point_B_y,point_C_x,point_C_y,point_D_x,point_D_y)){
						cpt_intersection++;
					}
					num_point=num_point+2;
					if(num_point>liste.size()-4)
						num_point=0;
					point_C_x = liste.get(num_point);
					point_C_y = liste.get(num_point+1);
					point_D_x = liste.get(num_point+2);
					point_D_y = liste.get(num_point+3);
					cpt_tour++;
				}
				if(cpt_intersection%2==1){
					for(int i=0;i<point_fin;i++){
						liste2.add(liste.get(i));
					}
					for(int i=0;i<point_fin;i++){
						liste.remove(2);
					}
					if(liste.size()<liste2.size()){
						divisionTriangle(liste);
					}
					else{
						divisionTriangle(liste2);
					}
				}else{
					point_fin=point_fin+2;
					num_point=2;
					point_B_x=liste.get(point_fin);
					point_B_y=liste.get(point_fin+1);
					fail++;
				}
			}
			num_point=num_point+2;
			if(num_point>point_fin-2){
				num_point=num_point+4;
			}
			if(num_point>liste.size()-4)
				num_point=2;
		}	/*	
	}


	/*
				for(int i=point_debut;i<=point_fin;i++){
					liste2.add(liste.get(i));
					if(i<point_debut && i>point_fin){
						liste.remove(i);
					}
				}
				if(liste.size()<liste2.size()){
					triangle=divisionTriangle(liste,dos);
				}else{
					triangle=divisionTriangle(liste2,dos);
				}
				dessinerTriangle(triangle,dos);
				for(int i=point_debut;i<point_fin+1;i++){
					liste.remove(i);
				}*/


	//Retourne true si il y a une intersection
	public static boolean segmentCroisement(float point_A_x,float point_A_y,float point_B_x,float point_B_y,float point_C_x,float point_C_y,float point_D_x,float point_D_y){
		float point_I_x = point_B_x - point_A_x;
		float point_I_y = point_B_y - point_A_y;
		float point_J_x = point_D_x - point_C_x;
		float point_J_y = point_D_y - point_C_y;
		float parallele = (point_I_x * point_J_y - point_I_y * point_J_x);

		if(parallele!=0){
			float m = (point_I_x*point_A_y-point_I_x*point_C_y-point_I_y*point_A_x+point_I_y*point_C_x)/parallele;
			float k = (point_J_x*point_A_y-point_J_x*point_C_y-point_J_y*point_A_x+point_J_y*point_C_x)/parallele;
			if(0<m && m<1 && 0<k && k<1)
				return true;
			else
				return false;
		}else
			return false;
	}

	//Retourne true si il y a intersection
	public static boolean dansPolygone(float point_A_x,float point_A_y,float point_B_x,float point_B_y,float point_C_x,float point_C_y,float point_D_x,float point_D_y){
		float milieu_x=(point_A_x+point_B_x)/2;
		float milieu_y=(point_A_y+point_B_y)/2;
		float random_x=81320.0f;
		float random_y=44654.0f;
		float point_I_x = random_x - milieu_x;
		float point_I_y = random_y - milieu_y;
		float point_J_x = point_D_x - point_C_x;
		float point_J_y = point_D_y - point_C_y;
		float parallele = (point_I_x * point_J_y - point_I_y * point_J_x);
		if(point_C_x<milieu_x && point_D_x<milieu_x)
			return false;
		if(parallele!=0){
			float k = (point_J_x*milieu_y-point_J_x*point_C_y-point_J_y*milieu_x+point_J_y*point_C_x)/parallele;
			if(0<k && k<1)
				return true;
			else
				return false;
		}else
			return false;
	}
	
	//Retourne la liste avec roulement de 1
	public static ArrayList<Float> roulementListe(ArrayList<Float> liste){
		ArrayList<Float> liste2 = new ArrayList<Float>();
		
		return liste2;
	}
}
