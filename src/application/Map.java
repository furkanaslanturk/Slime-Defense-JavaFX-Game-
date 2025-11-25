package application;

import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;

/**
 * Map sınıfı, oyundaki grid tabanlı harita yapısını temsil eder.
 * - Grid oluşturur ve animasyonla sahneye ekler.
 * - Yol hücrelerini (path) takip eder.
 */
public class Map {

    private final int width;                   // Grid sütun sayısı
    private final int height;                  // Grid satır sayısı
    private final int tileSize = 50;           // Hücre boyutu (px)
    private final double spacing = 3;          // Hücreler arası boşluk

    private final List<String> pathCells;      // Yol hücreleri (örnek: "2,3")
    private final Rectangle[][] grid;          // Tüm hücrelerin görsel temsili
    private final GridPane gridPane;           // JavaFX görünüm bileşeni

    /**
     * Yeni bir Map (harita) nesnesi oluşturur.
     *
     * @param width     Grid sütun sayısı
     * @param height    Grid satır sayısı
     * @param pathCells Yol olarak işaretlenen hücrelerin listesi ("row,col" şeklinde)
     */
    public Map(int width, int height, List<String> pathCells) {
        this.width = width;
        this.height = height;
        this.pathCells = pathCells;
        this.grid = new Rectangle[height][width];
        this.gridPane = new GridPane();
        gridPane.setHgap(spacing);
        gridPane.setVgap(spacing);

        createAnimatedGrid();
    }

    /**
     * Haritayı oluşturan hücreleri hazırlar ve sahneye animasyonlu şekilde ekler.
     */
    private void createAnimatedGrid() {
        Timeline animation = new Timeline();
        double delay = 0;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Rectangle cell = new Rectangle(0, 0); // Başta nokta gibi
                cell.setWidth(tileSize);
                cell.setHeight(tileSize);
                cell.setArcWidth(8);
                cell.setArcHeight(8);

                String key = row + "," + col;
                if (pathCells.contains(key)) {
                    cell.setFill(Color.web("#F2E0C9")); // Path hücresi
                } else {
                    cell.setFill(Math.random() > 0.5 ? Color.web("#FAC443") : Color.web("#FBD058")); // Sarı tonları
                }

                cell.setScaleX(0);
                cell.setScaleY(0);

                grid[row][col] = cell;
                gridPane.add(cell, col, row);
            }
        }
        
        for (int j = 0; j < width; j++) {
        	for(int i = 0; i < width; i++) {
        		final int row = j;
        		final int col = i;
        		// Her hücre için animasyon başlatıyoruz
                KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.15 * j), event -> {
                    // Animation her hücre için birer birer gerçekleşecek
                    ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.3), grid[row][col]);
                    scaleTransition.setFromX(0.0);  // Başlangıçta x boyutu 0 (nokta gibi)
                    scaleTransition.setFromY(0.0);  // Başlangıçta y boyutu 0 (nokta gibi)
                    scaleTransition.setToX(1.0);    // Sonunda x boyutu 1.0 olacak
                    scaleTransition.setToY(1.0);    // Sonunda y boyutu 1.0 olacak
                    scaleTransition.setCycleCount(1);  // Animasyon bir kez oynasın
                    scaleTransition.setAutoReverse(false);  // Geriye dönmesin
                    scaleTransition.play();  // Animasyonu başlat
                });

                animation.getKeyFrames().add(keyFrame);
        	}              
        }  
        
        animation.setCycleCount(1);
        animation.play();
        
    }

    // --- Erişim Metotları ---

    /**
     * JavaFX GridPane bileşenini döndürür (ekranda gösterilecek harita).
     */
    public GridPane getGridPane() {
        return gridPane;
    }

    /**
     * Belirli bir hücreyi (row, col) koordinatları ile döndürür.
     */
    public Rectangle getCell(int row, int col) {
        return grid[row][col];
    }

    /**
     * Yol hücrelerinin listesini döndürür.
     */
    public List<String> getPathCells() {
        return pathCells;
    }

    /**
     * Belirtilen koordinat bir yol hücresi mi?
     */
    public boolean isPathCell(int row, int col) {
        return pathCells.contains(row + "," + col);
    }

    /**
     * Hücre boyutunu döndürür.
     */
    public int getTileSize() {
        return tileSize;
    }

    /**
     * Hücreler arası boşluk değerini döndürür.
     */
    public double getGridSpacing() {
        return spacing;
    }

    /**
     * Harita genişliğini (sütun sayısı) döndürür.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Harita yüksekliğini (satır sayısı) döndürür.
     */
    public int getHeight() {
        return height;
    }
}
