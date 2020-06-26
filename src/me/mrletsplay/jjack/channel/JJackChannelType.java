package me.mrletsplay.jjack.channel;

import java.util.function.Supplier;

import me.mrletsplay.jjack.JJack;

public enum JJackChannelType {
	
	SINGLE_INPUT(() -> JJack.createSingleInputChannel()),
	SINGLE_OUTPUT(() -> JJack.createSingleOutputChannel()),
	SINGLE_COMBO(() -> JJack.createSingleComboChannel()),
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
