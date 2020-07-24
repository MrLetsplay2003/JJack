package me.mrletsplay.jjack.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import me.mrletsplay.jjack.JJack;

public class JJackPreferencesController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private CheckBox allowOveramplification;

    @FXML
    private CheckBox useProgramPorts;

    @FXML
    void initialize() {
    	allowOveramplification.selectedProperty().addListener((v, o, n) -> JJack.setAllowOveramplification(n));
    	useProgramPorts.selectedProperty().addListener((v, o, n) -> JJack.setUseProgramPorts(n));
    }
}
