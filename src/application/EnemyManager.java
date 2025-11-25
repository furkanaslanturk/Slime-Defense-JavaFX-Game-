package application;

import character.SlimeEnemy;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * EnemyManager sınıfı, oyunda aktif olan tüm düşmanları yönetir.
 * - Ekleme, silme, temizleme işlemleri yapılır.
 * - Düşman listesi diğer sınıflar tarafından güncel olarak erişilebilir.
 */
public class EnemyManager {

    // Tüm aktif düşmanlar burada tutulur
    private static final List<SlimeEnemy> enemies = new ArrayList<>();

    /**
     * Yeni bir düşmanı oyun sahnesine ve listeye ekler.
     *
     * @param enemy Eklenecek düşman
     * @param root  Ekleneceği sahne (Pane)
     */
    public static void addEnemy(SlimeEnemy enemy, Pane root) {
        enemies.add(enemy);
        root.getChildren().add(enemy.getGroup());
    }

    /**
     * Belirli bir düşmanı sahneden ve listeden kaldırır.
     *
     * @param slime Silinecek düşman
     * @param pane  Sahne (null olabilir)
     */
    public static void removeEnemy(SlimeEnemy slime, Pane pane) {
        enemies.removeIf(e -> e == slime);  // Kimlik karşılaştırması (==)

        if (pane != null) {
            pane.getChildren().remove(slime.getGroup());
        }
    }

    /**
     * Tüm düşman listesini döndürür.
     */
    public static List<SlimeEnemy> getEnemies() {
        return enemies;
    }

    /**
     * Sadece hayatta olan düşmanları döndürür.
     */
    public static List<SlimeEnemy> getActiveEnemies() {
        return enemies.stream()
                      .filter(e -> !e.isDead())
                      .toList();
    }

    /**
     * Ölü düşmanları listeden temizler (sahneden kaldırmaz).
     */
    public static void updateEnemyList() {
        Iterator<SlimeEnemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            SlimeEnemy e = iterator.next();
            if (e.isDead()) {
                iterator.remove();
            }
        }
    }

    /**
     * Tüm düşmanları hem sahneden hem listeden tamamen temizler.
     *
     * @param pane Düşmanların eklendiği sahne
     */
    public static void clearAll(Pane pane) {
        for (SlimeEnemy e : enemies) {
            pane.getChildren().remove(e.getGroup());
        }
        enemies.clear();
    }
}
