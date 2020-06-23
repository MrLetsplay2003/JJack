package me.mrletsplay.jjack.pulseaudio;

import java.util.HashMap;
import java.util.Map;

public class PulseAudioSink {

	private int index;
	private boolean isDefault;
	private String name;
	private int moduleNumber;
	private Map<String, String> properties;
	
	public PulseAudioSink(int index, boolean isDefault) {
		this.index = index;
		this.isDefault = isDefault;
		this.moduleNumber = -1;
		this.properties = new HashMap<>();
	}
	
	public int getIndex() {
		return index;
	}
	
	public boolean isDefault() {
		return isDefault;
	}
	
	void setModuleNumber(int moduleNumber) {
		this.moduleNumber = moduleNumber;
	}
	
	public int getModuleNumber() {
		return moduleNumber;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
	public String getProperty(String key) {
		return properties.get(key);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getJackName() {
		return getProperty("jack.client_name");
	}
	
}
