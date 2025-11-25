package application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * LevelLoader sınıfı, bir seviye (level) dosyasını okuyarak:
 * - Harita genişlik/yüksekliğini
 * - Yol hücrelerini
 * - Dalgaları (Wave)
 * yükler ve ilgili verilere erişim sağlar.
 *
 * Dosya formatı örneği:
 * WIDTH: 10
 * HEIGHT: 6
 * 2,1
 * 2,2
 * ...
 * WAVE_DATA
 * 3 2 1 1.5 2.0
 * 4 3 2 1.2 3.0
 */
public class LevelLoader {

    private int width;
    private int height;

    private final List<String> pathCells = new ArrayList<>();
    private final List<Wave> waves = new ArrayList<>();

    /**
     * Belirtilen dosya yolundan seviye verilerini yükler.
     *
     * @param filePath Seviye dosyasının yolu
     */
    public LevelLoader(String filePath) {
        loadLevelData(filePath);
    }

    /**
     * Dosyayı okuyarak genişlik, yükseklik, path ve dalga verilerini ayrıştırır.
     */
    private void loadLevelData(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isWaveSection = false;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("WIDTH")) {
                    width = Integer.parseInt(line.split(":")[1].trim());

                } else if (line.startsWith("HEIGHT")) {
                    height = Integer.parseInt(line.split(":")[1].trim());

                } else if (line.startsWith("WAVE_DATA")) {
                    isWaveSection = true;

                } else if (!isWaveSection) {
                    // Yol hücresi örneği: "2,3"
                    pathCells.add(line);

                } else {
                    // Dalga verisi örneği: "3 2 1 1.5 2.0"
                    String[] parts = line.split(" ");
                    if (parts.length >= 5) {
                        int slow = Integer.parseInt(parts[0]);
                        int normal = Integer.parseInt(parts[1]);
                        int fast = Integer.parseInt(parts[2]);
                        double delayBetween = Double.parseDouble(parts[3]);
                        double delayStart = Double.parseDouble(parts[4]);

                        waves.add(new Wave(slow, normal, fast, delayBetween, delayStart));
                    }
                }
            }

        } catch (Exception e) {
            // Daha iyi hata yönetimi yapılabilir (GUI uyarısı vs.)
            System.err.println("Hata: Seviye dosyası okunamadı → " + e.getMessage());
        }
    }

    // --- Getter metodları ---

    /**
     * Harita genişliğini döndürür.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Harita yüksekliğini döndürür.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Yol hücrelerini ("row,col" formatında) döndürür.
     */
    public List<String> getPathCells() {
        return pathCells;
    }

    /**
     * Tüm Wave (dalgaları) döndürür.
     */
    public List<Wave> getWaves() {
        return waves;
    }
}
