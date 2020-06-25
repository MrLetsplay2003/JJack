package me.mrletsplay.jjack.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.controlsfx.control.CheckListView;

import javafx.beans.property.SimpleListProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Slider;
import me.mrletsplay.jjack.JJack;
import me.mrletsplay.jjack.channel.JJackStereoInputChannel;
import me.mrletsplay.jjack.channel.JJackStereoOutputChannel;
import me.mrletsplay.jjack.channel.NoSelectionModel;
import me.mrletsplay.jjack.port.stereo.JJackStereoInputPort;

public class JJackStereoInputChannelController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ComboBox<JJackStereoInputPort> inDevice;

	@FXML
	private Slider volume;

	@FXML
	private Slider volumeOutL;

	@FXML
	private Slider volumeOutR;

	protected JJackStereoInputChannel channel;
	
	public void setChannel(JJackStereoInputChannel channel) {
		this.channel = channel;
		
		volumeOutL.valueProperty().bind(channel.getCurrentLeftVolumeProperty());
		volumeOutR.valueProperty().bind(channel.getCurrentRightVolumeProperty());
		volume.valueProperty().bindBidirectional(channel.getVolumeProperty());
		inDevice.valueProperty().bindBidirectional(channel.getInputPortProperty());
		inDevice.itemsProperty().bind(new SimpleListProperty<>(JJack.getStereoInputPorts()));
	}

	@FXML
	void removeChannel(ActionEvent event) {
		JJack.removeChannel(channel.getID());
	}

	@FXML
	void editOutputs(ActionEvent event) {
		CheckListView<JJackStereoOutputChannel> l = new CheckListView<>();
		
		l.getItems().addAll(JJack.getChannelsOfType(JJackStereoOutputChannel.class).stream()
				.filter(o -> o.getOutputPort() != null)
				.collect(Collectors.toList()));
		
		l.setPrefWidth(350);
		l.setPrefHeight(400);
		
		for(JJackStereoOutputChannel out : channel.getOutputs()) {
			l.getCheckModel().check(out);
		}
		
		l.setSelectionModel(new NoSelectionModel<>());
		
		Dialog<List<JJackStereoOutputChannel>> d = new Dialog<>();
		d.getDialogPane().setContent(l);
		d.setResultConverter((button) -> {
			if(button != ButtonType.OK) return null;
			return l.getCheckModel().getCheckedItems();
		});
		d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		var r = d.showAndWait();
		if(r.isPresent()) channel.setOutputs(r.get());
	}

	@FXML
	void initialize() {
		
	}

}
