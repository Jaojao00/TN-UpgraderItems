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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExcellentEnchantsHook {

    private final TNUpgraderPlugin plugin;
    private boolean excellentInstalled = false;
    private final Map<String, String> vietnameseEnchantNames = new HashMap<>();

    public ExcellentEnchantsHook(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
        initVietnameseEnchantMap();
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
            return "✦ " + formatted + " (Excellent)";
        }
        return formatted;
    }

    public ItemStack createEnchantedBook(Enchantment enchantment, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.addStoredEnchant(enchantment, level, true);
            String title = getEnchantDisplayName(enchantment) + " " + toRoman(level);
            storageMeta.displayName(TextUtil.color("&dSách Phù Phép: &e" + title));
            book.setItemMeta(storageMeta);
        }
        return book;
    }

    public List<Enchantment> getApplicableEnchantments(ItemStack item) {
        List<Enchantment> result = new ArrayList<>();
        if (item == null || item.getType().isAir()) {
            return result;
        }
        ItemMeta meta = item.getItemMeta();
        for (Enchantment enchant : Registry.ENCHANTMENT) {
            if (enchant.canEnchantItem(item) || (meta != null && meta.hasEnchant(enchant))) {
                result.add(enchant);
            }
        }
        return result;
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

    private void initVietnameseEnchantMap() {
        vietnameseEnchantNames.put("sharpness", "Sắc bén");
        vietnameseEnchantNames.put("smite", "Trừng phạt");
        vietnameseEnchantNames.put("bane_of_arthropods", "Hại chân đốt");
        vietnameseEnchantNames.put("knockback", "Bật lùi");
        vietnameseEnchantNames.put("fire_aspect", "Khía cạnh lửa");
        vietnameseEnchantNames.put("looting", "Nhặt đồ");
        vietnameseEnchantNames.put("sweeping_edge", "Quét kiếm");
        vietnameseEnchantNames.put("efficiency", "Hiệu suất");
        vietnameseEnchantNames.put("silk_touch", "Chạm nhẹ");
        vietnameseEnchantNames.put("unbreaking", "Chậm hỏng");
        vietnameseEnchantNames.put("fortune", "Gia tài");
        vietnameseEnchantNames.put("power", "Sức mạnh");
        vietnameseEnchantNames.put("punch", "Đấm bật");
        vietnameseEnchantNames.put("flame", "Mũi tên lửa");
        vietnameseEnchantNames.put("infinity", "Vô hạn");
        vietnameseEnchantNames.put("protection", "Bảo vệ");
        vietnameseEnchantNames.put("fire_protection", "Bảo vệ khỏi lửa");
        vietnameseEnchantNames.put("feather_falling", "Rơi nhẹ");
        vietnameseEnchantNames.put("blast_protection", "Bảo vệ khỏi vụ nổ");
        vietnameseEnchantNames.put("projectile_protection", "Bảo vệ khỏi đạn");
        vietnameseEnchantNames.put("respiration", "Hô hấp");
        vietnameseEnchantNames.put("aqua_affinity", "Đào nhanh dưới nước");
        vietnameseEnchantNames.put("thorns", "Gai");
        vietnameseEnchantNames.put("depth_strider", "Sải bước dưới sâu");
        vietnameseEnchantNames.put("frost_walker", "Chân băng");
        vietnameseEnchantNames.put("soul_speed", "Tốc độ linh hồn");
        vietnameseEnchantNames.put("swift_sneak", "Lén lút nhanh");
        vietnameseEnchantNames.put("mending", "Sửa chữa");
        vietnameseEnchantNames.put("vanishing_curse", "Lời nguyền biến mất");
        vietnameseEnchantNames.put("binding_curse", "Lời nguyền ràng buộc");
        vietnameseEnchantNames.put("loyalty", "Trung thành");
        vietnameseEnchantNames.put("impaling", "Đâm xuyên");
        vietnameseEnchantNames.put("riptide", "Dòng chảy");
        vietnameseEnchantNames.put("channeling", "Dẫn sét");
        vietnameseEnchantNames.put("multishot", "Bắn nhiều tên");
        vietnameseEnchantNames.put("quick_charge", "Nạp nhanh");
        vietnameseEnchantNames.put("piercing", "Xuyên thủng");
        vietnameseEnchantNames.put("density", "Mật độ (Chùy 1.21)");
        vietnameseEnchantNames.put("breach", "Phá giáp (Chùy 1.21)");
        vietnameseEnchantNames.put("wind_burst", "Gió nổ (Chùy 1.21)");
    }
}
