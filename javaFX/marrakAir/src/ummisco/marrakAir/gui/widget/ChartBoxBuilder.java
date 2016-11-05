package ummisco.marrakAir.gui.widget;

import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart.Series;

public class ChartBoxBuilder {
	    private Axis<Number> xAxis ;
	    private Axis<Number> yAxis ;
	    private Timeline animation ;
	    private String toFollow;
	    private String toPlots;
		private ObservableList<Series<Number,Number>> data ;

	    public static ChartBoxBuilder create() {
	        return new ChartBoxBuilder();
	    }

	    public ChartBoxBuilder XAxis(Axis<Number> xAxis) {
	        this.xAxis = xAxis ;
	        return this ;
	    }

	    public ChartBoxBuilder YAxis(Axis<Number> yAxis) {
	        this.yAxis = yAxis ;
	        return this ;
	    }

	    public ChartBoxBuilder follow(String varToFollow) {
	    	this.toFollow = varToFollow;
	    	return this ;
	    }
	    public ChartBoxBuilder plots(String varToPlot) {
	    	this.toPlots = varToPlot;
	    	return this ;
	    }
	    
	    
	    public ChartBoxBuilder animation(Timeline animation) {
	        this.animation = animation ;
	        return this ;
	    }

	    public ChartBoxBuilder data(Series<Number, Number> sdata) {
	    	this.data= FXCollections.observableArrayList();
	    	this.data.add(sdata);
	        //this.data = new SorttedList<Series<Number, Number>>(sdata);
	        		
	        		//new ElementObservableListBase<Series<Number,Number>>() ; //sdata.. ;
	        return this ;
	    }

	    public LineChartBox build() {
	    	LineChartBox tmp;
	        // if else may not be necessary, depending on how you define constructors in LiveLineChart
	        if (data == null) {
	        	tmp= new LineChartBox( xAxis, yAxis);
	        } else {
	        	tmp= new LineChartBox( xAxis, yAxis, data);
	        }
	        if(toFollow !=null)
	        	tmp.setFollow(toFollow);
	        if(toPlots !=null)
	        	tmp.setPlots(toPlots);
	        return tmp;
	    }
	}