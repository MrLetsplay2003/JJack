package me.mrletsplay.jjack.pulseaudio;

import java.util.HashMap;
import java.util.Map;

public class PulseAudioSinkInput {

	private int index;
	private int sink;
	private Map<String, String> properties;
	
	public PulseAudioSinkInput(int index) {
		this.index = index;
		this.properties = new HashMap<>();
	}
	
	public int getIndex() {
		return index;
	}
	
	void setSink(int sink) {
		this.sink = sink;
	}
	
	public int getSink() {
		return sink;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
	public String getProperty(String key) {
		return properties.get(key);
	}
	
	public String getProcessBinary() {
		return getProperty("application.process.binary");
	}
	
	public String getName() {
		return getProperty("media.name") != null ? getProperty("media.name") + (getProperty("application.name") != null ? " (" + getProperty("application.name") + ")" : ""): getProcessBinary();
	}
	
}
