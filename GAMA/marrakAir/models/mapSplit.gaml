/**
* Name: mapSplit
* Author: nicolas
* Description: Describe here the model and its experiments
* Tags: Tag1, Tag2, TagN
*/

model mapSplit

global {
	string mynetwork <- "../includes/SIG_demonstrateur/roads_gama.shp";
	file mshape_file <- shape_file(mynetwork);
	geometry shape <- envelope(mshape_file);
	init
	{
		create road from:mshape_file;
		ask road
		{
			do buildPolyline(20#m, 2#m);
			write(self.shape);
		}
		write length(road accumulate(each.my_geoms));
		create cell from:road accumulate(each.my_geoms);
		save cell type:'shp' to:"/Users/nicolas/git/agrignard/MarrakAirProject/GAMA/marrakAir/includes/SIG_demonstrateur/road_cells.shp";
	}
	
}

species cell
{
	
}

species road
{
	list<geometry> my_geoms <- [];
	action buildPolyline(float lgt, float buffer_width)
	{
		
		write "start length "+name+" "+ lgt;
		
		list<point> mp <-shape.points;
		int mid <- 0;
		float delta <- 0;
		point x1 <- mp[mid];
		point x2 <- mp[mid+1];
		point old_point <- x1;
		point new_point <- nil;
		geometry latest_buffer <- nil;
		loop while:length(mp) - 1 >mid
		{
				
			point vec <- {x2.x-x1.x,x2.y-x1.y};
			float vecDistance <- 	sqrt(vec.x*vec.x+vec.y*vec.y);
			point normalizedVect <- {vec.x/vecDistance,vec.y/vecDistance};
			point vec1 <-{-1*normalizedVect.y*buffer_width,normalizedVect.x*buffer_width};
			point vec2 <-{normalizedVect.y*buffer_width,-1*normalizedVect.x*buffer_width};
			float cumulatedDistance <- 0;
			point a <- old_point + vec1;
			point b <- old_point + vec2;
			point c <- x2 + vec2;
			point d <- x2 + vec1;
			point g <- nil;
			point h <- nil;
			
			write "distance "+name+" "+ vecDistance;
			if(vecDistance > lgt )
			{
				loop while: (cumulatedDistance<(vecDistance - lgt))
				{
					new_point <- {normalizedVect.x * lgt+old_point.x , normalizedVect.y * lgt+old_point.y};
					a <- old_point + vec1;
					b <- old_point + vec2;
					d <- new_point + vec1;
					c <- new_point + vec2;
					geometry g1 <- polygon([a,b,c,d,a]); //1#m around a; //circle(new_point,10#m);//polygon([a,b,c,d,a]);
					my_geoms <- my_geoms + g1; //+g3+g4;
					old_point <- new_point;
					cumulatedDistance <- cumulatedDistance + lgt;
					write "section distance "+name+" "+ cumulatedDistance+ "  "+ vecDistance;
				
				}
				a <- d;
				b <- c;
				delta <- vecDistance - cumulatedDistance;
			}
				
			mid <- mid + 1;	
			write "a "+a +" b "+b+" c " + c+" d "+d;
			if(mid < length(mp) -1 )
			{
				 //1#m around a; //circle(new_point,10#m);//polygon([a,b,c,d,a]);
				x1 <- x2;
				x2 <- mp[mid+1];
				point vec <- {x2.x-x1.x,x2.y-x1.y};
				float vecDistance <- 	sqrt(vec.x*vec.x+vec.y*vec.y);
				point normalizedVect <- {vec.x/vecDistance,vec.y/vecDistance};
				point vec1 <-{-1*normalizedVect.y*buffer_width,normalizedVect.x*buffer_width};
				point vec2 <-{normalizedVect.y*buffer_width,-1*normalizedVect.x*buffer_width};
				new_point <- {normalizedVect.x * delta+x1.x , normalizedVect.y * delta+x1.y};
				point e <- x1 + vec1;
				point f <- x1 + vec2;
				g <- new_point + vec1;
				h <- new_point + vec2;
				
				latest_buffer <- polygon([a,b,f,h,g,e,a]);
				x1 <- new_point;
				old_point <- x1;
				my_geoms <- my_geoms + latest_buffer; //+g3+g4;
				//break;
				//latest_buffer <- union(latest_buffer, polygon([b,e,f,c,b]));
				
			}
			else
			{
		/*		if(a = nil or b = nil or c = nil or d = nil)
				{
					if(new_point = nil)
					{
						vec <- {x2.x-x1.x,x2.y-x1.y};
						vecDistance <- 	sqrt(vec.x*vec.x+vec.y*vec.y);
						normalizedVect <- {vec.x/vecDistance,vec.y/vecDistance};
						vec1 <-{-1*normalizedVect.y*buffer_width,normalizedVect.x*buffer_width};
						vec2 <-{normalizedVect.y*buffer_width,-1*normalizedVect.x*buffer_width};
				
						new_point <- x1;
					}
					a <- new_point +vec1;
					b <- new_point + vec2;
					c <- x2 + vec2;
					d <- x2 + vec1;
					
				}*/
				
				
				c <- x2 + vec2;
				d <- x2 + vec1;
				
				write " "+a+" "+b+" "+x2 + " "+ my_geoms;
				
				my_geoms <- my_geoms + polygon([a,b,c,d,a]);  
			}

		}
		
		return nil;
	}

	aspect base
	{
		int i <- 0;
		loop while:i<length(my_geoms)
		{
			draw my_geoms[i] border:#black color:#red; //transparency:0.5;
			i <- i + 1;
		}
		draw shape color:#black;
		 
	}
}

experiment mapSplit type: gui {
	/** Insert here the definition of the input and output of the model */
	output {
		display mgraph
		{
			species road aspect:base;
		}
	}
}
