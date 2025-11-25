package towers;

import application.Map;
import character.SlimeEnemy;
import javafx.scene.Node;
import javafx.scene.shape.Polygon;

/**
 * Tüm kulelerin ortak özelliklerini tanımlayan soyut (abstract) sınıf.
 * Kulelerin konumu, menzili, hasarı, seviye sistemi ve sahneye çizilecek görsel şekli burada tanımlanır.
 */
public abstract class Tower {
    protected double x, y;               // Kule merkezinin sahnedeki konumu
    protected double range;             // Menzil (birim olarak)
    protected double damage;            // Verdiği hasar
    protected int cost;                 // Satın alma maliyeti
    protected Node towerShape;          // Kuleye ait görsel (JavaFX Node)
    protected boolean placed = false;   // Yerleştirilip yerleştirilmediği
    protected Map map;                  // Kule yerleştirildiği harita referansı
    protected int level = 1;            // Kule seviyesi
    protected final int MAX_LEVEL = 3;  // Maksimum yükseltme seviyesi

    /**
     * Yeni bir kule nesnesi oluşturur.
     *
     * @param x X koordinatı (başlangıç pozisyonu)
     * @param y Y koordinatı (başlangıç pozisyonu)
     */
    public Tower(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Kule davranışını tanımlar. Her frame çağrılır.
     * Hedef seçip ateş etme mantığı buraya yazılır (alt sınıflarda).
     *
     * @param enemies Sahnedeki tüm düşmanlar
     */
    public abstract void update(java.util.List<SlimeEnemy> enemies);

    /**
     * Sekizgen şekil oluşturur. Bazı kulelerin temel görsel şekli için kullanılır.
     *
     * @param cx Merkez X
     * @param cy Merkez Y
     * @param r  Yarıçap (mesafe)
     * @return Sekizgen polygon şekli
     */
    protected static Polygon createOctagon(double cx, double cy, double r) {
        Polygon polygon = new Polygon();
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(45 * i);
            double x = cx + r * Math.cos(angle);
            double y = cy + r * Math.sin(angle);
            polygon.getPoints().addAll(x, y);
        }
        return polygon;
    }

    /**
     * Kuleyi yükseltir. Hasar ve menzili artırır.
     *
     * @return Eğer seviye arttırılabildiyse true, zaten max ise false
     */
    public boolean upgrade() {
        if (level >= MAX_LEVEL) return false;
        level++;
        this.range *= 1.2;
        this.damage *= 1.3;
        updateVisualsByLevel();
        return true;
    }

    /**
     * Yükseltme sonrası görsel güncellemeleri yapmak için override edilebilir.
     * Örn: renk veya efekt değişimi.
     */
    protected void updateVisualsByLevel() {}

    /**
     * Bir düşman kule menzilinde mi kontrol eder.
     *
     * @param e Düşman
     * @return Menzildeyse true
     */
    public boolean isInRange(SlimeEnemy e) {
        if (e == null || e.getGroup() == null) return false;

        double towerCenterX = x + towerShape.getLayoutBounds().getWidth() / 2;
        double towerCenterY = y + towerShape.getLayoutBounds().getHeight() / 2;

        double enemyCenterX = e.getGroup().getTranslateX() + e.getGroup().getLayoutBounds().getWidth() / 2;
        double enemyCenterY = e.getGroup().getTranslateY() + e.getGroup().getLayoutBounds().getHeight() / 2;

        double dx = enemyCenterX - towerCenterX;
        double dy = enemyCenterY - towerCenterY;

        return Math.sqrt(dx * dx + dy * dy) <= range;
    }

    // -------------------- GETTER & SETTER --------------------

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRange() {
        return range;
    }

    public double getDamage() {
        return damage;
    }

    public int getCost() {
        return cost;
    }

    public int getLevel() {
        return level;
    }

    public int getUpgradeCost() {
        return cost * level / 5; // Örn: 1.seviye için 50, 2.seviye için 100

    }

    public Node getTowerShape() {
        return towerShape;
    }

    public boolean isPlaced() {
        return placed;
    }

    public void setPlaced(boolean placed) {
        this.placed = placed;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setMap(Map map) {
        this.map = map;
    }
}
