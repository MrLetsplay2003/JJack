module JJack {
	
	requires transitive javafx.base;
	requires transitive javafx.fxml;
	requires transitive javafx.controls;
	requires transitive javafx.media;
	requires transitive javafx.graphics;
	requires transitive jnajack;
	requires transitive MrCore;
	
	opens me.mrletsplay.jjack to javafx.graphics, javafx.fxml;
	
}
