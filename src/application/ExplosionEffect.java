package application;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.Random;

/**
 * Düşman öldüğünde patlama efekti oluşturur.
 * Bu efekt; ses, parçacık animasyonu, solma ve büyüme animasyonlarını içerir.
 * Efekt sadece bir Pane'e (sahne öğesi) uygulanabilir.
 */
public class ExplosionEffect {

    /**
     * Belirtilen konumda bir patlama efekti oynatır.
     *
     * @param x      Efektin X koordinatı (sahneye göre)
     * @param y      Efektin Y koordinatı (sahneye göre)
     * @param parent Efektin gösterileceği Node (Pane olmalı)
     */
    public static void play(double x, double y, Node parent) {
        // Ses efektini oynat
        AudioClip explosionSound = new AudioClip(
            ExplosionEffect.class.getResource("/media/slime_hit.mp3").toExternalForm()
        );
        explosionSound.play();

        // Eğer parent Pane değilse hiçbir şey yapma
        if (!(parent instanceof Pane pane)) return;

        Random rand = new Random();

        // 20 parçacık oluştur
        for (int i = 0; i < 20; i++) {
            Circle particle = new Circle(3, Color.RED);

            // Rastgele yön ve mesafe hesapla
            double angle = rand.nextDouble() * 2 * Math.PI;
            double distance = 20 + rand.nextDouble() * 20;
            double dx = Math.cos(angle) * distance;
            double dy = Math.sin(angle) * distance;

            // Başlangıç konumu ayarla
            particle.setTranslateX(x);
            particle.setTranslateY(y);
            particle.setEffect(new GaussianBlur(2));
            pane.getChildren().add(particle);

            // Parçacığı şeffaflaştırma animasyonu
            FadeTransition fade = new FadeTransition(Duration.millis(500), particle);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> pane.getChildren().remove(particle));
            fade.play();

            // Büyüme animasyonu
            ScaleTransition scale = new ScaleTransition(Duration.millis(500), particle);
            scale.setFromX(1);
            scale.setToX(2);
            scale.setFromY(1);
            scale.setToY(2);
            scale.play();

            // Hedef konuma hareket ettir
            particle.setTranslateX(x + dx);
            particle.setTranslateY(y + dy);
        }
    }
}
