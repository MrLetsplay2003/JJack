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
import me.mrletsplay.jjack.channel.JJackDefaultInputChannel;
import me.mrletsplay.jjack.channel.JJackDefaultOutputChannel;
import me.mrletsplay.jjack.channel.NoSelectionModel;
import me.mrletsplay.jjack.port.JJackInputPort;

public class JJackDefaultInputChannelController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ComboBox<JJackInputPort> inDevice;

	@FXML
	private Slider volume;

	@FXML
	private Slider volumeOut;

	protected JJackDefaultInputChannel channel;
	
	public void setChannel(JJackDefaultInputChannel channel) {
		this.channel = channel;
		
		volumeOut.valueProperty().bind(channel.getCurrentVolumeProperty());
		volume.valueProperty().bindBidirectional(channel.getVolumeProperty());
		inDevice.valueProperty().bindBidirectional(channel.getInputPortProperty());
		inDevice.itemsProperty().bind(new SimpleListProperty<>(JJack.getInputPorts()));
	}

	@FXML
	void removeChannel(ActionEvent event) {
		JJack.removeChannel(channel.getID());
	}

	@FXML
	void editOutputs(ActionEvent event) {
		CheckListView<JJackDefaultOutputChannel> l = new CheckListView<>();
		
		l.getItems().addAll(JJack.getChannelsOfType(JJackDefaultOutputChannel.class).stream()
				.filter(o -> o.getOutputPort() != null)
				.collect(Collectors.toList()));
		
		l.setPrefWidth(350);
		l.setPrefHeight(400);
		
		for(JJackDefaultOutputChannel out : channel.getOutputs()) {
			l.getCheckModel().check(out);
		}
		
		l.setSelectionModel(new NoSelectionModel<>());
		
		Dialog<List<JJackDefaultOutputChannel>> d = new Dialog<>();
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
