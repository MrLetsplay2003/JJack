package me.mrletsplay.jjack.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.controlsfx.control.CheckListView;

import javafx.beans.property.SimpleListProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.StringConverter;
import me.mrletsplay.jjack.JJack;
import me.mrletsplay.jjack.channel.JJackSingleInputChannel;
import me.mrletsplay.jjack.channel.JJackSingleOutputChannel;
import me.mrletsplay.jjack.channel.NoSelectionModel;
import me.mrletsplay.jjack.port.JJackInputPort;

public class JJackSingleInputChannelController {

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
	
	@FXML
	private Label channelIndicator;

	protected JJackSingleInputChannel channel;
	
	public void setChannel(JJackSingleInputChannel channel) {
		this.channel = channel;
		
		volumeOut.valueProperty().bind(channel.getCurrentVolumeProperty());
		volume.valueProperty().bindBidirectional(channel.getVolumeProperty());
		inDevice.valueProperty().bindBidirectional(channel.getInputPortProperty());
		inDevice.itemsProperty().bind(new SimpleListProperty<>(JJack.getInputPorts()));
		channelIndicator.textProperty().bind(channel.getIDProperty().asString());
	}

	@FXML
	void removeChannel(ActionEvent event) {
		JJack.removeChannel(channel.getID());
	}

	@FXML
	void editOutputs(ActionEvent event) {
		CheckListView<JJackSingleOutputChannel> l = new CheckListView<>();
		
		l.getItems().addAll(JJack.getChannelsOfType(JJackSingleOutputChannel.class));
		
		l.setPrefWidth(350);
		l.setPrefHeight(400);
		
		for(JJackSingleOutputChannel out : channel.getOutputs()) {
			l.getCheckModel().check(out);
		}
		
		l.setSelectionModel(new NoSelectionModel<>());
		
		l.setCellFactory(lv -> {
			CheckBoxListCell<JJackSingleOutputChannel> checkBoxListCell = new CheckBoxListCell<>(item -> l.getItemBooleanProperty(item));
			
			checkBoxListCell.focusedProperty().addListener((o, ov, nv) -> {
				if (nv) checkBoxListCell.getParent().requestFocus();
			});
			
			checkBoxListCell.setConverter(new StringConverter<JJackSingleOutputChannel>() {
				@Override
				public String toString(JJackSingleOutputChannel channel) {
					return "Channel #" + channel.getID() + " ("	+ (channel.getOutputPort() == null ? "none" : channel.getOutputPort().getName()) + ")";
				}

				@Override
				public JJackSingleOutputChannel fromString(String string) {
					return checkBoxListCell.getItem();
				}
			});
			
			return checkBoxListCell;
		});
		
		Dialog<List<JJackSingleOutputChannel>> d = new Dialog<>();
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
