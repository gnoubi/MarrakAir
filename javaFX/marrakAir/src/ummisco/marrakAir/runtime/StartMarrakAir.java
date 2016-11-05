package ummisco.marrakAir.runtime;

import application.Main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ummisco.marrakAir.gui.GuiController;
import ummisco.marrakAir.network.MQTTConnector;

public class StartMarrakAir extends Application {
	private VBox rootLayout;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			 MQTTConnector connection = new MQTTConnector("localhost", null, null);
			 GuiController.setConnection(connection);
			 rootLayout = (VBox)FXMLLoader.load(getClass().getResource("application.fxml"));
			 GuiController.setRootPane(rootLayout);
			//rootLayout.getChildren().get(1);
			 SplitPane n = (SplitPane) rootLayout.getChildren().get(1);
			 ScrollPane sp = (ScrollPane) n.getItems().get(1);
		//	 sp.setContent(value);
			 for(Node v :n.getItems())
            {
            	System.out.println("Node node " + v);
            }
			 
			 // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
