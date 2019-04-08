package ummisco.map.shpToStl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

public class ShpFile{
	
	private File fileName;
	
	public ShpFile(File fileName){
		this.fileName=fileName;	
	}
	
	
	//Ouverture du fichier ShapeFile
	public ArrayList<SimpleFeature> readFile() throws IOException{
		Map<String, Object> map = new HashMap<>();
		map.put("url", fileName.toURI().toURL());
		DataStore dataStore = DataStoreFinder.getDataStore(map);
		String typeName = dataStore.getTypeNames()[0];
		FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
		Filter filter = Filter.INCLUDE;
		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
		FeatureIterator<SimpleFeature> features = collection.features();
		ArrayList<SimpleFeature> res = new ArrayList<SimpleFeature>();		
		while(features.hasNext())
			res.add(features.next());
		features.close();
		return res;
	}

}
