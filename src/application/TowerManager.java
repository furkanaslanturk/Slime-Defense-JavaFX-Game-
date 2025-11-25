package application;

import character.SlimeEnemy;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import towers.Tower;

import java.util.ArrayList;
import java.util.List;

/**
 * TowerManager sınıfı, oyundaki tüm kulelerin davranışlarını merkezi olarak yönetir.
 * Kuleleri sahneye ekler, günceller, temizler ve lazer görselleri gibi özel durumları kontrol eder.
 */
public class TowerManager {

    private static final List<Tower> towers = new ArrayList<>();
    private static Pane overlayPane;  // UIManager üzerinden gelen sahne üstü çizim alanı

    // Tüm kuleleri her frame'de güncelleyen zamanlayıcı
    private static final AnimationTimer towerUpdateTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            List<SlimeEnemy> enemies = EnemyManager.getEnemies();
            if (enemies == null || towers.isEmpty()) return;

            for (Tower tower : towers) {
                if (tower.isPlaced()) {
                    tower.update(enemies);
                }
            }
        }
    };

    /**
     * Kule sahneleri, lazer çizgileri vs. için kullanılacak overlay pane'i set eder.
     */
    public static void setOverlayPane(Pane pane) {
        overlayPane = pane;
    }

    /**
     * Şu anki overlayPane'i döndürür.
     */
    public static Pane getOverlayPane() {
        return overlayPane;
    }

    /**
     * Oyuna yeni bir kule ekler.
     * @param tower Yeni kule nesnesi
     */
    public static void addTower(Tower tower) {
        towers.add(tower);
    }

    /**
     * Oyundan bir kuleyi kaldırır.
     * @param tower Kaldırılacak kule
     */
    public static void removeTower(Tower tower) {
        towers.remove(tower);
    }

    /**
     * Tüm kuleleri ve görsellerini sahneden temizler.
     * Genellikle level yeniden başlatıldığında çağrılır.
     */
    public static void clear() {
        if (overlayPane != null) {
            for (Tower tower : towers) {
                overlayPane.getChildren().remove(tower.getTowerShape());
            }
        }
        towers.clear();
    }

    /**
     * Oyun başladığında kule davranışlarını çalıştırır.
     */
    public static void start() {
        towerUpdateTimer.start();
    }

    /**
     * Oyun durduğunda kule güncellemelerini durdurur.
     */
    public static void stop() {
        towerUpdateTimer.stop();
    }

    /**
     * Mevcut sahnedeki tüm kuleleri listeler.
     * Genellikle debug veya analiz için kullanılır.
     */
    public static List<Tower> getTowers() {
        return towers;
    }
}
