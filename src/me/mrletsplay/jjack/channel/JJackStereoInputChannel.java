package me.mrletsplay.jjack.channel;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import me.mrletsplay.jjack.JJack;
import me.mrletsplay.jjack.port.stereo.JJackStereoInputPort;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class JJackStereoInputChannel implements JJackInputChannel {

	private IntegerProperty idProperty;
	private ObjectProperty<JJackStereoInputPort> inputPortProperty;
	private ListProperty<JJackStereoOutputChannel> outputsProperty;
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
	
	public JJackStereoInputChannel(int id) {
		this.idProperty = new SimpleIntegerProperty(id);
		this.inputPortProperty = new SimpleObjectProperty<>();
		this.outputsProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
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
		return JJackChannelType.STEREO_INPUT;
	}

	public ObjectProperty<JJackStereoInputPort> getInputPortProperty() {
		return inputPortProperty;
	}
	
	public void setInputPort(JJackStereoInputPort inputPort) {
		Platform.runLater(() -> inputPortProperty.set(inputPort));
	}
	
	public JJackStereoInputPort getInputPort() {
		return inputPortProperty.get();
	}
	
	public ListProperty<JJackStereoOutputChannel> getOutputsProperty() {
		return outputsProperty;
	}
	
	public void setOutputs(List<JJackStereoOutputChannel> outputs) {
		Platform.runLater(() -> outputsProperty.setAll(outputs));
	}
	
	public List<JJackStereoOutputChannel> getOutputs() {
		return outputsProperty.get();
	}

	@Override
	public void setVolume(double volume) {
		Platform.runLater(() -> volumeProperty.set(volume * 100));
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
	public void updateUI() {
		currentVolumeProperty.set(currentVolume);
		currentLeftVolumeProperty.set(currentLeftVolume);
		currentRightVolumeProperty.set(currentRightVolume);
	}
	
	public FloatBuffer yieldLeft() {
		FloatBuffer in = getInputPort().getLeft().getJackPort().getFloatBuffer();
		JJack.adjustVolume(in, getVolume() / 100);
		return in;
	}
	
	public FloatBuffer yieldRight() {
		FloatBuffer in = getInputPort().getRight().getJackPort().getFloatBuffer();
		JJack.adjustVolume(in, getVolume() / 100);
		return in;
	}
	
	@Override
	public void update() {
		if(getInputPort() != null && getInputPort().isClosed()) setInputPort(null);
		
		if(getInputPort() == null) return;
		
		FloatBuffer lIn = getInputPort().getLeft().getJackPort().getFloatBuffer();
		FloatBuffer leftIn = FloatBuffer.allocate(lIn.remaining());
		leftIn.put(lIn);
		lIn.rewind();
		
		JJack.adjustVolume(leftIn, getVolume() / 100);
		
		double leftVolume = JJack.averageVolume(leftIn);
		
		leftVolume = leftVolume == 0 ? 0 : Math.max(0, (0.4 * Math.log10(leftVolume) + 1) * 100);
		
		if(leftVolume < currentLeftVolume) {
			currentLeftVolume = Math.max(leftVolume, currentLeftVolume - .4f);
		}else {
			currentLeftVolume = Math.min(leftVolume, 100);
		}
		
		FloatBuffer rIn = getInputPort().getRight().getJackPort().getFloatBuffer();
		FloatBuffer rightIn = FloatBuffer.allocate(rIn.remaining());
		rightIn.put(rIn);
		rIn.rewind();
		
		JJack.adjustVolume(rightIn, getVolume() / 100);
		
		double rightVolume = JJack.averageVolume(rightIn);
		
		rightVolume = rightVolume == 0 ? 0 : Math.max(0, (0.4 * Math.log10(rightVolume) + 1) * 100);
		
		if(rightVolume < currentRightVolume) {
			currentRightVolume = Math.max(rightVolume, currentRightVolume - .4f);
		}else {
			currentRightVolume = Math.min(rightVolume, 100);
		}
		
		currentVolume = (currentLeftVolume + currentRightVolume) / 2;
	}
	
	@Override
	public JSONObject save() {
		JSONObject o = JJackInputChannel.super.save();
		o.set("input", getInputPort() == null ? null : getInputPort().getName());
		o.set("outputs", new JSONArray(getOutputs().stream().map(out -> out.getID()).collect(Collectors.toList())));
		return o;
	}
	
	@Override
	public void load(JSONObject object) {
		JJackInputChannel.super.load(object);
		
		synchronized (JJack.getStereoInputPorts()) {
			getInputPortProperty().set(JJack.getStereoInputPorts().stream()
					.filter(i -> i.getName().equals(object.getString("input")))
					.findFirst().orElse(null));
		}
		
		object.getJSONArray("outputs").stream()
			.map(o -> ((Long) o).intValue())
			.forEach(o -> getOutputs().add((JJackStereoOutputChannel) JJack.getChannel(o)));
	}

}
