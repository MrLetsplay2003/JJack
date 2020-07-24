package me.mrletsplay.jjack.channel;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.stream.Collectors;

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
import me.mrletsplay.jjack.port.JJackInputPort;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class JJackSingleInputChannel implements JJackInputChannel {

	private IntegerProperty idProperty;
	private ObjectProperty<JJackInputPort> inputPortProperty;
	private ListProperty<JJackSingleOutputChannel> outputsProperty;
	private DoubleProperty volumeProperty;
	private DoubleProperty maxVolumeProperty;
	private double currentVolume;
	private DoubleProperty currentVolumeProperty;
	
	public JJackSingleInputChannel(int id) {
		this.idProperty = new SimpleIntegerProperty(id);
		this.inputPortProperty = new SimpleObjectProperty<>();
		this.outputsProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
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
		return JJackChannelType.SINGLE_INPUT;
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
	
	public ListProperty<JJackSingleOutputChannel> getOutputsProperty() {
		return outputsProperty;
	}
	
	public void setOutputs(List<JJackSingleOutputChannel> outputs) {
		outputsProperty.setAll(outputs);
	}
	
	public List<JJackSingleOutputChannel> getOutputs() {
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
	public DoubleProperty getMaxVolumeProperty() {
		return maxVolumeProperty;
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
		if(getInputPort() != null && getInputPort().isClosed()) setInputPort(null);
		
		if(getInputPort() == null) return;

		FloatBuffer oIn = getInputPort().getJackPort().getFloatBuffer().duplicate();
		FloatBuffer in = FloatBuffer.allocate(oIn.remaining());
		in.put(oIn);
		oIn.rewind();
		
		JJack.adjustVolume(in, getVolume() / 100);
		
		double volume = JJack.averageVolume(in);
		
		volume = volume == 0 ? 0 : Math.max(0, (0.4 * Math.log10(volume) + 1) * 100);
		
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
		o.set("outputs", new JSONArray(getOutputs().stream().map(out -> out.getID()).collect(Collectors.toList())));
		return o;
	}
	
	@Override
	public void load(JSONObject object) {
		JJackInputChannel.super.load(object);
		getInputPortProperty().set(JJack.getInputPorts().stream()
				.filter(i -> i.getName().equals(object.getString("input")))
				.findFirst().orElse(null));
		
		object.getJSONArray("outputs").stream()
			.map(o -> ((Long) o).intValue())
			.forEach(o -> getOutputs().add((JJackSingleOutputChannel) JJack.getChannel(o)));
	}

}
