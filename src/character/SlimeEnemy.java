package character;

import javafx.animation.KeyFrame;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;

import application.ExplosionEffect;
import application.Game;
import application.GameState;

/**
 * SlimeEnemy sınıfı, oyundaki düşmanları temsil eder.
 * Her düşman tipi (slow, normal, fast) farklı can, hız ve animasyonla gelir.
 * Bu sınıf, düşmanın görüntüsü, sağlık durumu, animasyonu ve ölüm davranışlarını içerir.
 */
public class SlimeEnemy {
    private final Animator animator;
    private final ImageView view;
    private final Rectangle healthBar;
    private final Group group;
    private final EnemyType type;
    private final int maxHp;
    private final int speed;

    private int hp;
    private boolean isDead = false;
    private PathTransition pathTransition;

    /**
     * Yeni bir slime düşmanı oluşturur.
     * @param type Düşman tipi (slow, normal, fast)
     */
    public SlimeEnemy(EnemyType type) {
        this.type = type;
        this.hp = type.hp;
        this.maxHp = type.hp;
        this.speed = type.speed;

        String spriteFileName = type.getRandomSpritePath();
        String fullPath = getClass().getResource(spriteFileName).toExternalForm();

        animator = new Animator(fullPath, 32, 32, 8);
        view = animator.getImageView();
        view.setScaleX(type.scale);
        view.setScaleY(type.scale);

        healthBar = new Rectangle(32, 4, Color.LIMEGREEN);
        healthBar.setTranslateY(-20);

        group = new Group(view, healthBar);
        playIdle();
    }

    /**
     * Slime'ın idle (boşta) animasyonunu başlatır.
     */
    public void playIdle() {
        animator.playAnimation(List.of(
            0, 1, 2, 3,
            8, 9,
            16, 17, 18, 19, 20, 21, 22, 23,
            24, 25, 26, 27, 28, 29, 30, 31,
            32, 33
        ), 150);
    }

    /**
     * Slime ölüm animasyonunu başlatır ve bitince callback çağırır.
     * @param onDeathComplete Ölüm tamamlandığında çağrılacak kod
     */
    public void playDeath(Runnable onDeathComplete) {
        List<Integer> deathFrames = List.of(48, 49, 56, 57);
        animator.playAnimation(deathFrames, 150);
        Timeline wait = new Timeline(new KeyFrame(Duration.seconds(0.6), e -> onDeathComplete.run()));
        wait.play();
    }

    /**
     * Slime'a hasar uygular. Öldüyse animasyon, para, efekt gibi sonuçları tetikler.
     *
     * @param damage Verilen hasar
     * @param giveReward Para verilip verilmeyeceği
     * @param isLaserKill Lazer ile mi öldürüldü?
     * @param deathX Ölüm animasyonunun x konumu (sahneye göre)
     * @param deathY Ölüm animasyonunun y konumu (sahneye göre)
     * @param onDeathComplete Ölüm sonrası yapılacak işlemler
     */
    public void takeDamage(double damage, boolean giveReward, boolean isLaserKill, double deathX, double deathY, Runnable onDeathComplete) {
        if (isDead) return;

        hp -= damage;
        double newWidth = ((double) hp / maxHp) * 32;
        healthBar.setWidth(Math.max(0, newWidth));

        if (hp <= 0) {
            isDead = true;

            // Hareketi durdur
            if (pathTransition != null) {
                pathTransition.stop();
                pathTransition = null;
            }

            // Ödül ver
            if (giveReward) {
                GameState gameState = Game.getCurrentGameState();
                if (gameState != null) {
                    gameState.addMoneyForKill(type.reward);
                }
            }

            if (isLaserKill) {
                // Lazerle öldüyse → sprite'ı gizle, özel animasyon
                view.setVisible(false);
                Platform.runLater(() -> {
                    if (group.getParent() instanceof Pane parentPane) {
                        playDeathAnimationAt(deathX, deathY, parentPane, this);
                    }
                });
            }

            // Patlama efekti burada YOK → kuleden çağrılacak
            playDeath(onDeathComplete);
        }
    }


    /**
     * Geriye dönük uyum için basit hasar verme versiyonu.
     */
    public void takeDamage(double damage, boolean giveReward, Runnable onDeathComplete) {
        takeDamage(damage, giveReward, false, getSceneCenter().getX(), getSceneCenter().getY(), onDeathComplete);
    }

    /**
     * Varsayılan olarak ödül verir ve pozisyonu merkezden alır.
     */
    public void takeDamage(double damage, Runnable onDeathComplete) {
        takeDamage(damage, true, false, getSceneCenter().getX(), getSceneCenter().getY(), onDeathComplete);
    }

    /**
     * Düşmanın sahnedeki merkez koordinatını döndürür.
     * @return Sahne merkez noktası
     */
    public Point2D getSceneCenter() {
        Bounds bounds = group.localToScene(group.getBoundsInLocal());
        return new Point2D(
            bounds.getMinX() + bounds.getWidth() / 2,
            bounds.getMinY() + bounds.getHeight() / 2
        );
    }

    /**
     * Ölüm animasyonunu sahnede belirtilen yerde başlatır.
     */
    public static void playDeathAnimationAt(double x, double y, Pane parent, SlimeEnemy source) {
        Animator deathAnimator = new Animator(
            source.animator.getImageView().getImage().getUrl(),
            32, 32, 8
        );
        ImageView view = deathAnimator.getImageView();
        view.setScaleX(source.type.scale);
        view.setScaleY(source.type.scale);

        deathAnimator.playAnimation(List.of(48, 49, 56, 57), 150, false);

        view.setLayoutX(x - 16);
        view.setLayoutY(y - 16);

        parent.getChildren().add(view);

        Timeline cleanup = new Timeline(new KeyFrame(Duration.seconds(0.6), e -> {
            parent.getChildren().remove(view);
        }));
        cleanup.play();
    }

    // Getter & Setters

    public EnemyType getType() {
        return type;
    }

    public int getDamage() {
        return type.damage;
    }

    public Group getGroup() {
        return group;
    }

    public int getHp() {
        return hp;
    }

    public boolean isDead() {
        if (hp <= 0) {
            isDead = true;
        }
        return isDead;
    }

    public void setPathTransition(PathTransition transition) {
        this.pathTransition = transition;
    }
}
