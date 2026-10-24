package vn.tainguyen.tnupgrader.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(Material material, int amount) {
        this(new ItemStack(material, Math.max(1, Math.min(64, amount))));
    }

    public ItemBuilder(ItemStack base) {
        this.item = base.clone();
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder amount(int amount) {
        this.item.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    public ItemBuilder name(String name) {
        if (meta != null && name != null) {
            meta.displayName(TextUtil.color(name));
        }
        return this;
    }

    public ItemBuilder lore(String... lines) {
        return lore(Arrays.asList(lines));
    }

    public ItemBuilder lore(List<String> lines) {
        if (meta != null && lines != null) {
            meta.lore(TextUtil.colorList(lines));
        }
        return this;
    }

    public ItemBuilder addLore(String... lines) {
        if (meta != null && lines != null) {
            List<Component> current = meta.lore();
            if (current == null) {
                current = new ArrayList<>();
            } else {
                current = new ArrayList<>(current);
            }
            for (String line : lines) {
                current.add(TextUtil.color(line));
            }
            meta.lore(current);
        }
        return this;
    }

    public ItemBuilder glow(boolean glow) {
        if (meta != null) {
            if (glow) {
                meta.setEnchantmentGlintOverride(true);
            } else {
                meta.setEnchantmentGlintOverride(null);
            }
        }
        return this;
    }

    public ItemBuilder hideFlags() {
        if (meta != null) {
            meta.addItemFlags(ItemFlag.values());
        }
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, String value) {
        if (meta != null && key != null && value != null) {
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
        }
        return this;
    }

    public ItemBuilder pdcInt(NamespacedKey key, int value) {
        if (meta != null && key != null) {
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, value);
        }
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (meta != null && enchantment != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}
