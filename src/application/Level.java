package application;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Soyut Level sınıfı.
 * Tüm seviye sınıfları (LevelOne, LevelTwo, LevelManager vs.) bu sınıftan türetilmelidir.
 * Ortak bileşenler ve davranışlar burada tanımlanır.
 */
public abstract class Level {
    // Her seviyede ortak olacak temel oyun bileşenleri
    protected Map map;                      // Harita yapısı
    protected GameState gameState;          // Oyun durumu (para, can, vb.)
    protected UIManager uiManager;          // Arayüz kontrolörü
    protected WaveManager waveManager;      // Düşman dalga yöneticisi

    /**
     * Her seviye kendi sahnesini (Scene) üretir. JavaFX sahnesi döner.
     * 
     * @param stage JavaFX Stage (pencere)
     * @return Bu seviyeye ait sahne
     */
    public abstract Scene getScene(Stage stage);

    /**
     * Seviyenin sahip olduğu oyun durumu nesnesini döner.
     * 
     * @return GameState örneği
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Her seviye kendi WaveManager’ını oluşturmalıdır.
     * 
     * @return WaveManager örneği
     */
    public abstract WaveManager getWaveManager();
}
