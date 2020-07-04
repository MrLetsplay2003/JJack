package me.mrletsplay.jjack.port.program;

import me.mrletsplay.jjack.port.JJackPort;

public class JJackProgramPort {
	
	private String program;
	private JJackPort left;
	private JJackPort right;
	
	public JJackProgramPort(String program, JJackPort left, JJackPort right) {
		this.program = program;
		this.left = left;
		this.right = right;
	}
	
	public String getProgram() {
		return program;
	}
	
	public JJackPort getLeft() {
		return left;
	}

	public JJackPort getRight() {
		return right;
	}
	
	public String getName() {
		return getProgram();
	}
	
	public boolean isClosed() {
		return left.isClosed() || right.isClosed();
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
}
