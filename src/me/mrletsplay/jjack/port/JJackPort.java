package me.mrletsplay.jjack.port;

import org.jaudiolibs.jnajack.JackPort;

public class JJackPort {
	
	private String name;
	private JackPort jackPort;
	
	public JJackPort(String name, JackPort jackPort) {
		this.name = name;
		this.jackPort = jackPort;
	}

	public String getName() {
		return name;
	}
	
	public JackPort getJackPort() {
		return jackPort;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
