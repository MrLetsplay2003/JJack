package me.mrletsplay.jjack.port.program;

import me.mrletsplay.jjack.pulseaudio.PulseAudioSink;
import me.mrletsplay.jjack.pulseaudio.PulseAudioSinkInput;

public class JJackInputProgram {

	private String processBinary;
	private PulseAudioSinkInput sinkInput;
	private PulseAudioSink originalSink;
	private PulseAudioSink virtualSink;
	
	public JJackInputProgram(String processBinary, PulseAudioSinkInput sinkInput, PulseAudioSink originalSink, PulseAudioSink virtualSink) {
		this.processBinary = processBinary;
		this.sinkInput = sinkInput;
		this.originalSink = originalSink;
		this.virtualSink = virtualSink;
	}
	
	public String getProcessBinary() {
		return processBinary;
	}
	
	public PulseAudioSinkInput getSinkInput() {
		return sinkInput;
	}
	
	public PulseAudioSink getOriginalSink() {
		return originalSink;
	}
	
	public PulseAudioSink getVirtualSink() {
		return virtualSink;
	}
	
}
