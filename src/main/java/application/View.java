package application;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class View {
	
	private Stage stage;

	public View(Stage stage){
		this.stage = stage;
	}
	
	public void init(){
		DataEnterPane enterScene = new DataEnterPane();
		enterScene.makePane();
		stage.setScene(makeScene(enterScene.makePane()));
		stage.show();
	}
	private Scene makeScene(Pane p){
		Scene scene = new Scene(p,400,400);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		return scene;
	}

}
