package me.mrletsplay.jjack.channel;

import org.jaudiolibs.jnajack.JackClient;

public interface JJackOutputChannel extends JJackChannel {
	
	public void process(JackClient client, int numFrames);
	
}
