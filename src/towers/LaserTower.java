package towers;

import application.EnemyManager;
import application.TowerManager;
import character.SlimeEnemy;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * LaserTower sınıfı, sürekli olarak düşmanlara hasar veren lazer ışınları üretir.
 * Düşmanlara animasyonlu lazer çizer, lazer ucunda hasar verir ve gerektiğinde kaldırır.
 */
public class LaserTower extends Tower {

    private Timeline laserTimeline;
    private final List<Line> laserLines = new ArrayList<>();
    private final List<Line> animatedLasers = new ArrayList<>();
    private final List<Timeline> laserAnimations = new ArrayList<>();
    private static final List<Line> activeLasers = new ArrayList<>();

    private Group node;
    private Polygon outerOctagon, innerOctagon, innerCore;
    private Circle centerCore;

    /**
     * Butonlarda gösterilecek lazer kule ön izlemesi.
     */
    public static Node getPreviewShape() {
        Group g = new Group();

        Polygon outerOctagon = createOctagon(0, 0, 22);
        outerOctagon.setFill(Color.GRAY);
        outerOctagon.setStroke(Color.BLACK);
        outerOctagon.setStrokeWidth(2);

        Polygon innerOctagon = createOctagon(0, 0, 17);
        innerOctagon.setFill(Color.GRAY.brighter());
        innerOctagon.setStroke(Color.BLACK);
        innerOctagon.setStrokeWidth(2);

        Polygon innerCore = createOctagon(0, 0, 12);
        innerCore.setFill(Color.GRAY.brighter().brighter());
        innerCore.setStroke(Color.BLACK);
        innerCore.setStrokeWidth(2);

        Circle centerCore = new Circle(0, 0, 7, Color.RED);
        centerCore.setStroke(Color.BLACK);
        centerCore.setStrokeWidth(2);

        g.getChildren().addAll(outerOctagon, innerOctagon, innerCore, centerCore);
        return g;
    }

