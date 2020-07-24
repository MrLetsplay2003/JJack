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
import me.mrletsplay.jjack.channel.JJackStereoOutputChannel;
import me.mrletsplay.jjack.port.stereo.JJackStereoOutputPort;

public class JJackStereoOutputChannelController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ComboBox<JJackStereoOutputPort> outDevice;

	@FXML
	private Slider volume;

	@FXML
	private Slider volumeOutL;

	@FXML
	private Slider volumeOutR;
	
	@FXML
	private Label channelIndicator;

	protected JJackStereoOutputChannel channel;
	
	public void setChannel(JJackStereoOutputChannel channel) {
		this.channel = channel;
		
		volumeOutL.valueProperty().bind(channel.getCurrentLeftVolumeProperty());
		volumeOutR.valueProperty().bind(channel.getCurrentRightVolumeProperty());
		volume.valueProperty().bindBidirectional(channel.getVolumeProperty());
		channel.setMaxVolume(volume.getMax());
		volume.maxProperty().bind(channel.getMaxVolumeProperty());
		outDevice.valueProperty().bindBidirectional(channel.getOutputPortProperty());
		outDevice.itemsProperty().bind(new SimpleListProperty<>(JJack.getStereoOutputPorts()));
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
