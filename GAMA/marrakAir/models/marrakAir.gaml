/*****************************************************************************************************************************************************************
 ***  Module d'affectation des comptages routiers sur un réseau routier en milieu urbain
 ***  v.1.3.3
 ***
 ***  Author: Justin Emery 2 3, Nicolas Marilleau 1, , Thomas Thevenin 2, Nadège Martiny 3 
 *** 
 ***  1 UMI 209 UMMISCO IRD/UPMC, Bondy
 ***  2 Université de Bourgogne, UMR ThéMA, Dijon
 ***  3 Université de Bourgogne, UMR Biogéosciences, CRC, Dijon
 ***  
 */

model affectation

/* Insert your model definition here */

global
{
	int MOTORBYKE_ID <- 0;
	int CAR_ID <- 1;
	int TRUCK_ID <- 2;
	float ANGLE <-2.3;
		
	int TRAFFIC_LIGHT_DENSITY <- 10;
	list<int> TRAFFIC_LIGHT_SIZES <- [5,3];
	
	
	float MOTORBYKE_COEF <- 2;
	float CAR_COEF <- 1;
	float TRUCK_COEF <- 1;
	
	
	// Pas-de-temps à modifier en fonction de la taille du réseau
	float stepDuration <- 5#s; //#mn ; 	
	
	// Une periode de 15 minutes entre chaque mesure
	float capturePeriod <- 15#mn ; 	
	
	// choisir en fonction du reseau "speed" # "hierarchy" # "random"
	string carBehaviorChoice <- "hierarchy"; 
	
	//Génération du DeathTime (fonction aléatoire au sein d'une gaussienne de DeatTime--30 / Ecart-- 5)
	float gdeathDay <- 30#mn;
	float ecart <- 5#mn;
	
	//Génération de la graine aléatoire
	int randomSeed <- 1;
	
	string Name <-"H";
	
//************************************************************************************************************************************************************
//**********************************************************FICHIER GEOGRAPHIQUE******************************************************************************
//************************************************************************************************************************************************************

	// Choix du réseau de simulation
	//RESEAU DIJONNAIS	
	
	//string mynetwork <- "../includes/RESEAU_ROUTIER_MARRAKECH/ROUTE_MAR.shp");
	file mynetwork <- file("../includes/SIG_demonstrateur/roads_gama.shp");
	file trafic_show <- file("../includes/SIG_demonstrateur/traffic_show.shp");
	file node_shape <- file("../includes/SIG_demonstrateur/nodes_gama.shp");
	file PM <- file("../includes/SIG_demonstrateur/PM_T.shp");
	file cell_shape <- file("../includes/SIG_demonstrateur/road_cells.shp");
	file water <- file("../includes/SIG_demonstrateur/water.shp");
	file airport <- file("../includes/SIG_demonstrateur/airport.shp");
	file shape_file_buildings <- file("../includes/SIG_simu/buildings_gama.shp");
	file shape_file_bound <- file("../includes/keystone_layer.shp");
	file mydummynetwork <- file("../includes/SIG_demonstrateur/dummy_roads.shp");
	file logos <- file("../includes/logos.png");
		
	//COMPTAGES ROUTIER
	matrix countingData <- matrix(csv_file( '../includes/PM_Test.csv', ';'));
	
	//MATRICE DE CHOIX ROUTIER
	matrix<float> HierarchyMatrix <- matrix<float>(csv_file( '../includes/HierarchyChoice.csv', ';'));
	matrix<float> SpeedMatrix <- matrix<float>(csv_file( '../includes/SpeedChoice.csv', ';'));
	
	
	//Matrices de COPERT
	map<string,map<float,list<list<float>>>> copert;
	map<string,map<float,list<list<float>>>> copert07;
	map<string,map<float,list<list<float>>>> copert20;
	float energy <- 0.5;
	float vehicle_2020_norm_rate <- 0.5 ;
	list<building> alived_building;
//************************************************************************************************************************************************************
//************************************************************************************************************************************************************
//************************************************************************************************************************************************************	
	
	graph roads_graph;
	geometry shape <- envelope(shape_file_bound);
	
	road roadToDisplay;
	int cpt <- 0;		
	int nbCar_created <- 0;
	
	int nbCycleInPeriod <- int(capturePeriod / stepDuration);
	float maxNox <- 2500; //1.0 update: max(pollutant_grid collect(each.pollutant[world.pollutentIndex("nox")]));
	float maxNox_buildings <- 5; //0000 ;   //1.0 update: 10000; //max(building collect(each.pollutant[world.pollutentIndex("nox")]));
	float diffusion_rate <- 0.5;
	
	float max_speed <-  70#km/#h;
	float percent_of_car <- 0.7; //1 equal 100% cars...
	float percent_of_truck <- 0.3;
	
	bool show_trafic <- true;
	bool show_pollution <- true;
	bool show_keystone <- true;
	
	 
	map<float,list<list<float>>> readCopertData(string fileName)
	{
		map<float,list<list<float>>> res <-[];
		matrix<float> copert <- matrix<float>(csv_file( fileName, ';'));
	
		list<list<float>> cols <- rows_list(copert);
		int i <- 0;
		loop i from:0 to: length(cols) - 1
		{
			list<float> line <- cols at i;
			float speed <- line[0];
			remove index:0 from: line; //tyu
			
			list<float> essence <-[];
			list<float> diesel <-[];
			
			loop j from:0 to: (length(cols) / 2) - 1
			{
				add line[j*2] to: essence ;
				add line[j*2 +1 ] to: diesel ;
			}
			res <- res + (speed::[essence,diesel]);
		}
		return res;
	}
	
	int pollutentIndex(string name)
	{
		int idx <- 0;
		switch(name)
		{
			match "hfc" {idx <- 0;}
			match "co2" {idx <- 1;}
			match "nox" {idx <- 2;}
			match "pm" {idx <- 3;}
			match "co" {idx <- 4;}
			match "cov" {idx <- 5;}
		}
		return idx; //  (1 + idx *2 + (energy = "essence" ? 0:1));
	}
	
	int select_speed_hierarchy(float speed)
	{
		int res <- 0;
		if(speed <=25 )
		{
			res <- 5;
		}
		else if(speed <=50 )
		{
			res <- 4;
		}
		else if(speed <=90 )
		{
			res <- 3;
		}
		else if(speed <=110 )
		{
			res <- 2;
		}
		else if(speed <=130 )
		{
			res <- 1;
		}
		return res;
	}
	
	init
	{
		do initialize();
		create userAgent number:1
		{
	 		do connect to:"localhost";
			do expose variables:["gasoline_population","diesel_population"] with_name:"energy";
			do expose variables:["truck_population","car_population","motorbike_population"] with_name:"typeVehicle";
			do expose variables:["n2007","n2020","my_date"] with_name:"normVehicle";
			do expose variables:["my_date","pollution_nox_intanstanee",  "pollution_particule_instantanee","pollution_co2_intanstanee" ] with_name:"pollutantGraph";
			do listen with_name:"slide_energy" store_to:"selected_energy";
			do listen with_name:"slide_vehicule" store_to:"selected_vehicule";
			do listen with_name:"slide_speed" store_to:"selected_speed";
			do listen with_name:"show_pollution" store_to:"selected_pollution";
			do listen with_name:"show_trafic" store_to:"selected_trafic";
			do listen with_name:"reset" store_to:"reset_simulation";
			do listen with_name:"copert" store_to:"copert_2020_rate";
			do listen with_name:"show_keystone" store_to:"selected_keystone";
		}
		
	}
	
	
	action reset
	{
		ask carHierarchyChange
		{
			do die;
		}
		ask carRandomChange
		{
			do die;
		}
		ask crossroad 
		{
			do die;
		}
		ask building
		{
			do die;
		}
		ask landscape
		{
			do die;
		}
		
		ask carCounter
		{
			do die;
		}
		
		ask pollutant_grid
		{
			do die;
		}
		
		ask bound
		{
			do die;
		}
		ask road
		{
			do die;
		}
		ask traffic_light
		{
			do die;
		}
		
		do initialize();
		
		
	}
/*	
	action change_copert
	{
		if (vehicle_year = "2007")
		{
			copert <- copert07; //  + ("2007"::readCopertData( '../includes/CopertData2007.csv'));
		}
		if (vehicle_year = "2020")
		{
			copert <- copert20; // + ("2020"::readCopertData( '../includes/CopertData2020.csv'));
		}
		
	}
	*/
	action initialize
	{
		time <- 8#h;
		copert <-[];
		//Choix du parc automobile en fonction du parametres véhicle_year
		copert <- copert + ("2007"::readCopertData( '../includes/CopertData2007.csv'));
		copert <- copert + ("2020"::readCopertData( '../includes/CopertData2020.csv'));
		
	//	do change_copert;
		
		create colorSet;
		
		create crossroad from:node_shape;
		write "Map is loading..."; 
		
		//create traffic_light from: trafic_show with:[cell_index::int(read("num_cell"))];
		
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
		
		create landscape from:water
		{
			mycolor <- first(colorSet).WATER;
		}
		create landscape from: airport
		{
			mycolor <- first(colorSet).AIRPORT;
		}
		create pollutant_grid from:cell_shape{
			neighboor_buildings <- building at_distance 70#m;
			alived_building <- alived_building accumulate(neighboor_buildings); 
		}
		
		
		
		create bound from: shape_file_bound;
		// Génération du réseau routier Attention aux attributs caractérisant la hiérarchie du réseau routier en code (Vitesse # Hiérarchie) + Vitesse réglementaire
		create road from: mynetwork with:[mid::int(read("osm_id")), oneway::int(read("oneway")),hierarchy::float(read("TYPE_OSM_C")),roundaboutId::int(read("roundabout")),mspeed::float(read("maxspeed"))#km/#h]
		{
			point end <- last(shape.points) ;
			point begin <- first(shape.points);
			fcrossroad <- crossroad closest_to(begin);
			tcrossroad <- crossroad closest_to(end);
			containCarCounter_digit <- false;
			containCarCounter_ndigit <- false;
			speed_hierarchy <- world.select_speed_hierarchy(mspeed);
			
			// debut bac a sable de Tri
			if hierarchy <=5{
				aspect_size <- TRAFFIC_LIGHT_SIZES[0];
			}else{aspect_size <-TRAFFIC_LIGHT_SIZES[1];}
			segments_number <- length(shape.points)-1;
			loop i from: 0 to: segments_number-1{
				add shape.points[i+1].x - shape.points[i].x to: segments_x;
				add shape.points[i+1].y - shape.points[i].y to: segments_y;
				add sqrt(segments_x[i]^2 + segments_y[i]^2) to: segments_length;
				if oneway != 1 {
					add {segments_y[i]/segments_length[i]*4,- segments_x[i]/segments_length[i]*4} to: lane_position_shift;
				}
			}
			// fin bac  a sable de Tri
			
		} //initroad
		
		create dummy_road from: mydummynetwork with:[oneway::int(read("oneway")),linkedToRoad::int(read("linkedToRoad"))]
		{
			aspect_size <- TRAFFIC_LIGHT_SIZES[0];
			segments_number <- length(shape.points)-1;
			loop i from: 0 to: segments_number-1{
				add shape.points[i+1].x - shape.points[i].x to: segments_x;
				add shape.points[i+1].y - shape.points[i].y to: segments_y;
				add sqrt(segments_x[i]^2 + segments_y[i]^2) to: segments_length;
				if oneway != 1 {
					add {segments_y[i]/segments_length[i]*4,- segments_x[i]/segments_length[i]*4} to: lane_position_shift;
				}
			}
		} //initdummyroad
		
		create infoDisplay{}
		create legend{}
		
		// Génération des Postes de comptage DIGIT correspond au sens de comptage des PM et du sens de digitalisation du réseau routier
		create carCounter from:PM with:[mid::int(read("Id")), isdigitOriented::bool(read("DIGIT"))]
		{
			associatedRoad <- road closest_to(self);
			if(isdigitOriented)
			{
				associatedRoad.containCarCounter_digit <- true;
				nbCar_ndigit <- 0;
			}
			else
			{
				nbCar_ndigit <- nbCar_digit ;
				nbCar_digit <- 0;
				associatedRoad.containCarCounter_ndigit <- true;
			}
		} //initcarCounter 
		 
		write "Traffic rule assigment...";
		int nbRoad <- length(road);
		int counterm <- 0;
		int lastUpdate  <- 0;
		 
		ask road
		{
			tnextRoad <- road where (((each.tcrossroad = self.tcrossroad and !each.containCarCounter_digit and (each.oneway !=1)) or (each.fcrossroad = self.tcrossroad  and !each.containCarCounter_ndigit) ) and each !=self);
			//tnextRoad <- road where (((each.fcrossroad = self.tcrossroad and !each.containCarCounter_digit and (each.oneway !="TF")) or (each.tcrossroad = self.tcrossroad  and !each.containCarCounter_ndigit) and (each.oneway !="FT") ) and each !=self);
			TOSM <- computeOSM(tnextRoad);
			TSPEED <- computeSPEED(tnextRoad);
			fnextRoad <- road where (((each.fcrossroad = self.fcrossroad and !each.containCarCounter_digit and (each.oneway !=1)) or (each.tcrossroad = self.fcrossroad  and !each.containCarCounter_ndigit) ) and each !=self);
			//fnextRoad <- road where (((each.fcrossroad = self.fcrossroad and !each.containCarCounter_digit and (each.oneway !="TF")) or (each.tcrossroad = self.fcrossroad  and !each.containCarCounter_ndigit) and (each.oneway !="FT") ) and each !=self);
			FOSM <- computeOSM(fnextRoad);
			FSPEED <- computeSPEED(fnextRoad);
			//speed_hierarchy <- TSPEED;
			counterm <- counterm + 1 ;
			if(int((counterm / nbRoad) * 100) != lastUpdate )
			{
				lastUpdate <- int((counterm / nbRoad) * 100) ;
				write " "+ lastUpdate + "% " +TSPEED ;
			}
		}//askroad
		 
		step <- stepDuration;
		
		
		
		write "Traffic counter data loading..."; 
		
		loop temp_line over: rows_list(countingData) 
		{
			int nbCol <- length(temp_line);
			list<string> milieux <- [];
			loop j from: 1 to: ( nbCol - 1 ) 
			{
				milieux <-milieux +  int(temp_line at j);
			}
			carCounter crr <- carCounter first_with(each.mid =  int(temp_line at 0));
			crr.carCounts <- milieux;
		} // loop temp_line
	 
		// Route pour le RoadToDisplay ID à modifier pour un suivi ciblé	 ici Bld Mohamed Abdelkaraim el Khattabi
		roadToDisplay <-(road first_with(each.mid = 305882936 ));	
		//write copert;
		
		 
		
	} //init 
	
	reflex suivi when:false
	{
		write " TimeMachine " + (cycle)+ "--"+(cycle/60)+"h" ;
	}
	
	// Définition de la fin de la simulation 24h + 1sec pour obtenir le dernier 1/4h
	reflex stop_sim when:  time >= 24#h+1#sec
	{
		do halt;
	} 
} //global

species bound schedules:[] {
	/*reflex doDie when: cycle=2{
		do die;
	}*/
	

	
	aspect base {
		if (show_keystone){
			draw polygon([{1000,1000},{1000,5000}, {8000,5000}, {8000,1000}]) color: #black;
			draw shape color: #red;	
			if cycle > 0 {show_keystone <-false;}		
		}
	}
}

species traffic_light schedules:[]
{
	int cell_index;
	aspect base
	{
		if((cell_index + cycle) mod TRAFFIC_LIGHT_DENSITY = 0)
		{
			draw circle(2#m) color:#white;	
		}
	}
}

species userAgent skills:[remoteGUI]
{
	int gasoline_population <- 0 ;
	int diesel_population <- 0 ;
	int truck_population <- 0 ;
	int car_population <- 0 ;
	int n2007 <- 0;
	int n2020 <- 0;
	int motorbike_population <- 0 ;
	float pollution_nox_max <- 0 ;
	float pollution_nox_intanstanee <- 0 ;
	//float polution_nox_intantanee <- 0 ;
	float pollution_particule_max <- 0 ;
	float pollution_particule_instantanee <- 0 ;
	float pollution_co2_intanstanee <- 0;
	int my_date <- 0;
	float selected_energy <- round(energy * 100);
	float selected_energy_old <-  round(energy * 100);
	 
	float selected_vehicule <- int(percent_of_car*100);
	float selected_vehicule_old <- int(percent_of_car*100);
	
	float selected_speed <- 0;
	float selected_speed_old <- 0;
	float selected_trafic <- -1;
	float selected_pollution <- -1;
	float reset_simulation <- 0;
	float copert_2020_rate;
	float selected_keystone <- 1;
	
	//float polution_particule_intantanee <- 0 ;
	reflex update_data when: (cycle mod 12) = 0
	{ 
		int tt <-length(carHierarchyChange); 
		
		gasoline_population <- round(tt=0?50:((carHierarchyChange count (each.my_energy = 0))/tt*100));
		diesel_population <-  100 -  gasoline_population ; //count (each.my_energy = 1);
		truck_population <- round(tt=0?20:((carHierarchyChange count (each.my_type_of_vehicle = TRUCK_ID))/tt*100));
		car_population <-  round(tt=0?40:((carHierarchyChange count (each.my_type_of_vehicle = CAR_ID))/tt*100)); 
		motorbike_population <- 100 - truck_population -  car_population ; //carHierarchyChange count (each.my_type_of_vehicle = MOTORBYKE_ID);
	//	pollution_nox_max <- max(list(pollutant_grid collect(each.pollutant[world.pollutentIndex("nox")])));
		pollution_nox_intanstanee<- mean(list(building collect(mean(each.pollutant_history[world.pollutentIndex("co")]))));
		pollution_co2_intanstanee<- mean(list(building collect(mean(each.pollutant_history[world.pollutentIndex("co2")]))));
//		pollution_particule_max <- max(list(pollutant_grid collect(each.pollutant[world.pollutentIndex("pm")])));
		pollution_particule_instantanee <- mean(list(building collect(mean(each.pollutant_history[world.pollutentIndex("pm")]))));
	/*	pollution_nox_max <- max(list(pollutant_grid collect(each.pollutant[world.pollutentIndex("nox")])));
		pollution_nox_intanstanee<- mean(list(building collect(each.pollutant[world.pollutentIndex("nox")])));
		pollution_particule_max <- max(list(pollutant_grid collect(each.pollutant[world.pollutentIndex("pm")])));
		pollution_particule_instantanee <- mean(list(pollutant_grid collect(each.pollutant[world.pollutentIndex("pm")])));
	 */	n2007 <- round(tt=0?50:(carHierarchyChange count (each.my_vehicle_year = 2007)/tt)*100) ;
		n2020 <- 100 - n2007 ;
		
		my_date <- cycle;
		
		
		
		if(selected_energy != selected_energy_old) {
			energy <- selected_energy / 100;
			selected_energy_old <- selected_energy;
		}
		
		if(selected_vehicule != selected_vehicule_old) {
			percent_of_car <- selected_vehicule/100;
			selected_vehicule_old <- selected_vehicule;
		}
		
		if(selected_speed != selected_speed_old) {
			max_speed <- selected_speed#km/#h;
			selected_speed_old <- selected_speed;
		}
		
		if(selected_trafic >=0) {
			show_trafic <- selected_trafic = 1;
			selected_trafic <- -1.0;
		}
		
		if(selected_pollution >=0) {
			show_pollution <- selected_pollution = 1;
			selected_pollution <- -1.0;
		}
		
		if(selected_keystone >= 0){
			show_keystone <- selected_keystone = 1;
			selected_keystone <- -1.0;
		}
		
		if(reset_simulation = 1) {
			ask world {
				do reset;
			}
				
		}
		//write "copert "+ (copert_2020_rate / 100) + "  "+ vehicle_2020_norm_rate;
		
		if(copert_2020_rate != nil and copert_2020_rate/100 != vehicle_2020_norm_rate)
		{
			vehicle_2020_norm_rate <- copert_2020_rate/100;
			
		}
		
	}
}

species pollutant_grid schedules:[]
	{
		list<float> pollutant <- list_with(6,0.0);
		list<building> neighboor_buildings;
		aspect nox_aspect
		{
			draw shape color:rgb(0,255-int((1-pollutant[world.pollutentIndex("nox")]/maxNox)*255),0) ;
		}
	}
species landscape schedules:[]
{
	rgb mycolor <- #blue;
	
	aspect base 
	{
		draw shape color:mycolor;
	}
}
species road schedules: ( time mod capturePeriod ) = 0 and time != 0.0 ? road :[]
//species road 
{
	crossroad fcrossroad;
	crossroad tcrossroad;
	int mid;
	bool containCarCounter_digit <- false;
	bool containCarCounter_ndigit <- false;
	int traffic <- 0;
	int sumTraffic <- 0;
	int meanTraffic <- 0;
	int lastTraffic <- 0;
	int capacity <- 0;
	int long <- 0;
	int pcapacity <- 0;
	int speed_hierarchy <-0;
	int hierarchy <- 0;
	//string oneway <- "";
	int oneway;
	int roundaboutId <- nil;
	float mspeed <- 0.0;
	float distance <-  shape.perimeter;
	
	// bac a sable de Tri
	float density <- 1.0;
	float traffic_density <- 0.0;
	int segments_number ;
	int aspect_size <-1 ;
//	rgb aspect_color <- °white;
	list<float> segments_x <- [];
	list<float> segments_y <- [];
	list<float> segments_length <- [];
	list<point> lane_position_shift <- [];
	// fin du bac a sable de Tri
	
	list<float> pollutant <- list<float>(list_with(6,0));
	
	list<float> sum_pollutant <- list<float>(list_with(6,0));
	list<road> fnextRoad;
	list<road> tnextRoad;
	
	list<list<road>> FOSM;
	list<list<road>> TOSM;
	
	list<list<road>> FSPEED;
	list<list<road>> TSPEED;
	
		
	list<list<road>> computeOSM(list<road> pouet)
	{
		list<list<road>> rt <- nil;
		if(pouet = nil)
		{
			rt <- [nil,nil,nil,nil,nil,nil,nil,nil,nil,nil,nil];
		}
		else
		{
			list<road> OSM1 <- 	pouet where(each.hierarchy = 1.0);
			list<road> OSM2 <- 	pouet where(each.hierarchy = 2.0);
			list<road> OSM3 <- 	pouet where(each.hierarchy = 3.0);
			list<road> OSM4 <- 	pouet where(each.hierarchy = 4.0);
			list<road> OSM5 <- 	pouet where(each.hierarchy = 5.0);
			list<road> OSM6 <- 	pouet where(each.hierarchy = 6.0);
			list<road> OSM7 <- 	pouet where(each.hierarchy = 7.0);
			list<road> OSM8 <- 	pouet where(each.hierarchy = 8.0);
			list<road> OSM9 <- 	pouet where(each.hierarchy = 9.0);
			list<road> OSM10 <- pouet where(each.hierarchy = 10.0);
			list<road> OSM11 <- pouet where(each.hierarchy = 11.0);
		 
			rt <- [OSM1,OSM2,OSM3,OSM4,OSM5,OSM6,OSM7,OSM8,OSM9,OSM10,OSM11];	
		}
		return rt;
	} //computeOSM

	list<list<road>> computeSPEED(list<road> pouett)
	{
		list<list<road>> rtt <- nil;
		if(pouett = nil)
		{
			rtt <- [nil,nil,nil,nil,nil];
		}
		else
		{		
			list<road> route130 <- 	pouett where(each.speed_hierarchy = 1.0);
			list<road> route110 <- 	pouett where(each.speed_hierarchy = 2.0);
			list<road> route90 <- 	pouett where(each.speed_hierarchy = 3.0);
			list<road> route50 <- 	pouett where(each.speed_hierarchy = 4.0);
			list<road> route25 <- 	pouett where(each.speed_hierarchy = 5.0);
								
			 rtt <- [route130,route110,route90,route50,route25];	
		}
		return rtt;
	} //COMPUTESPEED
	
	
	 
	
	// Sauvegarde automatique du fichier . txt tous les 1/4h pour chauqe axe de l'espace d'étude
	reflex save_result_captuPeriod 
	{
		//save ("\t" + cycle + "\t" + mid + "\t" + traffic + "\t" + sumTraffic) type:text to:carBehaviorChoice+"_SIMU_MARRAKECH_"+Name+"_"+randomSeed+"_"+gdeathDay+".txt";
	}	
	
	// bac a sable Tri
	reflex updateTrafficDensity
	{
	//	write time;
	//	write cycle;
		traffic_density <- 0.8*traffic_density  + traffic;
	//	write traffic_density;
	} 
	// fin bac a sable Tri
	
	reflex updateCounter when:( time mod capturePeriod ) = 0 and time != 0.0
	{
		sumTraffic <- sumTraffic + traffic;
		lastTraffic <- traffic;
		traffic <- 0;
		meanTraffic <- sumTraffic / int(time / capturePeriod );
	}
	

	
	
	aspect base
	{
		draw 5#m around shape depth: 0 color:#white  ;
	}
	aspect base3D
	{	
		draw shape depth:(meanTraffic)  color:rgb(float(meanTraffic*2),255-float(meanTraffic),0) size:meanTraffic; 
	}
	aspect base3D_capacity
	{	
		draw shape depth:(traffic/capacity*100) color:rgb(50+float(meanTraffic*2),255-float(meanTraffic*2),0);	
	}
	aspect car_lights
	{
		
		point new_point;
		int lights_number;

		if cycle > 0 {
		
			density <- min([10,traffic_density]);
			
	
		 	loop i from: 0 to: segments_number-1{
		 	
		 	
		 		
		 		// pour afficher des petits triangles pour indiquer le sens de circulation sur chaque route (pour faire des tests)
//		 		if oneway = 1 {
//		 			float angleTriangle <- acos(segments_x[i]/segments_length[i]);
//		 			angleTriangle <- segments_y[i]<0 ? - angleTriangle : angleTriangle;
//					draw triangle(10) at:  shape.points[i] + {0.5*segments_x[i],0.5*segments_y[i]} rotate: 90+angleTriangle color: °cyan;
//		 		}
		 		
		 		
				lights_number <- max([1,int(density * segments_length[i]/150)]);
			 	loop j from:0 to: lights_number-1{
			 		if oneway = 1{
			 			
			 				new_point <- {shape.points[i].x + segments_x[i] * (j +  mod(cycle,100)/100)/lights_number, shape.points[i].y + segments_y[i] * (j + mod(cycle,100)/100)/lights_number};
							draw circle(aspect_size, new_point) color: first(colorSet).LIGHTS;
			 		}else{
			 			
			 				new_point <- {lane_position_shift[i].x + shape.points[i].x + segments_x[i] * (j -  mod(cycle,100)/100)/lights_number, lane_position_shift[i].y + shape.points[i].y + segments_y[i] * (j - mod(cycle,100)/100)/lights_number};
							draw circle(aspect_size, new_point) color: first(colorSet).LIGHTS;
							new_point <- {-lane_position_shift[i].x + shape.points[i].x + segments_x[i] * (j +  mod(cycle,100)/100)/lights_number, -lane_position_shift[i].y + shape.points[i].y + segments_y[i] * (j + mod(cycle,100)/100)/lights_number};
							draw circle(aspect_size, new_point) color: first(colorSet).LIGHTS;
							
			 		}
				
					
				}
			}
			
		}else{
			draw 5#m around shape depth: 0 color:#purple  ;
		}
		
		
	}
} //road

species dummy_road schedules: [] 
{
	int oneway;
	float density <- 1.0;
	float traffic_density <- 8.0;
	int segments_number ;
	int aspect_size <-1 ;	
	list<float> segments_x <- [];
	list<float> segments_y <- [];
	list<float> segments_length <- [];
	list<point> lane_position_shift <- [];

	aspect car_lights
	{
		
		point new_point;
		int lights_number;
	//	draw 5#m around shape depth: 0 color:#white  ;
		
		
		density <- min([10,traffic_density]);

	 	loop i from: 0 to: segments_number-1{
			lights_number <- max([1,int(density * segments_length[i]/150)]);
		 	loop j from:0 to: lights_number-1{
		 		if oneway = 1{
		 			
		 				new_point <- {shape.points[i].x + segments_x[i] * (j +  mod(cycle,100)/100)/lights_number, shape.points[i].y + segments_y[i] * (j + mod(cycle,100)/100)/lights_number};
						draw circle(aspect_size, new_point) color: first(colorSet).LIGHTS;
		 		}else{
		 			
		 				new_point <- {lane_position_shift[i].x + shape.points[i].x + segments_x[i] * (j -  mod(cycle,100)/100)/lights_number, lane_position_shift[i].y + shape.points[i].y + segments_y[i] * (j - mod(cycle,100)/100)/lights_number};
						draw circle(aspect_size, new_point) color: first(colorSet).LIGHTS;
						new_point <- {-lane_position_shift[i].x + shape.points[i].x + segments_x[i] * (j +  mod(cycle,100)/100)/lights_number, -lane_position_shift[i].y + shape.points[i].y + segments_y[i] * (j + mod(cycle,100)/100)/lights_number};
						draw circle(aspect_size, new_point) color: first(colorSet).LIGHTS;
						
		 		}
			
				
			}
		}
		
		
	}
} //dummy_road



species crossroad
{
	int nbCarIncome;
	reflex eatcars
	{
		point mylocation <- location;
		list<car> realcars <- car where (each.location = mylocation and !(each.isGhost ));
		list<car> ghostcars <- car where (each.location = mylocation and (each.isGhost ));
		int nbcar <- min([length(realcars ),length(ghostcars)]);
			
		int i <-0;	
		if( nbcar > 0)
		{
			realcars <- shuffle(realcars);
			ghostcars <-shuffle(ghostcars);
			loop while:( i < length(realcars) and length(ghostcars) > 0)
			{
				car myCar <- realcars[i];
				car ghostcar <- ghostcars first_with(each.previousRoad != myCar.previousRoad);					
				if(ghostcar != nil)				
					{
						remove myCar from:realcars ;
						remove ghostcar from:ghostcars ;
						ask myCar
						{
							do die;
						}
						ask ghostcar
						{
							do die;
						}	
					}
				i <- i +1;
			}//loop						
		}
	}//eatcars
	
	aspect base
	{
		draw circle(2) color:rgb("red");
	}
}// crossroads

species carCounter schedules: ( time mod 1#mn ) = 0 ? carCounter: []
{
	int mid;
	list<int> carCounts <- [];
	int nbCar_digit; 
	int nbCar_ndigit;
	bool isdigitOriented;
	road associatedRoad;
	float nbCar_digit_to_create <- 0;
	float nbCar_ndigit_to_create <- 0;
	
	//Nombre de véhicules créés pour la période
	float nbCar_created <- 0;
	
	reflex updateData when:  (time mod capturePeriod ) = 0 
	{
		int idex <-  int(time / (capturePeriod )) ;
		int carToCreate <- carCounts at idex ;
		nbCar_created <- 0;
		
		if(isdigitOriented)
		{
			nbCar_digit <- carToCreate ;
			nbCar_ndigit <- 0;
			associatedRoad.containCarCounter_digit <- true;
		}
		else
		{
			nbCar_ndigit <- carToCreate ;
			nbCar_digit <- 0;
			associatedRoad.containCarCounter_ndigit <- true;
		}
	} //updateData
	
	reflex addcarToCreate
	{
		nbCar_digit_to_create <- nbCar_digit_to_create + nbCar_digit / nbCycleInPeriod;
		nbCar_ndigit_to_create <- nbCar_ndigit_to_create + nbCar_ndigit / nbCycleInPeriod;
		
		nbCar_digit_to_create <- nbCar_digit < nbCar_created + nbCar_digit_to_create ?  0 : nbCar_digit_to_create;
		nbCar_ndigit_to_create <- nbCar_ndigit < nbCar_created + nbCar_ndigit_to_create ?  0 : nbCar_ndigit_to_create;

	} // Génération des véhicules toutes les 15 minutes
	
	reflex createcarDigit when: nbCar_digit_to_create >= 1 // and false
	{
		nbCar_digit_to_create <-  createcars(associatedRoad.tcrossroad,associatedRoad.fcrossroad,nbCar_digit_to_create);
	} //Sens de compatge et de digitalisation des véhicules sur le réseau routier
	
	reflex createcarnDigit when: nbCar_ndigit_to_create >= 1// and false
	{
		nbCar_ndigit_to_create <- createcars(associatedRoad.fcrossroad,associatedRoad.tcrossroad,nbCar_ndigit_to_create);
	} //#Sens inverse
	
	float createcars(crossroad destination,crossroad ghostDestination, float nbCarToCreate)
	{
		loop while: nbCarToCreate >= 1
		{
			int is_gasoline <- flip(energy)?0:1;
			int tmp_norm <- flip(vehicle_2020_norm_rate)?2020:2007;
			
//			int type_of_vehicule <- flip(percent_of_truck)?2:(flip(percent_of_car)?1:0);
			int type_of_vehicule <- (flip(percent_of_car)?(flip(percent_of_truck)?2:1):0);
			
			write "type of vehi" + type_of_vehicule;
				
		 	if(carBehaviorChoice = "hierarchy")
		 	{
		 		create carHierarchyChange number:1
				{
					location <- myself.location;
					myDestination <- destination;
					isGhost <- false;
					mycolor <- rgb('blue');
					currentRoad <- myself.associatedRoad;
					deathDay <- time + gauss({gdeathDay,ecart}); // fonction Gaussienne de disparition
					mspeed <- myself.associatedRoad.mspeed;
					currentRoad <- location;	
					my_energy <- is_gasoline;
					my_type_of_vehicle <- type_of_vehicule;
					my_vehicle_year <- tmp_norm;
				}
				create carHierarchyChange number:1
				{
					location <- myself.location;
					myDestination <- ghostDestination;
					isGhost <- true;
					mycolor <- rgb('red');
					currentRoad <- myself.associatedRoad;
					deathDay <- time + gauss({gdeathDay,ecart});
					mspeed <- myself.associatedRoad.mspeed;
					currentRoad <- location;			
					my_energy <- is_gasoline;
					my_type_of_vehicle <- type_of_vehicule;
					my_vehicle_year <- tmp_norm;
				}
		 	}//CarHierarchy
		 	else 
		 	{
			 		create carRandomChange number:1
					{
						location <- myself.location;
						myDestination <- destination;
						isGhost <- false;
						mycolor <- rgb('blue');
						deathDay <- time + gauss({gdeathDay,ecart});
						currentRoad <- myself.associatedRoad;				
						mspeed <- myself.associatedRoad.mspeed;
						my_energy <- is_gasoline;
						my_type_of_vehicle <- type_of_vehicule;
						my_vehicle_year <- tmp_norm;
					}
					create carRandomChange number:1
					{
						location <- myself.location;
						myDestination <- ghostDestination;
						isGhost <- true;
						mycolor <- rgb('red');
						deathDay <- time + gauss({gdeathDay,ecart});
						currentRoad <- myself.associatedRoad;
						mspeed <- myself.associatedRoad.mspeed;					
						my_energy <- is_gasoline;
						my_type_of_vehicle <- type_of_vehicule;
						my_vehicle_year <- tmp_norm;
					}
		 	}
			nbCar_created <- nbCar_created + 1;
			nbCarToCreate <- nbCarToCreate - 1;
			associatedRoad.traffic <- associatedRoad.traffic + 1;
		}
		return nbCarToCreate;
	}//createCar
	
	aspect base
	{
		draw circle(6) color:#black;
	}
}//carCounter

species carRandomChange parent:car  
{
	action changeDestination 
	{
		list<road> selectedRoads <- self.getNextRoadList();
		
		if(empty(selectedRoads))
		{
			do die;
		}

		road selectedOneRoad <- one_of(selectedRoads);
		myDestination <- selectNextcrossroad(selectedOneRoad,self.location);
		selectedOneRoad.traffic <- selectedOneRoad.traffic + 1;
		currentRoad <- selectedOneRoad;
	}//changeDestination
	
	aspect base
	{
		if( ! isGhost )
		{
			draw circle(5) color:colorCar();
		}
	}
}

species carHierarchyChange parent:car  
{
	
	action changeDestination 
	{
		list<list<road>> selectedRoads <- self.getClassifiedNextRoadHList();	
		if(empty(self.getNextRoadList()))
		{
			do die;
		}	

		road selectedOneRoad <- nil;
		
		
		loop while:selectedOneRoad = nil
		{
			selectedOneRoad <- chooseARoad(selectedRoads);
		}
		//	selectedOneRoad <- chooseARoad(OSM1,OSM2,OSM3,OSM4,OSM5,OSM6,OSM7,OSM8,OSM9,OSM10,OSM11);
		//write "selected road "+ selectedOneRoad;
		myDestination <- selectNextcrossroad(selectedOneRoad,self.location);
		selectedOneRoad.traffic <- selectedOneRoad.traffic + 1;
		currentRoad <- selectedOneRoad;
	}//changeDestination
	
	road chooseARoad(list<list<road>> OSMs)
	  {
		road res <- nil;
		list<float> hierarchyChoice <- [] ;
		float myplaceh <- self.currentRoad.hierarchy;
		hierarchyChoice <- HierarchyMatrix row_at (int(myplaceh) - 1);
		float myrnd <- rnd(1000)/1000;
		int choice_id<- -1;
		float rndCumulatorH<-0;
		int tempidh <- 0;
		float cumulatorNormH <- 0.0;
		
		loop while:tempidh < length(OSMs) 
		{
			if( (OSMs at tempidh) != nil )
			{
				cumulatorNormH <- cumulatorNormH + (hierarchyChoice at tempidh);
			}
			tempidh <- tempidh + 1;
		}

		loop while:choice_id < 0   or (myrnd > rndCumulatorH and choice_id < length(hierarchyChoice))
		{
			choice_id <- choice_id + 1;
			
			if(OSMs at choice_id !=nil)
			{
				rndCumulatorH <- rndCumulatorH + (hierarchyChoice at choice_id)/cumulatorNormH;	
			}
		}
		
		list<road> chosenOSM <- OSMs at choice_id;
		
		if(empty(chosenOSM))
		{
			return nil;
		}
		else
		{
			return one_of(chosenOSM);
		}
	}
	
	aspect base
	{
		if( ! isGhost  and show_trafic)
		{
			draw circle(5) color:colorCar(); //#green;
		}
	}
	aspect ghost
	{
		if( isGhost )
		{
			draw circle(5) color:#gray;
		}	
	}	
}

species car skills: [driving]
{
	int my_type_of_vehicle <- 1; // 0 moto, 1 VL et 2 PL
	crossroad myDestination;
	road previousRoad;
	road currentRoad;
	bool isGhost;
	rgb mycolor <- rgb('blue');
	int deathDay <- 999999999;
	path road_path;
	float mspeed <- 0;
	float nextCrossRoadDate <- 0;
	action changeDestination ;
	float endStreetArrival;
	int my_energy <- 1;
	float my_vehicle_year;
	

	rgb colorCar
	{
		return my_energy=1?#red:#green;
	}
	
	float select_coeff
	{
		switch(my_type_of_vehicle)
		{
			match MOTORBYKE_ID{return MOTORBYKE_COEF; }
			match CAR_ID {return CAR_COEF; }
			match TRUCK_ID {return TRUCK_COEF; }
		}
	}
	
		
	reflex cloud
	{
		float spp <- int(mspeed*#h / #km /10) *10;
		
		list<float> mcopert <- ((world.copert at (""+int(my_vehicle_year)) ) at spp)[my_energy];
		float distance_done <- spp / step;
		
		
		
		
		list<float> cloud <- mcopert collect(each * distance_done *select_coeff() );
		ask currentRoad
		{
			list<float> mres <- [];
			list<float> mCell <- [];
			loop i from:0 to:length(pollutant) - 1// <<+ cloud;
			{
				mres <- mres +  [(pollutant[i] + cloud[i])];
			}
			
			pollutant <- mres;
		}

		pollutant_grid  cells <- pollutant_grid first_with(each overlaps self);
		
		if(cells != nil)
		{
			loop i from:0 to:length(cloud) - 1// <<+ cloud;
			{
				cells.pollutant[i] <- cells.pollutant[i] + cloud[i];
					
			}
			
			loop j over:cells.neighboor_buildings
			{
				 ask j 
				 {
				 	do add_pollutant(cloud);
				 }	
			}
			
		}
	}
	
	reflex killcar when: deathDay < time
	{
		do die;
	}
			
	reflex gotocrossroad when: myDestination != nil and myDestination.location != self.location 
	{
		//write "spped +" + mspeed+ " "+myDestination.location + " "+ location ;
		road_path <- self goto [on:: currentRoad ,target::myDestination.location, speed:: min([max_speed,mspeed]), return_path::true ] ;  //* !!!!stepDuration; speed:: 0.5 !!!VITESSE A 50km/h
		previousRoad <- currentRoad;
	}
	
	reflex changeDestination when:  myDestination != nil and myDestination.location = self.location
	{
		do changeDestination;
		endStreetArrival <- 0;
		road_path <- nil;
	}
	
	list<road> getNextRoadList
	{
		return currentRoad.tcrossroad = myDestination ? currentRoad.tnextRoad:currentRoad.fnextRoad;
	}

	list<list<road>> getClassifiedNextRoadSList
	{
		return currentRoad.tcrossroad = myDestination ? currentRoad.TSPEED:currentRoad.FSPEED;
	}	
	
	list<list<road>> getClassifiedNextRoadHList
	{
		return currentRoad.tcrossroad = myDestination ? currentRoad.TOSM:currentRoad.FOSM;
	}
	
   	crossroad selectNextcrossroad(road currentRoad, point currentLocation)
	{
		if(currentRoad.fcrossroad.location = currentLocation)
		{
			return currentRoad.tcrossroad;
		}
		else
		{
			return currentRoad.fcrossroad;
		}
	}//nextcrossroad
		
	aspect base
	{
		if( ! isGhost )
		{
			draw circle(10) color:#blue;
		}
	}
	aspect ghost
	{
		if( isGhost )
		{
			draw circle(10) color:#gray;
		}	
	}
}//speciescar



species building schedules: alived_building {
	list<float> pollutant <- list_with(6,0.0);
	list<list<float>> pollutant_history <-list_with(6,[]);
	
	string type;
	rgb color <- rgb(220,220,220);
	int height;
	
	action add_pollutant(list<float> cloud)
	{
		loop i from:0 to:length(cloud) - 1
		{
			pollutant[i] <- pollutant[i] + cloud[i];			
			list<float> cList <- pollutant_history[i];
			if(length(cList)>=10)
			{
				remove index:0 from:	cList;
								
			}
			pollutant_history[i] <- cList + cloud[i];
			
		}
	}	
	
	reflex diffuse_pollutant {
		loop i from:0 to:length(pollutant) - 1 {
				pollutant[i] <- pollutant[i] * (1 - diffusion_rate);	
			}
	}
	
	aspect base {
		
		if(show_pollution)
		{
			float rate <- pollutant[world.pollutentIndex("pm")]/maxNox_buildings;
			
			rgb mcol <- ((maxNox_buildings/2)>rate)?first(colorSet).BUILDING1 : (maxNox_buildings<rate?first(colorSet).BUILDING3:first(colorSet).BUILDING2);
			draw shape color: mcol depth: 0 at: location +{0,0,5};
			//draw shape color: mcol depth: 0 ;
		//	draw shape color: rgb(255,int((1-pollutant[world.pollutentIndex("nox")]/maxNox_buildings)*255),255) depth: height;
		}
		else
		{
			draw shape color:#white depth: 0;
		}
		
	}
}

species infoDisplay {

	point location <- {800,1700};
	
	float cx <- cos(ANGLE);
	float sx <- sin(ANGLE);
		
	point dimensions <- {2500,1500};
	float dx;
	float ymax <- 1400;
	int labelOffset <- 200;
	
	list<float> infoList <-[0.0]; 
	
	
	float info;
	
	
	
	point pos(point po)
	{
		float nx <- location.x + cx * po.x - sx * po.y;
		float ny <- location.y + sx * po.x + cx * po.y;
		return {nx,ny};
	}
	
	string as_time(int t)
	{
		int hh <- int(t / 3600);
		int mm <- int(t / 60) - 60 * hh;
		int ss <- int(t - 60 * mm - 3600 * hh);
		
		return (hh< 10? " ":"")+string(hh) + (mm< 10? "h  ":"h ") + string(mm) + (ss< 10? "mn  ":"mn ") +string(ss)+"s";
	}
	
	
	aspect base{
		
	 	//	write(info);
	
		// info <- mean (road collect each.traffic_density);
		if building != nil{
		info <- mean(list(building collect(mean(each.pollutant_history[world.pollutentIndex("co")]))))/stepDuration*3600;
		
		add info to: infoList;
		if info> ymax   
			{
				ymax <- info;
			}
			
		}
	
	//	remove index:0 from: infoList; 
	
		float maxInfoList <- max([0.1,max(infoList)]);
		int digits <- cycle = 0 ? 0: int(log(cycle));
		
		if length(infoList) > 1
		{
		dx <- dimensions.x / (length(infoList)-1);
			loop i from:0 to: length(infoList)-2{
				draw polygon([pos({i*dx,-dimensions.y*infoList[i]/ymax}),pos({(i+1)*dx,-dimensions.y*infoList[i+1]/ymax}),pos({(i+1)*dx,0}),pos({i*dx,0})]) color: first(colorSet).CURVES; 
			}
		}

		draw 5#m around (line([pos({- 200, - dimensions.y*maxInfoList/ymax}),pos({dimensions.x, - dimensions.y*maxInfoList/ymax})])) color: first(colorSet).TEXT2;
		draw(string(int(maxInfoList))+" g per hour") font: font(20) color: first(colorSet).TEXT2 at: pos({ 20, - dimensions.y * maxInfoList/ymax - 25}) rotate: ANGLE;
		draw 5#m around line([pos({- 200,5}),pos({dimensions.x,5})]) color: first(colorSet).TEXT2;
		draw("0") color: first(colorSet).TEXT2 font: font(20) at: pos({20, 190}) rotate: ANGLE;
		draw 5#m around line([pos({0, 200}),pos({0, - dimensions.y * maxInfoList/ymax - 200})]) color: first(colorSet).TEXT2;
		draw("NOx") font: font(20) at: pos({- 50, - dimensions.y * maxInfoList /ymax /2 + labelOffset}) color: first(colorSet).TEXT2 rotate: -90+ANGLE;
	//	draw("Time "+string(cycle)) font: font(20) at:  pos({dimensions.x - 650 - digits*80, 190}) color: °white rotate: ANGLE; 
		draw(as_time(cycle*5)) font: font(20) at:  pos({dimensions.x - 900, 190}) color: first(colorSet).TEXT2 rotate: ANGLE; 
	
	}
	
}// fin infoDisplay



species legend schedules:[]
{

	point size <- {500,200};
	point location <- {8000,6500,1};
	point offset <- {size.x * cos(ANGLE), size.x * sin(ANGLE)};
	point textOffset <- {-125,40,2};
	point tmp <-{- 0.25*size.x,2*size.y}; //{- 0.5*size.x,0.5*size.y};
	point labelOffset <- {-sin(ANGLE)*tmp.y + tmp.x * cos(ANGLE),cos(ANGLE)*tmp.y + tmp.x * sin(ANGLE),2} ;
		
	geometry rect <- polygon([{0,0},{size.x*cos(ANGLE),size.x*sin(ANGLE)},{size.x*cos(ANGLE)-size.y*sin(ANGLE),size.y*cos(ANGLE)+size.x*sin(ANGLE)},{-size.y*sin(ANGLE),size.y*cos(ANGLE)}]);
	

	aspect base{
//			file images <- file("../includes/6.png");
		
//		draw images at:{0,0,3} ;
			
		draw rect at: location color: °green;
		draw rect at: location+offset color: °orange;
		draw rect at: location+offset+offset color: °red;

		draw("LOW") at: location+textOffset color:first(colorSet).TEXT1 rotate: ANGLE;
		draw("MED") at: location+offset+textOffset color:first(colorSet).TEXT1 rotate: ANGLE;
		draw("HIGH") at: location+offset+offset+textOffset color: first(colorSet).TEXT1 rotate: ANGLE;
		draw("NOx level") at: location + labelOffset font: font(30) color:first(colorSet).TEXT1 rotate: ANGLE;
		
		
		/* de cote pour une issue sur le rotate 
			float angle <- 3.0;
			point size <- {500,200};
			point location <- {9000,6000};
			point offset <- {size.x * cos(angle), size.x * sin(angle)};
			aspect base{
			draw rectangle(size) at: location color:°green rotate: angle;
			draw rectangle(size) at: location+offset color:°orange rotate: angle;
			draw rectangle(size) at: location+offset+offset color:°red rotate: angle;
		
	*/	
		draw logos at: {1000,6500,3} size:{1678,186} ;
		
	}
}

// espece temporaire, juste pour modifier plus facilement les couleurs pour ce soir
species colorSet schedules:[]{	
//	rgb BACKGROUND <- rgb(10,10,10);
//	rgb TEXT1 <- #white;
//	rgb LIGHTS <- #white;
//	rgb TEXT2 <- #white;
//	rgb CURVES <- #blue;
//	rgb AIRPORT <-  rgb(30,30,30);
//	rgb WATER <- #blue;
//	rgb BUILDING1 <- #green;
//	rgb BUILDING2 <- #orange;
//	rgb BUILDING3 <- #red;
		
	rgb BACKGROUND <- rgb(250,250,250);
	rgb TEXT1 <- rgb(97,186,231);
	rgb LIGHTS <- rgb(235,99,25);
	rgb TEXT2 <-rgb(97,186,231);
	rgb CURVES <- rgb(97,186,231);
	rgb AIRPORT <-  rgb(200,200,200);
	rgb WATER <- rgb(97,186,231);
	rgb BUILDING1 <- rgb(17,99,13);
	rgb BUILDING2 <- rgb(203,105,25);
	rgb BUILDING3 <- rgb(117,20,17);
	
	
}



experiment affect type:gui
{
	parameter "TimeToDeath: " var:gdeathDay ; //Parametre à explorer GAUSS ???
	parameter "EcartGauss: " var:ecart ;
	parameter "NbSeed: " var:randomSeed ;
	parameter "CarBehavior (Random, Hierarchy, Speed) :" var:carBehaviorChoice; 
	parameter "ChangeCarParc (0: Essence / 1: Diesel ): " var:energy ;
	parameter "ChangeYearParc (2007 or 2020) :" var:vehicle_2020_norm_rate ;
	
	init
	{
		seed <- randomSeed;	
	}
	
	output {


		display Suivi_Vehicules_3D  type:opengl camera_pos:{5000,4000,8500}  rotate: ANGLE  background:(show_keystone = true?#white:first(colorSet).BACKGROUND) refresh_every:10 use_shader: true keystone: true//[{0.158,0.265},{0.917,0.26},{0.12,0.764},{0.947,0.782}]  


		{
			//grid parcArea;
			species bound aspect: base;
		//	species pollutant_grid aspect:nox_aspect ;
			species road aspect:car_lights;
			species dummy_road aspect:car_lights;
			species building aspect:base; // transparency:0.5;
			species landscape aspect:base;
			species carCounter aspect:base;
			//species crossroad aspect:base;
			species car  aspect:ghost;
			species carHierarchyChange  aspect:base;
			species carRandomChange aspect:base;
			species infoDisplay aspect: base;
			species legend aspect: base;
			
/* 	 		chart "Traffic jam" size: {0.5, 0.5} position: {0.05, 0.05} type: stack{
// 		chart "Traffic jam" size: {0.5, 0.5} position: {0.05, 0.05} title_font_size: 0 legend_font_size: 0 background: °black axes: °white style: line color: °white  tick_font_size: 1 y_range: {0,1}{

 				 data "Mean road traffic coefficient" value: mean (road collect each.traffic_density) style: line color: #blue ;

	

			}*/
		}
		
		/*display Suivi_Vehicules_2D   background:#black use_shader: true keystone: true //refresh_every:15 
		{
			//grid parcArea;
			species building aspect:base; // transparency:0.5;
		//	species pollutant_grid aspect:nox_aspect ;
			species road aspect:base;
			species carCounter aspect:base;
			//species crossroad aspect:base;
			species car  aspect:ghost;
			species carHierarchyChange  aspect:base;
			species carRandomChange aspect:base;
			
		}*/

/*		display Emissions_Totales draw_diffuse_light:true scale:true
		{
				species road aspect:base;
				species pollutant_grid aspect:nox_aspect transparency:0.5;
			
		}
		
		display Suivi_Emissions refresh_every: 5 
		{

			chart name: 'Evolution maximum des émissions de NOx_Moyenne' type: series background: rgb('white') size: {0.5,0.5} position: {0, 0} 
			{
			data 'Emissions cumulées de NOx (g)' value:max(list(pollutant_grid collect(each.pollutant[world.pollutentIndex("nox")])));
			}
			
			chart name: 'Evolution moyenne des émissions de particule' type: series background: rgb('white') size: {0.5,0.5} position: {0, 0.5} 
			{
			data 'Emissions cumulées de particules (g)' value:max(list(pollutant_grid collect(each.pollutant[world.pollutentIndex("pm")])));
			}
			
			chart name: 'Quantité de NOx émis par grille' type: series background: rgb('white')  size: {0.5,0.5} position: {0.5, 0} 
			{
			data 'Emissions Moyenne de NOx (g)' value:list(pollutant_grid collect(each.pollutant[world.pollutentIndex("nox")])) marker:false;
			}
			
			chart name: 'Quantité de PM émis par grille' type: series background: rgb('white')  size: {0.5,0.5} position: {0.5, 0.5} 
			{
			data 'Emissions Moyenne de PM (g)' value:list(pollutant_grid collect(each.pollutant[world.pollutentIndex("pm")])) marker:false;
			}
		}		
		
	
		display Frequentation_Moyenne type:opengl
		{
			species road aspect:base3D;
		}*/
	/*
		display TrafficDensity type:opengl
		{
	 		species road aspect:base3D_capacity;
	 	}

	 	display Suivi_Global refresh_every: 15
		{
			chart name: 'Evolution moyenne de la fréquentation globale' type: series background: rgb('white') size: {1,0.5} position: {0, 0} 
			{
				data 'Debit Moyen' value: mean(list(road collect(each.lastTraffic))) color: rgb('blue') marker:false;
			}
			chart name: 'Nombre Total de véhicules par axe' type: series background: rgb('white') size: {0.5,0.5} position: {0, 0.5} 
			{
				data 'Axes routiers' value:road collect (each.sumTraffic) color: rgb('black') marker:false; //xlabel:'axesroutiers' ???
			}
			chart name: 'Courbe Debit/Capacité' type: series background: rgb('white') size: {0.5,0.5} position: {0.5, 0.5}// time_series:mean(road collect (each.lastTraffic)) x_tick_unit:0.1 x_range:{0,1} //mean(road collect (each.capacity/4)) x_tick_unit:0.1 x_range:{0,1} 
			{
				data 'Debit 1/4h' value:mean(road collect(each.lastTraffic)) color: rgb('red') marker:true line_visible:false ;
			}
		}
		
		display Suivi_Axe refresh_every: 15
		{
			chart 'Courbe Debit/Capacité' type:series x_tick_unit:0.1 x_range:{0,1} size: {1,0.5} position: {0, 0} //time_series:(roadToDisplay.lastTraffic)///(roadToDisplay.capacity/4))
			{
				data 'Debit 1/4h' value:roadToDisplay.lastTraffic marker:true line_visible:false;
			}
			chart name: 'Evolution du Debit/capacité' type: series background: rgb('white') size: {1,0.5} position: {0, 0.5} 
			{
				data 'Debit' value: roadToDisplay.lastTraffic color: rgb('blue') marker:false;
			}
		} 
 */
	} //output
} // experiment