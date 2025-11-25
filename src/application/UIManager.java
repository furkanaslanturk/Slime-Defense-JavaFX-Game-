package application;

import character.SlimeEnemy;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import towers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * UIManager sınıfı, kullanıcı arayüzünün merkezidir.
 * - Kule yerleştirme panelini ve butonlarını oluşturur.
 * - Kulelerin sahneye yerleştirilmesini, sürüklenmesini ve satışını yönetir.
 * - Oyun durumuna göre para/can bilgilerini günceller.
 * - Kule yükseltme işlemlerini yürütür.
 */
public class UIManager {

    // --- Arayüz Bileşenleri ve Oyun Nesneleri ---

    private final VBox towerPanel;              // Sağ paneldeki kule butonlarının bulunduğu kutu
    private final Label moneyLabel, livesLabel, waveLabel; // Bilgi etiketleri
    private final GameState gameState;          // Oyun durumu referansı (para, can vs.)
    private final Map map;                      // Oyun haritası (grid sistemi)
    private final Pane overlayPane = new Pane(); // Kulelerin ve efektlerin çizileceği alan
    private Rectangle sellArea;                 // Kulelerin sürüklenip satılabileceği alan

    // --- Kule Yerleştirme/Yönetimi ---

    private Tower selectedTower = null;         // Seçilen kule nesnesi
    private Circle rangeCircle = null;          // Seçilen kulenin menzil çemberi

    // --- Upgrade Sistemi ---

    private boolean upgradeMode = false;        // Upgrade modu aktif mi?
    private final List<Circle> upgradeCircles = new ArrayList<>();
    private final List<StackPane> upgradeButtons = new ArrayList<>();

    // --- Kule Butonları ---

    private static class ButtonData {
        StackPane button;
        Rectangle bg;
        int cost;

        ButtonData(StackPane button, Rectangle bg, int cost) {
            this.button = button;
            this.bg = bg;
            this.cost = cost;
        }
    }

    private final List<ButtonData> towerButtons = new ArrayList<>();

    // --- Constructor ---

    /**
     * UIManager oluşturur.
     * @param gameState Oyun durumu
     * @param map Oyun haritası
     */
    public UIManager(GameState gameState, Map map) {
        this.gameState = gameState;
        this.map = map;
        this.towerPanel = new VBox(20);
        towerPanel.setStyle("-fx-background-color: FAF1DA; -fx-padding: 20;");
        towerPanel.setPrefWidth(250);
        towerPanel.setAlignment(Pos.CENTER);

        // Etiketleri oluştur ve panele ekle
        moneyLabel = new Label("Money: $" + gameState.getMoney());
        livesLabel = new Label("Lives: " + gameState.getLives());
        waveLabel = new Label();

        for (Label label : new Label[]{moneyLabel, livesLabel, waveLabel}) {
            label.setFont(Font.font(16));
            label.setTextFill(Color.web("#543D1E"));
        }

        towerPanel.getChildren().addAll(moneyLabel, livesLabel, waveLabel);
        createTowerButtons();
        overlayPane.setPickOnBounds(false); // Overlay'de tıklanabilir boşluklar olacak
    }

    // --- Kule Butonları ---

    /**
     * Kule butonlarını oluşturur.
     */
    private void createTowerButtons() {
        addTowerButton("Single Shot - $50", () -> new SingleShotTower(0, 0), 50);
        addTowerButton("Laser Tower - $120", () -> new LaserTower(0, 0), 120);
        addTowerButton("Triple Shot - $150", () -> new TripleShotTower(0, 0), 150);
        addTowerButton("Missile - $200", () -> new MissileLauncherTower(0, 0), 200);
    }

