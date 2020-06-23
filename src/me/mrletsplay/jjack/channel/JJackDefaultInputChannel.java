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

	@Override
	public ObjectProperty<JJackInputPort> getInputPortProperty() {
		return inputPortProperty;
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
	
	@Override
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
		
		if(volume < currentVolume) {
			currentVolume = Math.max(volume, currentVolume - .4f);
		}else {
			currentVolume = Math.min(volume, 100);
		}
	}

}
