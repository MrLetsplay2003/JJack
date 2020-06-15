package me.mrletsplay.jjack.channel;

import java.nio.FloatBuffer;

import org.jaudiolibs.jnajack.JackClient;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import me.mrletsplay.jjack.port.JJackInputPort;
import me.mrletsplay.jjack.port.JJackOutputPort;

public class JJackChannel {
	
	private int id;
	private JJackInputPort inputPort;
	private ObjectProperty<JJackInputPort> inputPortProperty;
	private JJackOutputPort outputPort;
	private ObjectProperty<JJackOutputPort> outputPortProperty;
	private double volume;
	private DoubleProperty volumeProperty;
	private double currentVolume;
	private DoubleProperty currentVolumeProperty;
	
	public JJackChannel(int id) {
		this.id = id;
		this.inputPortProperty = new SimpleObjectProperty<>();
		inputPortProperty.addListener(o -> {
			inputPort = inputPortProperty.get();
		});
		
		this.outputPortProperty = new SimpleObjectProperty<>();
		outputPortProperty.addListener(o -> {
			outputPort = outputPortProperty.get();
		});
		
		this.volume = 1;
		
		this.volumeProperty = new SimpleDoubleProperty(100);
		volumeProperty.addListener(o -> {
			volume = volumeProperty.get() / 100;
		});
		
		this.currentVolume = 0;
		this.currentVolumeProperty = new SimpleDoubleProperty();
	}
	
	public int getID() {
		return id;
	}
	
	public void setInputPort(JJackInputPort inputPort) {
		this.inputPort = inputPort;
		inputPortProperty.set(inputPort);
	}
	
	public JJackInputPort getInputPort() {
		return inputPort;
	}
	
	public ObjectProperty<JJackInputPort> getInputPortProperty() {
		return inputPortProperty;
	}
	
	public void setOutputPort(JJackOutputPort outputPort) {
		this.outputPort = outputPort;
		outputPortProperty.set(outputPort);
	}
	
	public JJackOutputPort getOutputPort() {
		return outputPort;
	}
	
	public ObjectProperty<JJackOutputPort> getOutputPortProperty() {
		return outputPortProperty;
	}
	
	public void setVolume(double volume) {
		this.volume = volume;
		volumeProperty.set(volume * 100);
	}
	
	public double getVolume() {
		return volume;
	}
	
	public DoubleProperty getVolumeProperty() {
		return volumeProperty;
	}
	
	public double getCurrentVolume() {
		return currentVolume;
	}
	
	public DoubleProperty getCurrentVolumeProperty() {
		return currentVolumeProperty;
	}
	
	public void process(JackClient client, FloatBuffer in, FloatBuffer out, int nframes) {
		if(inputPort == null || outputPort == null) return;
//		FloatBuffer in = inputPort.getJackPort().getFloatBuffer().duplicate();
//		FloatBuffer out = outputPort.getJackPort().getFloatBuffer();
		
		float[] floats = new float[in.remaining()];
		in.get(floats);
		
		for(int i = 0; i < floats.length; i++) floats[i] = (float) (floats[i] * Math.pow(volume, 2));
		
		double volume = 0;
		for(float f : floats) volume += Math.abs(f);
		volume /= floats.length;
		
		double fVolume = volume * 200;
		
		if(fVolume < currentVolume) {
			currentVolume = Math.max(fVolume, currentVolume - .4f);
		}else {
			currentVolume = Math.min(fVolume, 100);
		}
		
		out.put(floats);
	}
	
	public void updateUI() {
		currentVolumeProperty.set(currentVolume);
	}

}