    /**
     * Her bir kule için buton oluşturur.
     */
    private void addTowerButton(String labelText, Supplier<Tower> towerSupplier, int cost) {
        Node preview = switch (labelText) {
            case "Single Shot - $50" -> SingleShotTower.getPreviewShape();
            case "Laser Tower - $120" -> LaserTower.getPreviewShape();
            case "Triple Shot - $150" -> TripleShotTower.getPreviewShape();
            case "Missile - $200" -> MissileLauncherTower.getPreviewShape();
            default -> null;
        };

        StackPane button = new StackPane();
        Rectangle bg = new Rectangle(175, 100);
        bg.setArcHeight(10);
        bg.setArcWidth(10);
        bg.setStroke(Color.web("#ECCC88"));
        bg.setStrokeWidth(3);
        bg.setFill(Color.web("#F2D79D"));

        Label label = new Label(labelText);
        VBox vb = new VBox(preview, label);
        vb.setAlignment(Pos.CENTER);
        button.getChildren().addAll(bg, vb);

        button.setOnMouseEntered(e -> {
            if (gameState.getMoney() >= cost) {
                bg.setFill(Color.web("#E5C98A"));
            }
        });

        button.setOnMouseExited(e -> {
            if (gameState.getMoney() >= cost) {
                bg.setFill(Color.web("#F2D79D"));
            }
        });

        button.setOnMouseClicked(event -> {
            if (gameState.getMoney() < cost) return;

            // Önceden seçili kule varsa sahneden sil
            if (selectedTower != null && !selectedTower.isPlaced()) {
                overlayPane.getChildren().removeAll(selectedTower.getTowerShape(), rangeCircle);
                selectedTower = null;
                rangeCircle = null;
            }

            Tower tower = towerSupplier.get();
            selectedTower = tower;

            // Menzil çemberi oluştur
            rangeCircle = new Circle(tower.getRange(), Color.TRANSPARENT);
            rangeCircle.setStroke(Color.DARKRED);
            rangeCircle.setStrokeWidth(2);
            rangeCircle.getStrokeDashArray().addAll(10.0, 10.0);

            // Kuleyi mouse konumuna yerleştir
            Point2D local = overlayPane.sceneToLocal(event.getSceneX(), event.getSceneY());
            tower.getTowerShape().setTranslateX(local.getX());
            tower.getTowerShape().setTranslateY(local.getY());
            rangeCircle.setTranslateX(local.getX());
            rangeCircle.setTranslateY(local.getY());

            overlayPane.getChildren().removeAll(tower.getTowerShape(), rangeCircle);
            overlayPane.getChildren().addAll(tower.getTowerShape(), rangeCircle);
        });

        towerPanel.getChildren().add(button);
        towerButtons.add(new ButtonData(button, bg, cost));
    }

    // --- Getter'lar ---

    public VBox getTowerPanel() {
        return towerPanel;
    }

    public Pane getOverlayPane() {
        return overlayPane;
    }

    public Rectangle getSellArea() {
        return this.sellArea;
    }

    public void setSellArea(Rectangle sellArea) {
        this.sellArea = sellArea;
    }

    // --- Kule Yerleştirme ---

    /**
     * Kuleyi sahnede fareyi takip ederek yerleştirir.
     */
    public void enablePlacementBehavior(StackPane container) {
        overlayPane.setOnMouseMoved(e -> {
            if (selectedTower != null) {
                Point2D local = overlayPane.sceneToLocal(e.getSceneX(), e.getSceneY());
                selectedTower.getTowerShape().setTranslateX(local.getX());
                selectedTower.getTowerShape().setTranslateY(local.getY());
                if (rangeCircle != null) {
                    rangeCircle.setTranslateX(local.getX());
                    rangeCircle.setTranslateY(local.getY());
                }
            }
        });

        overlayPane.setOnMouseClicked(e -> {
            if (selectedTower == null) return;

            // Satış alanı kontrolü
            if (sellArea != null) {
                Bounds bounds = sellArea.localToScene(sellArea.getBoundsInLocal());
                if (!bounds.contains(e.getSceneX(), e.getSceneY())) {
                    overlayPane.getChildren().removeAll(selectedTower.getTowerShape(), rangeCircle);
                    selectedTower = null;
                    rangeCircle = null;
                    return;
                }
            }

            // Grid'e tıklanırsa yerleştir
            Point2D clickPoint = map.getGridPane().sceneToLocal(e.getSceneX(), e.getSceneY());
            for (int row = 0; row < map.getHeight(); row++) {
                for (int col = 0; col < map.getWidth(); col++) {
                    Rectangle cell = map.getCell(row, col);
                    Bounds bounds = cell.getBoundsInParent();

                    if (bounds.contains(clickPoint)) {
                        if (map.isPathCell(row, col)) return;

                        double centerX = bounds.getMinX() + bounds.getWidth() / 2;
                        double centerY = bounds.getMinY() + bounds.getHeight() / 2;

                        for (Tower existing : TowerManager.getTowers()) {
                            if (Math.abs(existing.getX() - centerX) < 1 && Math.abs(existing.getY() - centerY) < 1)
                                return;
                        }

                        selectedTower.getTowerShape().setTranslateX(centerX);
                        selectedTower.getTowerShape().setTranslateY(centerY);
                        selectedTower.setX(centerX);
                        selectedTower.setY(centerY);
                        selectedTower.setPlaced(true);

                        if (gameState.spendMoney(selectedTower.getCost())) {
                            TowerManager.addTower(selectedTower);
                            makeTowerDraggable(selectedTower);
                            updateUI();
                        }

                        overlayPane.getChildren().remove(rangeCircle);
                        selectedTower = null;
                        rangeCircle = null;
                        return;
                    }
                }
            }
        });
    }

