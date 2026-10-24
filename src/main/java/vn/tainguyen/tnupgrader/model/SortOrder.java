package vn.tainguyen.tnupgrader.model;

public enum SortOrder {
    PRICE_ASC("Giá tăng"),
    PRICE_DESC("Giá giảm"),
    CHANCE_DESC("Tỉ lệ cao ▸ thấp"),
    NAME_ASC("Tên A ▸ Z");

    private final String displayName;

    SortOrder(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public SortOrder next() {
        SortOrder[] vals = values();
        return vals[(this.ordinal() + 1) % vals.length];
    }
}
