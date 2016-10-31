/**
* Name: Marrasketch
* Author: Arnaud Grignard
* Description: Model only displaying the current gis file of the MarrakAir model
*/
  

model Marrasketch

global {
	file shape_file_roads <- file("../includes/SIG_simu/roads_gama.shp");
	file shape_file_buildings <- file("../includes/SIG_simu/buildings_gama.shp");
	file shape_file_bound <- file("../includes/SIG_simu/reperes.shp");
	graph the_graph; 
	geometry shape <- envelope(shape_file_bound);
	
	init {
		create road from: shape_file_roads with:[type::string(get("type"))]{
         if (type = 'primary') {
				color <- #red;
	     }
	     if (type = 'secondary') {
				color <- #blue;
	     }
	     if (type = 'tertiary') {
				color <- #black;
	     }
		}	
		the_graph <- as_edge_graph(road) ;
		
		
		create building from: shape_file_buildings with: [type:: string(read('building'))] {
			if type = 'small' {
				height<-10 + rnd(5);
			}
			if type = 'medium' {
				height<-50 + rnd(10);
			}
			if type = 'tall' {
				height<-100 +rnd(10) ;
			}
		}
		create people number: 100 {
			target <- any_location_in(one_of (road)) ;
			location <- any_location_in (one_of(building));
			source <- location;
		} 
	}

}

species road {
    string type;
    rgb color;
	aspect base {
		draw shape color: color;
	}
}

species building {
	string type;
	rgb color <- rgb(220,220,220);
	int height;
	aspect base {
		draw shape color: color depth: height;
	}
}

species people skills: [moving] {
	point target;
	path my_path; 
	point source;
	aspect base {
		draw circle(10) color: #green;
	}
	
	reflex movement {
		do goto target:target on:the_graph;
	}
}

experiment GIS_agentification type: gui {
	output {
		display city_display type:opengl{
		//	image '../includes/SIG_simu/background.png' refresh: false;
			species building aspect:base;
			species road aspect:base;
			species people aspect:base;
		}
	}
}