    // --- Kule Yükseltme Sistemi ---

    /**
     * Upgrade modunu açar veya kapatır. Her basışta toggle yapar.
     */
    public void toggleUpgradeMode() {
        if (!upgradeMode) {
            showUpgradeUI();
        } else {
            hideUpgradeUI();
        }
        upgradeMode = !upgradeMode;
    }

    /**
     * Tüm sahnedeki kuleler için yükseltme butonları ve menzil halkaları ekler.
     */
    private void showUpgradeUI() {
        for (Tower tower : TowerManager.getTowers()) {
            if (!tower.isPlaced()) continue;

            // Menzil çemberi
            Circle circle = new Circle(tower.getRange(), Color.TRANSPARENT);
            circle.setStroke(Color.DARKRED);
            circle.setStrokeWidth(2);
            circle.getStrokeDashArray().addAll(10.0, 10.0);
            circle.setTranslateX(tower.getX());
            circle.setTranslateY(tower.getY());
            circle.setMouseTransparent(true);
            upgradeCircles.add(circle);
            overlayPane.getChildren().add(circle);

            // Yükseltme butonu
            StackPane upgradeButton = createUpgradeButton(tower);
            upgradeButton.setTranslateX(tower.getX() - 30);
            upgradeButton.setTranslateY(tower.getY() - 40);
            upgradeButtons.add(upgradeButton);
            overlayPane.getChildren().add(upgradeButton);
            upgradeButton.toFront();
        }
    }

    /**
     * Upgrade UI öğelerini sahneden temizler.
     */
    void hideUpgradeUI() {
        overlayPane.getChildren().removeAll(upgradeCircles);
        overlayPane.getChildren().removeAll(upgradeButtons);
        upgradeCircles.clear();
        upgradeButtons.clear();
    }

    /**
     * Belirli bir kule için yükseltme butonu oluşturur.
     */
    private StackPane createUpgradeButton(Tower tower) {
        StackPane pane = new StackPane();
        pane.setPickOnBounds(false);
        pane.setMouseTransparent(false);

        Rectangle bg = new Rectangle(60, 25);
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        bg.setFill(Color.GOLD);

        Label label = new Label();
        if (tower.getLevel() >= 3) {
            label.setText("MAX");
        } else {
            label.setText("$" + tower.getUpgradeCost());
        }
        label.setFont(Font.font(12));
        label.setTextFill(Color.BLACK);

        pane.getChildren().addAll(bg, label);

        boolean canAfford = gameState.getMoney() >= tower.getUpgradeCost();
        if (!canAfford) {
            bg.setFill(Color.DARKGOLDENROD);
            label.setTextFill(Color.DARKRED);
        }

        pane.setOnMouseEntered(e -> {
            if (tower.getLevel() < 3 && canAfford) {
                bg.setFill(Color.DARKGOLDENROD);
            }
        });

        pane.setOnMouseExited(e -> {
            if (tower.getLevel() < 3 && canAfford) {
                bg.setFill(Color.GOLD);
            }
        });

        pane.setOnMouseClicked(e -> {
            if (tower.getLevel() >= 3) return;

            if (gameState.spendMoney(tower.getUpgradeCost())) {
                tower.upgrade();
                updateUI();
                hideUpgradeUI();
                showUpgradeUI();
            }
        });

        return pane;
    }

    // --- Kule Sürükleme ve Satma Sistemi ---

