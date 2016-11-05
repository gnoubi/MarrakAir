package ummisco.marrakAir.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import ummisco.marrakAir.gui.widget.LineChartBox.InternalPlot;
import ummisco.marrakAir.network.MQTTConnector;
import ummisco.marrakAir.runtime.FollowedVariable;

public class PieChartBox extends PieChart implements Observer {
	class InternalPlot
	{
		String varName ;
		String varLegend ;
		float defaultValue;
		PieChart.Data data;
		
		public InternalPlot(String name, String legend, float dfl)
		{
			this.varName = name;
			this.defaultValue = dfl;
			this.varLegend = legend;
			data=new PieChart.Data(varLegend, dfl);
		}
		public String getName()
		{
			return this.varName;
		}
	
		public PieChart.Data getData()
		{
			return data;
		}
		public void setValue(float v)
		{
			this.data.setPieValue(v);
		}
	}
	
	private FollowedVariable  flVariable ;
	private ArrayList<InternalPlot>  drawedData ;
	
	public PieChartBox()
	{
		super();
		this.drawedData  =  new ArrayList<InternalPlot>();
		ObservableList<PieChart.Data> pieChartData =FXCollections.observableArrayList();
		this.setData(pieChartData);
	}
	
	public void setFollow(String lbl)
	{
		this.flVariable = new FollowedVariable(lbl);
		this.flVariable.addObserver(this);
	}
	
	public String getFollow()
	{
		return flVariable.getName();
	}

	
	public void registerConnection(MQTTConnector con)
	{
		System.out.println("register " + this.flVariable);
		con.registerVariable(this.flVariable);
		this.flVariable.addObserver(this);
	}
	
	public void setSpecifyDraw(String lbl)
	{
		String[] variables = lbl.split(",");
		for(String tmp : variables)
		{
			String[] dt = tmp.split("::");
			InternalPlot plt = new InternalPlot(dt[0],dt[1], Float.valueOf(dt[2]).floatValue());
			this.drawedData.add(plt);
			//this.createSymbolsProperty();
			     
		}
		initialiseDraw();

	}
	
	private void initialiseDraw()
	{
		for(InternalPlot plt:this.drawedData)
		{
			this.getData().add(plt.getData());
		}
		//this.createSymbolsProperty();
	}

	public String getSpecifyDraw()
	{
		return "";
	}
	
	private void updateData(Map<String,Object> data)
	{
		for(InternalPlot dt:drawedData)
		{
			
			System.out.println("update "+ dt.getName() + " " +data.get(dt.getName()));
			Platform.runLater(() ->dt.setValue((float)((Number)data.get(dt.getName())).floatValue()));
		}
	}

	
	@Override
	public void update(Observable o, Object arg) {
		if(o instanceof FollowedVariable)
		{
			
			FollowedVariable f = (FollowedVariable)o;
			List<Map<String,Object>> datas = f.popLastData();
			System.out.println("pie update "+ datas);
			for(Map<String,Object> dts:datas)
			{
				updateData(dts);
			}
		}
	}


}
