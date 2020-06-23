package me.mrletsplay.jjack.channel;

import org.jaudiolibs.jnajack.JackClient;

import javafx.beans.property.ObjectProperty;
import me.mrletsplay.jjack.JJack;
import me.mrletsplay.jjack.port.JJackOutputPort;
import me.mrletsplay.mrcore.json.JSONObject;

public interface JJackOutputChannel extends JJackChannel {
	
	public ObjectProperty<JJackOutputPort> getOutputPortProperty();
	
	public default void setOutputPort(JJackOutputPort outputPort) {
		getOutputPortProperty().set(outputPort);
	}
	
	public default JJackOutputPort getOutputPort() {
		return getOutputPortProperty().get();
	}
	
	public void process(JackClient client, int numFrames);
	
	@Override
	public default JSONObject save() {
		JSONObject o = JJackChannel.super.save();
		o.set("output", getOutputPort() == null ? null : getOutputPort().getName());
		return o;
	}
	
	@Override
	public default void load(JSONObject object) {
		JJackChannel.super.load(object);
		getOutputPortProperty().set(JJack.getOutputPorts().stream()
				.filter(i -> i.getName().equals(object.getString("output")))
				.findFirst().orElse(null));
	}

}
