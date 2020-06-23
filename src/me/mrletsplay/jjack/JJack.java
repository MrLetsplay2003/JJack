package me.mrletsplay.jjack;

import java.io.File;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.jaudiolibs.jnajack.Jack;
import org.jaudiolibs.jnajack.JackClient;
import org.jaudiolibs.jnajack.JackOptions;
import org.jaudiolibs.jnajack.JackPort;
import org.jaudiolibs.jnajack.JackPortFlags;
import org.jaudiolibs.jnajack.JackPortType;
import org.jaudiolibs.jnajack.JackStatus;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import me.mrletsplay.jjack.channel.JJackChannel;
import me.mrletsplay.jjack.channel.JJackChannelType;
import me.mrletsplay.jjack.channel.JJackComboChannel;
import me.mrletsplay.jjack.channel.JJackDefaultComboChannel;
import me.mrletsplay.jjack.channel.JJackDefaultInputChannel;
import me.mrletsplay.jjack.channel.JJackDefaultOutputChannel;
import me.mrletsplay.jjack.channel.JJackInputChannel;
import me.mrletsplay.jjack.channel.JJackOutputChannel;
import me.mrletsplay.jjack.controller.JJackController;
import me.mrletsplay.jjack.port.JJackInputPort;
import me.mrletsplay.jjack.port.JJackOutputPort;
import me.mrletsplay.mrcore.config.ConfigLoader;
import me.mrletsplay.mrcore.config.FileCustomConfig;
import me.mrletsplay.mrcore.config.impl.DefaultFileCustomConfig;
import me.mrletsplay.mrcore.json.JSONObject;

public class JJack extends Application {
	
	public static final int DEFAULT_CHANNEL_COUNT = 4;
	
	public static Stage stage;
	
	private static JackClient client;
	private static JJackController controller;
	private static ObservableList<JJackInputPort> inputPorts;
	private static ObservableList<JJackOutputPort> outputPorts;
	private static List<JJackChannel> channels;
	
	@SuppressWarnings("deprecation")
	@Override
	public void start(Stage primaryStage) throws Exception {
		inputPorts = FXCollections.observableArrayList();
		outputPorts = FXCollections.observableArrayList();
		channels = new ArrayList<>();
		
		stage = primaryStage;
		
		URL iconURL = JJack.class.getResource("/include/icon.png");
		if(iconURL == null) iconURL = new File("./include/icon.png").toURI().toURL();

		stage.getIcons().add(new Image(iconURL.openStream()));
		
		URL url = JJack.class.getResource("/include/ui.fxml");
		if(url == null) url = new File("./include/ui.fxml").toURI().toURL();
		
		FXMLLoader l = new FXMLLoader(url);
		Parent pr = l.load(url.openStream());
		controller = l.getController();
		
		Scene sc = new Scene(pr);
		primaryStage.setOnCloseRequest(event -> exit());
		primaryStage.setTitle("JJack");
		primaryStage.setResizable(false);
		primaryStage.setScene(sc);
		primaryStage.show();
		
		client = Jack.getInstance().openClient("JJack", EnumSet.noneOf(JackOptions.class), EnumSet.noneOf(JackStatus.class));
		
		client.setProcessCallback((client, nframes) -> {
			getOutputPorts().forEach(p -> {
				p.initOutput(nframes);
			});
			
			for(JJackOutputChannel channel : getOutputChannels()) {
				channel.process(client, nframes);
			}
			
			getOutputPorts().forEach(p -> p.flushOutput());
			
			for(JJackChannel ch : channels) {
				ch.update();
			}
			return true;
		});
		
		client.activate();
		
		String[] outputPorts = Jack.getInstance().getPorts(null, JackPortType.AUDIO, EnumSet.of(JackPortFlags.JackPortIsOutput));
		for(String port : outputPorts) {
			String portClient = port.split(":")[0];
			if(portClient.equals(client.getName())) continue;
			JackPort p = client.registerPort(port, JackPortType.AUDIO, JackPortFlags.JackPortIsInput);
			Jack.getInstance().connect(client, port, p.getName());
			JJack.inputPorts.add(new JJackInputPort(p.getShortName(), p));
		}

		String[] inputPorts = Jack.getInstance().getPorts(null, JackPortType.AUDIO, EnumSet.of(JackPortFlags.JackPortIsInput));
		for(String port : inputPorts) {
			String portClient = port.split(":")[0];
			if(portClient.equals(client.getName())) continue;
			JackPort p = client.registerPort(port, JackPortType.AUDIO, JackPortFlags.JackPortIsOutput);
			Jack.getInstance().connect(client, p.getName(), port);
			JJack.outputPorts.add(new JJackOutputPort(p.getShortName(), p));
		}
		
		new AnimationTimer() {
			
			@Override
			public void handle(long now) {
				for(JJackChannel ch : new ArrayList<>(channels)) ch.updateUI();
			}
			
		}.start();
	}
	
