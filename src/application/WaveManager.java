package application;

import character.EnemyType;
import character.King;
import character.SlimeEnemy;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * WaveManager sınıfı, her dalgadaki düşmanların zamanlamalı şekilde spawn edilmesinden sorumludur.
 * Oyun içindeki akışı kontrol eder, UI'yi günceller ve dalgalar arası gecikmeleri yönetir.
 */
public class WaveManager {

    private final List<Wave> waves;
    private final Map map;
    private final GameState gameState;
    private King king;

    private int currentWaveIndex = 0;

    // Zamanlayıcılar
    private Timeline waveTimeline;
    private Timeline spawnTimeline;
    private Timeline startDelayTimeline;
    private Timeline waitForNextWaveTimeline;

    /**
     * Yeni bir WaveManager nesnesi oluşturur.
     * @param waves Dalga listesi
     * @param map Oyun haritası
     * @param gameState Oyun durumu
     * @param king Oyundaki ana karakter (canı temsil eder)
     */
    public WaveManager(List<Wave> waves, Map map, GameState gameState, King king) {
        this.waves = waves;
        this.map = map;
        this.gameState = gameState;
        this.king = king;
    }

    /**
     * Oyun başladığında ilk dalgayı başlatır.
     */
    public void start(Stage stage, UIManager uiManager) {
        waveTimeline = new Timeline();
        waveTimeline.setCycleCount(Timeline.INDEFINITE);

        // Oyun başlangıcında kısa gecikme (örneğin harita yüklenmesi için)
        Timeline delayBeforeStart = new Timeline(
                new KeyFrame(Duration.seconds(2), e -> nextWave(uiManager))
        );
        delayBeforeStart.setCycleCount(1);
        delayBeforeStart.play();
    }

    /**
     * Bir sonraki dalgayı başlatır. Eğer tüm dalgalar bitti ise kazanan durumuna geçer.
     */
    private void nextWave(UIManager uiManager) {
        if (currentWaveIndex >= waves.size()) {
            waitUntilAllEnemiesDead();
            return;
        }

        Wave wave = waves.get(currentWaveIndex);

        // UI tarafında dalga geri sayımı başlatılır
        uiManager.startWaveCountdown(wave.getStartDelay(), currentWaveIndex);
        gameState.advanceToNextWave();
        uiManager.updateUI();

        // Düşmanları spawn etmeye başla
        spawnWaveEnemies(wave, uiManager.getOverlayPane(), uiManager);
        currentWaveIndex++;
    }

    /**
     * Verilen dalgaya göre düşmanları sahneye sırayla ekler.
     */
    private void spawnWaveEnemies(Wave wave, Pane pane, UIManager uiManager) {
        List<int[]> pathList = PathUtils.getPathList(map);
        int tileSize = map.getTileSize();

        int totalEnemies = wave.getSlowCount() + wave.getNormalCount() + wave.getFastCount();

        // Tüm düşman tiplerini dalga ayarına göre sıraya koy
        List<EnemyType> spawnOrder = new java.util.ArrayList<>();
        for (int i = 0; i < wave.getSlowCount(); i++) spawnOrder.add(EnemyType.SLOW);
        for (int i = 0; i < wave.getNormalCount(); i++) spawnOrder.add(EnemyType.NORMAL);
        for (int i = 0; i < wave.getFastCount(); i++) spawnOrder.add(EnemyType.FAST);

        java.util.Collections.shuffle(spawnOrder); // Düşman sırasını karıştır

        spawnTimeline = new Timeline();
        spawnTimeline.setCycleCount(totalEnemies);

        final int[] counter = {0};

        KeyFrame frame = new KeyFrame(Duration.seconds(wave.getDelayBetweenEnemies()), e -> {
            if (gameState.isGameOver()) return;

            EnemyType type = spawnOrder.get(counter[0]);
            SlimeEnemy slime = new SlimeEnemy(type);
            counter[0]++;

            PathUtils.attachPathTransition(slime, pathList, tileSize, map, () -> {
                if (gameState.isGameOver()) return;

                int dmg = slime.getDamage();
                king.takeDamage(dmg);
                for (int i = 0; i < dmg; i++) {
                    gameState.loseLife();
                }

                // Düşman sahneden kaldırılır
                Platform.runLater(() -> pane.getChildren().remove(slime.getGroup()));
                EnemyManager.removeEnemy(slime, pane);
            });

            // Düşman başlangıç konumunu ayarla
            int[] first = pathList.get(0);
            double spacing = map.getGridSpacing();
            double startX = first[1] * (tileSize + spacing) + tileSize / 2.0 - 20;
            double startY = first[0] * (tileSize + spacing) + tileSize / 2.0 - 20;
            slime.getGroup().setTranslateX(startX);
            slime.getGroup().setTranslateY(startY);

            EnemyManager.addEnemy(slime, pane);
        });

        spawnTimeline.getKeyFrames().add(frame);

        // Düşmanlar start delay süresi kadar bekledikten sonra spawn edilmeye başlar
        startDelayTimeline = new Timeline(new KeyFrame(Duration.seconds(wave.getStartDelay()), ev -> spawnTimeline.play()));
        startDelayTimeline.play();

        // Bir sonraki dalga için otomatik geçiş ayarlanır
        waitForNextWaveTimeline = new Timeline(new KeyFrame(
                Duration.seconds(wave.getStartDelay() + wave.getDelayBetweenEnemies() * totalEnemies + 2),
                ev -> nextWave(uiManager)
        ));
        waitForNextWaveTimeline.play();
    }

    /**
     * Tüm düşmanların öldüğünü algılamak için döngüsel kontrol başlatır.
     * Eğer hiç düşman kalmazsa oyunu kazandırır.
     */
    private void waitUntilAllEnemiesDead() {
        Timeline checkEnemies = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            if (EnemyManager.getActiveEnemies().isEmpty()) {
                gameState.setGameWon(true);
            }
        }));
        checkEnemies.setCycleCount(Animation.INDEFINITE);
        checkEnemies.play();
    }

    /**
     * Tüm zamanlayıcıları durdurur. Genellikle oyun durduğunda çağrılır.
     */
    public void stopAllWaves() {
        if (waveTimeline != null) waveTimeline.stop();
        if (spawnTimeline != null) spawnTimeline.stop();
        if (startDelayTimeline != null) startDelayTimeline.stop();
        if (waitForNextWaveTimeline != null) waitForNextWaveTimeline.stop();
    }

    /**
     * Kral (can temsilcisi) nesnesini sonradan değiştirmek için kullanılır.
     */
    public void setKing(King king) {
        this.king = king;
    }
}
