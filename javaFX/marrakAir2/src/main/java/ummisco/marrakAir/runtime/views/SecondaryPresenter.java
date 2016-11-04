
package ummisco.marrakAir.runtime.views;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.Alert;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import ummisco.marrakAir.network.MQTTConnector;
import ummisco.marrakAir.runtime.GluonApplication;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class SecondaryPresenter {

    @FXML
    private View secondary;
    
    @FXML
    private TextField hote;
    
    @FXML
    private TextField port;
    
    @FXML
    private TextField password;
    
    @FXML
    private TextField user;


    @FXML
    public void initialize() {
        secondary.setShowTransitionFactory(BounceInRightTransition::new);
        /*FloatingActionButton btt = new FloatingActionButton(MaterialDesignIcon.ADD.text, e -> System.out.println("Info:"+MaterialDesignIcon.INFO.text+":"));
        secondary.getLayers().add(btt);
        FloatingActionButton bttm = new FloatingActionButton(MaterialDesignIcon.REMOVE.text, e -> System.out.println("Info:"+MaterialDesignIcon.INFO.text+":"));
        secondary.getLayers().add(bttm);*/
        secondary.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> 
                        MobileApplication.getInstance().showLayer(GluonApplication.MENU_LAYER)));
               appBar.setTitleText("Connection");
               
               // add prompt text to the textfields
               hote.setPromptText(GamePresenter.getConnection()==null?"localhost":GamePresenter.getConnection().SERVER_URL);
               port.setPromptText(GamePresenter.getConnection()==null?"1889":GamePresenter.getConnection().SERVER_PORT);
               password.setPromptText(GamePresenter.getConnection()==null?"password":GamePresenter.getConnection().PASSWORD);
               user.setPromptText(GamePresenter.getConnection()==null?"admin":GamePresenter.getConnection().LOGIN);
               
               BooleanBinding binding = new BooleanBinding() {
               	{
               		super.bind(user.textProperty(), port.textProperty(), hote.textProperty(), password.textProperty());
               	}
					@Override
					protected boolean computeValue() {
						// TODO Auto-generated method stub
						return (user.getText().isEmpty() || password.getText().isEmpty() || hote.getText().isEmpty() || port.getText().isEmpty());
					}
				};
              
				Button saveBtn = MaterialDesignIcon.SAVE.button(e -> {
					{
						try {
							GamePresenter.setConnection(new MQTTConnector(hote.getText(), port.getText(), user.getText(), password.getText()));
							GamePresenter.initConnection();
							Alert alert = new Alert(AlertType.INFORMATION, "Connection is working");
						        alert.showAndWait();
						} catch (MqttException e1) {
							// TODO Auto-generated catch block
							Alert alert = new Alert(AlertType.ERROR, "connection is not working ");
					        alert.showAndWait();
							e1.printStackTrace();
						}
					}
				});
				saveBtn.disableProperty().bind(binding);
                appBar.getActionItems().add(saveBtn);
              
            }
        });
    }
}
