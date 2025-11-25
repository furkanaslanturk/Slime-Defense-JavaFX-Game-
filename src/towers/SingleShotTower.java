package towers;

import application.ExplosionEffect;
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
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.List;

/**
 * SingleShotTower sınıfı, hedefe tek mermi fırlatan basit bir taret kulesidir.
 * Döner namlusu ile düşmana yönelir ve belirli aralıklarla mermi fırlatır.
 */
public class SingleShotTower extends Tower {

    private final double fireCooldown = 0.8; // saniye cinsinden ateş aralığı
    private long lastFireTime = 0;

    private Group node;
    private Rectangle barrel, barrel1, body;
    private Rotate rotate;
    private Timeline rotationTimeline;
    private double currentAngle = 0;

    /**
     * Bu kuleye ait butonlarda gösterilecek örnek şekil.
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

        Rectangle body = new Rectangle(-8.5, -15, 17, 25);
        body.setFill(Color.DARKRED);
        body.setArcWidth(10);
        body.setArcHeight(10);
        body.setStroke(Color.BLACK);
        body.setStrokeWidth(2);

        Rectangle barrel = new Rectangle(-5, -30, 10, 20);
        barrel.setFill(Color.DARKGRAY);
        barrel.setArcWidth(6);
        barrel.setArcHeight(6);
        barrel.setStroke(Color.BLACK);
        barrel.setStrokeWidth(2);

        Rectangle barrel1 = new Rectangle(-6, -32, 12, 5);
        barrel1.setFill(Color.DARKGRAY.darker());
        barrel1.setArcWidth(2);
        barrel1.setArcHeight(2);
        barrel1.setStroke(Color.BLACK);
        barrel1.setStrokeWidth(2);

        g.getChildren().addAll(outerOctagon, innerOctagon, body, barrel, barrel1);
        return g;
    }

    /**
     * Yeni bir SingleShotTower oluşturur.
     */
    public SingleShotTower(double x, double y) {
        super(x, y);
        this.range = 100;
        this.damage = 10;
        this.cost = 50;

        Polygon outerOctagon = createOctagon(0, 0, 22);
        outerOctagon.setFill(Color.GRAY);
        outerOctagon.setStroke(Color.BLACK);
        outerOctagon.setStrokeWidth(2);

        Polygon innerOctagon = createOctagon(0, 0, 15);
        innerOctagon.setFill(Color.GRAY.brighter());
        innerOctagon.setStroke(Color.BLACK);
        innerOctagon.setStrokeWidth(2);

        body = new Rectangle(-8.5, -15, 17, 25);
        body.setFill(Color.DARKRED);
        body.setArcWidth(10);
        body.setArcHeight(10);
        body.setStroke(Color.BLACK);
        body.setStrokeWidth(2);

        barrel = new Rectangle(-5, -30, 10, 20);
        barrel.setFill(Color.DARKGRAY);
        barrel.setArcWidth(6);
        barrel.setArcHeight(6);
        barrel.setStroke(Color.BLACK);
        barrel.setStrokeWidth(2);

        barrel1 = new Rectangle(-6, -32, 12, 5);
        barrel1.setFill(Color.DARKGRAY.darker());
        barrel1.setArcWidth(2);
        barrel1.setArcHeight(2);
        barrel1.setStroke(Color.BLACK);
        barrel1.setStrokeWidth(2);

        // Dönüş kontrolü için Rotate objesi
        rotate = new Rotate(0, 0, 0);
        barrel.getTransforms().add(rotate);
        barrel1.getTransforms().add(rotate);
        body.getTransforms().add(rotate);

        node = new Group(outerOctagon, innerOctagon, body, barrel, barrel1);
        this.towerShape = node;
    }

    /**
     * Her frame çağrılır: hedef seçip ateş eder.
     */
    @Override
    public void update(List<SlimeEnemy> enemies) {
        long now = System.currentTimeMillis();

        SlimeEnemy closest = null;
        double minDist = Double.MAX_VALUE;

        for (SlimeEnemy e : enemies) {
            if (e.isDead() || !isInRange(e)) continue;

            double dx = e.getGroup().getTranslateX() - x;
            double dy = e.getGroup().getTranslateY() - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < minDist) {
                minDist = dist;
                closest = e;
            }
        }

