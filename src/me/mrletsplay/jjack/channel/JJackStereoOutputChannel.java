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
import me.mrletsplay.jjack.port.stereo.JJackStereoOutputPort;
import me.mrletsplay.mrcore.json.JSONObject;

public class JJackStereoOutputChannel implements JJackOutputChannel {

	private IntegerProperty idProperty;
	private ObjectProperty<JJackStereoOutputPort> outputPortProperty;
	private DoubleProperty volumeProperty;
	private DoubleProperty maxVolumeProperty;
	
	private double
		currentVolume,
		currentLeftVolume,
		currentRightVolume;
	
	private DoubleProperty
		currentVolumeProperty,
		currentLeftVolumeProperty,
		currentRightVolumeProperty;
	
	public JJackStereoOutputChannel(int id) {
		this.idProperty = new SimpleIntegerProperty(id);
		this.outputPortProperty = new SimpleObjectProperty<>();
		this.volumeProperty = new SimpleDoubleProperty(100);
		this.maxVolumeProperty = new SimpleDoubleProperty();
		this.currentVolumeProperty = new SimpleDoubleProperty();
		this.currentLeftVolumeProperty = new SimpleDoubleProperty();
		this.currentRightVolumeProperty = new SimpleDoubleProperty();
	}
	
	@Override
	public IntegerProperty getIDProperty() {
		return idProperty;
	}
	
	@Override
	public JJackChannelType getType() {
		return JJackChannelType.STEREO_OUTPUT;
	}

	public ObjectProperty<JJackStereoOutputPort> getOutputPortProperty() {
		return outputPortProperty;
	}
	
	public void setOutputPort(JJackStereoOutputPort outputPort) {
		outputPortProperty.set(outputPort);
	}
	
	public JJackStereoOutputPort getOutputPort() {
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
	
	public DoubleProperty getCurrentLeftVolumeProperty() {
		return currentLeftVolumeProperty;
	}
	
	public DoubleProperty getCurrentRightVolumeProperty() {
		return currentRightVolumeProperty;
	}
	
	@Override
	public void process(JackClient client, int numFrames) {
		if(getOutputPort() != null && getOutputPort().isClosed()) setOutputPort(null);
		
		if(getOutputPort() == null) return;
		
		FloatBuffer outLeft = FloatBuffer.allocate(numFrames);
		FloatBuffer outRight = FloatBuffer.allocate(numFrames);
		
		for(JJackStereoInputChannel inC : JJack.getChannelsOfType(JJackStereoInputChannel.class)) {
			if(inC.getInputPort() == null || !inC.getOutputs().contains(this)) continue;

			FloatBuffer inLeft = inC.yieldLeft();
			JJack.combine(outLeft, inLeft, false);
			inLeft.rewind();
			
			FloatBuffer inRight = inC.yieldRight();
			JJack.combine(outRight, inRight, false);
			inRight.rewind();
		}
		
		JJack.adjustVolume(outLeft, getVolume() / 100);
		JJack.adjustVolume(outRight, getVolume() / 100);
		
		double leftVolume = JJack.averageVolume(outLeft);
		
		leftVolume = leftVolume == 0 ? 0 : Math.max(0, (0.4 * Math.log10(leftVolume) + 1) * 100);
		
		if(leftVolume < currentLeftVolume) {
			currentLeftVolume = Math.max(leftVolume, currentLeftVolume - .4f);
		}else {
			currentLeftVolume = Math.min(leftVolume, 100);
		}
		
		double rightVolume = JJack.averageVolume(outRight);
		
		rightVolume = rightVolume == 0 ? 0 : Math.max(0, (0.4 * Math.log10(rightVolume) + 1) * 100);
		
		if(rightVolume < currentRightVolume) {
			currentRightVolume = Math.max(rightVolume, currentRightVolume - .4f);
		}else {
			currentRightVolume = Math.min(rightVolume, 100);
		}
		
		currentVolume = (currentLeftVolume + currentRightVolume) / 2;
		
		getOutputPort().getLeft().writeOutput(outLeft);
		getOutputPort().getRight().writeOutput(outRight);
	}
	
	@Override
	public void updateUI() {
		currentVolumeProperty.set(currentVolume);
		currentLeftVolumeProperty.set(currentLeftVolume);
		currentRightVolumeProperty.set(currentRightVolume);
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
		
		synchronized (JJack.getStereoOutputPorts()) {
			getOutputPortProperty().set(JJack.getStereoOutputPorts().stream()
					.filter(i -> i.getName().equals(object.getString("output")))
					.findFirst().orElse(null));
		}
	}

}
