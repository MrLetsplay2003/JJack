package me.mrletsplay.jjack;

import java.io.File;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jaudiolibs.jnajack.Jack;
import org.jaudiolibs.jnajack.JackClient;
import org.jaudiolibs.jnajack.JackException;
import org.jaudiolibs.jnajack.JackOptions;
import org.jaudiolibs.jnajack.JackPort;
import org.jaudiolibs.jnajack.JackPortFlags;
import org.jaudiolibs.jnajack.JackPortRegistrationCallback;
import org.jaudiolibs.jnajack.JackPortType;
import org.jaudiolibs.jnajack.JackStatus;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import me.mrletsplay.jjack.channel.JJackChannel;
import me.mrletsplay.jjack.channel.JJackChannelType;
import me.mrletsplay.jjack.channel.JJackComboChannel;
import me.mrletsplay.jjack.channel.JJackInputChannel;
import me.mrletsplay.jjack.channel.JJackOutputChannel;
import me.mrletsplay.jjack.channel.JJackSingleComboChannel;
import me.mrletsplay.jjack.channel.JJackSingleInputChannel;
import me.mrletsplay.jjack.channel.JJackSingleOutputChannel;
import me.mrletsplay.jjack.channel.JJackStereoInputChannel;
import me.mrletsplay.jjack.channel.JJackStereoOutputChannel;
import me.mrletsplay.jjack.controller.JJackController;
import me.mrletsplay.jjack.controller.JJackPreferencesController;
import me.mrletsplay.jjack.port.JJackInputPort;
import me.mrletsplay.jjack.port.JJackOutputPort;
import me.mrletsplay.jjack.port.stereo.JJackStereoInputPort;
import me.mrletsplay.jjack.port.stereo.JJackStereoOutputPort;
import me.mrletsplay.jjack.pulseaudio.PulseAudio;
import me.mrletsplay.jjack.pulseaudio.PulseAudioSink;
import me.mrletsplay.jjack.pulseaudio.PulseAudioSinkInput;
import me.mrletsplay.mrcore.config.ConfigLoader;
import me.mrletsplay.mrcore.config.FileCustomConfig;
import me.mrletsplay.mrcore.config.impl.DefaultFileCustomConfig;
import me.mrletsplay.mrcore.json.JSONObject;
public class JJack extends Application {
	
	public static final String JACK_CLIENT_NAME = "JJack";
	public static final UUID INSTANCE_ID = UUID.randomUUID();
	
	public static final double
		MAX_VOLUME_DEFAULT = 125,
		MAX_VOLUME_OVERAMPLIFICATION = 150;
	
	public static Stage
		stage,
		preferencesStage;
	
	private static JackClient client;
	private static JJackController controller;
	private static ObservableList<JJackInputPort> inputPorts;
	private static ObservableList<JJackOutputPort> outputPorts;
	private static ObservableList<JJackStereoInputPort> stereoInputPorts;
	private static ObservableList<JJackStereoOutputPort> stereoOutputPorts;
	private static List<JJackChannel> channels;
	private static ScheduledExecutorService executor;
	
	private static Map<String, String> pendingProgramPorts = new HashMap<>();
	
	private static boolean
		allowOveramplification,
		useProgramPorts,
		programPortLenient,
		programPortConnectOriginal,
		programPortCheckProcessAlive,
		programPortByPID;
	
	private static String configOnStartup;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		List<PulseAudioSink> leftoverSinks = PulseAudio.getSinks().stream()
				.filter(s -> Objects.equals(s.getProperty("jjack.program_sink"), "true"))
				.collect(Collectors.toList());
		
		if(!leftoverSinks.isEmpty()) {
			Alert alert = new Alert(AlertType.INFORMATION, "JJack has found one or more existing JJack program sinks.\nThese could be left over from a crash or belong to another JJack instance.\n\nDo you want to remove them?", ButtonType.YES, ButtonType.NO);
			alert.setTitle("JJack");
			alert.setHeaderText("Found program sinks");
			Optional<ButtonType> b = alert.showAndWait();
			
			if(b.isPresent() && b.get() == ButtonType.YES)
				leftoverSinks.forEach(PulseAudio::removeSink);
		}
		
