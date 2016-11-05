package ummisco.marrakAir.runtime2;


import javafx.scene.image.Image;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ummisco.marrakAir.gui.GuiController;


public class StartMarrakAir extends Application {
	private VBox rootLayout;
	
	@Override
	public void start(Stage primaryStage) {
		try {
            // Load root layout from fxml file.
			 rootLayout = (VBox)FXMLLoader.load(getClass().getResource("application.fxml"));
			 GuiController.setRootPane(rootLayout);
			 
			 //MQTTConnector connection = new MQTTConnector("localhost", null, null);
			 //GuiController.setConnection(connection);
			//rootLayout.getChildren().get(1);
			 SplitPane n = (SplitPane) rootLayout.getChildren().get(0);
			 AnchorPane sp = (AnchorPane) n.getItems().get(0);
		//	 sp.setContent(value);
			 /*for(Node v :n.getItems())
            {
            	System.out.println("Node node " + v);
            }*/
			 /*Image image = new Image("chalk.png");  //pass in the image path
			 ImageCursor cursor = new ImageCursor(image);
			 ImageCursor.getBestSize(image.getWidth()/2, image.getHeight()/2);
	         sp.setCursor(cursor);*/
			 
			 // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            
            //AquaFx.style();
			scene.getStylesheets().add(getClass().getResource("ComplexApplication.css").toExternalForm());
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
