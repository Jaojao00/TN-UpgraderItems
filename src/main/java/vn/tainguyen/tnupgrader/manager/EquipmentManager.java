package vn.tainguyen.tnupgrader.manager;

import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.model.UpgradeAttribute;
import vn.tainguyen.tnupgrader.util.TextUtil;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EquipmentManager {

    private static final String LORE_HEADER_PLAIN = "=== ✦ CƯỜNG HÓA TRANG BỊ";
    private final TNUpgraderPlugin plugin;

    public EquipmentManager(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isUpgradableEquipment(ItemStack item) {
        if (item == null || item.getType().isAir() || item.getAmount() != 1) {
            return false;
        }
        if (plugin.getConfigManager().isUpgraderTool(item) || plugin.getConfigManager().isUpgraderBlockItem(item)) {
            return false;
        }
        Material mat = item.getType();
        String name = mat.name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE") || name.endsWith("_PICKAXE")
                || name.endsWith("_SHOVEL") || name.endsWith("_HOE") || name.endsWith("_HELMET")
                || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                || mat == Material.BOW || mat == Material.CROSSBOW || mat == Material.TRIDENT
                || mat == Material.MACE || mat == Material.SHIELD || mat == Material.ELYTRA
                || mat == Material.TURTLE_HELMET || mat == Material.SHEARS
                || mat == Material.FISHING_ROD || mat.getMaxDurability() > 0;
    }

    private NamespacedKey getAttrKey(UpgradeAttribute attr) {
        return new NamespacedKey(plugin, "attr_" + attr.name().toLowerCase(Locale.ROOT));
    }

    public int getAttributeLevel(ItemStack item, UpgradeAttribute attr) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        Integer lvl = pdc.get(getAttrKey(attr), PersistentDataType.INTEGER);
        return lvl != null ? Math.max(0, lvl) : 0;
    }

    public Map<UpgradeAttribute, Integer> getAllAttributes(ItemStack item) {
        Map<UpgradeAttribute, Integer> map = new EnumMap<>(UpgradeAttribute.class);
        if (item == null || !item.hasItemMeta()) {
            return map;
        }
        for (UpgradeAttribute attr : UpgradeAttribute.values()) {
            int lvl = getAttributeLevel(item, attr);
            if (lvl > 0) {
                map.put(attr, lvl);
            }
        }
        return map;
    }

    public int getTotalUpgradeStars(ItemStack item) {
        int total = 0;
        for (int lvl : getAllAttributes(item).values()) {
            total += lvl;
        }
        return total;
    }

    public int getMaxCustomAttributes() {
        return plugin.getConfig().getInt("equipment-upgrader.max-custom-attributes", 3);
    }

    public int getMaxAttributeLevel() {
        return plugin.getConfig().getInt("equipment-upgrader.max-attribute-level", 10);
    }

    public boolean canUpgradeAttribute(ItemStack item, UpgradeAttribute attr) {
        if (!isAttributeEnabled(attr)) {
            return false;
        }
        Map<UpgradeAttribute, Integer> current = getAllAttributes(item);
        int currentLevel = current.getOrDefault(attr, 0);
        if (currentLevel >= getMaxAttributeLevel()) {
            return false;
        }
        if (currentLevel == 0 && current.size() >= getMaxCustomAttributes()) {
            return false;
        }
        return true;
    }

    public boolean isAttributeEnabled(UpgradeAttribute attr) {
        return plugin.getConfig().getBoolean("equipment-upgrader.attributes." + attr.name() + ".enabled", true);
    }

    public String getAttributeDisplayName(UpgradeAttribute attr) {
        return plugin.getConfig().getString(
                "equipment-upgrader.attributes." + attr.name() + ".name",
                attr.getDefaultDisplayName()
        );
    }

    public double getAttributePerLevel(UpgradeAttribute attr) {
        return plugin.getConfig().getDouble(
                "equipment-upgrader.attributes." + attr.name() + ".per-level",
                attr.getDefaultPerLevel()
        );
    }

    public String getAttributeUnit(UpgradeAttribute attr) {
        return plugin.getConfig().getString(
                "equipment-upgrader.attributes." + attr.name() + ".unit",
                attr.getDefaultUnit()
        );
    }

    public String getAttributeDescription(UpgradeAttribute attr) {
        return plugin.getConfig().getString(
                "equipment-upgrader.attributes." + attr.name() + ".description",
                attr.getDefaultDescription()
        );
    }

    public double getAttributeUpgradeCostV(int nextLevel) {
        double base = plugin.getConfig().getDouble("equipment-upgrader.base-upgrade-cost-v", 500.0);
        double mult = plugin.getConfig().getDouble("equipment-upgrader.cost-multiplier-per-level", 1.55);
        return Math.round(base * Math.pow(mult, Math.max(0, nextLevel - 1)));
    }

    public double getEnchantUpgradeCostV(Enchantment enchant, int nextLevel) {
        double base = plugin.getConfig().getDouble("equipment-upgrader.enchant-upgrader.base-enchant-cost-v", 600.0);
        boolean isCustom = plugin.getExcellentEnchantsHook() != null
                && plugin.getExcellentEnchantsHook().isCustomEnchantment(enchant);
        if (isCustom) {
            base *= 1.35;
        }
        return Math.round(base * Math.pow(1.45, Math.max(0, nextLevel - 1)));
    }

    public int getMaxAllowedEnchantLevel(Enchantment enchant) {
        int bonus = plugin.getConfig().getInt("equipment-upgrader.enchant-upgrader.over-max-level-bonus", 3);
        if (enchant.getMaxLevel() == 1) {
            return 1;
        }
        return enchant.getMaxLevel() + Math.max(0, bonus);
    }

    /**
     * Nâng cấp dòng thuộc tính lên +1 cấp và cập nhật AttributeModifier 1.21 + Lore
     */
    public void upgradeAttribute(ItemStack item, UpgradeAttribute attr) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int newLevel = getAttributeLevel(item, attr) + 1;
        meta.getPersistentDataContainer().set(getAttrKey(attr), PersistentDataType.INTEGER, newLevel);
        item.setItemMeta(meta);

        applyBukkitAttributesAndLore(item);
    }

    /**
     * Nâng cấp Phù phép (Vanilla hoặc ExcellentEnchants) lên +1 cấp
     */
    public void upgradeEnchantment(ItemStack item, Enchantment enchant) {
        if (item == null || item.getType().isAir() || enchant == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        int current = meta.getEnchantLevel(enchant);
        meta.addEnchant(enchant, current + 1, true);
        item.setItemMeta(meta);
        applyBukkitAttributesAndLore(item);
    }

    /**
     * Xóa toàn bộ dòng thuộc tính bổ sung (Tẩy luyện lại từ đầu)
     */
    public void resetCustomAttributes(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        for (UpgradeAttribute attr : UpgradeAttribute.values()) {
            meta.getPersistentDataContainer().remove(getAttrKey(attr));
            Attribute bukkitAttr = attr.resolveBukkitAttribute();
            if (bukkitAttr != null) {
                NamespacedKey modKey = new NamespacedKey(plugin, "mod_" + attr.name().toLowerCase(Locale.ROOT));
                if (meta.getAttributeModifiers(bukkitAttr) != null) {
                    for (AttributeModifier mod : new ArrayList<>(meta.getAttributeModifiers(bukkitAttr))) {
                        if (mod.getKey().equals(modKey)) {
                            meta.removeAttributeModifier(bukkitAttr, mod);
                        }
                    }
                }
            }
        }
        Attribute breakSpeedAttr = UpgradeAttribute.resolveBlockBreakSpeedAttribute();
        if (breakSpeedAttr != null && meta.getAttributeModifiers(breakSpeedAttr) != null) {
            NamespacedKey digKey = new NamespacedKey(plugin, "mod_mining_speed");
            for (AttributeModifier mod : new ArrayList<>(meta.getAttributeModifiers(breakSpeedAttr))) {
                if (mod.getKey().equals(digKey)) {
                    meta.removeAttributeModifier(breakSpeedAttr, mod);
                }
            }
        }
        item.setItemMeta(meta);
        applyBukkitAttributesAndLore(item);
    }

    @SuppressWarnings("UnstableApiUsage")
    public void applyBukkitAttributesAndLore(ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Map<UpgradeAttribute, Integer> attrs = getAllAttributes(item);
        EquipmentSlotGroup slotGroup = resolveEquipmentSlotGroup(item.getType());

        // Bảo toàn chỉ số gốc (Vanilla Damage/Armor) trước khi thêm AttributeModifier tùy chỉnh
        if (!attrs.isEmpty() && !meta.hasAttributeModifiers()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                Multimap<Attribute, AttributeModifier> defaults = item.getType().getDefaultAttributeModifiers(slot);
                if (defaults != null && !defaults.isEmpty()) {
                    defaults.forEach((attribute, modifier) -> {
                        try {
                            meta.addAttributeModifier(attribute, modifier);
                        } catch (Exception ignored) {
                        }
                    });
                }
            }
        }

        for (UpgradeAttribute attr : UpgradeAttribute.values()) {
            Attribute bukkitAttr = attr.resolveBukkitAttribute();
            if (bukkitAttr == null) continue;

            NamespacedKey modKey = new NamespacedKey(plugin, "mod_" + attr.name().toLowerCase(Locale.ROOT));
            if (meta.getAttributeModifiers(bukkitAttr) != null) {
                for (AttributeModifier existing : new ArrayList<>(meta.getAttributeModifiers(bukkitAttr))) {
                    if (existing.getKey().equals(modKey)) {
                        meta.removeAttributeModifier(bukkitAttr, existing);
                    }
                }
            }

            int lvl = attrs.getOrDefault(attr, 0);
            if (lvl > 0) {
                double bonus = getAttributePerLevel(attr) * lvl;
                if (attr == UpgradeAttribute.MOVEMENT_SPEED) {
                    bonus = (getAttributePerLevel(attr) * 0.0022) * lvl;
                }
                AttributeModifier modifier = new AttributeModifier(
                        modKey,
                        bonus,
                        AttributeModifier.Operation.ADD_NUMBER,
                        slotGroup
                );
                meta.addAttributeModifier(bukkitAttr, modifier);
            }
        }

        // Tăng tốc độ đào khối thực tế (player.block_break_speed 1.21+) khi nâng cấp Tốc Đánh & Đào Nhanh
        Attribute breakSpeedAttr = UpgradeAttribute.resolveBlockBreakSpeedAttribute();
        if (breakSpeedAttr != null) {
            NamespacedKey digKey = new NamespacedKey(plugin, "mod_mining_speed");
            if (meta.getAttributeModifiers(breakSpeedAttr) != null) {
                for (AttributeModifier existing : new ArrayList<>(meta.getAttributeModifiers(breakSpeedAttr))) {
                    if (existing.getKey().equals(digKey)) {
                        meta.removeAttributeModifier(breakSpeedAttr, existing);
                    }
                }
            }
            int speedLvl = attrs.getOrDefault(UpgradeAttribute.ATTACK_SPEED, 0);
            if (speedLvl > 0) {
                AttributeModifier digMod = new AttributeModifier(
                        digKey,
                        0.25 * speedLvl,
                        AttributeModifier.Operation.ADD_NUMBER,
                        slotGroup
                );
                meta.addAttributeModifier(breakSpeedAttr, digMod);
            }
        }

        // Cập nhật khối Lore Cường Hóa đẹp mắt mà vẫn giữ nguyên Lore gốc (MythicMobs...)
        List<Component> currentLore = meta.lore();
        List<Component> cleanLore = new ArrayList<>();
        if (currentLore != null) {
            for (Component line : currentLore) {
                String plain = PlainTextComponentSerializer.plainText().serialize(line);
                if (plain.contains(LORE_HEADER_PLAIN)) {
                    break;
                }
                cleanLore.add(line);
            }
            while (!cleanLore.isEmpty() && PlainTextComponentSerializer.plainText().serialize(cleanLore.get(cleanLore.size() - 1)).isBlank()) {
                cleanLore.remove(cleanLore.size() - 1);
            }
        }

        int totalStars = getTotalUpgradeStars(item);
        if (totalStars > 0) {
            cleanLore.add(Component.empty());
            cleanLore.add(TextUtil.color("&6&l" + LORE_HEADER_PLAIN + " [+" + totalStars + " ⭐] ✦ ==="));
            cleanLore.add(TextUtil.color("&7Thuộc tính bổ sung: &e" + attrs.size() + "&7/&6" + getMaxCustomAttributes() + " dòng"));
            for (Map.Entry<UpgradeAttribute, Integer> entry : attrs.entrySet()) {
                UpgradeAttribute attr = entry.getKey();
                int lvl = entry.getValue();
                double totalVal = getAttributePerLevel(attr) * lvl;
                String extraInfo = (attr == UpgradeAttribute.ATTACK_SPEED)
                        ? " &7(&a+" + (lvl * 25) + "% Đào nhanh&7)"
                        : "";
                cleanLore.add(TextUtil.color(" &8▸ " + getAttributeDisplayName(attr)
                        + " &f(Cấp " + lvl + "): &a+" + TextUtil.formatValue(totalVal) + getAttributeUnit(attr) + extraInfo));
            }
        }

        meta.lore(cleanLore);
        item.setItemMeta(meta);
    }

    @SuppressWarnings("UnstableApiUsage")
    private EquipmentSlotGroup resolveEquipmentSlotGroup(Material mat) {
        String name = mat.name();
        if (name.endsWith("_HELMET") || mat == Material.TURTLE_HELMET) return EquipmentSlotGroup.HEAD;
        if (name.endsWith("_CHESTPLATE") || mat == Material.ELYTRA) return EquipmentSlotGroup.CHEST;
        if (name.endsWith("_LEGGINGS")) return EquipmentSlotGroup.LEGS;
        if (name.endsWith("_BOOTS")) return EquipmentSlotGroup.FEET;
        if (mat == Material.SHIELD) return EquipmentSlotGroup.OFFHAND;
        return EquipmentSlotGroup.MAINHAND;
    }
}
