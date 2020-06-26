package me.mrletsplay.jjack.channel;

import java.util.function.Function;

import me.mrletsplay.jjack.JJack;

public enum JJackChannelType {
	
	SINGLE_INPUT(id -> JJack.createSingleInputChannel(id)),
	SINGLE_OUTPUT(id -> JJack.createSingleOutputChannel(id)),
	SINGLE_COMBO(id -> JJack.createSingleComboChannel(id)),
	STEREO_INPUT(id -> JJack.createStereoInputChannel(id)),
	STEREO_OUTPUT(id -> JJack.createStereoOutputChannel(id)),;
	
	private Function<Integer, JJackChannel> channelSupplier;
	
	private JJackChannelType(Function<Integer, JJackChannel> channelSupplier) {
		this.channelSupplier = channelSupplier;
	}
	
	public JJackChannel createChannel(int id) {
		return channelSupplier.apply(id);
	}
	
}
