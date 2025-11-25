package towers;

import application.EnemyManager;
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
import java.util.stream.Collectors;

/**
 * MissileLauncherTower, hedefe füze göndererek çevresindeki düşmanlara hasar veren alan etkili bir kuledir.
 * Görsel olarak dönen namluya sahiptir ve geniş menzilde etkili patlama yaratır.
 */
public class MissileLauncherTower extends Tower {

    private double fireCooldown = 2.0; // saniye
    private long lastFireTime = 0;

    private Rectangle barrel;
    private Rotate rotate;
    private Group node;

    /**
     * Kule butonları için örnek şekli döndürür.
     */
    public static Node getPreviewShape() {
        return createTowerVisuals(false);
    }

    /**
     * Yeni bir füze kulesi oluşturur.
     */
    public MissileLauncherTower(double x, double y) {
        super(x, y);
        this.range = 120;
        this.damage = 10;
        this.cost = 200;

        node = (Group) createTowerVisuals(true);
        this.towerShape = node;
    }

    /**
     * Kule görselini oluşturur. Eğer rotateEnabled true ise namlu döner hale getirilir.
     */
    private static Node createTowerVisuals(boolean rotateEnabled) {
        int cx = 0, cy = 0;

        Polygon outer = createOctagon(cx, cy, 22);
        outer.setFill(Color.GRAY);
        outer.setStroke(Color.BLACK);

        Polygon inner = createOctagon(cx, cy, 16);
        inner.setFill(Color.GRAY.brighter());
        inner.setStroke(Color.BLACK);

        Rectangle body = new Rectangle(cx - 12, cy - 12, 24, 24);
        body.setArcWidth(10);
        body.setArcHeight(10);
        body.setFill(Color.DARKRED);
        body.setStroke(Color.BLACK);

        Rectangle barrel = new Rectangle(cx - 6, cy - 28, 12, 20);
        barrel.setArcWidth(6);
        barrel.setArcHeight(6);
        barrel.setFill(Color.DARKGRAY);
        barrel.setStroke(Color.BLACK);

        Rectangle barrel1 = new Rectangle(cx - 7, cy - 34, 14, 6);
        barrel1.setFill(Color.DARKGRAY.darker());
        barrel1.setArcWidth(2);
        barrel1.setArcHeight(2);
        barrel1.setStroke(Color.BLACK);

        Group group = new Group(outer, inner, body, barrel, barrel1);
        group.getProperties().put("body", body);  // Görsel referanslar

        if (rotateEnabled) {
            Rotate rotate = new Rotate(0, cx, cy);
            barrel.getTransforms().add(rotate);
            barrel1.getTransforms().add(rotate);
            body.getTransforms().add(rotate);

            group.getProperties().put("rotate", rotate);
            group.getProperties().put("barrel", barrel);
        }

        return group;
    }

    /**
     * Her frame çağrılır. En yakın düşmanı bulur, gerekirse füze fırlatır.
     */
    @Override
    public void update(List<SlimeEnemy> enemies) {
        long now = System.currentTimeMillis();

        SlimeEnemy closest = enemies.stream()
                .filter(e -> !e.isDead() && isInRange(e))
                .min((a, b) -> Double.compare(getDistanceTo(a), getDistanceTo(b)))
                .orElse(null);

        if (closest != null) {
            rotateToTarget(closest);
        }

        if (closest != null && now - lastFireTime >= fireCooldown * 1000) {
            launchMissile(closest, enemies);
            lastFireTime = now;
        }
    }

    /**
     * Kule namlusunu hedef düşmana döndürür.
     */
    private void rotateToTarget(SlimeEnemy target) {
        Rotate r = (Rotate) node.getProperties().get("rotate");
        if (r == null) return;

        Point2D towerPos = towerShape.localToScene(0, 0);
        Point2D enemyCenter = getScenePosition(target.getGroup());

        double dx = enemyCenter.getX() - towerPos.getX();
        double dy = enemyCenter.getY() - towerPos.getY();

        double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
        r.setAngle(angle);
    }

    /**
     * Seviye arttığında renk güncellemesi yapılır.
     */
    @Override
    protected void updateVisualsByLevel() {
        Rectangle body = (Rectangle) node.getProperties().get("body");
        if (body == null) return;
        switch (level) {
            case 2 -> body.setFill(Color.LIMEGREEN);
            case 3 -> body.setFill(Color.AQUA);
        }
    }

    /**
     * Füze fırlatır ve hedefe ulaştığında etki alanındaki tüm düşmanlara hasar verir.
     */
    private void launchMissile(SlimeEnemy target, List<SlimeEnemy> allEnemies) {
        Pane pane = (Pane) towerShape.getParent();
        if (pane == null) return;

        Rectangle barrel = (Rectangle) node.getProperties().get("barrel");
        Point2D tipScene = barrel.localToScene(barrel.getWidth() / 2, 0);
        Point2D tip = pane.sceneToLocal(tipScene);

        Point2D towerSceneCenter = getScenePosition(towerShape);

        Circle missile = new Circle(6);
        missile.setFill(Color.DARKSLATEGRAY);
        missile.setTranslateX(tip.getX());
        missile.setTranslateY(tip.getY());
        pane.getChildren().add(missile);

        final Timeline[] timelineRef = new Timeline[1];

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16), evt -> {
            Point2D targetPos = getScenePosition(target.getGroup());
            Point2D targetLocal = pane.sceneToLocal(targetPos);

            double dx = targetLocal.getX() - missile.getTranslateX();
            double dy = targetLocal.getY() - missile.getTranslateY();
            double dist = Math.sqrt(dx * dx + dy * dy);

            // 📏 Menzil kontrolü
            Point2D missileScene = pane.localToScene(missile.getTranslateX(), missile.getTranslateY());
            double missileDist = missileScene.distance(towerSceneCenter);

            if (missileDist > range) {
                pane.getChildren().remove(missile);
                if (timelineRef[0] != null) timelineRef[0].stop();
                return;
            }

            if (dist < 5) {
                pane.getChildren().remove(missile);

                for (SlimeEnemy enemy : allEnemies) {
                    if (enemy.isDead()) continue;

                    Point2D enemyPos = enemy.getSceneCenter();
                    if (enemyPos.distance(towerSceneCenter) <= range) {
                        enemy.takeDamage(damage, () -> {
                            if (enemy.getGroup().getParent() != null) {
                                pane.getChildren().remove(enemy.getGroup());
                            }
                        });

                        Point2D explosion = pane.sceneToLocal(enemyPos);
                        ExplosionEffect.play(explosion.getX(), explosion.getY(), pane);
                    }
                }

                if (timelineRef[0] != null) timelineRef[0].stop();
                return;
            }

            double step = 6;
            missile.setTranslateX(missile.getTranslateX() + (dx / dist) * step);
            missile.setTranslateY(missile.getTranslateY() + (dy / dist) * step);
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        timelineRef[0] = timeline;
    }

    /**
     * Verilen node'un sahnedeki merkez koordinatını döndürür.
     */
    private Point2D getScenePosition(Node node) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        return new Point2D(
                bounds.getMinX() + bounds.getWidth() / 2,
                bounds.getMinY() + bounds.getHeight() / 2
        );
    }

    /**
     * Düşmana olan mesafeyi verir (yerel konuma göre).
     */
    private double getDistanceTo(SlimeEnemy enemy) {
        double dx = enemy.getGroup().getTranslateX() - x;
        double dy = enemy.getGroup().getTranslateY() - y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
