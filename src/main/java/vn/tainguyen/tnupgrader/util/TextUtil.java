package vn.tainguyen.tnupgrader.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final DecimalFormat PERCENT_FORMAT;
    private static final DecimalFormat VALUE_FORMAT;
    private static final Map<Material, String> VIETNAMESE_NAMES = new EnumMap<>(Material.class);

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        PERCENT_FORMAT = new DecimalFormat("0.00", symbols);
        VALUE_FORMAT = new DecimalFormat("#.##", symbols);
        initVietnameseDictionary();
    }

    private TextUtil() {
    }

    public static Component color(String text) {
        if (text == null) {
            return Component.empty();
        }
        return SERIALIZER.deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> colorList(List<String> lines) {
        List<Component> result = new ArrayList<>();
        if (lines == null) {
            return result;
        }
        for (String line : lines) {
            result.add(color(line));
        }
        return result;
    }

    public static String legacyColor(String text) {
        if (text == null) {
            return "";
        }
        return LegacyComponentSerializer.legacySection().serialize(color(text));
    }

    public static String formatPercent(double value) {
        return PERCENT_FORMAT.format(Math.max(0.0, value));
    }

    public static String formatValue(double value) {
        if (value == (long) value) {
            return String.format(Locale.US, "%d", (long) value);
        }
        return VALUE_FORMAT.format(value);
    }

    public static String removeAccents(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
    }

    public static String getVietnameseName(Material material) {
        if (material == null) {
            return "Không rõ";
        }
        String custom = VIETNAMESE_NAMES.get(material);
        if (custom != null) {
            return custom;
        }
        String[] parts = material.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    private static void initVietnameseDictionary() {
        // Khoáng sản & Nguyên liệu (Chuẩn theo bộ ảnh mẫu)
        VIETNAMESE_NAMES.put(Material.IRON_INGOT, "Thỏi sắt");
        VIETNAMESE_NAMES.put(Material.RAW_IRON, "Sắt thô");
        VIETNAMESE_NAMES.put(Material.IRON_ORE, "Quặng sắt");
        VIETNAMESE_NAMES.put(Material.DEEPSLATE_IRON_ORE, "Quặng sắt đá sâu");
        VIETNAMESE_NAMES.put(Material.IRON_BLOCK, "Khối sắt");
        VIETNAMESE_NAMES.put(Material.COPPER_INGOT, "Thỏi đồng");
        VIETNAMESE_NAMES.put(Material.RAW_COPPER, "Đồng thô");
        VIETNAMESE_NAMES.put(Material.GOLD_INGOT, "Thỏi vàng");
        VIETNAMESE_NAMES.put(Material.RAW_GOLD, "Vàng thô");
        VIETNAMESE_NAMES.put(Material.GOLD_ORE, "Quặng vàng");
        VIETNAMESE_NAMES.put(Material.DEEPSLATE_GOLD_ORE, "Quặng vàng đá sâu");
        VIETNAMESE_NAMES.put(Material.GOLD_BLOCK, "Khối vàng");
        VIETNAMESE_NAMES.put(Material.DIAMOND, "Kim cương");
        VIETNAMESE_NAMES.put(Material.DIAMOND_ORE, "Quặng kim cương");
        VIETNAMESE_NAMES.put(Material.DEEPSLATE_DIAMOND_ORE, "Quặng kim cương đá sâu");
        VIETNAMESE_NAMES.put(Material.DIAMOND_BLOCK, "Khối kim cương");
        VIETNAMESE_NAMES.put(Material.EMERALD, "Ngọc lục bảo");
        VIETNAMESE_NAMES.put(Material.EMERALD_BLOCK, "Khối ngọc lục bảo");
        VIETNAMESE_NAMES.put(Material.NETHERITE_SCRAP, "Phế liệu Netherite");
        VIETNAMESE_NAMES.put(Material.NETHERITE_INGOT, "Thỏi Netherite");
        VIETNAMESE_NAMES.put(Material.NETHERITE_BLOCK, "Khối Netherite");
        VIETNAMESE_NAMES.put(Material.ANCIENT_DEBRIS, "Mảnh vỡ cổ đại");
        VIETNAMESE_NAMES.put(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, "Khuôn rèn nâng cấp Netherite");
        VIETNAMESE_NAMES.put(Material.LAPIS_LAZULI, "Ngọc lưu ly");
        VIETNAMESE_NAMES.put(Material.REDSTONE, "Đá đỏ");
        VIETNAMESE_NAMES.put(Material.COAL, "Than");
        VIETNAMESE_NAMES.put(Material.QUARTZ, "Thạch anh Nether");
        VIETNAMESE_NAMES.put(Material.AMETHYST_SHARD, "Mảnh thạch anh tím");
        VIETNAMESE_NAMES.put(Material.NETHER_STAR, "Sao Nether");
        VIETNAMESE_NAMES.put(Material.BEACON, "Đèn hiệu");
        VIETNAMESE_NAMES.put(Material.HEART_OF_THE_SEA, "Trái tim biển cả");
        VIETNAMESE_NAMES.put(Material.NAUTILUS_SHELL, "Vỏ ốc anh vũ");
        VIETNAMESE_NAMES.put(Material.BLAZE_ROD, "Que của Quỷ lửa");
        VIETNAMESE_NAMES.put(Material.ENDER_PEARL, "Ngọc Ender");
        VIETNAMESE_NAMES.put(Material.GHAST_TEAR, "Nước mắt Ghast");
        VIETNAMESE_NAMES.put(Material.SHULKER_SHELL, "Vỏ Shulker");
        VIETNAMESE_NAMES.put(Material.ECHO_SHARD, "Mảnh vọng âm");
        VIETNAMESE_NAMES.put(Material.BREEZE_ROD, "Que Gió lốc");
        VIETNAMESE_NAMES.put(Material.HEAVY_CORE, "Lõi nặng");
        VIETNAMESE_NAMES.put(Material.TRIAL_KEY, "Chìa khóa thử thách");
        VIETNAMESE_NAMES.put(Material.OMINOUS_TRIAL_KEY, "Chìa khóa thử thách điềm gở");

        // Vũ khí, Công cụ & Giáp Kim Cương (Chuẩn tên ảnh 2, 3, 4)
        VIETNAMESE_NAMES.put(Material.DIAMOND_BOOTS, "Ủng kim cương");
        VIETNAMESE_NAMES.put(Material.DIAMOND_SHOVEL, "Xẻng kim cương");
        VIETNAMESE_NAMES.put(Material.DIAMOND_SWORD, "Kiếm kim cương");
        VIETNAMESE_NAMES.put(Material.DIAMOND_HOE, "Cuốc kim cương");
        VIETNAMESE_NAMES.put(Material.DIAMOND_AXE, "Rìu kim cương");
        VIETNAMESE_NAMES.put(Material.DIAMOND_PICKAXE, "Cuốc chim kim cương");
        VIETNAMESE_NAMES.put(Material.DIAMOND_HELMET, "Mũ kim cương");
        VIETNAMESE_NAMES.put(Material.DIAMOND_HORSE_ARMOR, "Giáp ngựa kim cương");
        VIETNAMESE_NAMES.put(Material.DIAMOND_LEGGINGS, "Quần giáp kim cương");
        VIETNAMESE_NAMES.put(Material.DIAMOND_CHESTPLATE, "Áo giáp kim cương");

        // Vũ khí, Công cụ & Giáp Netherite
        VIETNAMESE_NAMES.put(Material.NETHERITE_BOOTS, "Ủng Netherite");
        VIETNAMESE_NAMES.put(Material.NETHERITE_SHOVEL, "Xẻng Netherite");
        VIETNAMESE_NAMES.put(Material.NETHERITE_SWORD, "Kiếm Netherite");
        VIETNAMESE_NAMES.put(Material.NETHERITE_HOE, "Cuốc Netherite");
        VIETNAMESE_NAMES.put(Material.NETHERITE_AXE, "Rìu Netherite");
        VIETNAMESE_NAMES.put(Material.NETHERITE_PICKAXE, "Cuốc chim Netherite");
        VIETNAMESE_NAMES.put(Material.NETHERITE_HELMET, "Mũ Netherite");
        VIETNAMESE_NAMES.put(Material.NETHERITE_LEGGINGS, "Quần giáp Netherite");
        VIETNAMESE_NAMES.put(Material.NETHERITE_CHESTPLATE, "Áo giáp Netherite");

        // Trang bị & Khối chức năng đặc biệt
        VIETNAMESE_NAMES.put(Material.TRIDENT, "Giáo kim cương / Đinh ba");
        VIETNAMESE_NAMES.put(Material.MACE, "Chùy Trọng Lực");
        VIETNAMESE_NAMES.put(Material.ELYTRA, "Cánh Elytra");
        VIETNAMESE_NAMES.put(Material.TOTEM_OF_UNDYING, "Vật tổ bất tử");
        VIETNAMESE_NAMES.put(Material.BOW, "Cung");
        VIETNAMESE_NAMES.put(Material.CROSSBOW, "Nỏ");
        VIETNAMESE_NAMES.put(Material.SHIELD, "Khiên");
        VIETNAMESE_NAMES.put(Material.TURTLE_HELMET, "Mai rùa");
        VIETNAMESE_NAMES.put(Material.BLAST_FURNACE, "Lò luyện kim");
        VIETNAMESE_NAMES.put(Material.ANVIL, "Cái đe");
        VIETNAMESE_NAMES.put(Material.ENCHANTING_TABLE, "Bàn phù phép");

        // Thức ăn (Chuẩn ảnh 9)
        VIETNAMESE_NAMES.put(Material.HONEY_BOTTLE, "Chai mật ong");
        VIETNAMESE_NAMES.put(Material.BEETROOT_SOUP, "Súp củ dền");
        VIETNAMESE_NAMES.put(Material.RABBIT_STEW, "Thịt thỏ hầm");
        VIETNAMESE_NAMES.put(Material.CHORUS_FRUIT, "Quả điệp khúc");
        VIETNAMESE_NAMES.put(Material.GOLDEN_CARROT, "Cà rốt vàng");
        VIETNAMESE_NAMES.put(Material.CAKE, "Bánh kem");
        VIETNAMESE_NAMES.put(Material.GOLDEN_APPLE, "Táo vàng");
        VIETNAMESE_NAMES.put(Material.ENCHANTED_GOLDEN_APPLE, "Táo vàng phù phép");
        VIETNAMESE_NAMES.put(Material.COOKED_BEEF, "Thịt bò bít tết");
        VIETNAMESE_NAMES.put(Material.COOKED_PORKCHOP, "Thịt heo nướng");

        // Khối xây dựng & Tự nhiên (Chuẩn ảnh 7)
        VIETNAMESE_NAMES.put(Material.BAMBOO, "Tre");
        VIETNAMESE_NAMES.put(Material.ANDESITE_SLAB, "Phiến đá andesit");
        VIETNAMESE_NAMES.put(Material.BIRCH_SLAB, "Phiến gỗ bạch dương");
        VIETNAMESE_NAMES.put(Material.COBBLESTONE_SLAB, "Phiến đá cuội");
        VIETNAMESE_NAMES.put(Material.DIORITE_SLAB, "Phiến đá diorit");
        VIETNAMESE_NAMES.put(Material.GRANITE_SLAB, "Phiến đá hoa cương");
        VIETNAMESE_NAMES.put(Material.STONE_SLAB, "Phiến đá");
        VIETNAMESE_NAMES.put(Material.OAK_LOG, "Thân gỗ sồi");
        VIETNAMESE_NAMES.put(Material.COBBLESTONE, "Đá cuội");
        VIETNAMESE_NAMES.put(Material.STONE, "Đá");
        VIETNAMESE_NAMES.put(Material.OBSIDIAN, "Hắc diện thạch");
        VIETNAMESE_NAMES.put(Material.CRYING_OBSIDIAN, "Hắc diện thạch khóc");
        VIETNAMESE_NAMES.put(Material.SPONGE, "Bọt biển");
    }
}
