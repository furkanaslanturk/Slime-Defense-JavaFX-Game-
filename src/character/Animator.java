package character;

import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * Animator sınıfı, sprite sheet (birden fazla kare içeren tek resim) kullanarak
 * karakter veya nesne animasyonlarını kare kare oynatır.
 */
public class Animator {
    private final ImageView imageView;       // Animasyonun gösterileceği görsel nesne
    private Timeline animationTimeline;      // Kare geçişleri için zamanlayıcı
    private final int frameWidth;            // Her bir karenin genişliği
    private final int frameHeight;           // Her bir karenin yüksekliği
    private final int framesPerRow;          // Sprite sheet'teki satır başına kare sayısı

    private List<Integer> frameSequence;     // Oynatılacak karelerin sırası (örneğin [0,1,2,3])
    private int currentFrameIndex = 0;       // Şu an gösterilen kare

    /**
     * Yeni bir Animator nesnesi oluşturur.
     *
     * @param spriteSheetPath Sprite sheet'in dosya yolu
     * @param frameWidth      Her bir karenin piksel cinsinden genişliği
     * @param frameHeight     Her bir karenin yüksekliği
     * @param framesPerRow    Sprite sheet'teki yatay kare sayısı
     */
    public Animator(String spriteSheetPath, int frameWidth, int frameHeight, int framesPerRow) {
        this.imageView = new ImageView(new Image(spriteSheetPath, false)); // önbelleğe alma kapalı
        this.imageView.setViewport(new Rectangle2D(0, 0, frameWidth, frameHeight)); // ilk kareyi göster

        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.framesPerRow = framesPerRow;
    }

    /**
     * Bu animasyona ait ImageView nesnesini döndürür.
     */
    public ImageView getImageView() {
        return imageView;
    }

    /**
     * Animasyonu belirtilen kare sırasına ve hızına göre başlatır (sonsuz döngü).
     *
     * @param frameSequence Karelerin sırası (örnek: [0,1,2,3])
     * @param speedMillis   Her bir kare arasında geçiş süresi (ms)
     */
    public void playAnimation(List<Integer> frameSequence, int speedMillis) {
        playAnimation(frameSequence, speedMillis, true);
    }

    /**
     * Animasyonu belirtilen kare sırasına ve hızına göre başlatır.
     *
     * @param frameSequence Karelerin sırası (örnek: [0,1,2,3])
     * @param speedMillis   Her bir kare arasında geçiş süresi (ms)
     * @param loop          true → sonsuz döngü, false → bir kere oynat
     */
    public void playAnimation(List<Integer> frameSequence, int speedMillis, boolean loop) {
        if (animationTimeline != null) {
            animationTimeline.stop();  // Önceki animasyonu durdur
        }

        this.frameSequence = frameSequence;
        this.currentFrameIndex = 0;

        animationTimeline = new Timeline(new KeyFrame(Duration.millis(speedMillis), e -> updateFrame()));
        animationTimeline.setCycleCount(loop ? Timeline.INDEFINITE : frameSequence.size());
        animationTimeline.play();
    }

    /**
     * Her karede çağrılır. Yeni kareyi sprite sheet'ten gösterir.
     */
    private void updateFrame() {
        int frameNumber = frameSequence.get(currentFrameIndex);

        int row = frameNumber / framesPerRow;
        int col = frameNumber % framesPerRow;

        int x = col * frameWidth;
        int y = row * frameHeight;

        imageView.setViewport(new Rectangle2D(x, y, frameWidth, frameHeight));

        currentFrameIndex = (currentFrameIndex + 1) % frameSequence.size();
    }

    /**
     * Şu anda oynayan animasyonu durdurur.
     */
    public void stopAnimation() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
    }
}
