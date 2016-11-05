package ummisco.marrakAir.gui;

import java.awt.Rectangle;
import java.io.IOException;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.jfree.fx.FXGraphics2D;

import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;

public class MapCanvas {
	private Canvas canvas;
	private MapContent map;
	private GraphicsContext gc;

	public MapCanvas(int width, int height) {
		canvas = new Canvas(width, height);
		gc = canvas.getGraphicsContext2D();
		initMap();
		drawMap(gc);
		initEvent();
		initPaintThread();
	}

	public Node getCanvas() {
		return canvas;
	}

	private void initMap() {
		try {
			FileDataStore store = FileDataStoreFinder
					.getDataStore(this.getClass().getClassLoader().getResource("maps/countries.shp"));
			SimpleFeatureSource featureSource = store.getFeatureSource();
			map = new MapContent();
			map.setTitle("Quickstart");
			Style style = SLD.createSimpleStyle(featureSource.getSchema());
			FeatureLayer layer = new FeatureLayer(featureSource, style);
			map.addLayer(layer);
			map.getViewport().setScreenArea(new Rectangle((int) canvas.getWidth(), (int) canvas.getHeight()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean repaint = true;

	private void drawMap(GraphicsContext gc) {
		if (!repaint) {
			return;
		}
		repaint = false;
		StreamingRenderer draw = new StreamingRenderer();
		draw.setMapContent(map);
		
		FXGraphics2D graphics = new FXGraphics2D(gc);
		graphics.setBackground(java.awt.Color.WHITE);
		graphics.clearRect(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight());
		Rectangle rectangle = new Rectangle((int) canvas.getWidth(), (int) canvas.getHeight());
		draw.paint(graphics, rectangle, map.getViewport().getBounds());
	}

	private double baseDrageX;
	private double baseDrageY;

	/*
	 *initial for mouse event 
	 */
	private void initEvent() {
		/*
		 * setting the original coordinate
		 */
		canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent e) {
				baseDrageX = e.getSceneX();
				baseDrageY = e.getSceneY();
				e.consume();
			}
		});
		/*
		 * translate according to the mouse drag
		 */
		canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				double difX = e.getSceneX() - baseDrageX;
				double difY = e.getSceneY() - baseDrageY;
				baseDrageX = e.getSceneX();
				baseDrageY = e.getSceneY();
				DirectPosition2D newPos = new DirectPosition2D(difX, difY);
				DirectPosition2D result = new DirectPosition2D();
				map.getViewport().getScreenToWorld().transform(newPos, result);
				ReferencedEnvelope env = new ReferencedEnvelope(map.getViewport().getBounds());
				env.translate(env.getMinimum(0) - result.x, env.getMaximum(1) - result.y);
				doSetDisplayArea(env);
				e.consume();

			}
		});
		/*
		 * double clicks to restore to original map
		 */
		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent t) {
				if (t.getClickCount() > 1) {
					doSetDisplayArea(map.getMaxBounds());
				}
				t.consume();
			}
		});
		/*
		 * scroll for zoom in and out
		 */
		canvas.addEventHandler(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {

			@Override
			public void handle(ScrollEvent e) {
				ReferencedEnvelope envelope = map.getViewport().getBounds();
				double percent = e.getDeltaY() / canvas.getWidth();
				double width = envelope.getWidth();
				double height = envelope.getHeight();
				double deltaW = width * percent;
				double deltaH = height * percent;
				envelope.expandBy(deltaW, deltaH);
				doSetDisplayArea(envelope);
				e.consume();
			}
		});
	}

	private static final double PAINT_HZ = 50.0;
	private void initPaintThread() {
		ScheduledService<Boolean> svc = new ScheduledService<Boolean>() {
			protected Task<Boolean> createTask() {
				return new Task<Boolean>() {
					protected Boolean call() {
						Platform.runLater(() -> {
							drawMap(gc);
						});
						return true;
					}
				};
			}
		};
		svc.setPeriod(Duration.millis(1000.0 / PAINT_HZ));
		svc.start();
	}

	protected void doSetDisplayArea(ReferencedEnvelope envelope) {
		map.getViewport().setBounds(envelope);
		repaint = true;
	}
}