		executor = Executors.newScheduledThreadPool(1);
		
		inputPorts = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
		outputPorts = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
		stereoInputPorts = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
		stereoOutputPorts = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
		channels = Collections.synchronizedList(new ArrayList<>());
		
		client = Jack.getInstance().openClient(JACK_CLIENT_NAME, EnumSet.noneOf(JackOptions.class), EnumSet.noneOf(JackStatus.class));
		
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
		
		URL prefsURL = JJack.class.getResource("/include/preferences.fxml");
		if(prefsURL == null) prefsURL = new File("./include/preferences.fxml").toURI().toURL();
		FXMLLoader prefsL = new FXMLLoader(prefsURL);
		Parent prefsPr = prefsL.load(prefsURL.openStream());
		JJackPreferencesController prefsC = prefsL.getController();
		
		Scene prefsScene = new Scene(prefsPr, 720, 480);
		
		preferencesStage = new Stage();
		preferencesStage.setTitle("JJack - Preferences");
		preferencesStage.setScene(prefsScene);
		preferencesStage.initOwner(JJack.stage);
		preferencesStage.setResizable(false);
		preferencesStage.setOnShown(event -> {
			prefsC.update();
		});
		
		preferencesStage.setOnCloseRequest(e -> {
			preferencesStage.hide();
		});
		
		client.setProcessCallback((client, nframes) -> {
			List<JJackOutputPort> outPorts = new ArrayList<>(getOutputPorts());
			
			outPorts.forEach(p -> {
				p.initOutput(nframes);
			});
			
			for(JJackOutputChannel channel : getOutputChannels()) {
				channel.process(client, nframes);
			}
			
			outPorts.forEach(p -> p.flushOutput());
			
			for(JJackChannel ch : new ArrayList<>(channels)) {
				ch.update();
			}
			return true;
		});
		
		client.setPortRegistrationCallback(new JackPortRegistrationCallback() {
			
			@Override
			public void portUnregistered(JackClient client, String portFullName) {
				JJackInputPort ip;
				synchronized (inputPorts) {
					ip = inputPorts.stream()
							.filter(pt -> pt.getName().equals(portFullName))
							.findFirst().orElse(null);
				}
				
				if(ip != null) {
					Platform.runLater(() -> {
						unregisterInputPort(ip);
						synchronized (stereoInputPorts) {
							stereoInputPorts.removeIf(s -> s.isClosed());
						}
					});
				}
				
				JJackOutputPort op;
				synchronized (outputPorts) {
					op = outputPorts.stream()
							.filter(pt -> pt.getName().equals(portFullName))
							.findFirst().orElse(null);
				}
				
				if(op != null) {
					Platform.runLater(() -> {
						unregisterOutputPort(op);
						synchronized (stereoOutputPorts) {
							stereoOutputPorts.removeIf(s -> s.isClosed());
						}
					});
				}
			}
			
			@Override
			public void portRegistered(JackClient client, String portFullName) {
				if(client == null) return;
				
				executor.execute(() -> {
					try {
						String[] outPorts = Jack.getInstance().getPorts(client, null, JackPortType.AUDIO, EnumSet.of(JackPortFlags.JackPortIsOutput));
						boolean isOutput = Arrays.asList(outPorts).contains(portFullName);
						
						registerPort(portFullName, isOutput);
						createStereoChannels();
					}catch(Exception e) {
						e.printStackTrace();
					}
				});
			}
		});
		
		client.activate();
		
		String[] outputPorts = Jack.getInstance().getPorts(client, null, JackPortType.AUDIO, EnumSet.of(JackPortFlags.JackPortIsOutput));
		for(String port : outputPorts) {
			registerPort(port, true);
		}

		String[] inputPorts = Jack.getInstance().getPorts(client, null, JackPortType.AUDIO, EnumSet.of(JackPortFlags.JackPortIsInput));
		for(String port : inputPorts) {
			registerPort(port, false);
		}
		
		createStereoChannels();
		
		new AnimationTimer() {
			
			@Override
			public void handle(long now) {
				for(JJackChannel ch : new ArrayList<>(channels)) ch.updateUI();
			}
			
		}.start();
		
