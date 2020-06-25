package me.mrletsplay.jjack.channel;

import java.util.function.Supplier;

import me.mrletsplay.jjack.JJack;

public enum JJackChannelType {
	
	INPUT(() -> JJack.createDefaultInputChannel()),
	OUTPUT(() -> JJack.createDefaultOutputChannel()),
	COMBO(() -> JJack.createComboChannel()),
	STEREO_INPUT(() -> JJack.createStereoInputChannel()),
	STEREO_OUTPUT(() -> JJack.createStereoOutputChannel()),;
	
	private Supplier<JJackChannel> channelSupplier;
	
	private JJackChannelType(Supplier<JJackChannel> channelSupplier) {
		this.channelSupplier = channelSupplier;
	}
	
	public JJackChannel createChannel() {
		return channelSupplier.get();
	}
	
}
