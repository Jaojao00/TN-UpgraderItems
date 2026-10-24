package vn.tainguyen.tnupgrader.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.model.PlayerProfile;
import vn.tainguyen.tnupgrader.model.UiTheme;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProfileManager {

    private final TNUpgraderPlugin plugin;
    private final File file;
    private YamlConfiguration config;
    private final Map<UUID, PlayerProfile> profiles = new ConcurrentHashMap<>();

    public ProfileManager(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "profiles.yml");
        loadAll();
    }

    public PlayerProfile getProfile(Player player) {
        PlayerProfile profile = profiles.computeIfAbsent(
                player.getUniqueId(),
                uuid -> new PlayerProfile(uuid, player.getName())
        );
        profile.setPlayerName(player.getName());
        return profile;
    }

    public Collection<PlayerProfile> getAllProfiles() {
        return profiles.values();
    }

    public void loadAll() {
        if (!file.exists()) {
            config = new YamlConfiguration();
            return;
        }
        config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("players");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                ConfigurationSection pSec = section.getConfigurationSection(key);
                if (pSec == null) continue;
                String name = pSec.getString("name", "Player");
                PlayerProfile profile = new PlayerProfile(uuid, name);
                try {
                    profile.setTheme(UiTheme.valueOf(pSec.getString("theme", "GOLD")));
                } catch (IllegalArgumentException ignored) {
                    profile.setTheme(UiTheme.GOLD);
                }
                profile.setSpinAnimation(pSec.getBoolean("spin-animation", true));
                profile.setSoundEnabled(pSec.getBoolean("sound-enabled", true));
                profile.setBroadcastEnabled(pSec.getBoolean("broadcast-enabled", true));
                profile.setTotalAttempts(pSec.getInt("total-attempts", 0));
                profile.setTotalWins(pSec.getInt("total-wins", 0));
                profile.setTotalLosses(pSec.getInt("total-losses", 0));
                profile.setCurrentStreak(pSec.getInt("current-streak", 0));
                profile.setBestStreak(pSec.getInt("best-streak", 0));
                profile.setTotalWageredValue(pSec.getDouble("total-wagered", 0.0));
                profile.setTotalWonValue(pSec.getDouble("total-won", 0.0));
                profile.setPityBonus(pSec.getDouble("pity-bonus", 0.0));
                List<String> favs = pSec.getStringList("favorites");
                profile.getFavoriteItemIds().addAll(favs);
                profiles.put(uuid, profile);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public synchronized void saveAll() {
        if (config == null) {
            config = new YamlConfiguration();
        }
        for (PlayerProfile profile : profiles.values()) {
            String path = "players." + profile.getUuid().toString();
            config.set(path + ".name", profile.getPlayerName());
            config.set(path + ".theme", profile.getTheme().name());
            config.set(path + ".spin-animation", profile.isSpinAnimation());
            config.set(path + ".sound-enabled", profile.isSoundEnabled());
            config.set(path + ".broadcast-enabled", profile.isBroadcastEnabled());
            config.set(path + ".total-attempts", profile.getTotalAttempts());
            config.set(path + ".total-wins", profile.getTotalWins());
            config.set(path + ".total-losses", profile.getTotalLosses());
            config.set(path + ".current-streak", profile.getCurrentStreak());
            config.set(path + ".best-streak", profile.getBestStreak());
            config.set(path + ".total-wagered", profile.getTotalWageredValue());
            config.set(path + ".total-won", profile.getTotalWonValue());
            config.set(path + ".pity-bonus", profile.getPityBonus());
            config.set(path + ".favorites", new ArrayList<>(profile.getFavoriteItemIds()));
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Không thể lưu profiles.yml: " + e.getMessage());
        }
    }
}
