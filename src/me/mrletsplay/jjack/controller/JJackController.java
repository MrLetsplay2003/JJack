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
import me.mrletsplay.jjack.channel.JJackDefaultComboChannel;
import me.mrletsplay.jjack.channel.JJackDefaultInputChannel;
import me.mrletsplay.jjack.channel.JJackDefaultOutputChannel;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class JJackController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private AnchorPane mainPane;
	
	@FXML
	private JJackDefaultInputChannelController channel0Controller;
	
	@FXML
	private JJackDefaultInputChannelController channel1Controller;
	
	@FXML
	private JJackDefaultInputChannelController channel2Controller;
	
	@FXML
	private JJackDefaultOutputChannelController channel3Controller;

	@FXML
	void about(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION, "Made by MrLetsplay", ButtonType.OK);
		alert.setTitle("About");
		alert.setHeaderText("JJack");
		alert.initOwner(JJack.stage);
		alert.show();
	}

	@FXML
	void addInputChannel(ActionEvent event) {
		JJack.createDefaultInputChannel();
	}
	
	@FXML
	void addOutputChannel(ActionEvent event) {
		JJack.createDefaultOutputChannel();
	}
	
	@FXML
	void addComboChannel(ActionEvent event) {
		JJack.createComboChannel();
	}
	
	public void addComboChannel(JJackDefaultComboChannel channel) {
		double oldW = JJack.stage.getWidth();
		JJack.stage.setWidth(oldW + 175);
		
		try {
			URL url = JJack.class.getResource("/include/combo-channel.fxml");
			if(url == null) url = new File("./include/combo-channel.fxml").toURI().toURL();
			FXMLLoader l = new FXMLLoader(url);
			Parent pr = l.load(url.openStream());
			JJackComboChannelController ctrl = l.getController();
			ctrl.setChannel(channel);
			mainPane.getChildren().add(pr);
			AnchorPane.setTopAnchor(pr, 10d);
			AnchorPane.setLeftAnchor(pr, oldW - 10);
			AnchorPane.setBottomAnchor(pr, 10d);
			pr.setId("channel" + channel.getID());
		}catch(Exception e) {
			throw new FriendlyException(e);
		}
	}
	
	public void addDefaultInputChannel(JJackDefaultInputChannel channel) {
		double oldW = JJack.stage.getWidth();
		JJack.stage.setWidth(oldW + 175);
		
		try {
			URL url = JJack.class.getResource("/include/input-channel.fxml");
			if(url == null) url = new File("./include/input-channel.fxml").toURI().toURL();
			FXMLLoader l = new FXMLLoader(url);
			Parent pr = l.load(url.openStream());
			JJackDefaultInputChannelController ctrl = l.getController();
			ctrl.setChannel(channel);
			mainPane.getChildren().add(pr);
			AnchorPane.setTopAnchor(pr, 10d);
			AnchorPane.setLeftAnchor(pr, oldW - 10);
			AnchorPane.setBottomAnchor(pr, 10d);
			pr.setId("channel" + channel.getID());
		}catch(Exception e) {
			throw new FriendlyException(e);
		}
	}
	
	public void addDefaultOutputChannel(JJackDefaultOutputChannel channel) {
		double oldW = JJack.stage.getWidth();
		JJack.stage.setWidth(oldW + 175);
		
		try {
			URL url = JJack.class.getResource("/include/output-channel.fxml");
			if(url == null) url = new File("./include/output-channel.fxml").toURI().toURL();
			FXMLLoader l = new FXMLLoader(url);
			Parent pr = l.load(url.openStream());
			JJackDefaultOutputChannelController ctrl = l.getController();
			ctrl.setChannel(channel);
			mainPane.getChildren().add(pr);
			AnchorPane.setTopAnchor(pr, 10d);
			AnchorPane.setLeftAnchor(pr, oldW - 10);
			AnchorPane.setBottomAnchor(pr, 10d);
			pr.setId("channel" + channel.getID());
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
		JJack.getChannels().add(new JJackDefaultInputChannel(0));
		JJack.getChannels().add(new JJackDefaultInputChannel(1));
		JJack.getChannels().add(new JJackDefaultInputChannel(2));
		JJack.getChannels().add(new JJackDefaultOutputChannel(3));
		
		channel0Controller.setChannel((JJackDefaultInputChannel) JJack.getChannel(0));
		channel1Controller.setChannel((JJackDefaultInputChannel) JJack.getChannel(1));
		channel2Controller.setChannel((JJackDefaultInputChannel) JJack.getChannel(2));
		channel3Controller.setChannel((JJackDefaultOutputChannel) JJack.getChannel(3));
	}
}