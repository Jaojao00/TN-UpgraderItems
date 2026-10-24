package vn.tainguyen.tnupgrader.hook;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.util.TextUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExcellentEnchantsHook {

    private final TNUpgraderPlugin plugin;
    private boolean excellentInstalled = false;
    private final Map<String, String> vietnameseEnchantNames = new LinkedHashMap<>();
    private final Map<String, String> vietnameseEnchantDescriptions = new HashMap<>();
    private final Map<String, String> vietnameseEnchantEffects = new HashMap<>();

    public ExcellentEnchantsHook(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
        initVietnameseEnchantData();
        checkHook();
    }

    public final void checkHook() {
        this.excellentInstalled = (Bukkit.getPluginManager().getPlugin("ExcellentEnchants") != null
                || Bukkit.getPluginManager().getPlugin("nightcore") != null)
                && plugin.getConfig().getBoolean("hooks.excellentenchants.enabled", true);
        if (this.excellentInstalled) {
            plugin.getLogger().info("Đã kết nối thành công với ExcellentEnchants! Đã tải toàn bộ phù phép tùy chỉnh từ Registry 1.21+.");
        }
    }

    public boolean isExcellentInstalled() {
        return excellentInstalled;
    }

    public List<Enchantment> getAllRegisteredEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        for (Enchantment enchant : Registry.ENCHANTMENT) {
            list.add(enchant);
        }
        return list;
    }

    public boolean isCustomEnchantment(Enchantment enchantment) {
        if (enchantment == null) return false;
        NamespacedKey key = enchantment.getKey();
        if (!key.getNamespace().equalsIgnoreCase("minecraft")) {
            return true;
        }
        return !vietnameseEnchantNames.containsKey(key.getKey().toLowerCase(Locale.ROOT));
    }

    public String getEnchantDisplayName(Enchantment enchantment) {
        if (enchantment == null) return "Không rõ";
        String key = enchantment.getKey().getKey().toLowerCase(Locale.ROOT);
        String vn = vietnameseEnchantNames.get(key);
        if (vn != null) {
            return vn;
        }
        String[] parts = key.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        String formatted = sb.toString().trim();
        if (isCustomEnchantment(enchantment)) {
            return "✦ " + formatted + " (Phù phép Đặc biệt)";
        }
        return formatted;
    }

    public String getEnchantDescription(Enchantment enchantment) {
        if (enchantment == null) return "Cường hóa sức mạnh cho trang bị của bạn.";
        String key = enchantment.getKey().getKey().toLowerCase(Locale.ROOT);
        String desc = vietnameseEnchantDescriptions.get(key);
        if (desc != null) {
            return desc;
        }
        if (isCustomEnchantment(enchantment)) {
            return "Phù phép đặc biệt mở rộng từ ExcellentEnchants giúp kích hoạt kỹ năng phụ trợ.";
        }
        return "Tăng hiệu quả sử dụng và sức mạnh cho trang bị.";
    }

    public String getEnchantEffectSummary(Enchantment enchantment, int level) {
        if (enchantment == null) return "";
        String key = enchantment.getKey().getKey().toLowerCase(Locale.ROOT);
        String template = vietnameseEnchantEffects.get(key);
        if (template != null) {
            return template.replace("{level}", String.valueOf(level))
                    .replace("{percent}", String.valueOf(level * 20))
                    .replace("{dmg}", TextUtil.formatValue(0.5 * level + 0.5));
        }
        return "Hiệu lực cường hóa Cấp " + level;
    }

    public ItemStack createEnchantedBook(Enchantment enchantment, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.addStoredEnchant(enchantment, level, true);
            String title = getEnchantDisplayName(enchantment) + " " + toRoman(level);
            storageMeta.displayName(TextUtil.color("&d&l📘 Sách Phù Phép: &e&l" + title));
            List<String> lore = Arrays.asList(
                    "&7Công dụng: &f" + getEnchantDescription(enchantment),
                    "&7Hiệu lực: &a" + getEnchantEffectSummary(enchantment, level),
                    "",
                    "&e▸ Hướng dẫn sử dụng:",
                    "&f  Đem sách này ghép vào trang bị trên &6Cái Đe (Anvil)",
                    "&f  hoặc dùng trực tiếp &6Bàn Cường Hóa Trang Bị &fđể",
                    "&f  ép thẳng phù phép lên Vũ khí / Công cụ / Giáp!"
            );
            storageMeta.lore(TextUtil.colorList(lore));
            book.setItemMeta(storageMeta);
        }
        return book;
    }

    /**
     * Lấy danh sách phù phép phù hợp với trang bị, tự động ưu tiên đưa Hiệu Suất (Đào Nhanh),
     * Chậm Hỏng, Sửa Chữa, Gia Tài, Sắc Bén, Bảo Vệ lên đầu danh sách để dễ nhìn nhất!
     */
    public List<Enchantment> getApplicableEnchantments(ItemStack item) {
        List<Enchantment> result = new ArrayList<>();
        if (item == null || item.getType().isAir()) {
            return result;
        }
        Material mat = item.getType();
        String matName = mat.name();
        ItemMeta meta = item.getItemMeta();

        boolean isTool = matName.endsWith("_PICKAXE") || matName.endsWith("_AXE")
                || matName.endsWith("_SHOVEL") || matName.endsWith("_HOE")
                || mat == Material.SHEARS || mat == Material.FLINT_AND_STEEL || mat == Material.FISHING_ROD;
        boolean isWeapon = matName.endsWith("_SWORD") || matName.endsWith("_AXE")
                || mat == Material.TRIDENT || mat == Material.MACE
                || mat == Material.BOW || mat == Material.CROSSBOW;
        boolean isArmor = matName.endsWith("_HELMET") || matName.endsWith("_CHESTPLATE")
                || matName.endsWith("_LEGGINGS") || matName.endsWith("_BOOTS")
                || mat == Material.ELYTRA || mat == Material.TURTLE_HELMET || mat == Material.SHIELD;
        boolean isDamageable = mat.getMaxDurability() > 0;

        for (Enchantment enchant : Registry.ENCHANTMENT) {
            String key = enchant.getKey().getKey().toLowerCase(Locale.ROOT);
            // Bỏ qua các lời nguyền bất lợi (Curse) để người chơi không bấm nhầm
            if (key.contains("curse")) {
                continue;
            }

            boolean canApply = enchant.canEnchantItem(item) || (meta != null && meta.hasEnchant(enchant));

            // Đảm bảo các phù phép cốt lõi như Hiệu Suất (Đào Nhanh), Chậm Hỏng, Sửa Chữa, Gia Tài luôn xuất hiện đúng nhóm đồ
            if (!canApply) {
                if (isDamageable && (key.equals("unbreaking") || key.equals("mending"))) {
                    canApply = true;
                } else if (isTool && (key.equals("efficiency") || key.equals("fortune") || key.equals("silk_touch"))) {
                    canApply = true;
                } else if (isWeapon && (key.equals("sharpness") || key.equals("looting") || key.equals("fire_aspect") || key.equals("unbreaking") || key.equals("mending"))) {
                    canApply = true;
                } else if (isArmor && (key.equals("protection") || key.equals("thorns") || key.equals("unbreaking") || key.equals("mending"))) {
                    canApply = true;
                }
            }

            if (canApply) {
                result.add(enchant);
            }
        }

        // Sắp xếp theo mức độ ưu tiên: Phù phép quan trọng nhất (Hiệu suất, Chậm hỏng, Sửa chữa, Gia tài, Sắc bén, Bảo vệ) lên đầu!
        result.sort((a, b) -> Integer.compare(getPriorityScore(a, isTool, isWeapon, isArmor), getPriorityScore(b, isTool, isWeapon, isArmor)));
        return result;
    }

    private int getPriorityScore(Enchantment enchant, boolean isTool, boolean isWeapon, boolean isArmor) {
        String key = enchant.getKey().getKey().toLowerCase(Locale.ROOT);
        if (isTool) {
            return switch (key) {
                case "efficiency" -> 1;   // Đào nhanh / Hiệu suất luôn ở ô số 1!
                case "unbreaking" -> 2;   // Chậm hỏng ở ô số 2!
                case "fortune" -> 3;      // Gia tài ở ô số 3!
                case "mending" -> 4;      // Sửa chữa ở ô số 4!
                case "silk_touch" -> 5;   // Chạm nhẹ ở ô số 5!
                default -> 50;
            };
        }
        if (isWeapon) {
            return switch (key) {
                case "sharpness", "power", "density" -> 1;
                case "unbreaking" -> 2;
                case "looting", "breach" -> 3;
                case "mending" -> 4;
                case "fire_aspect", "flame", "wind_burst" -> 5;
                case "sweeping_edge", "infinity" -> 6;
                case "knockback", "punch" -> 7;
                case "efficiency" -> 8;
                default -> 50;
            };
        }
        if (isArmor) {
            return switch (key) {
                case "protection" -> 1;
                case "unbreaking" -> 2;
                case "mending" -> 3;
                case "aqua_affinity" -> 4; // Đào nhanh dưới nước
                case "respiration" -> 5;   // Hô hấp dưới nước
                case "feather_falling" -> 6;
                case "depth_strider" -> 7;
                case "thorns" -> 8;
                case "soul_speed", "swift_sneak" -> 9;
                default -> 50;
            };
        }
        return switch (key) {
            case "efficiency" -> 1;
            case "unbreaking" -> 2;
            case "mending" -> 3;
            default -> 50;
        };
    }

    public static String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(number);
        };
    }

    private void initVietnameseEnchantData() {
        // --- NHÓM ĐÀO NHANH, CÔNG CỤ & ĐỘ BỀN ---
        registerEnchant("efficiency",
                "⛏ Hiệu Suất (Đào Nhanh)",
                "Tăng mạnh tốc độ đào đá, quặng, gỗ và phá hủy mọi loại khối",
                "Tăng tốc độ đào nhanh vượt trội (Cấp {level})");

        registerEnchant("unbreaking",
                "🛡 Chậm Hỏng (Tăng Độ Bền)",
                "Giảm mạnh tỉ lệ bị mất độ bền khi đào khối hoặc chiến đấu, giúp đồ siêu bền",
                "Giảm hao mòn độ bền gấp {level} lần (Cấp {level})");

        registerEnchant("mending",
                "💚 Sửa Chữa (Tự Hồi Độ Bền)",
                "Tự động dùng điểm Kinh Nghiệm (EXP) nhặt được để sửa lành lại trang bị",
                "Tự động hồi độ bền bằng hạt EXP");

        registerEnchant("fortune",
                "💎 Gia Tài (Nhân Đôi Khoáng Sản)",
                "Tăng số lượng Kim cương, Ngọc lục bảo, Vàng, Sắt, Than rơi ra khi đào quặng",
                "Tăng tới +{percent}% sản lượng khoáng sản (Cấp {level})");

        registerEnchant("silk_touch",
                "✨ Chạm Nhẹ (Đào Nguyên Khối)",
                "Đào và thu hoạch nguyên vẹn khối quặng, kính, tổ ong, băng mà không làm vỡ khối",
                "Thu hoạch nguyên vẹn khối gốc");

        registerEnchant("aqua_affinity",
                "🌊 Đào Nhanh Dưới Nước (Thợ Lặn)",
                "Giúp tốc độ đào khối khi đang lặn dưới nước nhanh ngang với khi ở trên cạn",
                "Xóa bỏ phạt giảm tốc độ đào dưới nước");

        // --- NHÓM VŨ KHÍ CẬN CHIẾN & TẦM XA ---
        registerEnchant("sharpness",
                "⚔ Sắc Bén (Tăng Sát Thương)",
                "Tăng trực tiếp sát thương đòn chém cận chiến lên mọi quái vật, Boss và người chơi",
                "Cộng thêm +{dmg} sát thương mỗi đòn đánh (Cấp {level})");

        registerEnchant("smite",
                "☀ Trừng Phạt (Khắc Chế Xác Sống)",
                "Tăng mạnh sát thương khi đánh Zombie, Skeleton, Wither và quái vật bất tử",
                "Tăng mạnh sát thương lên quái Xác Sống (Cấp {level})");

        registerEnchant("bane_of_arthropods",
                "🕷 Hại Chân Đốt (Khắc Chế Nhện)",
                "Tăng sát thương và làm chậm Nhện, Nhện hang, Cá bạc, Ong",
                "Tăng sát thương & làm chậm loài Chân Đốt (Cấp {level})");

        registerEnchant("looting",
                "🍀 Nhặt Đồ (Tăng Rơi Đồ Quái)",
                "Tăng số lượng vật phẩm rơi ra và tăng tỉ lệ rớt đồ hiếm khi tiêu diệt quái/Boss",
                "Tăng +{percent}% lượng đồ rơi từ quái (Cấp {level})");

        registerEnchant("fire_aspect",
                "🔥 Khía Cạnh Lửa (Đốt Cháy)",
                "Thiêu đốt mục tiêu khi chém trúng, gây sát thương lửa liên tục theo thời gian",
                "Đốt cháy mục tiêu trong {level}x4 giây");

        registerEnchant("sweeping_edge",
                "💨 Quét Kiếm (Chém Lan)",
                "Tăng mạnh sát thương của đòn chém lan rộng ra nhiều kẻ địch đứng gần nhau",
                "Tăng sát thương đòn đánh lan (Cấp {level})");

        registerEnchant("knockback",
                "💨 Bật Lùi (Đẩy Lùi Kẻ Địch)",
                "Đẩy văng mục tiêu ra xa hơn sau mỗi đòn tấn công để giữ khoảng cách an toàn",
                "Tăng lực đẩy lùi mục tiêu (Cấp {level})");

        // --- NHÓM GIÁP TRỤ & PHÒNG THỦ ---
        registerEnchant("protection",
                "🛡 Bảo Vệ Toàn Diện",
                "Giảm sát thương nhận vào từ hầu hết mọi nguồn (Chém, tên bắn, lửa, vụ nổ)",
                "Giảm sát thương tổng hợp nhận vào (Cấp {level})");

        registerEnchant("fire_protection",
                "🔥 Bảo Vệ Khỏi Lửa",
                "Giảm sát thương từ lửa, dung nham (Lava), cầu lửa Blaze và dập lửa nhanh hơn",
                "Kháng sát thương lửa & dung nham (Cấp {level})");

        registerEnchant("blast_protection",
                "💥 Bảo Vệ Khỏi Vụ Nổ",
                "Giảm sát thương và lực hất văng từ Creeper, TNT, Wither và pha lê End",
                "Kháng sát thương nổ & giảm hất tung (Cấp {level})");

        registerEnchant("projectile_protection",
                "🏹 Bảo Vệ Khỏi Đạn Tên",
                "Giảm sát thương từ mũi tên Skeleton, đinh ba Drowned và đạn của Pillager",
                "Kháng sát thương tên & đạn tầm xa (Cấp {level})");

        registerEnchant("feather_falling",
                "🪶 Rơi Nhẹ (Giảm Sát Thương Ngã)",
                "Bảo vệ đôi chân, giảm mạnh sát thương khi rơi từ trên cao hoặc dùng Ngọc Ender",
                "Giảm mạnh sát thương khi rơi từ trên cao (Cấp {level})");

        registerEnchant("respiration",
                "🫧 Hô Hấp (Thở Dưới Nước)",
                "Kéo dài thời gian nín thở dưới nước và giúp tầm nhìn dưới đáy biển sáng rõ hơn",
                "Cộng thêm +{level}x15 giây thở dưới nước");

        registerEnchant("depth_strider",
                "🏊 Sải Bước Dưới Sâu (Bơi Nhanh)",
                "Tăng mạnh tốc độ di chuyển và bơi lội khi đang ở dưới nước",
                "Tăng tốc độ di chuyển dưới nước (Cấp {level})");

        registerEnchant("thorns",
                "🌵 Gai Phản Đòn",
                "Tự động phản lại một phần sát thương vào kẻ nào dám tấn công bạn",
                "Phản lại sát thương vào kẻ tấn công (Cấp {level})");

        registerEnchant("soul_speed",
                "👻 Tốc Độ Linh Hồn",
                "Chạy cực nhanh trên Cát Linh Hồn (Soul Sand) và Đất Linh Hồn ở Nether",
                "Tăng tốc chạy trên Cát Linh Hồn (Cấp {level})");

        registerEnchant("swift_sneak",
                "🥷 Lén Lút Nhanh",
                "Giúp di chuyển nhanh hơn rất nhiều khi đang giữ phím ngồi (Sneak/Shift)",
                "Tăng tốc độ di chuyển khi cúi người (Cấp {level})");

        registerEnchant("frost_walker",
                "❄ Chân Băng (Đi Trên Mặt Nước)",
                "Đóng băng mặt nước dưới chân thành băng cứng để đi bộ qua sông hồ",
                "Đóng băng mặt nước bán kính rộng (Cấp {level})");

        // --- NHÓM CUNG, NỎ, ĐINH BA & CHÙY 1.21 ---
        registerEnchant("power",
                "🏹 Sức Mạnh (Tăng Đam Cung)",
                "Tăng mạnh sát thương của mũi tên bắn ra từ Cung",
                "Tăng +{percent}% sát thương mũi tên (Cấp {level})");

        registerEnchant("punch",
                "💨 Đấm Bật (Tên Đẩy Lùi)",
                "Mũi tên bắn trúng sẽ đẩy văng kẻ địch ra xa",
                "Tăng lực đẩy lùi của mũi tên (Cấp {level})");

        registerEnchant("flame",
                "🔥 Mũi Tên Lửa",
                "Biến mọi mũi tên bắn ra thành tên lửa thiêu đốt mục tiêu",
                "Bắn ra mũi tên lửa đốt cháy kẻ địch");

        registerEnchant("infinity",
                "♾ Vô Hạn Tên",
                "Bắn cung không bao giờ tốn mũi tên thường (Chỉ cần có 1 mũi tên trong túi)",
                "Bắn tên không tiêu hao số lượng");

        registerEnchant("loyalty",
                "🔱 Trung Thành (Đinh Ba Tự Quay Về)",
                "Đinh ba sau khi phóng đi sẽ tự động bay ngược về tay chủ nhân",
                "Tốc độ bay về tay cấp {level}");

        registerEnchant("riptide",
                "🌊 Dòng Chảy (Phi Thân)",
                "Phóng người bay vọt đi cực nhanh khi đang ở dưới nước hoặc dưới trời mưa",
                "Lao vọt tốc độ cao (Cấp {level})");

        registerEnchant("channeling",
                "⚡ Dẫn Sét (Triệu Hồi Sấm Sét)",
                "Gọi tia sét giáng thẳng xuống mục tiêu khi phóng Đinh ba trúng đích",
                "Triệu hồi sấm sét đánh xuống mục tiêu");

        registerEnchant("impaling",
                "🔱 Đâm Xuyên Thủy Quái",
                "Tăng mạnh sát thương của Đinh ba lên các sinh vật biển và mục tiêu chạm nước",
                "Tăng sát thương Đinh ba (Cấp {level})");

        registerEnchant("multishot",
                "🏹 Bắn Nhiều Tên (Nỏ Ba Tia)",
                "Bắn ra cùng lúc 3 mũi tên hoặc 3 pháo hoa chỉ với 1 viên đạn duy nhất",
                "Bắn tỏa 3 mũi tên cùng lúc");

        registerEnchant("quick_charge",
                "⚡ Nạp Nhanh (Lên Đạn Nỏ)",
                "Rút ngắn thời gian kéo dây nạp đạn cho Nỏ, giúp bắn liên thanh cực nhanh",
                "Giảm mạnh thời gian nạp Nỏ (Cấp {level})");

        registerEnchant("piercing",
                "🎯 Xuyên Thủng (Tên Xuyên Thấu)",
                "Mũi tên bắn từ Nỏ xuyên qua nhiều mục tiêu và xuyên qua cả Khiên chắn",
                "Xuyên qua {level} mục tiêu & nhặt lại được tên");

        registerEnchant("density",
                "🔨 Mật Độ (Tăng Đam Chùy 1.21)",
                "Tăng mạnh sát thương đập từ trên cao xuống của Chùy Trọng Lực (Mace)",
                "Tăng sát thương theo độ cao rơi (Cấp {level})");

        registerEnchant("breach",
                "💥 Phá Giáp (Chùy Xuyên Giáp 1.21)",
                "Bỏ qua phần lớn chỉ số giáp phòng thủ của mục tiêu khi đập bằng Chùy",
                "Xuyên thủng giáp đối phương (Cấp {level})");

        registerEnchant("wind_burst",
                "🌪 Gió Nổ (Hất Tung Liên Hoàn 1.21)",
                "Tạo vụ nổ gió đẩy bạn bay ngược lên không trung sau khi đập trúng mục tiêu",
                "Tạo luồng gió bật nhảy liên hoàn (Cấp {level})");

        // --- CẦN CÂU ---
        registerEnchant("luck_of_the_sea",
                "🎣 Lộc Biển Cả (Câu Đồ Hiếm)",
                "Tăng tỉ lệ câu được kho báu quý hiếm (Sách phù phép, Cung, Thẻ tên) và giảm rác",
                "Tăng tỉ lệ câu trúng kho báu (Cấp {level})");

        registerEnchant("lure",
                "🐟 Nhử Mồi (Câu Cá Nhanh)",
                "Giảm mạnh thời gian chờ cá cắn câu, giúp câu cá nhanh gấp bội",
                "Cá cắn câu nhanh hơn (Cấp {level})");
    }

    private void registerEnchant(String key, String vnName, String vnDesc, String vnEffect) {
        vietnameseEnchantNames.put(key, vnName);
        vietnameseEnchantDescriptions.put(key, vnDesc);
        vietnameseEnchantEffects.put(key, vnEffect);
    }
}
