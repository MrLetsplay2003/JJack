package me.mrletsplay.jjack.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleListProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import me.mrletsplay.jjack.JJack;
import me.mrletsplay.jjack.channel.JJackSingleOutputChannel;
import me.mrletsplay.jjack.port.JJackOutputPort;

public class JJackSingleOutputChannelController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ComboBox<JJackOutputPort> outDevice;

	@FXML
	private Slider volume;

	@FXML
	private Slider volumeOut;
	
	@FXML
	private Label channelIndicator;

	protected JJackSingleOutputChannel channel;
	
	public void setChannel(JJackSingleOutputChannel channel) {
		this.channel = channel;
		
		volumeOut.valueProperty().bind(channel.getCurrentVolumeProperty());
		volume.valueProperty().bindBidirectional(channel.getVolumeProperty());
		channel.setMaxVolume(volume.getMax());
		volume.maxProperty().bind(channel.getMaxVolumeProperty());
		outDevice.valueProperty().bindBidirectional(channel.getOutputPortProperty());
		outDevice.itemsProperty().bind(new SimpleListProperty<>(JJack.getOutputPorts()));
		channelIndicator.textProperty().bind(channel.getIDProperty().asString());
	}

	@FXML
	void removeChannel(ActionEvent event) {
		JJack.removeChannel(channel.getID());
	}

	@FXML
	void initialize() {
		
	}

}
