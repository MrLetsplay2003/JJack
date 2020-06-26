package me.mrletsplay.jjack.port;

import org.jaudiolibs.jnajack.JackPort;

public class JJackPort {
	
	private String name;
	private JackPort jackPort;
	private boolean closed;
	
	public JJackPort(String name, JackPort jackPort) {
		this.name = name;
		this.jackPort = jackPort;
	}
	
	public String getOriginalClientName() {
		return name.split(":")[0];
	}

	public String getName() {
		return name;
	}
	
	public JackPort getJackPort() {
		return jackPort;
	}
	
	public void close() {
		closed = true;
	}
	
	public boolean isClosed() {
		return closed;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
