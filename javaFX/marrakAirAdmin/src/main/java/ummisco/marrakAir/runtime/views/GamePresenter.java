package ummisco.marrakAir.runtime.views;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import ummisco.marrakAir.gui.widgets.LineChartBox;
import ummisco.marrakAir.gui.widgets.PieChartBox;
import ummisco.marrakAir.gui.widgets.ValueChangedEvent;
import ummisco.marrakAir.network.MQTTConnector;
import ummisco.marrakAir.runtime.GluonApplication;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class GamePresenter {

	private static MQTTConnector connection = null;
	private static GamePresenter scope;

	@FXML
	private View game;

	@FXML
	Button showTrafficB;
	boolean traficShow =true;

	@FXML 
	Button showPollutantB;
	boolean pollutantShow =true;

	@FXML
	Button resetSim;

	@FXML
	Button toggleKeystoneB;
	boolean toggleKeystone = true;
	
	


	private void change(ObservableValue obs, Object oldValue, Object newValue)
	{
		if((Boolean)newValue) {
			AppBar appBar = MobileApplication.getInstance().getAppBar();
			EventHandler<ActionEvent> evt = new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					MobileApplication.getInstance().showLayer(GluonApplication.MENU_LAYER);

				}
			};

			appBar.setNavIcon(MaterialDesignIcon.MENU.button(evt));
			//            appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> 
			//                       MobileApplication.getInstance().showLayer(GluonApplication.MENU_LAYER)));
			appBar.setTitleText("City controller");

			EventHandler<ActionEvent> evt2 = new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					System.out.println("Search");
				}
			};


			//     appBar.getActionItems().add(MaterialDesignIcon.SEARCH.button(evt2));
		}
	}


	@FXML
	private void valueChanged(ValueChangedEvent evt)
	{
		System.out.println("Changement de valeur "+ evt.getAgentName()+" attribut:"+evt.getAgentAttributeName()+" valeur:"+evt.getValue());
		if(this.connection!=null)
		{
			try {
				this.connection.sendMessage(evt.getAgentAttributeName(), evt.getValue());
				System.out.println("message sended");
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

	@FXML
	void buttonTrafficPressed(ActionEvent event)
	{
		if(this.connection!=null)
		{
			try {
				this.connection.sendMessage("show_trafic", traficShow?0:1);
				traficShow = !	traficShow ;
				System.out.println("message sended");
				showTrafficB.setDisable(true);
				new Thread(){
					@Override
					public void run(){
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Platform.runLater(()->{
							showTrafficB.setDisable(false);
						});
					}
				}.start();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(traficShow)
			{
				showTrafficB.setText("Masquer le trafic"); 
			}
			else
			{
				showTrafficB.setText("Afficher le trafic"); 
			}
		}
	}

	@FXML
	void buttonPollutionPressed(ActionEvent event)
	{
		if(this.connection!=null)
		{

			try {
				this.connection.sendMessage("show_pollution", pollutantShow?0:1);
				pollutantShow = !	pollutantShow ;
				System.out.println("message sended");
				showPollutantB.setDisable(true);
				new Thread(){
					@Override
					public void run(){
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Platform.runLater(()->{
							showPollutantB.setDisable(false);
						});
					}
				}.start();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(pollutantShow)
			{
				showPollutantB.setText("Masquer la pollution"); 
			}
			else
			{
				showPollutantB.setText("Afficher la pollution"); 
			}
		}

	}

	@FXML
	void toggleKeystoneEvent(ActionEvent event){
		if(this.connection!=null){
			try {
				this.connection.sendMessage("show_keystone", toggleKeystone?0:1);
				toggleKeystone = ! toggleKeystone;
				System.out.println("message sended");
				toggleKeystoneB.setDisable(true);
				new Thread(){
					@Override
					public void run(){
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Platform.runLater(()->{
							toggleKeystoneB.setDisable(false);
						});
					}
				}.start();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(toggleKeystone){
				toggleKeystoneB.setText("Masquer le keystone");
			}else{
				toggleKeystoneB.setText("Afficher le keystone");
			}
		}
	}

	
	@FXML
	void buttonResetPressed(ActionEvent event){
		if(this.connection!=null)
		{

			try {
				this.connection.sendMessage("reset", 1);
				System.out.println("message sended");
				resetSim.setDisable(true);
				new Thread(){
					@Override
					public void run(){
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Platform.runLater(()->{
							resetSim.setDisable(false);
						});
					}
				}.start();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	public static void initConnection()
	{
		scope.initializeConnection();
	}

	public void initializeConnection()
	{
		if(connection!=null&&!connection.isConnected()){
			System.out.println("doing something");
		}
	}

	@FXML
	public void initialize() {

		GamePresenter.scope = this;
		initializeConnection();


		if(connection!=null&&connection.isConnected()){
			System.out.println("doing something");

		}


		ChangeListener<? super Boolean> et = new ChangeListener() {

			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue) {
				change(observable,oldValue,newValue);

			}
		};

		game.showingProperty().addListener(et);

		//	game.showingProperty().addListener();
	}
	public static void setConnection(MQTTConnector b)
	{
		connection=b;
	}

	protected static MQTTConnector getConnection(){
		return connection;
	}
}