    /**
     * Yeni bir lazer kulesi oluşturur.
     */
    public LaserTower(double x, double y) {
        super(x, y);
        this.range = 100;
        this.damage = 1;
        this.cost = 120;

        outerOctagon = createOctagon(0, 0, 22);
        outerOctagon.setFill(Color.GRAY);
        outerOctagon.setStroke(Color.BLACK);
        outerOctagon.setStrokeWidth(2);

        innerOctagon = createOctagon(0, 0, 17);
        innerOctagon.setFill(Color.GRAY.brighter());
        innerOctagon.setStroke(Color.BLACK);
        innerOctagon.setStrokeWidth(2);

        innerCore = createOctagon(0, 0, 12);
        innerCore.setFill(Color.GRAY.brighter().brighter());
        innerCore.setStroke(Color.BLACK);
        innerCore.setStrokeWidth(2);

        centerCore = new Circle(0, 0, 7, Color.RED);
        centerCore.setStroke(Color.BLACK);
        centerCore.setStrokeWidth(2);

        node = new Group(outerOctagon, innerOctagon, innerCore, centerCore);
        this.towerShape = node;

        // Her 100ms'de bir lazer efektlerini güncelle
        laserTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> updateLasers()));
        laserTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    /**
     * Güncelleme burada yapılmıyor, lazerler kendi timeline'ı içinde döner.
     */
    @Override
    public void update(List<SlimeEnemy> enemies) {
        // handled by laserTimeline
    }

    /**
     * Kule sahneye yerleştirildiğinde lazer başlasın, kaldırıldığında dursun.
     */
    @Override
    public void setPlaced(boolean placed) {
        super.setPlaced(placed);
        if (placed) {
            laserTimeline.play();
        } else {
            laserTimeline.stop();
        }
    }

    /**
     * Seviye arttıkça lazerin merkezi renk değiştirsin.
     */
    @Override
    protected void updateVisualsByLevel() {
        Color color = getLaserColorByLevel(level);
        centerCore.setFill(color);
    }

    /**
     * Lazer çizgilerini günceller ve her uygun düşmana hasar uygular.
     */
    private void updateLasers() {
        if (!(towerShape.getParent() instanceof Pane pane)) return;

        pane.getChildren().removeAll(laserLines);
        pane.getChildren().removeAll(animatedLasers);
        laserLines.clear();
        animatedLasers.clear();

        List<SlimeEnemy> enemies = EnemyManager.getEnemies();
        Color laserColor = getLaserColorByLevel(level);

        for (SlimeEnemy e : enemies) {
            if (e.isDead() || !isInRange(e)) continue;

            // Kule ve düşman merkezleri
            Point2D start = getSceneCenter(towerShape);
            Point2D end = getSceneCenter(e.getGroup());

            Point2D localStart = TowerManager.getOverlayPane().sceneToLocal(start);
            Point2D localEnd = TowerManager.getOverlayPane().sceneToLocal(end);

            // Ölüm animasyonu için düşmanın parent koordinatına çevir
            Point2D sceneDeathPos = TowerManager.getOverlayPane().localToScene(localEnd);
            Point2D localDeathPos = ((Pane) e.getGroup().getParent()).sceneToLocal(sceneDeathPos);

            // Lazer çizgisi: düz ve animasyonlu
            Line solid = new Line(localStart.getX(), localStart.getY(), localEnd.getX(), localEnd.getY());
            solid.setStroke(laserColor.darker());
            solid.setStrokeWidth(3);
            solid.setOpacity(0.75);

            Line dash = new Line(localStart.getX(), localStart.getY(), localEnd.getX(), localEnd.getY());
            dash.setStroke(laserColor.brighter());
            dash.setStrokeWidth(2);
            dash.setOpacity(0.9);
            dash.getStrokeDashArray().addAll(12.0, 12.0);

            // Animasyonlu çizgi hareket ettiriliyor
            Timeline anim = new Timeline(new KeyFrame(Duration.millis(20), ev -> {
                dash.setStrokeDashOffset(dash.getStrokeDashOffset() + 2);
            }));
            anim.setCycleCount(Timeline.INDEFINITE);
            anim.play();

            // Hasar ver → lazer efektinde ses/patlama yok
            e.takeDamage(damage, true, true, localDeathPos.getX(), localDeathPos.getY(), () -> {
                if (e.isDead()) pane.getChildren().remove(e.getGroup());
            });

            pane.getChildren().addAll(solid, dash);
            laserLines.add(solid);
            animatedLasers.add(dash);
            activeLasers.add(solid);
            activeLasers.add(dash);
            laserAnimations.add(anim);
        }
    }

    /**
     * Düşmanın menzil içinde olup olmadığını kontrol eder.
     */
    @Override
    public boolean isInRange(SlimeEnemy e) {
        return super.isInRange(e);
    }

    /**
     * Lazer çizgilerini durdurur ve sahneden kaldırır.
     */
    public void pauseLasers() {
        if (laserTimeline != null) laserTimeline.stop();
        clearLaserGraphics();
    }

    /**
     * Lazer çizgilerini yeniden başlatır.
     */
    public void resumeLasers() {
        if (isPlaced() && laserTimeline != null) laserTimeline.play();
    }

    /**
     * Sahnedeki tüm lazer çizgilerini siler.
     */
    private void clearLaserGraphics() {
        if (towerShape.getParent() instanceof Pane pane) {
            pane.getChildren().removeAll(laserLines);
            pane.getChildren().removeAll(animatedLasers);
        }
        laserLines.clear();
        animatedLasers.clear();
        laserAnimations.forEach(Timeline::stop);
        laserAnimations.clear();
    }

    /**
     * Lazerin seviyeye göre rengini döndürür.
     */
    private Color getLaserColorByLevel(int level) {
        return switch (level) {
            case 1 -> Color.RED;
            case 2 -> Color.LIMEGREEN;
            case 3 -> Color.AQUA;
            default -> Color.DARKRED;
        };
    }

    /**
     * Tüm lazerleri global olarak temizler (örn: level geçince).
     */
    public static void clearAllLasers(Pane pane) {
        for (Line laser : activeLasers) {
            pane.getChildren().remove(laser);
        }
        activeLasers.clear();
    }

    /**
     * Lazer çizimlerini ve güncellemeleri durdurur.
     */
    public void stopLasers() {
        if (laserTimeline != null) laserTimeline.stop();
        clearLaserGraphics();
    }

    /**
     * Yardımcı: Node'un sahne merkezini döndürür.
     */
    private Point2D getSceneCenter(Node node) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        return new Point2D(bounds.getMinX() + bounds.getWidth() / 2,
                           bounds.getMinY() + bounds.getHeight() / 2);
    }
}
