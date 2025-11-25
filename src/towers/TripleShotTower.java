package towers;

import application.ExplosionEffect;
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
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TripleShotTower, aynı anda maksimum 3 düşmana mermi fırlatabilen bir kuledir.
 * Her hedefe ayrı mermi gönderir ve çarpma noktasında patlama animasyonu oynatır.
 */
public class TripleShotTower extends Tower {

    private final double fireCooldown = 1.2; // saniye
    private long lastFireTime = 0;

    private Group node;
    private Rectangle body;
    private Polygon outerOctagon, innerOctagon;

    /**
     * Kule seçim butonları için görsel ön izleme oluşturur.
     */
    public static Node getPreviewShape() {
        Group g = new Group();

        Polygon outerOctagon = createOctagon(0, 0, 22);
        outerOctagon.setFill(Color.GRAY);
        outerOctagon.setStroke(Color.BLACK);
        outerOctagon.setStrokeWidth(2);

        Polygon innerOctagon = createOctagon(0, 0, 15);
        innerOctagon.setFill(Color.GRAY.brighter());
        innerOctagon.setStroke(Color.BLACK);
        innerOctagon.setStrokeWidth(2);

        Rectangle body = new Rectangle(-12.5, -12.5, 25, 25);
        body.setFill(Color.DARKRED);
        body.setArcWidth(15);
        body.setArcHeight(15);
        body.setStroke(Color.BLACK);
        body.setStrokeWidth(2);

        g.getChildren().addAll(outerOctagon, innerOctagon, body);
        return g;
    }

    /**
     * Yeni bir TripleShotTower oluşturur.
     */
    public TripleShotTower(double x, double y) {
        super(x, y);
        this.range = 100;
        this.damage = 8;
        this.cost = 150;

        outerOctagon = createOctagon(0, 0, 22);
        outerOctagon.setFill(Color.GRAY);
        outerOctagon.setStroke(Color.BLACK);
        outerOctagon.setStrokeWidth(2);

        innerOctagon = createOctagon(0, 0, 15);
        innerOctagon.setFill(Color.GRAY.brighter());
        innerOctagon.setStroke(Color.BLACK);
        innerOctagon.setStrokeWidth(2);

        body = new Rectangle(-12.5, -12.5, 25, 25);
        body.setFill(Color.DARKRED);
        body.setArcWidth(15);
        body.setArcHeight(15);
        body.setStroke(Color.BLACK);
        body.setStrokeWidth(2);

        node = new Group(outerOctagon, innerOctagon, body);
        this.towerShape = node;
    }

    /**
     * Her frame çağrılır. 3 düşman seçer ve her birine mermi fırlatır.
     */
    @Override
    public void update(List<SlimeEnemy> enemies) {
        long now = System.currentTimeMillis();
        if (now - lastFireTime < fireCooldown * 1000) return;

        List<SlimeEnemy> targets = enemies.stream()
                .filter(e -> !e.isDead() && isInRange(e))
                .sorted(Comparator.comparingDouble(this::getDistanceTo))
                .limit(3)
                .collect(Collectors.toList());

        if (!targets.isEmpty()) {
            for (SlimeEnemy target : targets) {
                shoot(target);
            }
            lastFireTime = now;
        }
    }

    /**
     * Her hedefe ayrı mermi gönderir ve hedefe ulaştığında patlama efektini tetikler.
     */
    private void shoot(SlimeEnemy target) {
        Circle bullet = new Circle(6, Color.ORANGE);
        Pane grid = TowerManager.getOverlayPane();
        if (grid == null) return;

        Point2D towerCenterScene = towerShape.localToScene(
                towerShape.getBoundsInLocal().getWidth() / 2,
                towerShape.getBoundsInLocal().getHeight() / 2
        );

        Point2D localStart = grid.sceneToLocal(towerCenterScene);
        bullet.setTranslateX(localStart.getX());
        bullet.setTranslateY(localStart.getY());
        grid.getChildren().add(bullet);

        final Timeline[] timelineRef = new Timeline[1];

        Timeline missileTimeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (target.isDead()) {
                grid.getChildren().remove(bullet);
                if (timelineRef[0] != null) timelineRef[0].stop();
                return;
            }

            Point2D targetPos = getScenePosition(target.getGroup());
            Point2D localTarget = grid.sceneToLocal(targetPos);

            double dx = localTarget.getX() - bullet.getTranslateX();
            double dy = localTarget.getY() - bullet.getTranslateY();
            double dist = Math.sqrt(dx * dx + dy * dy);

            // Menzil dışı kontrol
            Point2D bulletScene = grid.localToScene(bullet.getTranslateX(), bullet.getTranslateY());
            double bx = bulletScene.getX() - towerCenterScene.getX();
            double by = bulletScene.getY() - towerCenterScene.getY();
            double bulletDistance = Math.sqrt(bx * bx + by * by);

            if (bulletDistance > range) {
                grid.getChildren().remove(bullet);
                if (timelineRef[0] != null) timelineRef[0].stop();
                return;
            }

            if (dist < 5) {
                grid.getChildren().remove(bullet);
                target.takeDamage(damage, () -> grid.getChildren().remove(target.getGroup()));
                ExplosionEffect.play(localTarget.getX(), localTarget.getY(), grid);
                if (timelineRef[0] != null) timelineRef[0].stop();
                return;
            }

            double step = 5;
            bullet.setTranslateX(bullet.getTranslateX() + (dx / dist) * step);
            bullet.setTranslateY(bullet.getTranslateY() + (dy / dist) * step);
        }));

        missileTimeline.setCycleCount(Timeline.INDEFINITE);
        missileTimeline.play();
        timelineRef[0] = missileTimeline;
    }

    /**
     * Seviye yükseldiğinde gövde rengi güncellenir.
     */
    @Override
    protected void updateVisualsByLevel() {
        switch (level) {
            case 2 -> body.setFill(Color.LIMEGREEN);
            case 3 -> body.setFill(Color.AQUA);
        }
    }

    /**
     * Düşmanın kuleye olan mesafesini döndürür.
     */
    private double getDistanceTo(SlimeEnemy enemy) {
        double dx = enemy.getGroup().getTranslateX() - x;
        double dy = enemy.getGroup().getTranslateY() - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Bir Node'un sahne merkez konumunu döndürür.
     */
    private Point2D getScenePosition(Node node) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        return new Point2D(
                bounds.getMinX() + bounds.getWidth() / 2,
                bounds.getMinY() + bounds.getHeight() / 2
        );
    }

    /**
     * Menzil kontrolü için override edilmiş versiyon.
     * Tower'daki versiyonla aynı işlevi görüyor.
     */
    @Override
    public boolean isInRange(SlimeEnemy e) {
        return super.isInRange(e);
    }
}
