package ummisco.marrakAir.gui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import ummisco.marrakAir.network.MQTTConnector;
import ummisco.marrakAir.common.FollowedVariable;

public class LineChartBox<X,Y> extends LineChart<X,Y> implements Observer {
	class InternalPlot<X,Y>
	{
		X absciss;
		Y ordinate;
		LineChart.Series<X, Y> datas;
		
		public InternalPlot(X a, Y b, String leg)
		{
			this.absciss = a;
			this.ordinate = b;
			datas=new LineChart.Series<X, Y>();
			datas.setName(leg);
			
			
		}
		public X getAbsciss()
		{
			return this.absciss;
		}
		public Y getOrdinate()
		{
			return this.ordinate;
		}
		public synchronized LineChart.Series<X, Y> getSeries()
		{
			return this.datas;
		}
		public synchronized ObservableList<Data<X, Y>> getData()
		{
			return datas.getData();
		}
	}
	
	FollowedVariable flVariable;
	List<InternalPlot<X, Y>> myPlots;
	boolean start;
	ObservableList<Series<X,Y>> myData;
/*	public LineChartBox()
	{
		super((Axis<X>)new NumberAxis(), (Axis<Y>)new NumberAxis());
		myPlots = new ArrayList<InternalPlot<X, Y>>();
		this.setAnimated(true);
		myData = FXCollections.observableArrayList();
		this.setData(myData);
	}*/
	
	@SuppressWarnings("unchecked")
	public LineChartBox( Axis<X> xAxis,  Axis<Y> yAxis) {
		super(xAxis, yAxis);
		myPlots = new ArrayList<InternalPlot<X, Y>>();
		this.setAnimated(true);
		this.setCreateSymbols(false);
		myData = FXCollections.observableArrayList();
		this.setData(myData);
		start = true;
	}
	
	public LineChartBox( Axis<X> xAxis,  Axis<Y> yAxis,ObservableList<Series<X,Y>> data) {
		super(xAxis, yAxis, data);
		myPlots = new ArrayList<InternalPlot<X, Y>>();
		this.setAnimated(true);
		myData = FXCollections.observableArrayList();
		this.setData(myData);
		start = true;

		}
	
	
	
	public void setFollow(String lbl)
	{
		this.flVariable = new FollowedVariable(lbl);
		this.flVariable.addObserver(this);
	}
	
	public void registerConnection(MQTTConnector con)
	{
		System.out.println("register " + this.flVariable);
		con.registerVariable(this.flVariable);
		this.flVariable.addObserver(this);
	}
	

	public String getFollow()
	{
		return flVariable.getName();
	}
	
	public String getPlots()
	{
		String res="";
		boolean start=true;
		for(InternalPlot<X,Y> k:myPlots)
		{
			res=res+((!start)&&myPlots.size()>1?",":"")+k.getAbsciss()+"::"+k.getOrdinate();
		}
		return flVariable.getName();
	}
	
	public void setPlots(String plots)
	{
		String[] variables = plots.split(",");
		for(String tmp : variables)
		{
			String[] dt = tmp.split("::");
			InternalPlot<X,Y> plt = new InternalPlot(dt[0], dt[1],dt[2]);
			System.out.println("register "+ tmp);
			myPlots.add(plt);
			myData.add(plt.getSeries());
			this.createSymbolsProperty();
			     
		}
	}

	private  void updateData(final Map<String,Object> data)
	{
		for(final InternalPlot<X, Y> dt:myPlots)
		{
			Runnable rn = new Runnable() {
				@Override
				public void run() {
						dt.getData().add(new XYChart.Data<X, Y>((X)data.get(dt.getAbsciss()), (Y)data.get(dt.getOrdinate())));
				}

				
			};
			Platform.runLater(rn);

		
		}
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if(o instanceof FollowedVariable)
		{
			FollowedVariable f = (FollowedVariable)o;
			List<Map<String,Object>> datas = f.popLastData();
			for(Map<String,Object> dts:datas)
			{
				System.out.println("how"+dts);
				updateData(dts);
				
			}
		}
	}
	
	


}
