package me.mrletsplay.jjack;

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
import me.mrletsplay.jjack.channel.JJackChannel;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class JJackController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private AnchorPane mainPane;
	
	@FXML
	private JJackChannelController channel0Controller;
	
	@FXML
	private JJackChannelController channel1Controller;
	
	@FXML
	private JJackChannelController channel2Controller;
	
	@FXML
	private JJackChannelController channel3Controller;

	@FXML
	void about(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION, "Made by MrLetsplay", ButtonType.OK);
		alert.setTitle("About");
		alert.setHeaderText("JJack");
		alert.initOwner(JJack.getStage());
		alert.show();
	}

    @FXML
    void addNewChannel(ActionEvent event) {
    	addNewChannel();
    }
    
    public JJackChannel addNewChannel() {
    	double oldW = JJack.getStage().getWidth();
    	JJack.getStage().setWidth(oldW + 175);
    	
    	try {
			JJackChannel ch = JJack.createChannel();
	    	URL url = JJack.class.getResource("/include/channel.fxml");
			if(url == null) url = new File("./include/channel.fxml").toURI().toURL();
			FXMLLoader l = new FXMLLoader(url);
			Parent pr = l.load(url.openStream());
			JJackChannelController ctrl = l.getController();
			ctrl.setChannel(ch);
			mainPane.getChildren().add(pr);
			AnchorPane.setTopAnchor(pr, 10d);
			AnchorPane.setLeftAnchor(pr, oldW - 10);
			AnchorPane.setBottomAnchor(pr, 10d);
			pr.setId("channel" + ch.getID());
			return ch;
    	}catch(Exception e) {
    		throw new FriendlyException(e);
    	}
    }
    
    public void removeChannel(int channelID) {
    	double oldW = JJack.getStage().getWidth();
    	JJack.getStage().setWidth(oldW - 175);
    	
    	JJack.removeChannel(channelID);
    	
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
    	for(Node n : mainPane.lookupAll(".channel")) {
    		AnchorPane a = (AnchorPane) n;
    		if(!IntStream.range(0, JJack.DEFAULT_CHANNEL_COUNT).anyMatch(i -> a.getId().equals("channel" + i))) {
    			mainPane.getChildren().remove(a);
    			continue;
    		}
    	}
    }

	@FXML
	void loadConfiguration(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File("."));
		chooser.getExtensionFilters().add(new ExtensionFilter("Yaml Configuration file", "*.yml", "*.yaml"));
		File f = chooser.showOpenDialog(JJack.getStage());
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
	}

	@FXML
	void preferences(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION, "Coming soon", ButtonType.OK);
		alert.setTitle("Preferences");
		alert.setHeaderText("JJack");
		alert.initOwner(JJack.getStage());
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
		File f = chooser.showSaveDialog(JJack.getStage());
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
		channel0Controller.setChannel(JJack.getChannel(0));
		channel1Controller.setChannel(JJack.getChannel(1));
		channel2Controller.setChannel(JJack.getChannel(2));
		channel3Controller.setChannel(JJack.getChannel(3));
	}
}
