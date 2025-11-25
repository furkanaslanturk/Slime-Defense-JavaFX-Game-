package character;

import java.util.Random;

/**
 * EnemyType enum'u, oyundaki düşman tiplerini tanımlar.
 * Her düşman tipi için:
 * - Can (HP)
 * - Hız
 * - Görsel boyut (scale)
 * - Verdiği hasar
 * - Öldürünce verilen para ödülü
 * - Sprite görsel dosya yolları
 * bilgilerini içerir.
 */
public enum EnemyType {

    // Yavaş ama dayanıklı düşman
    SLOW(
        50,                         // HP
        50,                         // Hız (düşük)
        2.0,                        // Sprite boyutu (büyük)
        2,                          // Kraldan eksilteceği can
        20,                         // Öldürülünce verilen para
        new String[]{"/images/slime_enemy2_sheet.png"} // Sprite dosyaları
    ),

    // Dengeli düşman
    NORMAL(
        30,
        75,
        1.5,
        1,
        15,
        new String[]{"/images/slime_enemy5_sheet.png"}
    ),

    // Hızlı ama kırılgan düşman
    FAST(
        20,
        125,
        1.2,
        1,
        10,
        new String[]{"/images/slime_enemy3_sheet.png"}
    );

    // Rastgele sprite seçimi için Random nesnesi
    private static final Random random = new Random();

    // Enum sabitlerinin alanları
    public final int hp;
    public final int speed;
    public final double scale;
    public final int damage;
    public final int reward;
    public final String[] spritePaths;

    /**
     * Her EnemyType için yapılandırıcı.
     */
    EnemyType(int hp, int speed, double scale, int damage, int reward, String[] spritePaths) {
        this.hp = hp;
        this.speed = speed;
        this.scale = scale;
        this.damage = damage;
        this.reward = reward;
        this.spritePaths = spritePaths;
    }

    /**
     * Eğer birden fazla sprite varsa, rastgele birini döndürür.
     * Şu anda her düşmanda tek dosya olsa da geleceğe dönük genişleme için uygundur.
     *
     * @return Seçilen sprite dosyasının yolu
     */
    public String getRandomSpritePath() {
        return spritePaths[random.nextInt(spritePaths.length)];
    }
}
