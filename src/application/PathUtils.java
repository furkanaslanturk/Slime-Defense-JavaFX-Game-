package application;

import character.SlimeEnemy;
import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * PathUtils sınıfı, harita üzerindeki yol bilgisini işlemede yardımcı olan statik metodları içerir.
 * Düşmanların harita üzerinde nasıl hareket edeceğini belirler.
 */
public class PathUtils {

    /**
     * Map sınıfından gelen yol hücrelerini ["2,0", "2,1", "2,2"] gibi bir listeden (row, col) int[] listesine dönüştürür.
     *
     * @param map Harita nesnesi
     * @return Yolu temsil eden int[] (row, col) listesi
     */
    public static List<int[]> getPathList(Map map) {
        List<String> rawPath = map.getPathCells(); // String formatındaki yol verisi
        List<int[]> pathList = new ArrayList<>();

        for (String coord : rawPath) {
            String[] split = coord.split(",");
            if (split.length == 2) {
                try {
                    int row = Integer.parseInt(split[0].trim());
                    int col = Integer.parseInt(split[1].trim());
                    pathList.add(new int[]{row, col});
                } catch (NumberFormatException ignored) {
                    // Geçersiz koordinat atla
                }
            }
        }

        return pathList;
    }

    /**
     * Belirtilen SlimeEnemy nesnesine, verilen path üzerinden animasyonla hareket ettirilmesini sağlar.
     *
     * @param slime    Hareket edecek düşman
     * @param path     (row, col) formatında int[] listesi
     * @param tileSize Hücre boyutu (px)
     * @param map      Harita (grid boşlukları için gerekli)
     * @param onFinish Düşman sona ulaştığında çalışacak callback
     */
    public static void attachPathTransition(SlimeEnemy slime, List<int[]> path, int tileSize, Map map, Runnable onFinish) {
        double spacing = map.getGridSpacing(); // Hücreler arası boşluk
        Path enemyPath = new Path();

        for (int i = 0; i < path.size(); i++) {
            int[] point = path.get(i);
            double x = point[1] * (tileSize + spacing) + tileSize / 2.0;
            double y = point[0] * (tileSize + spacing) + tileSize / 2.0;

            if (i == 0) {
                enemyPath.getElements().add(new MoveTo(x, y)); // Başlangıç noktası
            } else {
                enemyPath.getElements().add(new LineTo(x, y)); // Takip eden noktalar
            }

            // Son noktadaysak, biraz daha ileri taşınarak yol "bitmiş gibi" görünsün
            if (i == path.size() - 1 && path.size() >= 2) {
                int[] prev = path.get(i - 1);
                int dx = point[1] - prev[1];
                int dy = point[0] - prev[0];

                double extendX = x + dx * (tileSize + spacing) * 0.12;
                double extendY = y + dy * (tileSize + spacing) * 0.12;
                enemyPath.getElements().add(new LineTo(extendX, extendY));
            }
        }

        // Hareket süresi düşmanın hızına göre ayarlanır
        double baseSpeed = 100.0;              // Referans hız
        double enemySpeed = slime.getType().speed;
        double baseDurationPerTile = 0.5;      // Her bir tile için baz süre
        double speedFactor = baseSpeed / enemySpeed;
        double durationSeconds = path.size() * baseDurationPerTile * speedFactor;

        PathTransition transition = new PathTransition(
                Duration.seconds(durationSeconds),
                enemyPath,
                slime.getGroup()
        );

        transition.setInterpolator(Interpolator.LINEAR); // Sabit hız
        transition.setOrientation(PathTransition.OrientationType.NONE);

        // Düşman sona ulaşırsa ve hala hayattaysa, finish callback çağrılır
        transition.setOnFinished(e -> {
            if (!slime.isDead()) {
                onFinish.run();
            }
        });

        // PathTransition referansı düşmanın içinde tutulur (duraklatmak vs. için)
        slime.setPathTransition(transition);
        transition.play();
    }
}
