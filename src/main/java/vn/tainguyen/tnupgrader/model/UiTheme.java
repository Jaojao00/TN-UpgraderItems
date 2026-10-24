package vn.tainguyen.tnupgrader.model;

import org.bukkit.Material;

public enum UiTheme {
    GOLD("Vàng Hoàng Kim (Mặc định)", "&6", Material.YELLOW_STAINED_GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE),
    DIAMOND("Xanh Kim Cương", "&b", Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE),
    EMERALD("Lục Bảo Thần Thoại", "&a", Material.LIME_STAINED_GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE),
    CRIMSON("Đỏ Hỏa Ngục", "&c", Material.RED_STAINED_GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE),
    AMETHYST("Tím Huyền Bí", "&d", Material.PURPLE_STAINED_GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE),
    DARK("Đen Tối Giản", "&f", Material.GRAY_STAINED_GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE);

    private final String displayName;
    private final String accentColor;
    private final Material accentGlass;
    private final Material backgroundGlass;

    UiTheme(String displayName, String accentColor, Material accentGlass, Material backgroundGlass) {
        this.displayName = displayName;
        this.accentColor = accentColor;
        this.accentGlass = accentGlass;
        this.backgroundGlass = backgroundGlass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public Material getAccentGlass() {
        return accentGlass;
    }

    public Material getBackgroundGlass() {
        return backgroundGlass;
    }

    public UiTheme next() {
        UiTheme[] vals = values();
        return vals[(this.ordinal() + 1) % vals.length];
    }
}
