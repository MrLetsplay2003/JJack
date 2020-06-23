package me.mrletsplay.jjack.channel;

import java.nio.FloatBuffer;

import javafx.beans.property.ObjectProperty;
import me.mrletsplay.jjack.JJack;
import me.mrletsplay.jjack.port.JJackInputPort;
import me.mrletsplay.mrcore.json.JSONObject;

public interface JJackInputChannel extends JJackChannel {
	
	public ObjectProperty<JJackInputPort> getInputPortProperty();
	
	public default void setInputPort(JJackInputPort inputPort) {
		getInputPortProperty().set(inputPort);
	}
	
	public default JJackInputPort getInputPort() {
		return getInputPortProperty().get();
	}
	
	public FloatBuffer yieldData();
	
	@Override
	public default JSONObject save() {
		JSONObject o = JJackChannel.super.save();
		o.set("input", getInputPort() == null ? null : getInputPort().getName());
		return o;
	}
	
	@Override
	public default void load(JSONObject object) {
		JJackChannel.super.load(object);
		getInputPortProperty().set(JJack.getInputPorts().stream()
				.filter(i -> i.getName().equals(object.getString("input")))
				.findFirst().orElse(null));
	}
	
}
