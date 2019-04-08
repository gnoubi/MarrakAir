package ummisco.map.shpToStl;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class WriteSTL {

	public WriteSTL(){}
	
	
	//Ecrit le fichier STL
	public void ecrireSTL(ArrayList<Triangle> tri, int num) throws IOException{
		FileOutputStream fos = new FileOutputStream("STL"+num+".stl");
		DataOutputStream dos = new DataOutputStream(fos);
		ecrireCommentaire(dos);
		ecrireNbTriangle(dos,tri.size());
		ecrireTriangles(dos,tri);
	}
	
	//Ecrit le commentaire du fichier STL
	public void ecrireCommentaire(DataOutputStream dos) throws IOException{
		for(int i=0;i<20;i++){
			dos.writeInt(0);
		}
	}

	
	//Ecrit le nombre de triangle dans le fichier STL
	public void ecrireNbTriangle(DataOutputStream dos,int size) throws IOException{
		writeIntLE(dos,size);
	}

	
	//Ecrit les triangles dans le fichier STL
	public void ecrireTriangles(DataOutputStream dos,ArrayList<Triangle> tri) throws IOException{
		Point3D[] point;
		for(int t=0;t<tri.size();t++){
			for(int g=0;g<3;g++)
				writeIntLE(dos,0);
			point=tri.get(t).getPoint3D();
			for(int l=0;l<3;l++){
				writeFloatLE(dos,point[l].getX());
				writeFloatLE(dos,point[l].getY());
				writeFloatLE(dos,point[l].getZ());
			}
			dos.writeShort(0);
		}
	}

	
	//Ecrit des float en little endian dans le fichier STL
	public static void writeFloatLE(DataOutputStream out, float value) throws IOException{
		writeIntLE(out,Float.floatToRawIntBits(value));
	}

	
	//Ecrit des int en little endian dans le fichier STL
	public static void writeIntLE(DataOutputStream out, int value) throws IOException{
		out.writeByte(value & 0xFF);
		out.writeByte((value >> 8) & 0xFF);
		out.writeByte((value >> 16) & 0xFF);
		out.writeByte((value >> 24) & 0xFF);
	}
}
