module jjack {
	
	requires transitive javafx.base;
	requires transitive javafx.fxml;
	requires transitive javafx.controls;
	requires transitive javafx.media;
	requires transitive javafx.graphics;
	requires transitive jnajack;
	requires transitive MrCore;
	requires transitive org.controlsfx.controls;
	
	exports me.mrletsplay.jjack;
	exports me.mrletsplay.jjack.channel;
	exports me.mrletsplay.jjack.controller;
	exports me.mrletsplay.jjack.port;
	exports me.mrletsplay.jjack.port.stereo;
	exports me.mrletsplay.jjack.pulseaudio;
	
	opens me.mrletsplay.jjack to javafx.graphics, javafx.fxml;	
	opens me.mrletsplay.jjack.controller to javafx.graphics, javafx.fxml;
	
}
