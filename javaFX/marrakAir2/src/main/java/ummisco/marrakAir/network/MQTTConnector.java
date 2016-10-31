package ummisco.marrakAir.network;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import ummisco.marrakAir.common.FollowedVariable;



public final class MQTTConnector {
	public String SERVER_URL = "SERVER_URL";
	public String SERVER_PORT = "SERVER_PORT";
	public String LOCAL_NAME = "LOCAL_NAME";
	public String LOGIN = "LOGIN";
	public String PASSWORD = "PASSWORD";

	public static String DEFAULT_USER = "admin";
	public static String DEFAULT_LOCAL_NAME = "gama-ui"+Calendar.getInstance().getTimeInMillis()+"@";
	public static String DEFAULT_PASSWORD = "password";
	public static String DEFAULT_HOST =  "localhost";
	public static String DEFAULT_PORT =  "1883";
	
	protected MqttClient sendConnection = null;
	Map<String, ArrayList<FollowedVariable>> receivedData ;
	
	public MQTTConnector(String server, String port,  String userName, String password) throws MqttException
	{	
		this.SERVER_URL  = (server==null?DEFAULT_HOST:server);
		this.SERVER_PORT = (port==null?DEFAULT_PORT:port);
		this.LOGIN = (userName==null?DEFAULT_USER:userName);
		this.PASSWORD =(password==null?DEFAULT_PASSWORD:userName);
		this.connectToServer();
		receivedData = new HashMap<String, ArrayList<FollowedVariable>>();
	}
	
	class Callback implements MqttCallback
	{
		@Override
		public void connectionLost(Throwable arg0)  {
			//throw new MqttException(arg0);
			System.out.println("connection lost");
		}
		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
			System.out.println("message sended");
		}
		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			String body = message.toString();
			storeData(topic,body);
		}
	}
	
/*	public List<Map<String,Object>>  getLastData(String topic)
	{
		//Object  data = storeDataS(topic,null);
		FollowedVariable tmp=this.receivedData.get(topic);
		if(tmp==null)
			return null;
		return tmp.popLastData();
	}
*/	
	
	private final void storeData(String topic, String message)
	{
		//AbstractDriver
		System.out.println("message received "+ topic);
		XStream dataStreamer = new XStream(new DomDriver() );// DomDriver());
		@SuppressWarnings("unchecked")
		Map<String, Object> data = (Map<String, Object>)dataStreamer.fromXML(message);
		ArrayList<FollowedVariable> dts=this.receivedData.get(topic);
		System.out.println("message received "+ topic+" "+data);
		
		if(dts==null)
			{
				//dts = new FollowedVariable(topic);
				//this.receivedData.put(topic,dts);
				return;
			}
		for(FollowedVariable dt:dts)
			dt.pushNewData(data);
	}

	public final void releaseConnection() throws MqttException{
			sendConnection.disconnect();
			sendConnection = null;
	}

	public void registerVariable(FollowedVariable var)
	{
		ArrayList<FollowedVariable> observers = this.receivedData.get(var.getName());
		if(observers ==null)
		{
			observers = new ArrayList<FollowedVariable>();
			this.receivedData.put(var.getName(), observers);
		}
		observers.add(var);
		try {
			subscribeToGroup(var.getName());
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public final void sendMessage(String dest, Object data ) throws MqttException
	{
		XStream dataStreamer = new XStream(new DomDriver());
		String dataS = dataStreamer.toXML(data);
		this.sendFormatedMessage(dest, dataS);
	}
	
	private final void sendFormatedMessage( String receiver, String content) throws MqttException {
			MqttMessage mm = new MqttMessage(content.getBytes());
			sendConnection.publish(receiver, mm);
	}

	public void subscribeToGroup(String boxName)  throws MqttException {
			sendConnection.subscribe(boxName);
	}
	
	public void unsubscribeGroup(String boxName) throws MqttException   {
			sendConnection.unsubscribe(boxName);
		}

	protected void connectToServer() throws MqttException {
		if(sendConnection == null) {
			
			String localName = DEFAULT_LOCAL_NAME+this.SERVER_URL;
			sendConnection = new MqttClient("tcp://"+this.SERVER_URL+":"+this.SERVER_PORT, localName, new MemoryPersistence());
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			sendConnection.setCallback(new Callback());
		    connOpts.setCleanSession(true);
		    connOpts.setKeepAliveInterval(30);
		    connOpts.setUserName(this.LOGIN);
		    connOpts.setPassword(this.PASSWORD.toCharArray());
		  	sendConnection.connect(connOpts);
		}
	}
	
	
	
	public boolean isConnected(){
		return this.sendConnection== null? true: false;
	}
}
