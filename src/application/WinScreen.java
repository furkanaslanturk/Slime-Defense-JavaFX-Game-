package application;

import java.util.function.Supplier;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class WinScreen {
    public static Scene openScene(Runnable onNextLevel) {
        // 🎯 Oyun mantığını durdur
        TowerManager.stop();  // Kuleler ateş etmeyi durdursun
        TowerManager.clear(); // Sahnedeki kuleler temizlensin
        Game.getCurrentGameState().getWaveManager().stopAllWaves(); // Dalgalar dursun

        VBox pane = new VBox(20);
        HBox pane2 = new HBox(50);
        pane2.setSpacing(3);

        Text title = new Text("YOU WON THE LEVEL");
        title.setFont(Font.font("CAL SANS", FontWeight.BOLD, 60));
        title.setFill(Color.BLACK);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setWrappingWidth(800);

        FadeTransition fade = new FadeTransition(Duration.seconds(1.5), title);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setCycleCount(1);
        fade.play();

        Button btNEXT = new Button();
        btNEXT.setGraphic(ButtonEffects.buttonText("NEXT LEVEL"));
        btNEXT.setStyle("-fx-background-color: transparent");
        ButtonEffects.buttonEffects(btNEXT);
        btNEXT.setOnAction(e -> {
            setupClick();
            Game.loadLevel(Game.currentLevel + 1);
        });

        Button btMENU = new Button();
        btMENU.setGraphic(ButtonEffects.buttonText("MAIN MENU"));
        btMENU.setStyle("-fx-background-color: transparent");
        ButtonEffects.buttonEffects(btMENU);
        btMENU.setOnAction(e -> {
            setupClick();
            TowerManager.clear();
            Game.mainStage.setScene(OpeningScreen.openScene(() -> Game.loadLevel(1)));
        });

        btNEXT.setPrefWidth(500);
        btMENU.setPrefWidth(500);

        pane2.getChildren().addAll(btNEXT, btMENU);
        pane2.setAlignment(Pos.CENTER);
        pane.getChildren().addAll(title, pane2);
        pane.setAlignment(Pos.CENTER);
        pane.setStyle("-fx-background-color: #FAF1DA;");

        return new Scene(pane, Game.SCENE_WIDTH, Game.SCENE_HEIGHT);
    }

    private static void setupClick() {
        Media media = new Media(WinScreen.class.getResource("/media/Button Sound Effects (Copyright Free) [LVEWkghDh9A] (online-audio-converter.com).mp3").toExternalForm());
        MediaPlayer clickSound = new MediaPlayer(media);
        clickSound.setStartTime(Duration.seconds(18.8));
        clickSound.setStopTime(Duration.seconds(20));
        clickSound.play();
    }
}
