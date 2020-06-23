package me.mrletsplay.jjack.pulseaudio;

import java.util.HashMap;
import java.util.Map;

public class PulseAudioSinkInput {

	private int index;
	private Map<String, String> properties;
	
	public PulseAudioSinkInput(int index) {
		this.index = index;
		this.properties = new HashMap<>();
	}
	
	public int getIndex() {
		return index;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
	public String getProperty(String key) {
		return properties.get(key);
	}
	
	public String getName() {
		return getProperty("media.name") != null ? getProperty("media.name") : getProperty("application.process.binary");
	}
	
}
