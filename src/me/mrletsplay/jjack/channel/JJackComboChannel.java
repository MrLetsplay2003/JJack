package me.mrletsplay.jjack.channel;

import org.jaudiolibs.jnajack.JackClient;

public interface JJackComboChannel extends JJackOutputChannel {
	
	public void process(JackClient client, int numFrames);
	
}
