package application;

import character.King;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import towers.LaserTower;

/**
 * Tüm levelleri dinamik şekilde yöneten merkezi Level sınıfı.
 * Farklı seviye yapılandırmaları (txt yolu, başlangıç para, king konumu vs.) constructor parametresiyle alınır.
 */
public class LevelManager extends Level {
    private final LevelLoader loader;
    private King king;
    private final String levelFilePath;
    private final int startingMoney;
    private final double kingX;
    private final double kingY;

    public LevelManager(String levelFilePath, int startingMoney, double kingX, double kingY) {
        this.levelFilePath = levelFilePath;
        this.startingMoney = startingMoney;
        this.kingX = kingX;
        this.kingY = kingY;

        this.loader = new LevelLoader(levelFilePath);
        this.map = new Map(loader.getWidth(), loader.getHeight(), loader.getPathCells());
        this.gameState = new GameState(startingMoney);
        this.uiManager = new UIManager(gameState, map);
        this.gameState.setUIManager(uiManager);
        this.waveManager = new WaveManager(loader.getWaves(), map, gameState, king);
    }

    @Override
    public Scene getScene(Stage stage) {
        // Satış alanı görünmeyen boş dikdörtgen
        Rectangle rec = new Rectangle(
            map.getTileSize() + ((map.getWidth() - 1) * (map.getGridSpacing() + map.getTileSize())) + 3,
            map.getTileSize() + ((map.getWidth() - 1) * (map.getGridSpacing() + map.getTileSize())) + 3
        );
        rec.setStyle("-fx-fill: #543D1E; -fx-opacity: 0;");
        rec.setArcHeight(8);
        rec.setArcWidth(8);
        rec.setMouseTransparent(true);
        uiManager.setSellArea(rec);

        // Ana layout yapısı
        BorderPane layout = new BorderPane();
        StackPane center = new StackPane();
        center.setStyle("-fx-background-color: #FAF1DA;");
        GridPane grid = new GridPane();
        grid.getChildren().add(map.getGridPane());
        grid.getChildren().add(uiManager.getOverlayPane());
        grid.setAlignment(Pos.CENTER);
        center.getChildren().addAll(grid, rec);

        // Kral nesnesi
        this.king = new King(getClass().getResource("/images/king_sheet_combined.png").toExternalForm());
        this.gameState.setKing(this.king);
        this.waveManager.setKing(this.king);
        center.getChildren().addAll(king.getView(), king.getQuoteLabel());

        // Kralın pozisyonunu ayarla
        king.getView().setTranslateX(kingX);
        king.getView().setTranslateY(kingY);
        king.getQuoteLabel().setTranslateX(kingX);
        king.getQuoteLabel().setTranslateY(kingY - 40);

        center.setAlignment(Pos.CENTER);
        layout.setCenter(center);
        layout.setRight(uiManager.getTowerPanel());

        Scene scene = new Scene(layout, Game.SCENE_WIDTH, Game.SCENE_HEIGHT);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                uiManager.toggleUpgradeMode();
            }
        });

        // Kuleler, davranışlar ve dalga sistemi başlatılıyor
        uiManager.enablePlacementBehavior(center);
        TowerManager.setOverlayPane(uiManager.getOverlayPane());
        TowerManager.start();
        gameState.setWaveManager(waveManager);
        waveManager.start(stage, uiManager);

        // Kazanma kontrolü
        final Timeline[] winCheck = new Timeline[1];
        winCheck[0] = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            if (gameState.isGameWon()) {
                winCheck[0].stop();

                LaserTower.clearAllLasers(uiManager.getOverlayPane());
                waveManager.stopAllWaves();
                TowerManager.stop();
                uiManager.hideUpgradeUI();
                EnemyManager.clearAll(uiManager.getOverlayPane());
                TowerManager.clear();

                Timeline delay = new Timeline(new KeyFrame(Duration.seconds(2), ev -> {
                    if (Game.currentLevel == 5) {
                        Game.mainStage.setScene(EndWinScreen.openScene());
                    } else {
                        Game.mainStage.setScene(WinScreen.openScene(() -> Game.loadLevel(Game.currentLevel + 1)));
                    }
                }));
                delay.setCycleCount(1);
                delay.play();
            }
        }));
        winCheck[0].setCycleCount(Animation.INDEFINITE);
        winCheck[0].play();

        // Kaybetme kontrolü
        final Timeline[] loseCheck = new Timeline[1];
        loseCheck[0] = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            if (gameState.isGameOver()) {
                loseCheck[0].stop();

                LaserTower.clearAllLasers(uiManager.getOverlayPane());
                waveManager.stopAllWaves();
                uiManager.hideUpgradeUI();
                TowerManager.stop();
                TowerManager.clear();

                Timeline delay = new Timeline(new KeyFrame(Duration.seconds(3), ev -> {
                    Game.mainStage.setScene(LoseScreen.openScene());
                }));
                delay.setCycleCount(1);
                delay.play();
            }
        }));
        loseCheck[0].setCycleCount(Animation.INDEFINITE);
        loseCheck[0].play();

        return scene;
    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public WaveManager getWaveManager() {
        return waveManager;
    }
}
