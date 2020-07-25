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
import me.mrletsplay.jjack.port.JJackInputPort;
import me.mrletsplay.jjack.port.JJackOutputPort;
import me.mrletsplay.mrcore.json.JSONObject;

public class JJackSingleComboChannel implements JJackComboChannel {
	
	private IntegerProperty idProperty;
	private ObjectProperty<JJackInputPort> inputPortProperty;
	private ObjectProperty<JJackOutputPort> outputPortProperty;
	private DoubleProperty volumeProperty;
	private DoubleProperty maxVolumeProperty;
	private double currentVolume;
	private DoubleProperty currentVolumeProperty;
	
	public JJackSingleComboChannel(int id) {
		this.idProperty = new SimpleIntegerProperty(id);
		this.inputPortProperty = new SimpleObjectProperty<>();
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
		return JJackChannelType.SINGLE_COMBO;
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
		if(getInputPort() != null && getInputPort().isClosed()) setInputPort(null);
		if(getOutputPort() != null && getOutputPort().isClosed()) setOutputPort(null);
		
		if(getInputPort() == null || getOutputPort() == null) return;
		
		FloatBuffer in = getInputPort().getJackPort().getFloatBuffer();
		FloatBuffer out = FloatBuffer.allocate(numFrames);
		
		out.put(in);
		
		JJack.adjustVolume(out, getVolume() / 100);
		
		double volume = JJack.averageVolume(out);
		
		volume = volume == 0 ? 0 : Math.max(0, (0.3 * Math.log(volume) + 1) * 100);
		
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
		JSONObject o = JJackComboChannel.super.save();
		o.set("input", getInputPort() == null ? null : getInputPort().getName());
		o.set("output", getOutputPort() == null ? null : getOutputPort().getName());
		return o;
	}
	
	@Override
	public void load(JSONObject object) {
		JJackComboChannel.super.load(object);
		
		synchronized (JJack.getInputPorts()) {
			getInputPortProperty().set(JJack.getInputPorts().stream()
					.filter(i -> i.getName().equals(object.getString("input")))
					.findFirst().orElse(null));
		}
		
		synchronized (JJack.getOutputPorts()) {
			getOutputPortProperty().set(JJack.getOutputPorts().stream()
					.filter(i -> i.getName().equals(object.getString("output")))
					.findFirst().orElse(null));
		}
	}
	
}
