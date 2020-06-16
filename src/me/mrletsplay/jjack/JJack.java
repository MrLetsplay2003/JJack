package me.mrletsplay.jjack;

import java.io.File;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import me.mrletsplay.jjack.port.JJackInputPort;
import me.mrletsplay.jjack.port.JJackOutputPort;
import me.mrletsplay.mrcore.config.ConfigLoader;
import me.mrletsplay.mrcore.config.FileCustomConfig;
import me.mrletsplay.mrcore.config.impl.DefaultFileCustomConfig;

public class JJack extends Application {
	
	public static final int DEFAULT_CHANNEL_COUNT = 4;
	
	private static Stage stage;
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
		channels = new ArrayList<>(DEFAULT_CHANNEL_COUNT);
		
		for(int i = 0; i < DEFAULT_CHANNEL_COUNT; i++) {
			channels.add(new JJackChannel(i));
		}
		
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
			Map<String, FloatBuffer> portOutput = new HashMap<>();
			
			for(JJackChannel channel : new ArrayList<>(channels)) {
				if(channel.getInputPort() == null || channel.getOutputPort() == null) continue;
				
				JackPort inPort = channel.getInputPort().getJackPort();
				JackPort outPort = channel.getOutputPort().getJackPort();
				
				FloatBuffer in = inPort.getFloatBuffer();
				
				if(portOutput.containsKey(outPort.getShortName())) {
					FloatBuffer oldOut = portOutput.get(outPort.getShortName());
					int numFloats = oldOut.rewind().remaining();
					
					FloatBuffer newOut = FloatBuffer.allocate(numFloats);
					channel.process(client, in, newOut, nframes);
					
					float[] oldFloats = new float[numFloats];
					oldOut.get(oldFloats);
					
					float[] newFloats = new float[numFloats];
					newOut.flip().get(newFloats);
					
					for(int i = 0; i < numFloats; i++) {
						newFloats[i] = oldFloats[i] + newFloats[i];
					}
					
					oldOut.rewind().put(newFloats);
				}else {
					FloatBuffer out = outPort.getFloatBuffer();
					channel.process(client, in, out, nframes);
					portOutput.put(outPort.getShortName(), out);
				}
				
				in.rewind();
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
	
	public static void exit() {
		client.close();
		System.exit(0);
	}
	
	public static Stage getStage() {
		return stage;
	}
	
	public static JackClient getClient() {
		return client;
	}
	
	public static JJackController getController() {
		return controller;
	}
	
	public static ObservableList<JJackInputPort> getInputPorts() {
		return inputPorts;
	}
	
	public static ObservableList<JJackOutputPort> getOutputPorts() {
		return outputPorts;
	}
	
	public static List<JJackChannel> getChannels() {
		return channels;
	}
	
	public static JJackChannel getChannel(int channel) {
		return channels.stream()
				.filter(ch -> ch.getID() == channel)
				.findFirst().orElse(null);
	}
	
	public static JJackChannel createChannel() {
		int id = channels.stream().mapToInt(JJackChannel::getID).max().getAsInt() + 1;
		JJackChannel ch = new JJackChannel(id);
		channels.add(ch);
		controller.addChannel(ch);
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
			cc.set("channel." + channel.getID() + ".in", channel.getInputPort() == null ? null : channel.getInputPort().getName());
			cc.set("channel." + channel.getID() + ".out", channel.getOutputPort() == null ? null : channel.getOutputPort().getName());
			cc.set("channel." + channel.getID() + ".volume", channel.getVolume());
		}
		
		cc.saveToFile();
	}
	
	public static void loadConfiguration(File file) {
		FileCustomConfig cc = ConfigLoader.loadFileConfig(file);
		
		resetChannels();
		
		for(String channel : cc.getKeys("channel")) {
			int channelID = Integer.parseInt(channel);
			JJackChannel ch = getChannel(channelID);
			if(ch == null) ch = JJack.createChannel();
			
			String in = cc.getString("channel." + channel + ".in");
			String out = cc.getString("channel." + channel + ".out");
			double volume = cc.getDouble("channel." + channel + ".volume");
			
			JJackInputPort inP = inputPorts.stream()
					.filter(p -> p.getName().equals(in))
					.findFirst().orElse(null);
			
			JJackOutputPort outP = outputPorts.stream()
					.filter(p -> p.getName().equals(out))
					.findFirst().orElse(null);
			
			ch.setInputPort(inP);
			ch.setOutputPort(outP);
			ch.setVolume(volume);
		}
	}

}
