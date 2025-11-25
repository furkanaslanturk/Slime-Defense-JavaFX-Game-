package character;

import java.util.List;
import java.util.Random;

import application.Game;
import application.GameState;
import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * King sınıfı, oyuncunun temsilcisi olan kral karakterini yönetir.
 * - Animasyonlar
 * - Hasar alma
 * - Ölüm animasyonu
 * - Alaycı konuşma balonları (quote)
 */
public class King {
    private final Animator animator;
    private final ImageView view;
    private final Label quoteLabel = new Label();

    private boolean isDead = false;           // Geçici ölüm (animasyon bazlı)
    private boolean permanentlyDead = false;  // Kalıcı ölüm (oyun sonu)

    private final Random random = new Random();

    // Rastgele gösterilecek cümleler
    private static final List<String> QUOTES = List.of(
        "You slimes dare to attack the King?!",
        "I didn't pay you for this!",
        "I shall not fall!",
        "Who is that incompetent person sitting there?",
        "Are you kidding?",
        "Protect me better!"
    );

    /**
     * Yeni bir King nesnesi oluşturur.
     *
     * @param fullSheet Sprite sheet yolu
     */
    public King(String fullSheet) {
        animator = new Animator(fullSheet, 64, 64, 4);
        view = animator.getImageView();
        playIdle();  // Başlangıçta idle animasyon başlatılır

        // Konuşma balonu ayarları
        quoteLabel.setFont(new Font("Arial", 18));
        quoteLabel.setStyle("-fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.7); -fx-padding: 5; -fx-background-radius: 10;");
        quoteLabel.setVisible(false);
    }

    /**
     * Kral kalıcı olarak ölür. Idle animasyon durur ve ölüm animasyonu oynatılır.
     */
    public void dieForever() {
        if (permanentlyDead) return;

        permanentlyDead = true;
        isDead = true;

        animator.stopAnimation();
        animator.playAnimation(
            List.of(12, 13, 14, 15, 16, 17, 17, 18, 18, 19, 20, 21, 22, 23),
            150,
            false // 🔁 sadece bir kez oynat
        );

        quoteLabel.setVisible(false);
    }

    /**
     * Belirtilen hasar alınır. Can sıfırın altına inerse kalıcı olarak ölür.
     * Aksi halde rastgele bir alaycı cümle gösterilir.
     */
    public void takeDamage(int damage) {
        if (isDead || permanentlyDead) return;

        GameState state = Game.getCurrentGameState();
        if (state != null && state.getLives() - damage <= 0) {
            dieForever();
        } else {
            showRandomQuote();
        }
    }

    /**
     * Rastgele bir alaycı konuşma balonu gösterir.
     */
    private void showRandomQuote() {
        String quote = QUOTES.get(random.nextInt(QUOTES.size()));
        quoteLabel.setText(quote);
        quoteLabel.setVisible(true);

        // Konuşma 2.5 saniye sonra kaybolur
        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
        pause.setOnFinished(e -> quoteLabel.setVisible(false));
        pause.play();
    }

    /**
     * Idle (boşta durma) animasyonunu başlatır.
     */
    private void playIdle() {
        if (!permanentlyDead) {
            animator.playAnimation(
                List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
                150,
                true
            );
        }
    }

    // ----------------------
    // Getter metodları
    // ----------------------

    /**
     * Kralın görsel nesnesini döndürür.
     */
    public ImageView getView() {
        return view;
    }

    /**
     * Kralın konuşma balonunu döndürür.
     */
    public Label getQuoteLabel() {
        return quoteLabel;
    }

    /**
     * Kral ölü mü?
     */
    public boolean isDead() {
        return isDead;
    }
}