        if (closest != null) {
            rotateToTarget(closest);

            if (now - lastFireTime >= fireCooldown * 1000) {
                shoot(closest);
                lastFireTime = now;
            }
        }
    }

    /**
     * Düşmana dönük olacak şekilde namluyu döndürür.
     */
    private void rotateToTarget(SlimeEnemy target) {
        Bounds towerBounds = towerShape.localToScene(towerShape.getBoundsInLocal());
        double towerX = towerBounds.getMinX() + towerBounds.getWidth() / 2;
        double towerY = towerBounds.getMinY() + towerBounds.getHeight() / 2;

        Bounds enemyBounds = target.getGroup().localToScene(target.getGroup().getBoundsInLocal());
        double enemyX = enemyBounds.getMinX() + enemyBounds.getWidth() / 2;
        double enemyY = enemyBounds.getMinY() + enemyBounds.getHeight() / 2;

        double dx = enemyX - towerX;
        double dy = enemyY - towerY;

        double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
        smoothRotateTo(angle);
    }

    /**
     * Namlunun yumuşak dönüşünü sağlar.
     */
    private void smoothRotateTo(double targetAngle) {
        if (rotationTimeline != null) rotationTimeline.stop();

        targetAngle = (targetAngle + 360) % 360;
        currentAngle = (rotate.getAngle() + 360) % 360;

        double shortestDiff = targetAngle - currentAngle;
        if (shortestDiff > 180) shortestDiff -= 360;
        if (shortestDiff < -180) shortestDiff += 360;

        final int frames = 20;
        final double step = shortestDiff / frames;

        rotationTimeline = new Timeline(new KeyFrame(Duration.millis(1), e -> {
            currentAngle += step;
            rotate.setAngle(currentAngle);
        }));
        rotationTimeline.setCycleCount(frames);
        rotationTimeline.play();
    }

    /**
     * Düşmana mermi fırlatır ve hedefe ulaştığında hasar + efekt uygular.
     */
    private void shoot(SlimeEnemy target) {
        Circle bullet = new Circle(5, Color.BLACK);

        Pane pane = (Pane) towerShape.getParent();
        if (pane == null) return;

        Point2D barrelTip = getBarrelTip();
        Point2D localStart = pane.sceneToLocal(barrelTip);

        bullet.setTranslateX(localStart.getX());
        bullet.setTranslateY(localStart.getY());
        pane.getChildren().add(bullet);

        final Timeline[] timelineRef = new Timeline[1];

        Timeline missileTimeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (target.isDead()) {
                pane.getChildren().remove(bullet);
                if (timelineRef[0] != null) timelineRef[0].stop();
                return;
            }

            Point2D currentTargetPos = getScenePosition(target.getGroup());
            Point2D localTarget = pane.sceneToLocal(currentTargetPos);

            double dx = localTarget.getX() - bullet.getTranslateX();
            double dy = localTarget.getY() - bullet.getTranslateY();
            double dist = Math.sqrt(dx * dx + dy * dy);

            Point2D towerCenter = towerShape.localToScene(towerShape.getBoundsInLocal().getWidth() / 2,
                                                          towerShape.getBoundsInLocal().getHeight() / 2);
            Point2D bulletPos = pane.localToScene(bullet.getTranslateX(), bullet.getTranslateY());
            double bx = bulletPos.getX() - towerCenter.getX();
            double by = bulletPos.getY() - towerCenter.getY();
            double bulletDistanceFromTower = Math.sqrt(bx * bx + by * by);

            if (bulletDistanceFromTower > range || dist < 5) {
                pane.getChildren().remove(bullet);
                if (dist < 5) {
                    target.takeDamage(damage, () -> pane.getChildren().remove(target.getGroup()));
                    ExplosionEffect.play(bullet.getTranslateX(), bullet.getTranslateY(), pane);
                }
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
     * Varil ucunun sahnedeki konumunu döndürür.
     */
    private Point2D getBarrelTip() {
        Bounds bounds = barrel1.localToScene(barrel1.getBoundsInLocal());
        return new Point2D(bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY());
    }

    /**
     * Belirli bir nesnenin sahnedeki merkezini döndürür.
     */
    private Point2D getScenePosition(Node node) {
        return node.localToScene(node.getBoundsInLocal().getWidth() / 2, node.getBoundsInLocal().getHeight() / 2);
    }

    /**
     * Seviye yükseldikçe gövde rengi değişir.
     */
    @Override
    protected void updateVisualsByLevel() {
        switch (level) {
            case 2 -> body.setFill(Color.LIMEGREEN);
            case 3 -> body.setFill(Color.AQUA);
        }
    }

    /**
     * (Override edilmiş) Düşman kule menzilinde mi kontrol eder.
     * Tower sınıfındaki ile aynı, istenirse kaldırılabilir.
     */
    @Override
    public boolean isInRange(SlimeEnemy e) {
        return super.isInRange(e); // Şimdilik üst sınıftan çağrılıyor
    }
}
