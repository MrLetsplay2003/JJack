package me.mrletsplay.jjack.port.stereo;

import me.mrletsplay.jjack.port.JJackPort;

public class JJackStereoPort {
	
	private JJackPort left;
	private JJackPort right;
	
	public JJackStereoPort(JJackPort left, JJackPort right) {
		this.left = left;
		this.right = right;
	}

	public JJackPort getLeft() {
		return left;
	}

	public JJackPort getRight() {
		return right;
	}
	
	public String getName() {
		return left.getOriginalClientName();
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
}
