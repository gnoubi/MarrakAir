package ummisco.marrakAir.runtime;

import ummisco.marrakAir.network.MQTTConnector;
import ummisco.marrakAir.runtime.views.GamePresenter;
import ummisco.marrakAir.runtime.views.GameView;
import ummisco.marrakAir.runtime.views.SecondaryView;

import java.util.function.Supplier;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.control.NavigationDrawer.Item;
import com.gluonhq.charm.glisten.layout.Layer;
import com.gluonhq.charm.glisten.layout.layer.SidePopupView;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.charm.glisten.visual.Swatch;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GluonApplication extends MobileApplication {

	public static final String PRIMARY_VIEW = HOME_VIEW;
	public static final String SECONDARY_VIEW = "Secondary View";
	public static final String MENU_LAYER = "Side Menu";

	@Override
	public void init() {
		try {
			MQTTConnector connection = new MQTTConnector("localhost", null, null, null);
			GamePresenter.setConnection(connection);
			System.out.println("connected !");
			
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("pb de connexion");
			
		}
		
		Supplier<View> v1 = new Supplier<View>() {

			@Override
			public View get() {
				// TODO Auto-generated method stub
				return (View) new GameView().getView();
			}
		};
		Supplier<View> v2 = new Supplier<View>() {

			@Override
			public View get() {
				// TODO Auto-generated method stub
				return (View) new SecondaryView().getView();
			}
		};
		
		addViewFactory(PRIMARY_VIEW, v1);
		addViewFactory(SECONDARY_VIEW, v2);
		final NavigationDrawer drawer = new NavigationDrawer();

		NavigationDrawer.Header header = new NavigationDrawer.Header("MarrakAir",
				"Smart city Participative project",
				new Avatar(21, new Image(GluonApplication.class.getResourceAsStream("/icon.png"))));
		drawer.setHeader(header);
				final Item primaryItem = new Item("Game", MaterialDesignIcon.HOME.graphic());
		final Item secondaryItem = new Item("Configuration", MaterialDesignIcon.DASHBOARD.graphic());
		drawer.getItems().addAll(primaryItem, secondaryItem);
		
		ChangeListener<? super Node> nd1 = new ChangeListener<Node>() {

			@Override
			public void changed(ObservableValue<? extends Node> obs, Node oldItem, Node newItem) {
				hideLayer(MENU_LAYER);
				switchView(newItem.equals(primaryItem) ? PRIMARY_VIEW : SECONDARY_VIEW);

			}
		};
		drawer.selectedItemProperty().addListener(nd1);
		Supplier<Layer> v3 = new Supplier<Layer>() {

			@Override
			public Layer get() {
				// TODO Auto-generated method stub
				return  new SidePopupView(drawer);
			}
		};
		System.out.println("pb de connexion 5");
		
		addLayerFactory(MENU_LAYER, v3);
		System.out.println("pb de connexion 6");
		

	}

	
	@Override
	public void postInit(Scene scene) {
		System.out.println("pb de connexion 7b");
		
		Swatch.GREEN.assignTo(scene);
		System.out.println("pb de connexion 7");
		
		((Stage) scene.getWindow()).setHeight(1024);
		((Stage) scene.getWindow()).setWidth(768);
		scene.getStylesheets().add(GluonApplication.class.getResource("style.css").toExternalForm());
		((Stage) scene.getWindow()).getIcons().add(new Image(GluonApplication.class.getResourceAsStream("/icon.png")));
	}
	
	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		super.stop();
		super.stop();
	    /*if(controller != null)
	    {
	        controller.startHousekeeping(); 
	    }*/
		
		Platform.exit();
		System.exit(0);
	}
}
