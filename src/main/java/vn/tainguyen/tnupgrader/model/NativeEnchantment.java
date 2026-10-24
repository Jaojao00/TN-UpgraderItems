package vn.tainguyen.tnupgrader.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Hệ thống 12 Cổ Ngữ & Phù Phép Độc Quyền của riêng TN-UpgraderItems.
 * Hoạt động độc lập 100% trên mọi server Paper 1.21+ mà KHÔNG cần cài ExcellentEnchants hay MythicMobs!
 */
public enum NativeEnchantment {

    // ==========================================================================
    // 1. NHÁNH CÔNG CỤ & KHAI THÁC (CUỐC CHIM, XẺNG, RÌU, CUỐC)
    // ==========================================================================
    VEIN_MINER(
            "&b⛏ Mũi Khoan Địa Tâm (Đào Mạch Quặng)",
            5,
            800.0,
            Material.DIAMOND_PICKAXE,
            TargetGroup.TOOLS,
            "Khi đào 1 khối quặng hoặc chặt 1 khúc gỗ, tự động phá vỡ liên hoàn cả mạch quặng/cây gỗ liền kề",
            "Đào liên hoàn tối đa {vein} khối quặng/gỗ cùng lúc"
    ),
    DRILL_3X3(
            "&e🔨 Búa Thần Khai Sơn (Đào Rộng 3x3)",
            3,
            1200.0,
            Material.NETHERITE_PICKAXE,
            TargetGroup.DIGGERS,
            "Mở rộng phạm vi đào đá, đất, sỏi, cát thành vùng rộng 3x3 giúp đào hầm và san lấp siêu tốc",
            "Tỉ lệ {chance3x3}% kích hoạt đào rộng 3x3 khối (Cấp 3 = 100% luôn đào 3x3!)"
    ),
    AUTO_SMELT(
            "&6🔥 Lò Nung Tự Động (Nung Quặng Tức Thì)",
            3,
            750.0,
            Material.BLAST_FURNACE,
            TargetGroup.TOOLS,
            "Tự động nung chảy Quặng Sắt, Vàng, Đồng, Cát, Đá cuội thành Thỏi kim loại & Kính ngay khi đào",
            "Tỉ lệ {chance3x3}% tự động nung quặng + rơi thêm kinh nghiệm (EXP)"
    ),
    TELEKINESIS(
            "&d🧲 Từ Trường Hút Đồ (Hút Thẳng Vào Túi)",
            1,
            900.0,
            Material.ENDER_EYE,
            TargetGroup.TOOLS_AND_WEAPONS,
            "Mọi khoáng sản đào được hoặc vật phẩm rơi từ quái sẽ bay thẳng vào túi đồ, không sợ rơi xuống Lava",
            "100% hút toàn bộ vật phẩm rơi thẳng vào túi đồ của bạn"
    ),
    HARVESTER(
            "&a🌾 Thu Hoạch Mùa Vụ (Tự Gieo Hạt & Gặt Lan)",
            3,
            600.0,
            Material.GOLDEN_HOE,
            TargetGroup.TOOLS,
            "Khi thu hoạch cây trồng đã chín sẽ tự động gieo lại hạt giống mới và thu hoạch lan xung quanh",
            "Tự động gieo lại hạt giống & thu hoạch bán kính rộng (Cấp {level})"
    ),

    // ==========================================================================
    // 2. NHÁNH VŨ KHÍ CHIẾN ĐẤU (KIẾM, RÌU, CUNG, NỎ, ĐINH BA, CHÙY)
    // ==========================================================================
    THUNDER_STRIKE(
            "&e⚡ Lôi Phạt Thiên Kích (Giáng Sấm Sét)",
            5,
            850.0,
            Material.LIGHTNING_ROD,
            TargetGroup.WEAPONS,
            "Đòn tấn công có xác suất triệu hồi tia sét giáng thẳng xuống mục tiêu, gây thêm sát thương chuẩn",
            "Tỉ lệ {proc}% giáng sét gây thêm +{dmg} sát thương"
    ),
    FROST_BITE(
            "&b❄ Hàn Băng Phong Ấn (Đóng Băng Làm Chậm)",
            5,
            700.0,
            Material.BLUE_ICE,
            TargetGroup.WEAPONS,
            "Phủ hàn khí đóng băng kẻ địch khi tấn công, làm mục tiêu bị Làm Chậm và giảm khả năng phản kháng",
            "Tỉ lệ {proc}% đóng băng & làm chậm mục tiêu trong {duration} giây"
    ),
    VENOMOUS(
            "&2☠ Độc Dược Hắc Ám (Trúng Độc & Khô Héo)",
            5,
            750.0,
            Material.FERMENTED_SPIDER_EYE,
            TargetGroup.WEAPONS,
            "Ám chất độc hắc ám vào vũ khí khiến kẻ địch bị rút máu liên tục bởi hiệu ứng Độc và Khô Héo",
            "Tỉ lệ {proc}% gây Trúng Độc & Khô Héo trong {duration} giây"
    ),
    EXECUTIONER(
            "&4🗡 Trảm Quyết Đột Kích (Kết Liễu Máu Thấp)",
            5,
            900.0,
            Material.NETHERITE_AXE,
            TargetGroup.WEAPONS,
            "Khi mục tiêu còn dưới 35% máu, đòn đánh của bạn bùng nổ sát thương kết liễu mục tiêu",
            "Tăng thêm +{exec}% sát thương lên mục tiêu dưới 35% máu"
    ),

