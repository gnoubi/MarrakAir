package ummisco.marrakAir.runtime;

import java.net.URL;
import java.util.ResourceBundle;

import org.eclipse.paho.client.mqttv3.MqttException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import ummisco.marrakAir.gui.widgets.LineChartBox;
import ummisco.marrakAir.gui.widgets.PieChartBox;
import ummisco.marrakAir.gui.widgets.ValueChangedEvent;
import ummisco.marrakAir.network.MQTTConnector;

public class GuiController implements Initializable {
	private static MQTTConnector connection = null;
	private static VBox rootLayout;

	@FXML private LineChartBox<Double,Double>	graphData;
	@FXML private PieChartBox	pieGraphData;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initialize()	;
	}

	@FXML
	protected void initialize()	
	{
		if(connection.isConnected()){
			System.out.println("wh");
			this.graphData.registerConnection(connection);
			this.pieGraphData.registerConnection(connection);
		}
	}

	@FXML
	private void valueChanged(ValueChangedEvent evt)
	{
		System.out.println("Changement de valeur "+ evt.getAgentName()+" attribut:"+evt.getAgentAttributeName()+" valeur:"+evt.getValue());
		try {
			this.connection.sendMessage(evt.getAgentAttributeName(), evt.getValue());
			System.out.println("message sended");
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	@FXML
	private void sliderValueChanged(MouseEvent evt)
	{
		System.out.println("open Mixer, value changed");
	}
	public static void setRootPane(VBox b)
	{
		rootLayout = b;
	}
	public static void setConnection(MQTTConnector b)
	{
		connection=b;
		//graphData.registerConnection(connection);
	}

}
