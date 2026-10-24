package vn.tainguyen.tnupgrader.hook;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.util.ItemBuilder;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

public class MythicMobsHook {

    private final TNUpgraderPlugin plugin;
    private boolean enabled = false;
    private final NamespacedKey mythicTypeKey;
    private final NamespacedKey fallbackMythicKey;

    public MythicMobsHook(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
        this.mythicTypeKey = new NamespacedKey("mythicmobs", "type");
        this.fallbackMythicKey = new NamespacedKey(plugin, "mythic_item_id");
        checkHook();
    }

    public final void checkHook() {
        this.enabled = Bukkit.getPluginManager().getPlugin("MythicMobs") != null
                && plugin.getConfig().getBoolean("hooks.mythicmobs.enabled", true);
        if (this.enabled) {
            plugin.getLogger().info("Đã kết nối thành công với MythicMobs! Hỗ trợ vật phẩm & trang bị MythicMobs.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Lấy ID vật phẩm MythicMobs từ ItemStack (hỗ trợ cả MythicMobs 5.x PDC và fallback key)
     */
    public String getMythicItemId(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(mythicTypeKey, PersistentDataType.STRING)) {
            return pdc.get(mythicTypeKey, PersistentDataType.STRING);
        }
        if (pdc.has(fallbackMythicKey, PersistentDataType.STRING)) {
            return pdc.get(fallbackMythicKey, PersistentDataType.STRING);
        }
        if (!enabled) {
            return null;
        }
        try {
            Class<?> mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            Object mythicInst = mythicBukkitClass.getMethod("inst").invoke(null);
            Object itemManager = mythicBukkitClass.getMethod("getItemManager").invoke(mythicInst);
            Method isMythicItem = itemManager.getClass().getMethod("isMythicItem", ItemStack.class);
            boolean isMythic = (boolean) isMythicItem.invoke(itemManager, item);
            if (isMythic) {
                Method getMythicTypeFromItem = itemManager.getClass().getMethod("getMythicTypeFromItem", ItemStack.class);
                Object result = getMythicTypeFromItem.invoke(itemManager, item);
                if (result instanceof String str) {
                    return str;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Tạo ItemStack từ tên vật phẩm MythicMobs. Nếu server chưa cài MythicMobs thì tạo vật phẩm mô phỏng đẹp mắt.
     */
    public ItemStack getMythicItemStack(String mythicId, int amount) {
        if (enabled) {
            try {
                Class<?> mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
                Object mythicInst = mythicBukkitClass.getMethod("inst").invoke(null);
                Object itemManager = mythicBukkitClass.getMethod("getItemManager").invoke(mythicInst);
                Method getItemStack = itemManager.getClass().getMethod("getItemStack", String.class, int.class);
                Object stackObj = getItemStack.invoke(itemManager, mythicId, amount);
                if (stackObj instanceof ItemStack stack && !stack.getType().isAir()) {
                    return stack;
                }
            } catch (Exception ignored) {
            }
        }
        // Fallback / Preview item khi chưa tải file item MythicMobs tương ứng
        Material icon = Material.NETHERITE_SWORD;
        String lower = mythicId.toLowerCase();
        if (lower.contains("crown") || lower.contains("helm")) icon = Material.GOLDEN_HELMET;
        else if (lower.contains("stone") || lower.contains("gem") || lower.contains("crystal")) icon = Material.AMETHYST_SHARD;
        else if (lower.contains("bow")) icon = Material.BOW;

        return new ItemBuilder(icon, amount)
                .name("&d&l✦ [Mythic] &e" + mythicId)
                .lore(
                        "&8Vật phẩm Thần Thoại (MythicMobs)",
                        "&7Mã vật phẩm: &f" + mythicId,
                        "",
                        "&d▸ Vật phẩm đặc biệt có thể dùng để",
                        "&d  đặt cược hoặc trang bị chiến đấu!"
                )
                .glow(true)
                .pdc(fallbackMythicKey, mythicId)
                .build();
    }

    public String getDisplayName(ItemStack stack, String fallbackId) {
        if (stack != null && stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()
                && stack.getItemMeta().displayName() != null) {
            return PlainTextComponentSerializer.plainText().serialize(stack.getItemMeta().displayName());
        }
        return "[Mythic] " + fallbackId;
    }

    @SuppressWarnings("unchecked")
    public Collection<String> getLoadedMythicItemNames() {
        if (!enabled) {
            return Collections.emptyList();
        }
        try {
            Class<?> mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            Object mythicInst = mythicBukkitClass.getMethod("inst").invoke(null);
            Object itemManager = mythicBukkitClass.getMethod("getItemManager").invoke(mythicInst);
            Method getItemNames = itemManager.getClass().getMethod("getItemNames");
            Object names = getItemNames.invoke(itemManager);
            if (names instanceof Collection<?> col) {
                return (Collection<String>) col;
            }
        } catch (Exception ignored) {
        }
        return Collections.emptyList();
    }
}
