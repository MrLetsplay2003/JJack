package me.mrletsplay.jjack.port;

import java.nio.FloatBuffer;

import org.jaudiolibs.jnajack.JackPort;

import me.mrletsplay.jjack.JJack;

public class JJackOutputPort extends JJackPort {
	
	private FloatBuffer output;
	private boolean average;
	
	public JJackOutputPort(String name, JackPort jackPort) {
		super(name, jackPort);
	}
	
	public void initOutput(int numFrames) {
		output = FloatBuffer.allocate(numFrames);
		average = false;
	}
	
	public void writeOutput(FloatBuffer output) {
		if(this.output == null) throw new IllegalStateException("writeOutput must be called from the process callback");
		JJack.combine(this.output, output, average);
		this.output.rewind();
		average = true;
	}
	
	public void flushOutput() {
		getJackPort().getFloatBuffer().put(output);
		output = null;
	}
	
}
