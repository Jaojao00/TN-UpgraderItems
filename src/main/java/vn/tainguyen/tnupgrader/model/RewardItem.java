package vn.tainguyen.tnupgrader.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vn.tainguyen.tnupgrader.util.TextUtil;

public class RewardItem {

    public enum SourceType {
        VANILLA,
        MYTHICMOBS,
        EXCELLENT_ENCHANT
    }

    private final String id;
    private final String displayName;
    private final Material iconMaterial;
    private final ItemStack prototypeStack;
    private final double unitValue;
    private final ItemCategory category;
    private final SourceType sourceType;

    public RewardItem(String id, String displayName, Material iconMaterial, ItemStack prototypeStack,
                      double unitValue, ItemCategory category, SourceType sourceType) {
        this.id = id;
        this.displayName = displayName;
        this.iconMaterial = iconMaterial != null ? iconMaterial : Material.PAPER;
        this.prototypeStack = prototypeStack != null ? prototypeStack.clone() : new ItemStack(this.iconMaterial);
        this.unitValue = Math.max(0.1, unitValue);
        this.category = category != null ? category : ItemCategory.BLOCKS;
        this.sourceType = sourceType != null ? sourceType : SourceType.VANILLA;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIconMaterial() {
        return iconMaterial;
    }

    public ItemStack createItemStack(int amount) {
        ItemStack clone = prototypeStack.clone();
        clone.setAmount(Math.max(1, Math.min(clone.getMaxStackSize(), amount)));
        return clone;
    }

    public double getUnitValue() {
        return unitValue;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    /**
     * Tính số lượng đề xuất tự động giống hệt cơ chế ảnh mẫu:
     * Nếu cược 972 V và Kim cương có giá 250 V -> đề xuất x4 (1000 V • 97.20%)
     */
    public int getSuggestedQuantity(double wagerValue) {
        if (wagerValue <= 0.0) {
            return 1;
        }
        int qty = (int) Math.ceil(wagerValue / unitValue);
        return Math.max(1, Math.min(6400, qty));
    }

    public double getTotalValue(int quantity) {
        return unitValue * Math.max(1, quantity);
    }

    public double calculateChance(double wagerValue, int quantity, double multiplierSetting, double maxChance) {
        if (wagerValue <= 0.0) {
            return 0.0;
        }
        double totalTarget = getTotalValue(quantity);
        if (totalTarget <= 0.0) {
            return 0.0;
        }
        double raw = (wagerValue / totalTarget) * 100.0 * multiplierSetting;
        return Math.max(0.0, Math.min(maxChance, raw));
    }

    public boolean matchesSearch(String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normQuery = TextUtil.removeAccents(query.trim());
        String normName = TextUtil.removeAccents(displayName);
        String normId = id.toLowerCase();
        return normName.contains(normQuery) || normId.contains(normQuery);
    }
}
