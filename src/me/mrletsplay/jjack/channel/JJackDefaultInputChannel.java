package me.mrletsplay.jjack.channel;

import java.nio.FloatBuffer;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import me.mrletsplay.jjack.JJack;
import me.mrletsplay.jjack.port.JJackInputPort;
import me.mrletsplay.mrcore.json.JSONObject;

public class JJackDefaultInputChannel implements JJackInputChannel {
	
	private int id;
	private ObjectProperty<JJackInputPort> inputPortProperty;
	private ListProperty<JJackDefaultOutputChannel> outputsProperty;
	private DoubleProperty volumeProperty;
	private double currentVolume;
	private DoubleProperty currentVolumeProperty;
	
	public JJackDefaultInputChannel(int id) {
		this.id = id;
		this.inputPortProperty = new SimpleObjectProperty<>();
		this.outputsProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
		this.volumeProperty = new SimpleDoubleProperty(100);
		this.currentVolumeProperty = new SimpleDoubleProperty();
	}
	
	@Override
	public int getID() {
		return id;
	}
	
	@Override
	public JJackChannelType getType() {
		return JJackChannelType.INPUT;
	}

	public ObjectProperty<JJackInputPort> getInputPortProperty() {
		return inputPortProperty;
	}
	
	public void setInputPort(JJackInputPort inputPort) {
		inputPortProperty.set(inputPort);
	}
	
	public JJackInputPort getInputPort() {
		return inputPortProperty.get();
	}
	
	public ListProperty<JJackDefaultOutputChannel> getOutputsProperty() {
		return outputsProperty;
	}
	
	public void setOutputs(List<JJackDefaultOutputChannel> outputs) {
		outputsProperty.setAll(outputs);
	}
	
	public List<JJackDefaultOutputChannel> getOutputs() {
		return outputsProperty.get();
	}

	@Override
	public void setVolume(double volume) {
		volumeProperty.set(volume * 100);
	}

	@Override
	public DoubleProperty getVolumeProperty() {
		return volumeProperty;
	}
	
	@Override
	public DoubleProperty getCurrentVolumeProperty() {
		return currentVolumeProperty;
	}
	
	@Override
	public void updateUI() {
		currentVolumeProperty.set(currentVolume);
	}
	
	public FloatBuffer yieldData() {
		FloatBuffer in = getInputPort().getJackPort().getFloatBuffer();
		JJack.adjustVolume(in, getVolume() / 100);
		return in;
	}
	
	@Override
	public void update() {
		if(getInputPort() == null) return;
		
		FloatBuffer in = getInputPort().getJackPort().getFloatBuffer();
		
		double volume = JJack.averageVolume(in);
		
		volume = volume == 0 ? 0 : Math.max(0, (0.3 * Math.log(volume) + 1) * 100);
		
		if(volume < currentVolume) {
			currentVolume = Math.max(volume, currentVolume - .4f);
		}else {
			currentVolume = Math.min(volume, 100);
		}
	}
	
	@Override
	public JSONObject save() {
		JSONObject o = JJackInputChannel.super.save();
		o.set("input", getInputPort() == null ? null : getInputPort().getName());
		return o;
	}
	
	@Override
	public void load(JSONObject object) {
		JJackInputChannel.super.load(object);
		getInputPortProperty().set(JJack.getInputPorts().stream()
				.filter(i -> i.getName().equals(object.getString("input")))
				.findFirst().orElse(null));
	}

}
