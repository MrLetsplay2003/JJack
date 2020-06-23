package me.mrletsplay.jjack.channel;

import java.nio.FloatBuffer;

public interface JJackInputChannel extends JJackChannel {
	
	public FloatBuffer yieldData();
	
}
