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
            "Tăng sát thương cận chiến trực tiếp",
            "generic.attack_damage",
            "attack_damage"
    ),
    MAX_HEALTH(
            "&d❤ Sinh Lực Tối Đa",
            2.0,
            " Máu",
            Material.GOLDEN_APPLE,
            "Tăng giới hạn máu tối đa cho nhân vật",
            "generic.max_health",
            "max_health"
    ),
    ARMOR(
            "&b🛡 Giáp Phòng Thủ",
            1.5,
            " Giáp",
            Material.DIAMOND_CHESTPLATE,
            "Tăng chỉ số giáp giảm sát thương vật lý",
            "generic.armor",
            "armor"
    ),
    ARMOR_TOUGHNESS(
            "&3💎 Độ Cứng Giáp",
            1.0,
            " Độ cứng",
            Material.NETHERITE_CHESTPLATE,
            "Chống xuyên giáp từ đòn đánh hạng nặng",
            "generic.armor_toughness",
            "armor_toughness"
    ),
    ATTACK_SPEED(
            "&e⚡ Tốc Độ Xuất Chiêu",
            0.12,
            " Tốc đánh",
            Material.GOLDEN_SWORD,
            "Tăng tốc độ vung vũ khí và công cụ",
            "generic.attack_speed",
            "attack_speed"
    ),
    MOVEMENT_SPEED(
            "&a👟 Tốc Độ Di Chuyển",
            0.008,
            " Tốc chạy",
            Material.FEATHER,
            "Giúp di chuyển linh hoạt hơn trong giao tranh",
            "generic.movement_speed",
            "movement_speed"
    ),
    LIFESTEAL(
            "&4🩸 Hút Máu Chiến Đấu",
            2.5,
            "% Hút máu",
            Material.REDSTONE,
            "Hồi máu theo % sát thương gây ra cho mục tiêu",
            null,
            null
    ),
    CRIT_STRIKE(
            "&6💥 Bạo Kích Huỷ Diệt",
            4.0,
            "% Chí mạng (x1.5 DMG)",
            Material.BLAZE_POWDER,
            "Xác suất nổ đòn chí mạng x1.5 sát thương",
            null,
            null
    ),
    TRUE_DAMAGE(
            "&5🔥 Sát Thương Chuẩn (Xuyên Giáp)",
            1.0,
            " Sát thương chuẩn",
            Material.NETHER_STAR,
            "Gây sát thương xuyên giáp đối phương & Boss MythicMobs",
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
}
