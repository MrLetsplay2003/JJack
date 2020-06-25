package me.mrletsplay.jjack.port.stereo;

import me.mrletsplay.jjack.port.JJackOutputPort;

public class JJackStereoOutputPort extends JJackStereoPort {

	public JJackStereoOutputPort(JJackOutputPort inputPort, JJackOutputPort outputPort) {
		super(inputPort, outputPort);
	}

	@Override
	public JJackOutputPort getLeft() {
		return (JJackOutputPort) super.getLeft();
	}
	
	@Override
	public JJackOutputPort getRight() {
		return (JJackOutputPort) super.getRight();
	}
	
}
