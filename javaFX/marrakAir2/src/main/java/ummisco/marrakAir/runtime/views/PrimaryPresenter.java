package ummisco.marrakAir.runtime.views;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import ummisco.marrakAir.runtime.GluonApplication;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PrimaryPresenter {

    @FXML
    private View primary;

    @FXML
    private Label label;

    public void initialize() {
      /*  primary.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> 
                        MobileApplication.getInstance().showLayer(GluonApplication.MENU_LAYER)));
                appBar.setTitleText("Primary");
               
                EventHandler<ActionEvent> evt = new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						 System.out.println("Search");
						
					}
				};
				appBar.getActionItems().add(MaterialDesignIcon.SEARCH.button(evt));
    
            }
        }*/
    }
            
    @FXML
    void buttonClick() {
        label.setText("Hello JavaFX Universe!");
    }
    
}
