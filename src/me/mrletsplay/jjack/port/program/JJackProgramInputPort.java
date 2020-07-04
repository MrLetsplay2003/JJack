package me.mrletsplay.jjack.port.program;

import me.mrletsplay.jjack.port.JJackInputPort;
import me.mrletsplay.jjack.pulseaudio.PulseAudioSink;
import me.mrletsplay.jjack.pulseaudio.PulseAudioSinkInput;

public class JJackProgramInputPort extends JJackProgramPort {

	private PulseAudioSinkInput pulseSinkInput;
	private PulseAudioSink pulseSink;
	
	public JJackProgramInputPort(String program, PulseAudioSinkInput pulseSinkInput, PulseAudioSink pulseSink, JJackInputPort left, JJackInputPort right) {
		super(program, left, right);
		this.pulseSinkInput = pulseSinkInput;
		this.pulseSink = pulseSink;
	}
	
	public PulseAudioSinkInput getPulseSinkInput() {
		return pulseSinkInput;
	}
	
	public PulseAudioSink getPulseSink() {
		return pulseSink;
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
