package vn.tainguyen.tnupgrader.model;

import org.bukkit.Material;

public enum ItemCategory {
    ALL("Tất cả", Material.COMPASS),
    EQUIPMENT("Vũ khí & Giáp", Material.DIAMOND_CHESTPLATE),
    TOOLS("Công cụ", Material.DIAMOND_PICKAXE),
    MINERALS("Khoáng sản & Quý hiếm", Material.DIAMOND),
    FOOD("Thức ăn", Material.GOLDEN_APPLE),
    BLOCKS("Khối & Tự nhiên", Material.GRASS_BLOCK),
    SPECIAL("MythicMobs & Phù phép", Material.ENCHANTED_BOOK);

    private final String displayName;
    private final Material icon;

    ItemCategory(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public ItemCategory next() {
        ItemCategory[] vals = values();
        return vals[(this.ordinal() + 1) % vals.length];
    }

    public static ItemCategory fromMaterial(Material mat) {
        if (mat == null) {
            return BLOCKS;
        }
        String name = mat.name();
        if (mat.isEdible() || mat == Material.CAKE || mat == Material.HONEY_BOTTLE) {
            return FOOD;
        }
        if (name.endsWith("_SWORD") || name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") || name.endsWith("_HORSE_ARMOR")
                || mat == Material.TRIDENT || mat == Material.MACE || mat == Material.ELYTRA
                || mat == Material.BOW || mat == Material.CROSSBOW || mat == Material.SHIELD
                || mat == Material.TOTEM_OF_UNDYING) {
            return EQUIPMENT;
        }
        if (name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE")) {
            return TOOLS;
        }
        if (name.contains("INGOT") || name.contains("DIAMOND") || name.contains("EMERALD")
                || name.contains("NETHERITE") || name.contains("GOLD") || name.contains("IRON")
                || name.contains("COPPER") || name.contains("ORE") || name.contains("SHARD")
                || name.contains("STAR") || name.contains("PEARL") || name.contains("ROD")
                || name.contains("SHELL") || name.contains("CORE") || name.contains("KEY")
                || name.contains("DEBRIS") || name.contains("TEMPLATE") || mat == Material.COAL
                || mat == Material.REDSTONE || mat == Material.LAPIS_LAZULI || mat == Material.QUARTZ) {
            return MINERALS;
        }
        if (mat == Material.ENCHANTED_BOOK) {
            return SPECIAL;
        }
        return BLOCKS;
    }
}
