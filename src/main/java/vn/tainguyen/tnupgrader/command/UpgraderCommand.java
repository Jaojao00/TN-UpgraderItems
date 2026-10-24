package vn.tainguyen.tnupgrader.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.gui.UpgraderSession;
import vn.tainguyen.tnupgrader.util.TextUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class UpgraderCommand implements CommandExecutor, TabCompleter {

    private final TNUpgraderPlugin plugin;

    public UpgraderCommand(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        String prefix = plugin.getConfigManager().getPrefix();

        if (args.length == 0 || args[0].equalsIgnoreCase("open")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(TextUtil.color(prefix + "&cChỉ người chơi mới có thể mở giao diện nâng cấp!"));
                return true;
            }
            if (!player.hasPermission("tnupgrader.use")) {
                player.sendMessage(TextUtil.color(prefix + "&cBạn không có quyền sử dụng tính năng này!"));
                return true;
            }
            if (plugin.getConfigManager().isRequireBlockOrTool() && !player.hasPermission("tnupgrader.admin")) {
                if (!plugin.getBlockAndToolListener().isNearOrHoldingUpgrader(player)) {
                    player.sendMessage(TextUtil.color(prefix
                            + "&cBạn cần đứng gần &6Đài Nâng Cấp &choặc cầm &eSổ Tay Nâng Cấp Di Động &ctrên tay!"));
                    return true;
                }
            }
            plugin.openUpgraderFor(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "equip", "cuonghoa" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(TextUtil.color(prefix + "&cChỉ người chơi mới có thể sử dụng lệnh này!"));
                    return true;
                }
                UpgraderSession session = plugin.getOrCreateSession(player);
                plugin.getEquipmentUpgraderGui().open(player, session);
                return true;
            }
            case "stats", "thongke" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(TextUtil.color(prefix + "&cChỉ người chơi mới có thể sử dụng lệnh này!"));
                    return true;
                }
                UpgraderSession session = plugin.getOrCreateSession(player);
                plugin.getSubMenusGui().openStatistics(player, session);
                return true;
            }
            case "tool" -> {
                if (!sender.hasPermission("tnupgrader.admin")) {
                    sender.sendMessage(TextUtil.color(prefix + "&cBạn không có quyền quản trị (tnupgrader.admin)!"));
                    return true;
                }
                Player target = (args.length >= 2) ? Bukkit.getPlayer(args[1]) : (sender instanceof Player p ? p : null);
                if (target == null) {
                    sender.sendMessage(TextUtil.color(prefix + "&cKhông tìm thấy người chơi!"));
                    return true;
                }
                int amount = 1;
                if (args.length >= 3) {
                    try {
                        amount = Math.max(1, Integer.parseInt(args[2]));
                    } catch (NumberFormatException ignored) {
                    }
                }
                UpgraderSession.giveItemSafely(target, plugin.getConfigManager().createPortableToolItem(amount));
                sender.sendMessage(TextUtil.color(prefix + "&aĐã trao &eSổ Tay Nâng Cấp Di Động x" + amount + " &acho &f" + target.getName()));
                return true;
            }
            case "block" -> {
                if (!sender.hasPermission("tnupgrader.admin")) {
                    sender.sendMessage(TextUtil.color(prefix + "&cBạn không có quyền quản trị (tnupgrader.admin)!"));
                    return true;
                }
                Player target = (args.length >= 2) ? Bukkit.getPlayer(args[1]) : (sender instanceof Player p ? p : null);
                if (target == null) {
                    sender.sendMessage(TextUtil.color(prefix + "&cKhông tìm thấy người chơi!"));
                    return true;
                }
                int amount = 1;
                if (args.length >= 3) {
                    try {
                        amount = Math.max(1, Integer.parseInt(args[2]));
                    } catch (NumberFormatException ignored) {
                    }
                }
                UpgraderSession.giveItemSafely(target, plugin.getConfigManager().createUpgraderBlockItem(amount));
                sender.sendMessage(TextUtil.color(prefix + "&aĐã trao &6Đài Nâng Cấp Tối Thượng x" + amount + " &acho &f" + target.getName()));
                return true;
            }
            case "setvalue" -> {
                if (!sender.hasPermission("tnupgrader.admin") || !(sender instanceof Player player)) {
                    sender.sendMessage(TextUtil.color(prefix + "&cChỉ Admin trong game mới có thể dùng lệnh này!"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(TextUtil.color(prefix + "&eSử dụng: &f/" + label + " setvalue <giá_trị_V>"));
                    return true;
                }
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType().isAir()) {
                    player.sendMessage(TextUtil.color(prefix + "&cHãy cầm vật phẩm cần đặt giá trị V trên tay chính!"));
                    return true;
                }
                try {
                    double val = Double.parseDouble(args[1]);
                    plugin.getConfigManager().setMaterialValue(hand.getType(), val);
                    player.sendMessage(TextUtil.color(prefix + "&aĐã cập nhật giá trị của &e"
                            + TextUtil.getVietnameseName(hand.getType()) + " (" + hand.getType().name()
                            + ") &athành &6" + TextUtil.formatValue(val) + " V&a!"));
                } catch (NumberFormatException e) {
                    player.sendMessage(TextUtil.color(prefix + "&cGiá trị V không hợp lệ!"));
                }
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("tnupgrader.admin")) {
                    sender.sendMessage(TextUtil.color(prefix + "&cBạn không có quyền quản trị (tnupgrader.admin)!"));
                    return true;
                }
                plugin.getMythicMobsHook().checkHook();
                plugin.getExcellentEnchantsHook().checkHook();
                plugin.getFloodgateHook().checkHook();
                plugin.getConfigManager().reload();
                plugin.getConfigManager().registerRecipes();
                sender.sendMessage(TextUtil.color(prefix + "&aĐã tải lại toàn bộ cấu hình, bảng giá V và công thức chế tạo TN-UpgraderItems!"));
                return true;
            }
            default -> {
                sender.sendMessage(TextUtil.color(prefix + "&eDanh sách lệnh &6&lTN-UpgraderItems &7(Tác giả: Tài Nguyễn):"));
                sender.sendMessage(TextUtil.color(" &6▸ &f/" + label + " &7- Mở giao diện Nâng Cấp Vật Phẩm (Đấu Giá 3x3)"));
                sender.sendMessage(TextUtil.color(" &6▸ &f/" + label + " equip &7- Mở Bàn Cường Hóa Trang Bị End-Game"));
                sender.sendMessage(TextUtil.color(" &6▸ &f/" + label + " stats &7- Xem Thống Kê & Bảng Xếp Hạng Server"));
                if (sender.hasPermission("tnupgrader.admin")) {
                    sender.sendMessage(TextUtil.color(" &c▸ &f/" + label + " tool [tên] [số_lượng] &7- Nhận Sổ Tay Nâng Cấp Di Động"));
                    sender.sendMessage(TextUtil.color(" &c▸ &f/" + label + " block [tên] [số_lượng] &7- Nhận Khối Đài Nâng Cấp Tối Thượng"));
                    sender.sendMessage(TextUtil.color(" &c▸ &f/" + label + " setvalue <V> &7- Đặt giá trị V cho vật phẩm trên tay"));
                    sender.sendMessage(TextUtil.color(" &c▸ &f/" + label + " reload &7- Tải lại cấu hình plugin"));
                }
                return true;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("open", "equip", "stats", "help"));
            if (sender.hasPermission("tnupgrader.admin")) {
                subs.addAll(Arrays.asList("tool", "block", "setvalue", "reload"));
            }
            String input = args[0].toLowerCase(Locale.ROOT);
            subs.removeIf(s -> !s.startsWith(input));
            return subs;
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("tool") || args[0].equalsIgnoreCase("block"))) {
            return null; // Danh sách người chơi online
        }
        return Collections.emptyList();
    }
}
