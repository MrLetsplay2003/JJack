module JJack {
	
	requires transitive javafx.base;
	requires transitive javafx.fxml;
	requires transitive javafx.controls;
	requires transitive javafx.media;
	requires transitive javafx.graphics;
	requires transitive jnajack;
	requires transitive MrCore;
	requires transitive org.controlsfx.controls;
	
	opens me.mrletsplay.jjack to javafx.graphics, javafx.fxml;	
	opens me.mrletsplay.jjack.controller to javafx.graphics, javafx.fxml;
	
}
