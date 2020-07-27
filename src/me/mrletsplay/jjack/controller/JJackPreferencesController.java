package me.mrletsplay.jjack.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import me.mrletsplay.jjack.JJack;

public class JJackPreferencesController {
	
	public static final File PREFERENCES_FILE = new File("jjack-prefs.yml");

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private CheckBox allowOveramplification;

    @FXML
    private CheckBox useProgramPorts;
    
    @FXML
    private TextField loadConfig;

    @FXML
    void initialize() {
    	JJack.loadPreferences(PREFERENCES_FILE);
    	
    	allowOveramplification.setSelected(JJack.isAllowOveramplification());
    	useProgramPorts.setSelected(JJack.isUseProgramPorts());
    	loadConfig.setText(JJack.getConfigOnStartup() == null ? "" : JJack.getConfigOnStartup());
    }
    
    @FXML
    void loadConfigBrowse(ActionEvent event) {
    	
    }
    
    @FXML
    void loadConfigClear(ActionEvent event) {
    	loadConfig.clear();
    }
    
    @FXML
    void cancel(ActionEvent event) {
    	JJack.preferencesStage.hide();
    }
    
    @FXML
    void apply(ActionEvent event) {
		JJack.setAllowOveramplification(allowOveramplification.isSelected());
		JJack.setUseProgramPorts(useProgramPorts.isSelected());
		JJack.setConfigOnStartup(loadConfig.getText().isBlank() ? null : loadConfig.getText());
		JJack.savePreferences(PREFERENCES_FILE);
    	JJack.preferencesStage.hide();
    }
    
}
