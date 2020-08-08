package me.mrletsplay.jjack.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
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
    private CheckBox programPortLenient;

    @FXML
    private CheckBox programPortConnectOriginal;

    @FXML
    private CheckBox programPortCheckProcessAlive;

    @FXML
    private CheckBox programPortByPID;
    
    @FXML
    private TextField loadConfig;

    @FXML
    void initialize() {
    }
    
    public void update() {
    	allowOveramplification.setSelected(JJack.isAllowOveramplification());
    	useProgramPorts.setSelected(JJack.isUseProgramPorts());
    	programPortLenient.setSelected(JJack.isProgramPortLenient());
    	programPortConnectOriginal.setSelected(JJack.isProgramPortConnectOriginal());
    	programPortCheckProcessAlive.setSelected(JJack.isProgramPortCheckProcessAlive());
    	programPortByPID.setSelected(JJack.isProgramPortByPID());
    	loadConfig.setText(JJack.getConfigOnStartup() == null ? "" : JJack.getConfigOnStartup());
    }
    
    @FXML
    void loadConfigBrowse(ActionEvent event) {
    	FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File("."));
		chooser.getExtensionFilters().add(new ExtensionFilter("Yaml Configuration file", "*.yml", "*.yaml"));
		File f = chooser.showOpenDialog(JJack.preferencesStage);
		if(f == null) return;
		loadConfig.setText(f.getAbsolutePath());
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
		JJack.setProgramPortLenient(programPortLenient.isSelected());
		JJack.setProgramPortConnectOriginal(programPortConnectOriginal.isSelected());
		JJack.setProgramPortCheckProcessAlive(programPortCheckProcessAlive.isSelected());
		JJack.setProgramPortByPID(programPortByPID.isSelected());
		JJack.setConfigOnStartup(loadConfig.getText().isBlank() ? null : loadConfig.getText());
		JJack.savePreferences(PREFERENCES_FILE);
    	JJack.preferencesStage.hide();
    }
    
}
