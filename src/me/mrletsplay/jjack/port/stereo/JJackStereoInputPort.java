package me.mrletsplay.jjack.port.stereo;

import me.mrletsplay.jjack.port.JJackInputPort;

public class JJackStereoInputPort extends JJackStereoPort {

	public JJackStereoInputPort(JJackInputPort left, JJackInputPort right) {
		super(left, right);
	}

	@Override
	public JJackInputPort getLeft() {
		return (JJackInputPort) super.getLeft();
	}
	
	@Override
	public JJackInputPort getRight() {
		return (JJackInputPort) super.getRight();
	}
	
}
