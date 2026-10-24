package vn.tainguyen.tnupgrader.model;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;

public enum UpgradeAttribute {

    ATTACK_DAMAGE(
            "&c⚔ Sát Thương Tấn Công",
            1.5,
            " Sát thương",
            Material.NETHERITE_SWORD,
            "Tăng trực tiếp sát thương khi đánh quái, Boss và người chơi",
            "generic.attack_damage",
            "attack_damage"
    ),
    MAX_HEALTH(
            "&d❤ Sinh Lực Tối Đa (Máu)",
            2.0,
            " Máu (1 Tim)",
            Material.GOLDEN_APPLE,
            "Tăng giới hạn máu tối đa cho nhân vật khi cầm hoặc mặc đồ",
            "generic.max_health",
            "max_health"
    ),
    ARMOR(
            "&b🛡 Giáp Phòng Thủ",
            1.5,
            " Giáp",
            Material.DIAMOND_CHESTPLATE,
            "Tăng chỉ số giáp giúp giảm mạnh sát thương vật lý nhận vào",
            "generic.armor",
            "armor"
    ),
    ARMOR_TOUGHNESS(
            "&3💎 Độ Cứng Giáp (Chống Xuyên Giáp)",
            1.0,
            " Độ cứng",
            Material.NETHERITE_CHESTPLATE,
            "Giúp giáp bền vững trước các đòn đánh hạng nặng của Boss",
            "generic.armor_toughness",
            "armor_toughness"
    ),
    ATTACK_SPEED(
            "&e⛏ Tốc Đánh & Đào Nhanh",
            0.15,
            " Tốc độ",
            Material.GOLDEN_PICKAXE,
            "Tăng tốc độ vung vũ khí VÀ tăng +25% tốc độ đào khối mỗi cấp",
            "generic.attack_speed",
            "attack_speed"
    ),
    MOVEMENT_SPEED(
            "&a👟 Tốc Độ Di Chuyển",
            4.0,
            "% Tốc chạy",
            Material.FEATHER,
            "Giúp nhân vật chạy nhanh và linh hoạt hơn trong chiến đấu",
            "generic.movement_speed",
            "movement_speed"
    ),
    LIFESTEAL(
            "&4🩸 Hút Máu Chiến Đấu",
            2.5,
            "% Hút máu",
            Material.REDSTONE,
            "Tự động hồi máu cho bản thân theo % sát thương gây ra",
            null,
            null
    ),
    CRIT_STRIKE(
            "&6💥 Bạo Kích Huỷ Diệt (Chí Mạng)",
            4.0,
            "% Chí mạng (x1.5 Đam)",
            Material.BLAZE_POWDER,
            "Tỉ lệ nổ đòn chí mạng gây gấp 1.5 lần sát thương kèm hiệu ứng",
            null,
            null
    ),
    TRUE_DAMAGE(
            "&5🔥 Sát Thương Chuẩn (Xuyên Giáp)",
            1.0,
            " Sát thương chuẩn",
            Material.NETHER_STAR,
            "Gây sát thương trừ thẳng vào máu, bỏ qua mọi loại giáp đối phương",
            null,
            null
    );

    private final String defaultDisplayName;
    private final double defaultPerLevel;
    private final String defaultUnit;
    private final Material defaultIcon;
    private final String defaultDescription;
    private final String legacyAttributeKey;
    private final String modernAttributeKey;

    UpgradeAttribute(String defaultDisplayName, double defaultPerLevel, String defaultUnit,
                     Material defaultIcon, String defaultDescription,
                     String legacyAttributeKey, String modernAttributeKey) {
        this.defaultDisplayName = defaultDisplayName;
        this.defaultPerLevel = defaultPerLevel;
        this.defaultUnit = defaultUnit;
        this.defaultIcon = defaultIcon;
        this.defaultDescription = defaultDescription;
        this.legacyAttributeKey = legacyAttributeKey;
        this.modernAttributeKey = modernAttributeKey;
    }

    public String getDefaultDisplayName() {
        return defaultDisplayName;
    }

    public double getDefaultPerLevel() {
        return defaultPerLevel;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }

    public Material getDefaultIcon() {
        return defaultIcon;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public boolean isVanillaAttribute() {
        return legacyAttributeKey != null;
    }

    public Attribute resolveBukkitAttribute() {
        if (legacyAttributeKey == null) {
            return null;
        }
        Attribute attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(legacyAttributeKey));
        if (attr == null && modernAttributeKey != null) {
            attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(modernAttributeKey));
        }
        return attr;
    }

    public static Attribute resolveBlockBreakSpeedAttribute() {
        Attribute attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("player.block_break_speed"));
        if (attr == null) {
            attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("block_break_speed"));
        }
        return attr;
    }
}