    // ==========================================================================
    // 3. NHÁNH GIÁP TRỤ HỘ THỂ (MŨ, ÁO, QUẦN, ỦNG, ELYTRA, KHIÊN)
    // ==========================================================================
    VITALITY_REGEN(
            "&d💚 Sinh Mệnh Tái Tạo (Hồi Phục Máu)",
            5,
            800.0,
            Material.GHAST_TEAR,
            TargetGroup.ARMOR,
            "Khi bị tấn công trong giao tranh, giáp tự động kích hoạt luồng sinh khí hồi phục máu liên tục",
            "Tỉ lệ {proc}% kích hoạt Hồi Phục (Regeneration) khi nhận sát thương"
    ),
    MOLTEN_SHIELD(
            "&6🔥 Khiên Hỏa Diệm (Phản Lửa & Kháng Hỏa)",
            5,
            750.0,
            Material.MAGMA_CREAM,
            TargetGroup.ARMOR,
            "Thiêu đốt kẻ nào dám tấn công bạn, đồng thời ban hiệu ứng Kháng Lửa bảo vệ cơ thể",
            "Đốt cháy kẻ địch {duration} giây & nhận Kháng Lửa khi bị đánh"
    ),
    WIND_STEP(
            "&a🌪 Bước Chân Thần Tốc (Hộ Mệnh Khẩn Cấp)",
            5,
            850.0,
            Material.SUGAR,
            TargetGroup.ARMOR,
            "Khi máu tụt xuống dưới 40%, tự động kích hoạt Tăng Tốc chạy và Lá Chắn Hấp Thụ để lật kèo",
            "Kích hoạt Tăng Tốc II & Tim Vàng Hấp Thụ trong {duration} giây khi nguy kịch"
    );

    public enum TargetGroup {
        TOOLS("Công cụ khai thác (Cuốc chim, Xẻng, Rìu, Cuốc)"),
        DIGGERS("Cuốc chim & Xẻng"),
        TOOLS_AND_WEAPONS("Mọi Công cụ & Vũ khí"),
        WEAPONS("Vũ khí chiến đấu (Kiếm, Rìu, Cung, Nỏ, Đinh ba, Chùy)"),
        ARMOR("Giáp trụ & Khiên (Mũ, Áo, Quần, Ủng, Elytra, Khiên)");

        private final String label;

        TargetGroup(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final String displayName;
    private final int maxLevel;
    private final double baseCostV;
    private final Material icon;
    private final TargetGroup targetGroup;
    private final String description;
    private final String effectTemplate;

    NativeEnchantment(String displayName, int maxLevel, double baseCostV, Material icon,
                      TargetGroup targetGroup, String description, String effectTemplate) {
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.baseCostV = baseCostV;
        this.icon = icon;
        this.targetGroup = targetGroup;
        this.description = description;
        this.effectTemplate = effectTemplate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public double getBaseCostV() {
        return baseCostV;
    }

    public Material getIcon() {
        return icon;
    }

    public TargetGroup getTargetGroup() {
        return targetGroup;
    }

    public String getDescription() {
        return description;
    }

    public String formatEffect(int level) {
        int lvl = Math.max(1, level);
        int chance3x3 = lvl >= 3 ? 100 : (lvl * 35);
        return effectTemplate
                .replace("{level}", String.valueOf(lvl))
                .replace("{vein}", String.valueOf(lvl * 2 + 1))
                .replace("{chance3x3}", String.valueOf(chance3x3))
                .replace("{proc}", String.valueOf(lvl * 8 + 5))
                .replace("{dmg}", String.valueOf(lvl * 2))
                .replace("{duration}", String.valueOf(lvl + 2))
                .replace("{exec}", String.valueOf(lvl * 12));
    }

    public double getUpgradeCostV(int nextLevel) {
        return Math.round(baseCostV * Math.pow(1.5, Math.max(0, nextLevel - 1)));
    }

    public boolean canApplyTo(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        Material mat = item.getType();
        String name = mat.name();

        boolean isPickOrShovel = name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL");
        boolean isTool = isPickOrShovel || name.endsWith("_AXE") || name.endsWith("_HOE") || mat == Material.SHEARS;
        boolean isWeapon = name.endsWith("_SWORD") || name.endsWith("_AXE")
                || mat == Material.BOW || mat == Material.CROSSBOW
                || mat == Material.TRIDENT || mat == Material.MACE;
        boolean isArmor = name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                || mat == Material.ELYTRA || mat == Material.SHIELD || mat == Material.TURTLE_HELMET;

        return switch (targetGroup) {
            case DIGGERS -> isPickOrShovel;
            case TOOLS -> isTool;
            case TOOLS_AND_WEAPONS -> isTool || isWeapon;
            case WEAPONS -> isWeapon;
            case ARMOR -> isArmor;
        };
    }

    public static List<NativeEnchantment> getApplicable(ItemStack item) {
        List<NativeEnchantment> list = new ArrayList<>();
        for (NativeEnchantment rune : values()) {
            if (rune.canApplyTo(item)) {
                list.add(rune);
            }
        }
        return list;
    }
}
