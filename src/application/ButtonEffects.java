package application;

import javafx.animation.ScaleTransition;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * ButtonEffects sınıfı, butonlar veya etiket (Label, Button, CheckBox gibi Labeled) bileşenlerine
 * görsel efektler (hover, basılı tutma, animasyon vs.) eklemek için yardımcı metotlar sağlar.
 */
public class ButtonEffects {

    /**
     * Bir Labeled (Button, CheckBox, Label vb.) bileşene animasyonlu efektler uygular.
     * Hover durumunda büyüme, tıklama sırasında küçülme ve gölge efekti içerir.
     *
     * @param btn Efekt uygulanacak Labeled bileşeni
     */
    public static void buttonEffects(Labeled btn) {
        // Gölge efekti tanımlanıyor
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.GRAY);
        shadow.setRadius(10);

        // Büyüme animasyonu (hover sırasında)
        ScaleTransition upScale = new ScaleTransition(Duration.millis(200), btn);
        upScale.setToX(1.1);
        upScale.setToY(1.1);

        // Normal boyuta dönüş animasyonu (hover çıkınca)
        ScaleTransition downScale = new ScaleTransition(Duration.millis(200), btn);
        downScale.setToX(1.0);
        downScale.setToY(1.0);

        // Hover başlarken → büyü + gölge
        btn.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            btn.setEffect(shadow);
            upScale.playFromStart();
        });

        // Hover biterken → eski haline dön
        btn.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            btn.setEffect(null);
            downScale.playFromStart();
        });

        // Mouse basıldığında → buton küçülür
        btn.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            btn.setScaleX(0.9);
            btn.setScaleY(0.9);
        });

        // Mouse bırakıldığında → normal boyuta döner
        btn.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
        });
    }

    /**
     * Oyun içinde stilize edilmiş başlık etiketi (Label) oluşturur.
     *
     * @param text Görüntülenecek yazı
     * @return Stil uygulanmış Label nesnesi
     */
    public static Label buttonText(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("CAL SANS", FontWeight.BOLD, 36));
        label.setTextFill(Color.DARKRED);
        return label;
    }
}
