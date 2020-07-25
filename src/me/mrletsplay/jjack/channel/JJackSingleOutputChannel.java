package me.mrletsplay.jjack.channel;

import java.nio.FloatBuffer;

import org.jaudiolibs.jnajack.JackClient;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import me.mrletsplay.jjack.JJack;
import me.mrletsplay.jjack.port.JJackOutputPort;
import me.mrletsplay.mrcore.json.JSONObject;

public class JJackSingleOutputChannel implements JJackOutputChannel {

	private IntegerProperty idProperty;
	private ObjectProperty<JJackOutputPort> outputPortProperty;
	private DoubleProperty volumeProperty;
	private DoubleProperty maxVolumeProperty;
	private double currentVolume;
	private DoubleProperty currentVolumeProperty;
	
	public JJackSingleOutputChannel(int id) {
		this.idProperty = new SimpleIntegerProperty(id);
		this.outputPortProperty = new SimpleObjectProperty<>();
		this.volumeProperty = new SimpleDoubleProperty(100);
		this.maxVolumeProperty = new SimpleDoubleProperty();
		this.currentVolumeProperty = new SimpleDoubleProperty();
	}
	
	@Override
	public IntegerProperty getIDProperty() {
		return idProperty;
	}
	
	@Override
	public JJackChannelType getType() {
		return JJackChannelType.SINGLE_OUTPUT;
	}

	public ObjectProperty<JJackOutputPort> getOutputPortProperty() {
		return outputPortProperty;
	}
	
	public void setOutputPort(JJackOutputPort outputPort) {
		outputPortProperty.set(outputPort);
	}
	
	public JJackOutputPort getOutputPort() {
		return outputPortProperty.get();
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
	public DoubleProperty getMaxVolumeProperty() {
		return maxVolumeProperty;
	}
	
	@Override
	public DoubleProperty getCurrentVolumeProperty() {
		return currentVolumeProperty;
	}
	
	@Override
	public void process(JackClient client, int numFrames) {
		if(getOutputPort() != null && getOutputPort().isClosed()) setOutputPort(null);
		
		if(getOutputPort() == null) return;
		
		FloatBuffer out = FloatBuffer.allocate(numFrames);
		
		for(JJackSingleInputChannel inC : JJack.getChannelsOfType(JJackSingleInputChannel.class)) {
			if(inC.getInputPort() == null || !inC.getOutputs().contains(this)) continue;

			FloatBuffer in = inC.yieldData();
			JJack.combine(out, in, false);
			in.rewind();
		}
		
		JJack.adjustVolume(out, getVolume() / 100);
		
		double volume = JJack.averageVolume(out);
		
		volume = volume == 0 ? 0 : Math.max(0, (0.4 * Math.log10(volume) + 1) * 100);
		
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
	public JSONObject save() {
		JSONObject o = JJackOutputChannel.super.save();
		o.set("output", getOutputPort() == null ? null : getOutputPort().getName());
		return o;
	}
	
	@Override
	public void load(JSONObject object) {
		JJackOutputChannel.super.load(object);
		
		synchronized (JJack.getOutputPorts()) {
			getOutputPortProperty().set(JJack.getOutputPorts().stream()
					.filter(i -> i.getName().equals(object.getString("output")))
					.findFirst().orElse(null));
		}
	}

}
