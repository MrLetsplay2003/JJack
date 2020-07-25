package me.mrletsplay.jjack.pulseaudio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class PulseAudio {
	
	public static List<PulseAudioSinkInput> getSinkInputs() {
		String str = execute("pacmd", "list-sink-inputs");
		
		List<PulseAudioSinkInput> sinkInputs = new ArrayList<>();
		PulseAudioSinkInput sinkIn = null;
		for(String line : str.split("\n")) {
			if(line.startsWith("    index: ")) {
				if(sinkIn != null) sinkInputs.add(sinkIn);
				sinkIn = new PulseAudioSinkInput(Integer.parseInt(line.trim().substring("index: ".length())));
				continue;
			}
			
			if(line.startsWith("\tsink: ")) {
				sinkIn.setSink(Integer.parseInt(line.trim().substring("sink: ".length(), line.indexOf('<') - 1).trim()));
			}
			
			if(line.startsWith("\t\t")) {
				String[] kv = line.trim().split(" = ");
				sinkIn.getProperties().put(kv[0], kv[1].substring(1, kv[1].length() - 1));
			}
		}

		if(sinkIn != null) sinkInputs.add(sinkIn);
		
		return sinkInputs;
	}
	
	public static void moveSinkInput(PulseAudioSinkInput input, PulseAudioSink sink) {
		execute("pacmd", "move-sink-input", String.valueOf(input.getIndex()), String.valueOf(sink.getIndex()));
	}
	
	public static PulseAudioSink createSink(String sinkName, Map<String, String> properties) throws FriendlyException {
		String id = execute("pactl", "load-module", "module-jack-sink",
				"client_name=\"" + sinkName + "\"",
				"connect=no",
				"sink_properties=\"" + properties.entrySet().stream()
					.map(en -> en.getKey() + "='" + en.getValue() + "'")
					.collect(Collectors.joining()) + "\"");
		if(id.isEmpty()) throw new FriendlyException("Failed to create sink (has the open file limit for pulseaudio been reached?)");
		int sinkID = Integer.parseInt(id.trim());
		return getSinks().stream()
				.filter(s -> s.getModuleNumber() == sinkID)
				.findFirst().orElseThrow(() -> new FriendlyException("Couldn't create sink"));
	}
	
	public static PulseAudioSink createSink(String sinkName) throws Exception {
		return createSink(sinkName, Collections.emptyMap());
	}
	
	public static PulseAudioSink getSink(int index) {
		return getSinks().stream()
				.filter(s -> s.getIndex() == index)
				.findFirst().orElse(null);
	}
	
	public static PulseAudioSink getSinkByModuleNumber(int moduleNumber) {
		return getSinks().stream()
				.filter(s -> s.getModuleNumber() == moduleNumber)
				.findFirst().orElse(null);
	}
	
	public static List<PulseAudioSink> getSinks() {
		String str = execute("pacmd", "list-sinks");
		
		List<PulseAudioSink> sinks = new ArrayList<>();
		PulseAudioSink sink = null;
		boolean isProps = false;
		for(String line : str.split("\n")) {
			if(line.startsWith("    index: ") || line.startsWith("  * index: ")) {
				if(sink != null) sinks.add(sink);
				String lnt = line.trim();
				boolean isDefault;
				if((isDefault = lnt.startsWith("* "))) lnt = lnt.substring(2);
				sink = new PulseAudioSink(Integer.parseInt(lnt.substring("index: ".length())), isDefault);
				continue;
			}
			
			if(line.startsWith("\t") && !line.startsWith("\t\t")) {
				isProps = line.equals("\tproperties:");
			}
			
			if(line.startsWith("\tmodule: ")) {
				sink.setModuleNumber(Integer.parseInt(line.trim().substring("module: ".length())));
			}
			
			if(line.startsWith("\tname: ")) {
				String n = line.trim().substring("name: ".length());
				sink.setName(n.substring(1, n.length() - 1));
			}
			
			if(line.startsWith("\t\t") && isProps) {
				String[] kv = line.trim().split(" = ");
				sink.getProperties().put(kv[0], kv[1].substring(1, kv[1].length() - 1));
			}
		}

		if(sink != null) sinks.add(sink);
		
		return sinks;
	}
	
	public static void removeSink(PulseAudioSink sink) {
		execute("pacmd", "unload-module", String.valueOf(sink.getModuleNumber()));
	}
	
	private static String execute(String... command) {
		try {
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor(5, TimeUnit.SECONDS);
			return new String(IOUtils.readAllBytes(p.getInputStream()), StandardCharsets.UTF_8);
		}catch(IOException | InterruptedException e) {
			throw new FriendlyException(e);
		}
	}

}
