package ummisco.marrakAir.gui.widgets;

import javafx.event.Event;
import javafx.event.EventType;

public class ValueChangedEvent extends  Event {

	/**
	 * 
	 */
	String agentName;
	String agentAttribute;
	Object value;
	
	

	
	private static final long serialVersionUID = -5620926436035005961L;
	public ValueChangedEvent(String agent,String attribute,Object value ) {
		super(VALUE_CHANGED);
		this.agentName = agent;
		this.agentAttribute = attribute;
		this.value = value;
	}
	
	 public static final EventType<ValueChangedEvent> VALUE_CHANGED =
            new EventType<>(Event.ANY, "VALUE_CHANGED");

	 public String getAgentName() {
			return agentName;
		}
     public String getAgentAttributeName() {
			return agentAttribute;
		}
     public Object getValue()
     {
    	 return this.value;
     }
		
		


}
