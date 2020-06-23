package me.mrletsplay.jjack.channel;

import java.nio.FloatBuffer;

import org.jaudiolibs.jnajack.JackClient;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import me.mrletsplay.jjack.JJack;
import me.mrletsplay.jjack.port.JJackInputPort;
import me.mrletsplay.jjack.port.JJackOutputPort;
import me.mrletsplay.mrcore.json.JSONObject;

public class JJackComboChannel implements JJackInputChannel, JJackOutputChannel {
	
	private int id;
	private ObjectProperty<JJackInputPort> inputPortProperty;
	private ObjectProperty<JJackOutputPort> outputPortProperty;
	private DoubleProperty volumeProperty;
	private double currentVolume;
	private DoubleProperty currentVolumeProperty;
	
	public JJackComboChannel(int id) {
		this.id = id;
		this.inputPortProperty = new SimpleObjectProperty<>();
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
		return JJackChannelType.COMBO;
	}

	@Override
	public ObjectProperty<JJackInputPort> getInputPortProperty() {
		return inputPortProperty;
	}

	@Override
	public ObjectProperty<JJackOutputPort> getOutputPortProperty() {
		return outputPortProperty;
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
	public FloatBuffer yieldData() {
		return null;
	}
	
	@Override
	public void process(JackClient client, int numFrames) {
		FloatBuffer in = getInputPort().getJackPort().getFloatBuffer();
		FloatBuffer out = FloatBuffer.allocate(numFrames);
		
		out.put(in);
		
		JJack.adjustVolume(out, getVolume() / 100);
		
		double volume = JJack.averageVolume(out);
		
		if(volume < currentVolume) {
			currentVolume = Math.max(volume, currentVolume - .4f);
		}else {
			currentVolume = Math.min(volume, 100);
		}
		
		getOutputPort().writeOutput(out);
		in.rewind();
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
		JSONObject o = JJackInputChannel.super.save();
		o.set("output", getOutputPort() == null ? null : getOutputPort().getName());
		return o;
	}
	
	@Override
	public void load(JSONObject object) {
		JJackInputChannel.super.load(object);
		getOutputPortProperty().set(JJack.getOutputPorts().stream()
				.filter(i -> i.getName().equals(object.getString("output")))
				.findFirst().orElse(null));
	}
	
}