    	JJack.loadPreferences(JJackPreferencesController.PREFERENCES_FILE);
		
		String config = getParameters().getNamed().get("config");
		if(config != null) {
			Platform.runLater(() -> loadConfiguration(new File(config)));
		}
		
		executor.scheduleWithFixedDelay(() -> updateProgramSinks(), 1, 1, TimeUnit.SECONDS);
	}
	
	private static void registerPort(String portName, boolean isOutput) throws JackException {
		String portClient = portName.split(":")[0];
		if(portClient.equals(client.getName()) || portClient.startsWith(JACK_CLIENT_NAME)) return;
		
		if(isOutput) {
			JackPort p = client.registerPort(portName, JackPortType.AUDIO, JackPortFlags.JackPortIsInput);
			Jack.getInstance().connect(client, portName, p.getName());
			inputPorts.add(new JJackInputPort(p.getShortName(), p));
		}else {
			JackPort p = client.registerPort(portName, JackPortType.AUDIO, JackPortFlags.JackPortIsOutput);
			Jack.getInstance().connect(client, p.getName(), portName);
			outputPorts.add(new JJackOutputPort(p.getShortName(), p));
		}
	}
	
	private static void unregisterInputPort(JJackInputPort port) {
		port.close();
		inputPorts.remove(port);
		
		try {
			client.unregisterPort(port.getJackPort());
		} catch (JackException e) {
			e.printStackTrace();
		}
	}
	
	private static void unregisterOutputPort(JJackOutputPort port) {
		port.close();
		outputPorts.remove(port);
		
		try {
			client.unregisterPort(port.getJackPort());
		} catch (JackException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized static void createStereoChannels() {
		Platform.runLater(() -> {
			createLeftStereoChannels();
			createRightStereoChannels();
		});
	}
	
	private static void createLeftStereoChannels() {
		Map<String, List<JJackInputPort>> portMap = new HashMap<>();
		
		synchronized (inputPorts) {
			for(JJackInputPort port : inputPorts) {
				String portClient = port.getOriginalClientName();
				List<JJackInputPort> ports = portMap.getOrDefault(portClient, new ArrayList<>());
				ports.add(port);
				portMap.put(portClient, ports);
			}
		}
		
		for(String client : portMap.keySet()) {
			List<JJackInputPort> ports = portMap.get(client);
			if(ports.size() != 2) continue;
			
			synchronized (stereoInputPorts) {
				if(stereoInputPorts.stream().anyMatch(s -> (s.getLeft() == ports.get(0) && s.getRight() == ports.get(1))
							|| (s.getLeft() == ports.get(1) && s.getRight() == ports.get(0))))
					continue;
			}
			
			JJackStereoInputPort inP = new JJackStereoInputPort(ports.get(0), ports.get(1));
			stereoInputPorts.add(inP);
		}
	}
	
	private static void createRightStereoChannels() {
		Map<String, List<JJackOutputPort>> portMap = new HashMap<>();
		
		synchronized (outputPorts) {
			for(JJackOutputPort port : outputPorts) {
				String portClient = port.getOriginalClientName();
				List<JJackOutputPort> ports = portMap.getOrDefault(portClient, new ArrayList<>());
				ports.add(port);
				portMap.put(portClient, ports);
			}
		}
		
		for(String client : portMap.keySet()) {
			List<JJackOutputPort> ports = portMap.get(client);
			if(ports.size() != 2) continue;
			
			synchronized (stereoOutputPorts) {
				if(stereoOutputPorts.stream().anyMatch(s -> (s.getLeft() == ports.get(0) && s.getRight() == ports.get(1))
							|| (s.getLeft() == ports.get(1) && s.getRight() == ports.get(0))))
					continue;
			}
			
			JJackStereoOutputPort outP = new JJackStereoOutputPort(ports.get(0), ports.get(1));
			stereoOutputPorts.add(outP);
		}
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
		for(float f : floats) volume += Math.pow(f, 2);
		volume /= floats.length;
		
		buffer.rewind();
		return (float) Math.sqrt(volume);
	}
	
	public static void adjustVolume(FloatBuffer buffer, double volume) {
		float[] floats = new float[buffer.rewind().remaining()];
		buffer.get(floats);
		
		for(int i = 0; i < floats.length; i++) floats[i] = (float) (floats[i] * Math.pow(volume, 2));
		
		buffer.rewind().put(floats).rewind();
	}
	
	public static void exit() {
		removeProgramSinks();
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
	
	public static ObservableList<JJackStereoInputPort> getStereoInputPorts() {
		return stereoInputPorts;
	}
	
	public static ObservableList<JJackStereoOutputPort> getStereoOutputPorts() {
		return stereoOutputPorts;
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
		synchronized (channels) {
			return channels.stream()
					.filter(type::isInstance)
					.map(type::cast)
					.collect(Collectors.toList());
		}
	}
	
	public static JJackChannel getChannel(int channel) {
		synchronized (channels) {
			return channels.stream()
					.filter(ch -> ch.getID() == channel)
					.findFirst().orElse(null);
		}
	}
	
	public static int newChannelID() {
		synchronized (channels) {
			return channels.stream().mapToInt(JJackChannel::getID).max().orElse(-1) + 1;
		}
	}
	
	private static double getMaxVolume() {
		return allowOveramplification ? MAX_VOLUME_OVERAMPLIFICATION : MAX_VOLUME_DEFAULT;
	}
	
	public static JJackSingleInputChannel createSingleInputChannel(int id) {
		JJackSingleInputChannel ch = new JJackSingleInputChannel(id);
		channels.add(ch);
		controller.addSingleInputChannel(ch);
		ch.setMaxVolume(getMaxVolume());
		return ch;
	}
	
	public static JJackSingleInputChannel createSingleInputChannel() {
		return createSingleInputChannel(newChannelID());
	}
	
	public static JJackSingleOutputChannel createSingleOutputChannel(int id) {
		JJackSingleOutputChannel ch = new JJackSingleOutputChannel(id);
		channels.add(ch);
		controller.addSingleOutputChannel(ch);
		ch.setMaxVolume(getMaxVolume());
		return ch;
	}
	
	public static JJackSingleOutputChannel createSingleOutputChannel() {
		return createSingleOutputChannel(newChannelID());
	}
	
	public static JJackSingleComboChannel createSingleComboChannel(int id) {
		JJackSingleComboChannel ch = new JJackSingleComboChannel(id);
		channels.add(ch);
		controller.addSingleComboChannel(ch);
		ch.setMaxVolume(getMaxVolume());
		return ch;
	}
	
	public static JJackSingleComboChannel createSingleComboChannel() {
		return createSingleComboChannel(newChannelID());
	}
	
	public static JJackStereoInputChannel createStereoInputChannel(int id) {
		JJackStereoInputChannel ch = new JJackStereoInputChannel(id);
		channels.add(ch);
		controller.addStereoInputChannel(ch);
		ch.setMaxVolume(getMaxVolume());
		return ch;
	}
	
	public static JJackStereoInputChannel createStereoInputChannel() {
		return createStereoInputChannel(newChannelID());
	}
	
	public static JJackStereoOutputChannel createStereoOutputChannel(int id) {
		JJackStereoOutputChannel ch = new JJackStereoOutputChannel(id);
		channels.add(ch);
		controller.addStereoOutputChannel(ch);
		ch.setMaxVolume(getMaxVolume());
		return ch;
	}
	
	public static JJackStereoOutputChannel createStereoOutputChannel() {
		return createStereoOutputChannel(newChannelID());
	}
	
	public static void removeChannel(int channel) {
		synchronized (channels) {
			channels.removeIf(ch -> ch.getID() == channel);
		}
		
		controller.removeChannel(channel);
	}
	
	public static void clearChannels() {
		channels.clear();
		controller.clearChannels();
	}
	
	public static void saveConfiguration(File file) {
		FileCustomConfig cc = new DefaultFileCustomConfig(file);
		
		synchronized (channels) {
			for(JJackChannel channel : channels) {
				cc.set("channel." + channel.getID() + ".type", channel.getType().name());
				cc.set("channel." + channel.getID() + ".properties", channel.save());
			}
		}
		
		cc.saveToFile();
	}
	
	public static void loadConfiguration(File file) {
		FileCustomConfig cc = ConfigLoader.loadFileConfig(file);
		
		clearChannels();
		
		for(String channel : cc.getKeys("channel")) {
			var type = JJackChannelType.valueOf(cc.getString("channel." + channel + ".type"));
			int channelID = Integer.parseInt(channel);
			type.createChannel(channelID);
		}
		
		for(String channel : cc.getKeys("channel")) {
			int channelID = Integer.parseInt(channel);
			JJackChannel ch = getChannel(channelID);
			
			JSONObject props = cc.getGeneric("channel." + channel + ".properties", JSONObject.class);
			ch.load(props);
		}
		
		allowOveramplification = cc.getBoolean("preferences.allow-overamplification");
	}
	
	public static void setAllowOveramplification(boolean allowOveramplification) {
		if(JJack.allowOveramplification == allowOveramplification) return;
		JJack.allowOveramplification = allowOveramplification;
		
		synchronized (JJack.getChannels()) {
			if(allowOveramplification) {
				JJack.getChannels().forEach(c -> c.setMaxVolume(MAX_VOLUME_OVERAMPLIFICATION));
			}else {
				JJack.getChannels().forEach(c -> c.setMaxVolume(MAX_VOLUME_DEFAULT));
			}
		}
	}
	
	public static boolean isAllowOveramplification() {
		return allowOveramplification;
	}
	
	public static void setUseProgramPorts(boolean useProgramPorts) {
		if(JJack.useProgramPorts == useProgramPorts) return;
		JJack.useProgramPorts = useProgramPorts;
		
		if(useProgramPorts) {
			updateProgramSinks();
		}else {
			removeProgramSinks();
		}
	}
	
	private static void updateProgramSinks() {
		if(!useProgramPorts) return;
		
		Map<String, String> baseProperties = new HashMap<>();
		baseProperties.put("jjack.program_sink", "true");
		baseProperties.put("jjack.instance_id", INSTANCE_ID.toString());
		
		List<PulseAudioSink> sinks = PulseAudio.getSinks().stream()
				.filter(s -> "true".equals(s.getProperty("jjack.program_sink"))
						&& INSTANCE_ID.toString().equals(s.getProperty("jjack.instance_id")))
				.collect(Collectors.toList());
		
		List<PulseAudioSink> allSinks = new ArrayList<>(sinks);
		
		Set<PulseAudioSink> remainingSinks = new HashSet<>();
		
		for(PulseAudioSinkInput sI : PulseAudio.getSinkInputs()) {
			PulseAudioSink rSink = sinks.stream()
					.filter(s -> s.getIndex() == sI.getSink()
							&& (programPortLenient || String.valueOf(sI.getIndex()).equals(s.getProperty("jjack.input_index"))))
					.findFirst().orElse(null);
			
			if(rSink == null) { // A new program appeared
				if(programPortByPID) {
					PulseAudioSink pSink = allSinks.stream()
							.filter(s -> sI.getProperty("application.process.id").equals(s.getProperty("jjack.input_process_id")))
							.findFirst().orElse(null);
					
					if(pSink != null) {
						remainingSinks.add(pSink);
						PulseAudio.moveSinkInput(sI, pSink);
						continue;
					}
				}
				
				Map<String, String> props = new HashMap<>(baseProperties);
				props.put("jjack.input_index", String.valueOf(sI.getIndex()));
				props.put("jjack.input_process_id", String.valueOf(sI.getProperty("application.process.id")));
				PulseAudioSink programSink = PulseAudio.createSink(sI.getName(), props);
				allSinks.add(programSink);
				
				if(programPortConnectOriginal) {
					PulseAudioSink origSink = PulseAudio.getSink(sI.getSink());
					if(origSink.getJackName() != null)
						pendingProgramPorts.put(programSink.getJackName(), origSink.getJackName());
				}
				
				PulseAudio.moveSinkInput(sI, programSink);
			}else {
				remainingSinks.add(rSink);
			}
		}
		
		sinks.removeAll(remainingSinks);
		sinks.forEach(sink -> {
			if(programPortCheckProcessAlive) {
				try {
					if(ProcessHandle.of(Integer.parseInt(sink.getProperty("jjack.input_process_id"))).isPresent())
						return;
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			PulseAudio.removeSink(sink);
		});
		
		if(programPortConnectOriginal && !pendingProgramPorts.isEmpty()) {
			for(String k : new ArrayList<>(pendingProgramPorts.keySet())) {
				String it = pendingProgramPorts.get(k);
				
				JJackStereoInputPort inP = stereoInputPorts.stream()
						.filter(ip -> ip.getName().equals(it))
						.findFirst().orElse(null);
				
				if(inP == null) continue;

				try {
					List<String> outPorts = Arrays.stream(Jack.getInstance().getPorts(JJack.client, null, JackPortType.AUDIO, EnumSet.of(JackPortFlags.JackPortIsOutput)))
							.filter(s -> s.startsWith(k + ":"))
							.collect(Collectors.toList());
					
					if(outPorts.size() == 2) {
						Jack.getInstance().connect(JJack.client, outPorts.get(0), inP.getLeft().getJackPort().getName());
						Jack.getInstance().connect(JJack.client, outPorts.get(1), inP.getRight().getJackPort().getName());
						pendingProgramPorts.remove(k);
					}
				}catch(JackException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void removeProgramSinks() {
		PulseAudio.getSinks().stream()
			.filter(s -> Objects.equals(s.getProperty("jjack.program_sink"), "true")
					&& Objects.equals(s.getProperty("jjack.instance_id"), INSTANCE_ID.toString()))
			.forEach(PulseAudio::removeSink);
	}
	
	public static boolean isUseProgramPorts() {
		return useProgramPorts;
	}
	
	public static void setProgramPortLenient(boolean programPortLenient) {
		JJack.programPortLenient = programPortLenient;
	}
	
	public static boolean isProgramPortLenient() {
		return programPortLenient;
	}
	
	public static void setProgramPortConnectOriginal(boolean programPortConnectOriginal) {
		JJack.programPortConnectOriginal = programPortConnectOriginal;
	}
	
	public static boolean isProgramPortConnectOriginal() {
		return programPortConnectOriginal;
	}
	
	public static void setProgramPortCheckProcessAlive(boolean programPortCheckProcessAlive) {
		JJack.programPortCheckProcessAlive = programPortCheckProcessAlive;
	}
	
	public static boolean isProgramPortCheckProcessAlive() {
		return programPortCheckProcessAlive;
	}
	
	public static void setProgramPortByPID(boolean programPortByPID) {
		JJack.programPortByPID = programPortByPID;
	}
	
	public static boolean isProgramPortByPID() {
		return programPortByPID;
	}
	
	public static void setConfigOnStartup(String configOnStartup) {
		JJack.configOnStartup = configOnStartup;
	}
	
	public static String getConfigOnStartup() {
		return configOnStartup;
	}
	
	public static void savePreferences(File file) {
		FileCustomConfig cc = new DefaultFileCustomConfig(file);
		cc.set("allow-overamplification", allowOveramplification);
		cc.set("use-program-ports", useProgramPorts);
		cc.set("program-port-lenient", programPortLenient);
		cc.set("program-port-connect-original", programPortConnectOriginal);
		cc.set("program-port-check-process-alive", programPortCheckProcessAlive);
		cc.set("program-port-by-pid", programPortByPID);
		cc.set("config-on-startup", configOnStartup);
		cc.saveToFile();
	}
	
	public static void loadPreferences(File file) {
		FileCustomConfig cc = ConfigLoader.loadFileConfig(file);
		setAllowOveramplification(cc.getBoolean("allow-overamplification"));
		setProgramPortLenient(cc.getBoolean("program-port-lenient", true, false));
		setProgramPortConnectOriginal(cc.getBoolean("program-port-connect-original", true, false));
		setProgramPortCheckProcessAlive(cc.getBoolean("program-port-check-process-alive", true, false));
		setProgramPortByPID(cc.getBoolean("program-port-by-pid", true, false));
		setUseProgramPorts(cc.getBoolean("use-program-ports"));
		setConfigOnStartup(cc.getString("config-on-startup"));
		
		if(configOnStartup != null) {
			File f = new File(configOnStartup);
			if(f.exists() && f.isFile() && f.canRead()) loadConfiguration(f);
		}
	}

}
