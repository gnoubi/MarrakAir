package ummisco.marrakAir.gui.widget;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SliderBox extends VBox {
	
	private static final String DEFAULT_LABEL = "UNDEFINED";
	private static final float DEFAULT_MIN_VALUE = 0;
	private static final float DEFAULT_MAX_VALUE = 100;
	private Slider slider;
	private Label label;
	private double value;
	private String agentName;
	private String agentAttribute;
	
	
	public SliderBox()
	{
		this(DEFAULT_LABEL,DEFAULT_MIN_VALUE,DEFAULT_MAX_VALUE);
	}
	
	public SliderBox(String textLabel,float min, float max)
	{
		super();
		this.slider=new Slider(min,max,(max-min)/2);
		this.slider.setOrientation(Orientation.VERTICAL);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMajorTickUnit(50);
		slider.setMinorTickCount(5);
		slider.setBlockIncrement(10);
		/*slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				//System.out.println("old "+ oldValue+" "+newValue);
				// TODO Auto-generated method stub
			}
		});*/
		this.value = slider.getValue();
		slider.setOnMouseReleased((Event e) -> { 
			if(this.value != slider.getValue()) {
				this.value = slider.getValue();
				this.fireEvent(new ValueChangedEvent(this.agentName,this.agentAttribute,this.value));
			}
		});
		this.setOnValueChanged( event -> {
		    System.out.println(event);
		});	
		this.setAlignment(Pos.CENTER);
		this.label=new Label(textLabel);
		this.getChildren().add(slider);
		this.getChildren().add(label);
	}
	
	public void setVertical(Boolean b)
	{
		this.slider.setOrientation(b?Orientation.VERTICAL:Orientation.HORIZONTAL);
	}
	public void setVertical(String b)
	{
		System.out.println("coucoudouf sq "+ b);
		setVertical(b.equalsIgnoreCase("true"));
	}
	
	
//    private ObjectProperty<EventHandler<ValueChangedEvent>> propertyOnAction = new SimpleObjectProperty<EventHandler<ValueChangedEvent>>();
    
    private  EventHandler<? super ValueChangedEvent> changedEventHandler = null;
    
  /*  public final ObjectProperty<EventHandler<ValueChangedEvent>> onMaouProperty() {
        return propertyOnAction;
    }
*/
    public final void setOnValueChanged(
            EventHandler<? super ValueChangedEvent> value) {
    	System.out.println("totot sisis ");
        this.addEventHandler(ValueChangedEvent.VALUE_CHANGED, value);
        changedEventHandler = value;
    }
    
   /* public final void setOnMaou(EventHandler<ValueChangedEvent> handler) {
    	System.out.println("totot sisis ");
        propertyOnAction.set(handler);
    }*/

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
