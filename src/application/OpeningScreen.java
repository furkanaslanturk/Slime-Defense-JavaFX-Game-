package application;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class OpeningScreen {
	public static Scene openScene(Runnable onPlay){
		Label title = new Label("SLIME DEFENSE");
		title.setFont(new Font("CAL SANS",60));
		title.setStyle("-fx-text-fill: BLACK; -fx-font-weight: bold");
		StackPane root = new StackPane();
		
	    VBox pane = new VBox(50);
	    StackPane stackpane = new StackPane();
	    

	    Button btPLAY = new Button();
	    btPLAY.setGraphic(ButtonEffects.buttonText("START GAME"));
	    btPLAY.setStyle("-fx-background-color: transparent");
	    ButtonEffects.buttonEffects(btPLAY);//linear gredient renkler arası geçiş sağlayıp estetik katar
	    btPLAY.setOnAction(e -> {
	        setupClick();
	        TowerManager.clear();  // Oyuna başlarken kule ve lazer temizleme
	        onPlay.run();
	    });


	    Button btQUIT = new Button();//üstteki geçici normalde new Level1().openScene()
	    btQUIT.setGraphic(ButtonEffects.buttonText("QUIT"));
	    btQUIT.setStyle("-fx-background-color: transparent");
	    ButtonEffects.buttonEffects(btQUIT);
	    btQUIT.setOnAction(e ->{
	    	setupClick();
	    	Platform.exit();});
	    btPLAY.prefWidth(125);
	    btQUIT.prefWidth(125);


		CheckBox musicCheckbox = new CheckBox();
		musicCheckbox.setGraphic(ButtonEffects.buttonText("MUSIC"));
	    ButtonEffects.buttonEffects(musicCheckbox);
	    setupMusic();
		musicCheckbox.setSelected(true);
		musicCheckbox.setOnAction(e -> {
		    if (mediaPlayer != null) {
		        if (musicCheckbox.isSelected()) {
		        	setupClick();
		            mediaPlayer.play();
		        } else {
		        	setupClick();
		            mediaPlayer.pause();
		        }
		    }
		});
		
		Image bgImage = new Image(OpeningScreen.class.getResource("/images/Marmara_Üniversitesi_logo.png").toExternalForm()); 
		ImageView bgImageView = new ImageView(bgImage);
		bgImageView.setFitWidth(600);
		bgImageView.setFitHeight(600);
		bgImageView.setPreserveRatio(true);
		bgImageView.setOpacity(0.05); // Şeffaflık
		
	    pane.getChildren().addAll(title,btPLAY, musicCheckbox,btQUIT);
	    pane.setAlignment(Pos.CENTER);
	    stackpane.getChildren().addAll(bgImageView,pane);
	    stackpane.setStyle("-fx-background-color: #FAF1DA;");
	    

		
		
		root.getChildren().addAll(stackpane);
		return new Scene(root,Game.SCENE_WIDTH, Game.SCENE_HEIGHT);
	}
	private static MediaPlayer mediaPlayer;// static yaptım çünkü her defasında main menuye döndüğümde tekrar müzik çaldıracak
	private static void setupMusic() {
		 if (mediaPlayer != null) {
			 return; // müzik çaldığından tekrar oluşturmuyorum
		    }
	    Media media = new Media(OpeningScreen.class.getResource("/media/openingTheme.mp3").toExternalForm());
	    mediaPlayer = new MediaPlayer(media);
	    mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);// Sonsuz döngüde
	    mediaPlayer.play();
	}
	private static void setupClick() {
		Media media = new Media(OpeningScreen.class.getResource("/media/Button Sound Effects (Copyright Free) [LVEWkghDh9A] (online-audio-converter.com).mp3").toExternalForm());
		MediaPlayer clickSound = new MediaPlayer(media);
    	clickSound.setStartTime(Duration.seconds(18.8));
    	clickSound.setStopTime(Duration.seconds(20));
    	clickSound.play();
	}
}

