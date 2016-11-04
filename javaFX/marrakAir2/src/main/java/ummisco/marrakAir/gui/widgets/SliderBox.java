package ummisco.marrakAir.gui.widgets;

import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class SliderBox extends VBox {
	
	private static final String DEFAULT_LABEL = "UNDEFINED";
	private static final float DEFAULT_MIN_VALUE = 0;
	private static final float DEFAULT_MAX_VALUE = 100;
	private Slider slider;
	private Label label;
	private double value;
	private Label valueLabel;
	private String agentName;
	private String agentAttribute;
	
	private boolean drawLabel = true;
	private boolean drawButton = true;
	
	
	public SliderBox()
	{
		this(DEFAULT_LABEL,DEFAULT_MIN_VALUE,DEFAULT_MAX_VALUE);
	//	this.setPadding(new Insets(10));
	}
	
	public SliderBox(String textLabel,float min, float max)
	{
		super();
	//	this.setPadding(new Insets(30));
		this.slider=new Slider(min,max,(max-min)/2);
		this.slider.setOrientation(Orientation.VERTICAL);
		this.slider.setBlockIncrement(1.0);
		slider.setShowTickLabels(true);
		slider.setSnapToTicks(true);
		slider.setBlockIncrement(1);
		slider.setShowTickMarks(true);
		slider.setMajorTickUnit(50);
		slider.setMinorTickCount(5);
		slider.setBlockIncrement(10);
		ChangeListener<Number> etmp = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println("old "+ oldValue+" "+newValue);
			}
		};
		slider.valueProperty().addListener(etmp);
				/*new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println("old "+ oldValue+" "+newValue);
				// TODO Auto-generated method stub
			}
		});*/
		this.value = slider.getValue();
		
		
		EventHandler<? super MouseEvent> ehand = new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				if(value != slider.getValue()) {
					value = Math.round(slider.getValue());
					valueLabel.setText(""+value);
					fireEvent(new ValueChangedEvent(agentName,agentAttribute,value));
				}
				
			}
		};
		slider.setOnMouseReleased(ehand);
		/*slider.setOnMouseReleased((Event e) -> { 
			if(this.value != slider.getValue()) {
				this.value = slider.getValue();
				this.fireEvent(new ValueChangedEvent(this.agentName,this.agentAttribute,this.value));
			}
		});*/
		
		EventHandler<? super ValueChangedEvent> evt = new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				System.out.println(event);
				
			}
		};
		this.setOnValueChanged(evt);
		
		this.setAlignment(Pos.CENTER);
		this.label=new Label(textLabel);
		this.valueLabel = new Label(""+this.value);
		initializeDisplay();
	}
	
	private void initializeDisplay()
	{
		this.setAlignment(Pos.CENTER);
		this.getChildren().clear();
		
		this.getChildren().add(drawSlider());
		if(this.drawLabel){
			this.getChildren().add(valueLabel);
			this.getChildren().add(label);
		}
			

	}
	
	private Node drawSlider()
	{
		if(drawButton == false)
			return drawSliderNoButton();
		else
			return drawSliderButton();
		
		
	}
	private Node drawSliderNoButton()
	{
		return this.slider;
	}
	private Node drawSliderButton()
	{
		GridPane res =new GridPane();
		res.setAlignment(Pos.CENTER);
		
		EventHandler<ActionEvent> evt1 = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				slider.increment();
				value = slider.getValue();
				valueLabel.setText(""+value);
				fireEvent(new ValueChangedEvent(agentName,agentAttribute,value));

				//fireEvent(new ValueChangedEvent(agentName,agentAttribute,value));
			}
		};
		
		Button badd = MaterialDesignIcon.ADD.button();// evt1);
		badd.setOnAction(evt1);
		badd.setPrefHeight(this.getHeight()/6);
		EventHandler<ActionEvent> evt2 = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				slider.decrement();
				value = slider.getValue();
				valueLabel.setText(""+value);
				fireEvent(new ValueChangedEvent(agentName,agentAttribute,value));

				//fireEvent(new ValueChangedEvent(agentName,agentAttribute,value));
			}
		};
		
		Button bremove = MaterialDesignIcon.REMOVE.button();// evt1);
		bremove.setOnAction(evt2);
		
		//FloatingActionButton bremove = new FloatingActionButton(MaterialDesignIcon.REMOVE.text, evt2);
		
		bremove.setPrefHeight(this.getHeight()/6);
		if(this.slider.getOrientation()==Orientation.VERTICAL)
			{
	     		res.add(badd, 0, 0);
	      		res.add(this.slider, 0, 1);
	      		res.add(bremove, 0, 2);
	       	}
		else
			{
     		res.add(badd, 0, 0);
      		res.add(this.slider, 1,0);
      		res.add(bremove, 2, 0);
		  
			}

     	return res;
	}
	
	
	public void setVertical(Boolean b)
	{
		this.slider.setOrientation(b?Orientation.VERTICAL:Orientation.HORIZONTAL);
		initializeDisplay();

	}
	public void setVertical(String b)
	{
		setVertical(b.equalsIgnoreCase("true"));
	}
	
	
//    private ObjectProperty<EventHandler<ValueChangedEvent>> propertyOnAction = new SimpleObjectProperty<EventHandler<ValueChangedEvent>>();
    
    private  EventHandler<? super ValueChangedEvent> changedEventHandler = null;
    
  /*  public final ObjectProperty<EventHandler<ValueChangedEvent>> onMaouProperty() {
        return propertyOnAction;
    }
*/
    public final void setOnValueChanged(EventHandler<? super ValueChangedEvent> value) 
    {
    	this.addEventHandler(ValueChangedEvent.VALUE_CHANGED, value);
        changedEventHandler = value;
    }
    
   
    public final  EventHandler<? super ValueChangedEvent> getOnValueChanged() {
        return changedEventHandler;

    }
	
	public String getVertical()
	{
		return Boolean.valueOf(this.slider.getOrientation()==Orientation.VERTICAL).toString();
	}
	
	public void setLabel(String lbl)
	{
		this.label.setText(lbl);
	}
	
	public String getLabel()
	{
		return this.label.getText();
	}

	public void setAgentName(String lbl)
	{
		this.agentName= lbl;
	}
	
	public String getAgentName()
	{
		return this.agentName;
	}

	public void setAgentAttribute(String lbl)
	{
		this.agentAttribute= lbl;
	}
	
	public String getAgentAttribute()
	{
		return this.agentAttribute;
	}

	
}
