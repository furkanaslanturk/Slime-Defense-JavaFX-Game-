package application;

import character.King;
import towers.Tower;

/**
 * GameState sınıfı, oyunun mevcut durumunu (para, can, dalga bilgisi, oyun sonu vs.)
 * takip eder ve oyun akışında merkezi bir kontrol noktası sağlar.
 */
public class GameState {

    private int money;                   // Oyuncunun parası
    private int lives;                   // Oyuncunun canı
    private int currentWaveIndex;       // Şu anki dalga numarası
    private boolean gameOver;           // Oyun bitti mi?
    private boolean gameWon;            // Oyuncu kazandı mı?

    private UIManager uiManager;        // UI ile iletişim için referans
    private King king;                  // Kral (can sembolü)
    private WaveManager waveManager;    // Dalgaları yöneten sınıf

    /**
     * Yeni bir oyun durumu başlatır.
     *
     * @param startingMoney Başlangıç parası
     */
    public GameState(int startingMoney) {
        this.money = startingMoney;
        this.lives = 5;
        this.currentWaveIndex = 0;
        this.gameOver = false;
        this.gameWon = false;
    }

    // --------------------------
    // Para işlemleri
    // --------------------------

    public int getMoney() {
        return money;
    }

    public void addMoney(int amount) {
        this.money += amount;
    }

    public boolean spendMoney(int amount) {
        if (money >= amount) {
            money -= amount;
            return true;
        }
        return false;
    }

    public void addMoneyForKill(int amount) {
        addMoney(amount);
        if (uiManager != null) {
            uiManager.updateUI();  // Arayüzde para güncelle
        }
    }

    // --------------------------
    // Can (lives) işlemleri
    // --------------------------

    public int getLives() {
        return lives;
    }

    public void loseLife() {
        if (gameOver) return;

        lives--;

        if (lives <= 0) {
            gameOver = true;

            if (waveManager != null) waveManager.stopAllWaves();

            // Tüm düşmanları sahneden sil
            EnemyManager.clearAll(TowerManager.getOverlayPane());

            // Tüm kuleleri sahneden sil
            for (Tower tower : TowerManager.getTowers()) {
                TowerManager.getOverlayPane().getChildren().remove(tower.getTowerShape());
            }
            TowerManager.clear();

            // Kralı öldür (animasyonsuz kalıcı)
            if (king != null) king.dieForever();
        }

        if (uiManager != null) {
            uiManager.updateUI();
        }
    }

    // --------------------------
    // Dalga (wave) işlemleri
    // --------------------------

    public int getCurrentWaveIndex() {
        return currentWaveIndex;
    }

    public void advanceToNextWave() {
        currentWaveIndex++;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public void setWaveManager(WaveManager waveManager) {
        this.waveManager = waveManager;
    }

    // --------------------------
    // Diğer yapılandırmalar
    // --------------------------

    public void setUIManager(UIManager uiManager) {
        this.uiManager = uiManager;
    }

    public void setKing(King king) {
        this.king = king;
    }

    // --------------------------
    // Oyun sonucu
    // --------------------------

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    /**
     * Oyunun durumunu tamamen sıfırlar (para, can, kuleler).
     */
    public void reset() {
        this.money = 100;
        this.lives = 5;
        this.currentWaveIndex = 0;
        this.gameOver = false;
        this.gameWon = false;
        TowerManager.clear(); // Tüm kuleleri kaldır
    }
}
