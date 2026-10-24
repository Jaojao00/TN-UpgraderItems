package vn.tainguyen.tnupgrader.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;

import java.lang.reflect.Method;
import java.util.UUID;

public class FloodgateHook {

    private final TNUpgraderPlugin plugin;
    private boolean floodgateAvailable = false;

    public FloodgateHook(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
        checkHook();
    }

    public final void checkHook() {
        this.floodgateAvailable = Bukkit.getPluginManager().getPlugin("floodgate") != null
                || Bukkit.getPluginManager().getPlugin("Geyser-Spigot") != null;
        if (this.floodgateAvailable) {
            plugin.getLogger().info("Đã phát hiện Geyser/Floodgate! Đã kích hoạt chế độ tối ưu hóa thao tác 1-chạm cho người chơi PE/Bedrock.");
        }
    }

    public boolean isBedrockPlayer(Player player) {
        if (player == null) return false;
        if (player.getName().startsWith(".")) {
            return true;
        }
        if (!floodgateAvailable) {
            return false;
        }
        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object instance = apiClass.getMethod("getInstance").invoke(null);
            Method isFloodgatePlayer = apiClass.getMethod("isFloodgatePlayer", UUID.class);
            return (boolean) isFloodgatePlayer.invoke(instance, player.getUniqueId());
        } catch (Exception ignored) {
            return false;
        }
    }
}
