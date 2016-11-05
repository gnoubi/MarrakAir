package ummisco.marrakAir.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainTutorial2 extends Application {

	@Override
	public void start(Stage primaryStage) {
		MapCanvas canvas = new MapCanvas(1024, 768);
		Pane pane = new Pane(canvas.getCanvas());
		Scene scene = new Scene(pane);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Map Tutorial 2");
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
