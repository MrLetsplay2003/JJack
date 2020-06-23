package me.mrletsplay.jjack.channel;

import java.nio.FloatBuffer;

import org.jaudiolibs.jnajack.JackClient;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import me.mrletsplay.jjack.JJack;
import me.mrletsplay.jjack.port.JJackOutputPort;

public class JJackDefaultOutputChannel implements JJackOutputChannel {
	
	private int id;
	private ObjectProperty<JJackOutputPort> outputPortProperty;
	private DoubleProperty volumeProperty;
	private double currentVolume;
	private DoubleProperty currentVolumeProperty;
	
	public JJackDefaultOutputChannel(int id) {
		this.id = id;
		this.outputPortProperty = new SimpleObjectProperty<>();
		this.volumeProperty = new SimpleDoubleProperty(100);
		this.currentVolumeProperty = new SimpleDoubleProperty();
	}
	
	@Override
	public int getID() {
		return id;
	}
	
	@Override
	public JJackChannelType getType() {
		return JJackChannelType.OUTPUT;
	}

	@Override
	public ObjectProperty<JJackOutputPort> getOutputPortProperty() {
		return outputPortProperty;
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
	public void process(JackClient client, int numFrames) {
		if(getOutputPort() == null) return;
		
		FloatBuffer out = FloatBuffer.allocate(numFrames);
		
		boolean av = false;
		for(JJackDefaultInputChannel inC : JJack.getDefaultInputChannels()) {
			if(inC.getInputPort() == null || !inC.getOutputs().contains(this)) continue;

			FloatBuffer in = inC.yieldData();
			JJack.combine(out, in, av);
			in.rewind();
			av = true;
		}
		
		JJack.adjustVolume(out, getVolume() / 100);
		
		double volume = JJack.averageVolume(out);
		
		if(volume < currentVolume) {
			currentVolume = Math.max(volume, currentVolume - .4f);
		}else {
			currentVolume = Math.min(volume, 100);
		}
		
		getOutputPort().writeOutput(out);
	}
	
	@Override
	public void updateUI() {
		currentVolumeProperty.set(currentVolume);
	}
	
	@Override
	public void update() {
		
	}
	
	@Override
	public String toString() {  
		return getOutputPort().getName();
	}

}
