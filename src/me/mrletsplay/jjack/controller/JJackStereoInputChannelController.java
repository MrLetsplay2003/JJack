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
	
	@FXML
	private Label channelIndicator;

	protected JJackStereoInputChannel channel;
	
	public void setChannel(JJackStereoInputChannel channel) {
		this.channel = channel;
		
		volumeOutL.valueProperty().bind(channel.getCurrentLeftVolumeProperty());
		volumeOutR.valueProperty().bind(channel.getCurrentRightVolumeProperty());
		volume.valueProperty().bindBidirectional(channel.getVolumeProperty());
		inDevice.valueProperty().bindBidirectional(channel.getInputPortProperty());
		inDevice.itemsProperty().bind(new SimpleListProperty<>(JJack.getStereoInputPorts()));
		channelIndicator.textProperty().bind(channel.getIDProperty().asString());
	}

	@FXML
	void removeChannel(ActionEvent event) {
		JJack.removeChannel(channel.getID());
	}

	@FXML
	void editOutputs(ActionEvent event) {
		CheckListView<JJackStereoOutputChannel> l = new CheckListView<>();
		
		l.getItems().addAll(JJack.getChannelsOfType(JJackStereoOutputChannel.class));
		
		l.setPrefWidth(350);
		l.setPrefHeight(400);
		
		for(JJackStereoOutputChannel out : channel.getOutputs()) {
			l.getCheckModel().check(out);
		}
		
		l.setSelectionModel(new NoSelectionModel<>());
		
		l.setCellFactory(lv -> {
			CheckBoxListCell<JJackStereoOutputChannel> checkBoxListCell = new CheckBoxListCell<>(item -> l.getItemBooleanProperty(item));
			
			checkBoxListCell.focusedProperty().addListener((o, ov, nv) -> {
				if (nv) checkBoxListCell.getParent().requestFocus();
			});
			
			checkBoxListCell.setConverter(new StringConverter<JJackStereoOutputChannel>() {
				@Override
				public String toString(JJackStereoOutputChannel channel) {
					return "Channel #" + channel.getID() + " ("	+ (channel.getOutputPort() == null ? "none" : channel.getOutputPort().getName()) + ")";
				}

				@Override
				public JJackStereoOutputChannel fromString(String string) {
					return checkBoxListCell.getItem();
				}
			});
			
			return checkBoxListCell;
		});
		
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