	public static void combine(FloatBuffer current, FloatBuffer additional, boolean average) {
		int numFloats = current.rewind().remaining();
		
		float[] oldFloats = new float[numFloats];
		current.get(oldFloats);
		
		float[] newFloats = new float[numFloats];
		additional.get(newFloats);
		
		for(int i = 0; i < numFloats; i++) {
			newFloats[i] = (oldFloats[i] + newFloats[i]) / (average ? 2 : 1);
		}
		
		current.rewind().put(newFloats);
	}
	
	public static float averageVolume(FloatBuffer buffer) {
		float[] floats = new float[buffer.rewind().remaining()];
		buffer.get(floats);
		
		float volume = 0;
		for(float f : floats) volume += Math.abs(f);
		volume /= floats.length;
		
		buffer.rewind();
		return volume;
	}
	
	public static void adjustVolume(FloatBuffer buffer, double volume) {
		float[] floats = new float[buffer.rewind().remaining()];
		buffer.get(floats);
		
		for(int i = 0; i < floats.length; i++) floats[i] = (float) (floats[i] * Math.pow(volume, 2));
		
		buffer.rewind().put(floats).rewind();
	}
	
	public static void exit() {
		client.close();
		System.exit(0);
	}
	
	public static JackClient getClient() {
		return client;
	}
	
	public static ObservableList<JJackInputPort> getInputPorts() {
		return inputPorts;
	}
	
	public static synchronized ObservableList<JJackOutputPort> getOutputPorts() {
		return outputPorts;
	}
	
	public static List<JJackChannel> getChannels() {
		return channels;
	}
	
	public static List<JJackInputChannel> getInputChannels() {
		return getChannelsOfType(JJackInputChannel.class);
	}
	
	public static List<JJackOutputChannel> getOutputChannels() {
		return getChannelsOfType(JJackOutputChannel.class);
	}
	
	public static List<JJackComboChannel> getComboChannels() {
		return getChannelsOfType(JJackComboChannel.class);
	}
	
	public static <T extends JJackChannel> List<T> getChannelsOfType(Class<T> type) {
		return channels.stream()
				.filter(type::isInstance)
				.map(type::cast)
				.collect(Collectors.toList());
	}
	
	public static JJackChannel getChannel(int channel) {
		return channels.stream()
				.filter(ch -> ch.getID() == channel)
				.findFirst().orElse(null);
	}
	
	public static JJackDefaultInputChannel createDefaultInputChannel() {
		int id = channels.stream().mapToInt(JJackChannel::getID).max().getAsInt() + 1;
		JJackDefaultInputChannel ch = new JJackDefaultInputChannel(id);
		channels.add(ch);
		controller.addDefaultInputChannel(ch);
		return ch;
	}
	
	public static JJackDefaultOutputChannel createDefaultOutputChannel() {
		int id = channels.stream().mapToInt(JJackChannel::getID).max().getAsInt() + 1;
		JJackDefaultOutputChannel ch = new JJackDefaultOutputChannel(id);
		channels.add(ch);
		controller.addDefaultOutputChannel(ch);
		return ch;
	}
	
	public static JJackDefaultComboChannel createComboChannel() {
		int id = channels.stream().mapToInt(JJackChannel::getID).max().getAsInt() + 1;
		JJackDefaultComboChannel ch = new JJackDefaultComboChannel(id);
		channels.add(ch);
		controller.addComboChannel(ch);
		return ch;
	}
	
	public static void removeChannel(int channel) {
		channels.removeIf(ch -> ch.getID() == channel);
		
		controller.removeChannel(channel);
	}
	
	public static void resetChannels() {
		channels.removeIf(ch -> ch.getID() >= DEFAULT_CHANNEL_COUNT);
		controller.resetChannels();
	}
	
	public static void saveConfiguration(File file) {
		FileCustomConfig cc = new DefaultFileCustomConfig(file);
		
		for(JJackChannel channel : channels) {
			cc.set("channel." + channel.getID() + ".type", channel.getType().name());
			cc.set("channel." + channel.getID() + ".properties", channel.save());
		}
		
		cc.saveToFile();
	}
	
	public static void loadConfiguration(File file) {
		FileCustomConfig cc = ConfigLoader.loadFileConfig(file);
		
		resetChannels();
		
		for(String channel : cc.getKeys("channel")) {
			var type = JJackChannelType.valueOf(cc.getString("channel." + channel + ".type", JJackChannelType.COMBO.name(), false));
			
			int channelID = Integer.parseInt(channel);
			JJackChannel ch = getChannel(channelID);
			if(ch == null) ch = type.createChannel();
			
			JSONObject props = cc.getGeneric("channel." + channel + ".properties", JSONObject.class);
			
			ch.load(props);
		}
	}

}
