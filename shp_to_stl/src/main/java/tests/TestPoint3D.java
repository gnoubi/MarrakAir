package tests;

import ummisco.map.shpToStl.Point3D;
import ummisco.map.shpToStl.Triangle;

public class TestPoint3D {

	public static void main(String[] a) {
		Point3D p0 = new Point3D(0,0,0);
		Point3D p1 = new Point3D(1,0,0);
		Point3D p2 = new Point3D(0,1,0);
		Point3D p3 = new Point3D(-0.5f,-0.5f,0.0f);
		
		Point3D[] tr = {p0,p1,p2,p0};
		Point3D[] trInv = {p0,p2,p1,p0};
		
		Triangle t = new Triangle(tr);
		Triangle tInv = new Triangle(trInv);
		
		// Produit scalaire
		System.out.println("" + p1.scalarProduct(p2) + " should be 0.0");
		System.out.println("" + p1.scalarProduct(p1) + " should be 1.0");
		System.out.println("" + p1.scalarProduct(p3) + " should be negative");
		
		// Normal
		System.out.println("" + t.computeNormal() + " should be 0.0");
		System.out.println("" + tInv.computeNormal() + " should be 0.0");
		
	}
	
}
