package me.mrletsplay.jjack.channel;

import javafx.beans.property.DoubleProperty;
import me.mrletsplay.mrcore.json.JSONObject;

public interface JJackChannel {
	
	public int getID();
	
	public JJackChannelType getType();
	
	public DoubleProperty getVolumeProperty();
	
	public default void setVolume(double volume) {
		getVolumeProperty().set(volume);
	}
	
	public default double getVolume() {
		return getVolumeProperty().get();
	}
	
	public DoubleProperty getCurrentVolumeProperty();
	
	public default double getCurrentVolume() {
		return getCurrentVolumeProperty().get();
	}
	
	public void updateUI();
	
	public void update();
	
	public default JSONObject save() {
		JSONObject obj = new JSONObject();
		obj.set("volume", getVolume());
		return obj;
	}
	
	public default void load(JSONObject object) {
		getVolumeProperty().set(object.getDouble("volume"));
	}

}
