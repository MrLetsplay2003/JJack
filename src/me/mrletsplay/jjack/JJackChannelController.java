package me.mrletsplay.jjack;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleListProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import me.mrletsplay.jjack.channel.JJackChannel;
import me.mrletsplay.jjack.port.JJackInputPort;
import me.mrletsplay.jjack.port.JJackOutputPort;

public class JJackChannelController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ComboBox<JJackInputPort> inDevice;

	@FXML
	private ComboBox<JJackOutputPort> outDevice;

	@FXML
	private Slider volume;

	@FXML
	private Slider volumeOut;

	protected JJackChannel channel;
	
	public void setChannel(JJackChannel channel) {
		this.channel = channel;
		
		volumeOut.valueProperty().bind(channel.getCurrentVolumeProperty());
		volume.valueProperty().bindBidirectional(channel.getVolumeProperty());
		inDevice.valueProperty().bindBidirectional(channel.getInputPortProperty());
		inDevice.itemsProperty().bind(new SimpleListProperty<>(JJack.getInputPorts()));
		outDevice.valueProperty().bindBidirectional(channel.getOutputPortProperty());
		outDevice.itemsProperty().bind(new SimpleListProperty<>(JJack.getOutputPorts()));
	}

	@FXML
	void removeChannel(ActionEvent event) {
		JJack.removeChannel(channel.getID());
	}

	@FXML
	void initialize() {
		
	}

}