    /**
     * Belirli bir kuleyi sahnede sürüklenebilir hale getirir.
     */
    public void makeTowerDraggable(Tower tower) {
        Node shape = tower.getTowerShape();
        final double[] originalX = {tower.getX()};
        final double[] originalY = {tower.getY()};

        Circle dragRangeCircle = new Circle(tower.getRange(), Color.TRANSPARENT);
        dragRangeCircle.setStroke(Color.DARKRED);
        dragRangeCircle.setStrokeWidth(2);
        dragRangeCircle.getStrokeDashArray().addAll(10.0, 10.0);

        // Sürükleme başlangıcı
        shape.setOnMousePressed(e -> {
            if (upgradeMode) {
                hideUpgradeUI();
                upgradeMode = false;
            }

            if (tower instanceof LaserTower laser) {
                laser.pauseLasers();
            }

            shape.toFront();
            originalX[0] = tower.getX();
            originalY[0] = tower.getY();
            tower.setPlaced(false);

            dragRangeCircle.setTranslateX(originalX[0]);
            dragRangeCircle.setTranslateY(originalY[0]);
            overlayPane.getChildren().add(dragRangeCircle);

            e.consume();
        });

        // Sürüklenirken kule konumu güncellenir
        shape.setOnMouseDragged(e -> {
            Point2D local = overlayPane.sceneToLocal(e.getSceneX(), e.getSceneY());
            shape.setTranslateX(local.getX());
            shape.setTranslateY(local.getY());
            tower.setX(local.getX());
            tower.setY(local.getY());
            dragRangeCircle.setTranslateX(local.getX());
            dragRangeCircle.setTranslateY(local.getY());
            e.consume();
        });

        // Sürükleme bırakıldığında:
        shape.setOnMouseReleased(e -> {
            overlayPane.getChildren().remove(dragRangeCircle);

            Point2D clickPoint = map.getGridPane().sceneToLocal(e.getSceneX(), e.getSceneY());

            // Eğer satış alanına bırakıldıysa
            if (sellArea != null) {
                Bounds bounds = sellArea.localToScene(sellArea.getBoundsInLocal());
                if (!bounds.contains(e.getSceneX(), e.getSceneY())) {
                    overlayPane.getChildren().remove(shape);
                    TowerManager.removeTower(tower);
                    gameState.addMoney(tower.getCost());
                    updateUI();
                    return;
                }
            }

            // Haritada geçerli bir hücreye bırakılmış mı?
            for (int row = 0; row < map.getHeight(); row++) {
                for (int col = 0; col < map.getWidth(); col++) {
                    Rectangle cell = map.getCell(row, col);
                    Bounds cellBounds = cell.getBoundsInParent();

                    if (cellBounds.contains(clickPoint)) {
                        if (map.isPathCell(row, col)) {
                            resetPosition(shape, tower, originalX[0], originalY[0]);
                            return;
                        }

                        for (Tower existing : TowerManager.getTowers()) {
                            if (existing == tower) continue;
                            double ex = existing.getX();
                            double ey = existing.getY();
                            double cx = cellBounds.getMinX() + cellBounds.getWidth() / 2;
                            double cy = cellBounds.getMinY() + cellBounds.getHeight() / 2;

                            if (Math.abs(ex - cx) < 1 && Math.abs(ey - cy) < 1) {
                                resetPosition(shape, tower, originalX[0], originalY[0]);
                                return;
                            }
                        }

                        double cx = cellBounds.getMinX() + cellBounds.getWidth() / 2;
                        double cy = cellBounds.getMinY() + cellBounds.getHeight() / 2;

                        shape.setTranslateX(cx);
                        shape.setTranslateY(cy);
                        tower.setX(cx);
                        tower.setY(cy);
                        tower.setPlaced(true);

                        if (tower instanceof LaserTower laser) {
                            laser.resumeLasers();
                        }

                        e.consume();
                        return;
                    }
                }
            }

            // Hiçbir geçerli yere bırakılmadıysa eski yerine geri döndür
            resetPosition(shape, tower, originalX[0], originalY[0]);
        });
    }

    /**
     * Sürüklenen kule geçersiz yere bırakılırsa eski konumuna döndürülür.
     */
    private void resetPosition(Node shape, Tower tower, double x, double y) {
        shape.setTranslateX(x);
        shape.setTranslateY(y);
        tower.setX(x);
        tower.setY(y);
        tower.setPlaced(true);
    }

    // --- Dalga Sayaçları ve UI Güncelleme ---

    private Timeline waveCountdown;

    /**
     * Yeni dalga başlamadan önce sayaç başlatır.
     */
    public void startWaveCountdown(double seconds, int waveIndex) {
        if (waveCountdown != null) waveCountdown.stop();

        int[] remaining = {(int) seconds};
        waveLabel.setText("Wave " + (waveIndex + 1) + " in: " + remaining[0] + "s");

        waveCountdown = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> {
            remaining[0]--;
            if (remaining[0] <= 0) {
                waveLabel.setText("Wave " + (waveIndex + 1) + " started!");
            } else {
                waveLabel.setText("Wave " + (waveIndex + 1) + " in: " + remaining[0] + "s");
            }
        }));
        waveCountdown.setCycleCount((int) seconds);
        waveCountdown.play();
    }

    /**
     * Para ve can etiketlerini günceller.
     * Ayrıca buton renklerini paraya göre değiştirir.
     */
    public void updateUI() {
        moneyLabel.setText("Money: $" + gameState.getMoney());
        livesLabel.setText("Lives: " + gameState.getLives());

        for (ButtonData data : towerButtons) {
            if (gameState.getMoney() < data.cost) {
                data.bg.setFill(Color.web("#D3C1A2")); // Yetersiz
            } else {
                data.bg.setFill(Color.web("#F2D79D")); // Alınabilir
            }
        }
    }
}


