package ummisco.marrakAir.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

public class FollowedVariable extends Observable {
	private ArrayList<Map<String,Object>> data  = null;
	private String name = null;
	private int lastIndex = 0;
	
	public FollowedVariable(String nm)
	{
		this.data = new ArrayList<Map<String,Object>>();
		this.name = nm;
	}
	
	public synchronized  void pushNewData(Map<String,Object> nd)
	{
		newData(nd);
	}
	
	public   List<Map<String,Object>> popAllData()
	{
		this.lastIndex = data.size();
		return this.newData(null);
	}
	
	public  List<Map<String,Object>> popLastData()
	{
		int tmp = lastIndex;
		this.lastIndex = data.size();
		System.out.println("taill "+ tmp+ " "+this.lastIndex);
		return this.data.subList(tmp, this.lastIndex);
	}

	
	public String getName()
	{
		return name;
	}
	
	public synchronized ArrayList<Map<String,Object>> newData(Map<String,Object> nd)
	{
		System.out.println("new Data XXX");
		if(nd== null )
		{
			ArrayList<Map<String,Object>> tmp = this.data;
			this.data = new ArrayList<Map<String,Object>>();
			this.lastIndex = 0;
			return tmp;
		}
		System.out.println("new Data "+ nd);
		
		this.data.add(nd);
		this.setChanged();
		this.notifyObservers();
		return this.data;
	}
	
}
