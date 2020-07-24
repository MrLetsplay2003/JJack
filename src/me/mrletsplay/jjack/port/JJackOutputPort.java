package me.mrletsplay.jjack.port;

import java.nio.FloatBuffer;

import org.jaudiolibs.jnajack.JackPort;

import me.mrletsplay.jjack.JJack;

public class JJackOutputPort extends JJackPort {
	
	private FloatBuffer output;
	
	public JJackOutputPort(String name, JackPort jackPort) {
		super(name, jackPort);
	}
	
	public void initOutput(int numFrames) {
		output = FloatBuffer.allocate(numFrames);
	}
	
	public void writeOutput(FloatBuffer output) {
		if(this.output == null) throw new IllegalStateException("writeOutput must be called from the process callback");
		JJack.combine(this.output, output, false);
		this.output.rewind();
	}
	
	public void flushOutput() {
		getJackPort().getFloatBuffer().put(output);
		output = null;
	}
	
}
