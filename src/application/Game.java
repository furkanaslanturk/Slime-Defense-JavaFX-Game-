package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Game extends Application {
    public static final int SCENE_WIDTH = 1600;
    public static final int SCENE_HEIGHT = 900;
    public static Stage mainStage;
    public static int currentLevel = 1;

    private static GameState currentGameState;

    @Override
    public void start(Stage primaryStage) {
        Game.mainStage = primaryStage;

        // Açılış ekranı → level 1'e geçiş lambda ile
        Scene openingScene = OpeningScreen.openScene(() -> Game.loadLevel(1));
        primaryStage.setScene(openingScene);
        primaryStage.setTitle("Tower Defense Game");
        primaryStage.show();
    }

    public static void loadLevel(int levelNumber) {
        Game.currentLevel = levelNumber;
        TowerManager.clear(); // Her yeni seviyeye geçerken kuleleri temizle

        // Level parametrelerini seç → LevelManager ile yükle
        String path = "resources/level" + levelNumber + ".txt";
        int money;
        double kingX, kingY;

        // Her seviyeye özel başlangıç ayarları
        switch (levelNumber) {
            case 1 -> {
                money = 1000;
                kingX = 300;
                kingY = 25;
            }
            case 2 -> {
                money = 150;
                kingX = 300;
                kingY = 130;
            }
            case 3 -> {
                money = 200;
                kingX = 300;
                kingY = -30;
            }
            case 4 -> {
                money = 250;
                kingX = 430;
                kingY = 50;
            }
            case 5 -> {
                money = 300;
                kingX = 430;
                kingY = 260;
            }
            default -> {
                System.err.println("Geçersiz seviye: " + levelNumber);
                return;
            }
        }

        // LevelManager ile yükle
        Level level = new LevelManager(path, money, kingX, kingY);
        Scene scene = level.getScene(mainStage);
        Game.setCurrentGameState(level.getGameState());
        mainStage.setScene(scene);
    }

    // Global game state erişimi
    public static GameState getCurrentGameState() {
        return currentGameState;
    }

    public static void setCurrentGameState(GameState state) {
        currentGameState = state;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
