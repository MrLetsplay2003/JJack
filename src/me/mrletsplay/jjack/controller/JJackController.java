package me.mrletsplay.jjack.controller;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import me.mrletsplay.jjack.JJack;
import me.mrletsplay.jjack.channel.JJackChannel;
import me.mrletsplay.jjack.channel.JJackSingleComboChannel;
import me.mrletsplay.jjack.channel.JJackSingleInputChannel;
import me.mrletsplay.jjack.channel.JJackSingleOutputChannel;
import me.mrletsplay.jjack.channel.JJackStereoInputChannel;
import me.mrletsplay.jjack.channel.JJackStereoOutputChannel;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class JJackController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private AnchorPane mainPane;
	
	@FXML
	private JJackStereoInputChannelController channel0Controller;
	
	@FXML
	private JJackStereoInputChannelController channel1Controller;
	
	@FXML
	private JJackStereoInputChannelController channel2Controller;
	
	@FXML
	private JJackStereoOutputChannelController channel3Controller;

	@FXML
	void about(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION, "Made by MrLetsplay", ButtonType.OK);
		alert.setTitle("About");
		alert.setHeaderText("JJack");
		alert.initOwner(JJack.stage);
		alert.show();
	}

	@FXML
	void addSingleInputChannel(ActionEvent event) {
		JJack.createSingleInputChannel();
	}
	
	@FXML
	void addSingleOutputChannel(ActionEvent event) {
		JJack.createSingleOutputChannel();
	}
	
	@FXML
	void addSingleComboChannel(ActionEvent event) {
		JJack.createSingleComboChannel();
	}

	@FXML
	void addStereoInputChannel(ActionEvent event) {
		JJack.createStereoInputChannel();
	}
	
	@FXML
	void addStereoOutputChannel(ActionEvent event) {
		JJack.createStereoOutputChannel();
	}
	
	public void addSingleComboChannel(JJackSingleComboChannel channel) {
		JJackSingleComboChannelController ctrl = addChannel(channel, "combo-channel");
		ctrl.setChannel(channel);
	}
	
	public void addSingleInputChannel(JJackSingleInputChannel channel) {
		JJackSingleInputChannelController ctrl = addChannel(channel, "input-channel");
		ctrl.setChannel(channel);
	}
	
	public void addSingleOutputChannel(JJackSingleOutputChannel channel) {
		JJackSingleOutputChannelController ctrl = addChannel(channel, "output-channel");
		ctrl.setChannel(channel);
	}
	
	public void addStereoInputChannel(JJackStereoInputChannel channel) {
		JJackStereoInputChannelController ctrl = addChannel(channel, "stereo-input-channel");
		ctrl.setChannel(channel);
	}
	
	public void addStereoOutputChannel(JJackStereoOutputChannel channel) {
		JJackStereoOutputChannelController ctrl = addChannel(channel, "stereo-output-channel");
		ctrl.setChannel(channel);
	}
	
	public <T extends JJackChannel, C> C addChannel(T channel, String fxFileName) {
		double oldW = JJack.stage.getWidth();
		JJack.stage.setWidth(oldW + 175);
		
		try {
			URL url = JJack.class.getResource("/include/" + fxFileName + ".fxml");
			if(url == null) url = new File("./include/" + fxFileName + ".fxml").toURI().toURL();
			FXMLLoader l = new FXMLLoader(url);
			Parent pr = l.load(url.openStream());
			C ctrl = l.getController();
			mainPane.getChildren().add(pr);
			AnchorPane.setTopAnchor(pr, 10d);
			AnchorPane.setLeftAnchor(pr, oldW - 10);
			AnchorPane.setBottomAnchor(pr, 10d);
			pr.setId("channel" + channel.getID());
			return ctrl;
		}catch(Exception e) {
			throw new FriendlyException(e);
		}
	}
	
	public void removeChannel(int channelID) {
		double oldW = JJack.stage.getWidth();
		JJack.stage.setWidth(oldW - 175);
		
		int i = 0;
		for(Node n : mainPane.lookupAll(".channel")) {
			AnchorPane a = (AnchorPane) n;
			if(a.getId().equals("channel" + channelID)) {
				mainPane.getChildren().remove(a);
				continue;
			}
			
			AnchorPane.setLeftAnchor(a, i * 175d + 10);
			i++;
		}
	}
	
	public void resetChannels() {
		JJack.stage.setWidth(720);
		for(Node n : mainPane.lookupAll(".channel")) {
			AnchorPane a = (AnchorPane) n;
			if(!IntStream.range(0, JJack.DEFAULT_CHANNEL_COUNT).anyMatch(i -> a.getId().equals("channel" + i))) {
				mainPane.getChildren().remove(a);
				continue;
			}
		}
	}
	
	public void sortChannels() {
		
	}

	@FXML
	void loadConfiguration(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File("."));
		chooser.getExtensionFilters().add(new ExtensionFilter("Yaml Configuration file", "*.yml", "*.yaml"));
		File f = chooser.showOpenDialog(JJack.stage);
		if(f == null) return;
		JJack.loadConfiguration(f);
	}

	@FXML
	void newConfiguration(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION, "Save your current configuration?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		alert.setTitle("Save");
		alert.setHeaderText("JJack");
		Optional<ButtonType> b = alert.showAndWait();
		if(!b.isPresent() || b.get() == ButtonType.CANCEL) return;
		
		if(b.get() == ButtonType.YES) saveConfigurationAs(null);
		
		JJack.resetChannels();
	}

	@FXML
	void preferences(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION, "Coming soon", ButtonType.OK);
		alert.setTitle("Preferences");
		alert.setHeaderText("JJack");
		alert.initOwner(JJack.stage);
		alert.show();
	}

	@FXML
	void quit(ActionEvent event) {
		JJack.exit();
	}

	@FXML
	void saveConfigurationAs(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File("."));
		File f = chooser.showSaveDialog(JJack.stage);
		if(f == null) return;
		
		if(!f.getName().endsWith(".yml") && !f.getName().endsWith(".yaml")) {
			Alert alert = new Alert(AlertType.INFORMATION, "The file's name doesn't end with a .yml or .yaml extension.\nDo you want to save it with that extension instead? (might override existing files)", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
			alert.setTitle("Save");
			alert.setHeaderText("JJack");
			Optional<ButtonType> b = alert.showAndWait();
			if(!b.isPresent() || b.get() == ButtonType.CANCEL) return;
			
			if(b.get() == ButtonType.YES) f = new File(f.getAbsolutePath() + ".yml");
		}
		
		JJack.saveConfiguration(f);
	}
	
	@FXML
	void initialize() {
		JJack.getChannels().add(new JJackStereoInputChannel(0));
		JJack.getChannels().add(new JJackStereoInputChannel(1));
		JJack.getChannels().add(new JJackStereoInputChannel(2));
		JJack.getChannels().add(new JJackStereoOutputChannel(3));
		
		channel0Controller.setChannel((JJackStereoInputChannel) JJack.getChannel(0));
		channel1Controller.setChannel((JJackStereoInputChannel) JJack.getChannel(1));
		channel2Controller.setChannel((JJackStereoInputChannel) JJack.getChannel(2));
		channel3Controller.setChannel((JJackStereoOutputChannel) JJack.getChannel(3));
	}
}
