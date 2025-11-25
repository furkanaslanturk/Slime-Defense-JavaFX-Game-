package application;

/**
 * Wave sınıfı, oyundaki bir düşman dalgasını temsil eder.
 * Her dalga; yavaş, normal ve hızlı düşman sayısı ile başlama gecikmesi ve düşmanlar arası süreyi içerir.
 */
public class Wave {

    // Düşman sayıları (her tip için ayrı)
    private final int slowCount;
    private final int normalCount;
    private final int fastCount;

    // Düşmanlar arası spawn süresi (saniye cinsinden)
    private final double delayBetweenEnemies;

    // Dalgadan önceki toplam gecikme süresi (saniye cinsinden)
    private final double startDelay;

    /**
     * Yeni bir Wave (düşman dalgası) oluşturur.
     * @param slow         Yavaş düşman sayısı
     * @param normal       Normal düşman sayısı
     * @param fast         Hızlı düşman sayısı
     * @param delayBetween Düşmanlar arası spawn gecikmesi (saniye)
     * @param startDelay   Dalga başlamadan önceki bekleme süresi (saniye)
     */
    public Wave(int slow, int normal, int fast, double delayBetween, double startDelay) {
        this.slowCount = slow;
        this.normalCount = normal;
        this.fastCount = fast;
        this.delayBetweenEnemies = delayBetween;
        this.startDelay = startDelay;
    }

    /**
     * Bu dalgadaki yavaş düşman sayısını döndürür.
     */
    public int getSlowCount() {
        return slowCount;
    }

    /**
     * Bu dalgadaki normal düşman sayısını döndürür.
     */
    public int getNormalCount() {
        return normalCount;
    }

    /**
     * Bu dalgadaki hızlı düşman sayısını döndürür.
     */
    public int getFastCount() {
        return fastCount;
    }

    /**
     * İki düşman spawn'ı arasındaki gecikmeyi döndürür (saniye).
     */
    public double getDelayBetweenEnemies() {
        return delayBetweenEnemies;
    }

    /**
     * Bu dalganın başlamasından önceki gecikmeyi döndürür (saniye).
     */
    public double getStartDelay() {
        return startDelay;
    }
}
