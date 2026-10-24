package vn.tainguyen.tnupgrader.listener;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.util.TextUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockAndToolListener implements Listener {

    private final TNUpgraderPlugin plugin;
    private final File blockFile;
    private final Set<String> placedBlockKeys = new HashSet<>();

    public BlockAndToolListener(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
        this.blockFile = new File(plugin.getDataFolder(), "blocks.yml");
        loadPlacedBlocks();
    }

    private String toBlockKey(Location loc) {
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    public boolean isUpgraderBlock(Block block) {
        if (block == null) return false;
        return placedBlockKeys.contains(toBlockKey(block.getLocation()));
    }

    public boolean isNearOrHoldingUpgrader(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        if (plugin.getConfigManager().isUpgraderTool(main) || plugin.getConfigManager().isUpgraderTool(off)) {
            return true;
        }
        Location pLoc = player.getLocation();
        for (int x = -5; x <= 5; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -5; z <= 5; z++) {
                    if (isUpgraderBlock(pLoc.clone().add(x, y, z).getBlock())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = event.getItem();

        // 1. Mở bằng Sổ Tay Nâng Cấp Di Động (Cầm trên tay Chuột phải / Chạm)
        if (handItem != null && plugin.getConfigManager().isUpgraderTool(handItem)) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                plugin.openUpgraderFor(player);
                return;
            }
        }

        // 2. Mở bằng Khối Đài Nâng Cấp Tối Thượng đặt dưới đất
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
            Block clicked = event.getClickedBlock();
            if (clicked != null && isUpgraderBlock(clicked)) {
                event.setCancelled(true);
                clicked.getWorld().spawnParticle(
                        Particle.TOTEM_OF_UNDYING,
                        clicked.getLocation().add(0.5, 1.1, 0.5),
                        12, 0.25, 0.25, 0.25, 0.05
                );
                player.playSound(clicked.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.9f, 1.2f);
                plugin.openUpgraderFor(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!plugin.getConfigManager().isUpgraderBlockItem(item)) {
            return;
        }
        Block block = event.getBlockPlaced();
        placedBlockKeys.add(toBlockKey(block.getLocation()));
        savePlacedBlocks();

        spawnHologram(block.getLocation());
        block.getWorld().spawnParticle(Particle.END_ROD, block.getLocation().add(0.5, 1.0, 0.5), 25, 0.3, 0.4, 0.3, 0.05);
        event.getPlayer().playSound(block.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.2f);
        event.getPlayer().sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                + "&aĐã đặt &6&lĐài Nâng Cấp Tối Thượng&a! Chạm/Chuột phải vào khối để mở giao diện."));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        String key = toBlockKey(block.getLocation());
        if (!placedBlockKeys.contains(key)) {
            return;
        }
        event.setDropItems(false);
        placedBlockKeys.remove(key);
        savePlacedBlocks();
        removeHologram(block.getLocation());

        block.getWorld().dropItemNaturally(
                block.getLocation().add(0.5, 0.5, 0.5),
                plugin.getConfigManager().createUpgraderBlockItem(1)
        );
        event.getPlayer().sendMessage(TextUtil.color(plugin.getConfigManager().getPrefix()
                + "&eĐã thu hồi &6&lĐài Nâng Cấp Tối Thượng&e."));
    }

    private void spawnHologram(Location blockLoc) {
        if (blockLoc.getWorld() == null) return;
        removeHologram(blockLoc);
        Location holoLoc = blockLoc.clone().add(0.5, 1.35, 0.5);
        try {
            blockLoc.getWorld().spawn(holoLoc, TextDisplay.class, display -> {
                display.text(TextUtil.color("&6&l⚒ ĐÀI NÂNG CẤP TỐI THƯỢNG ⚒\n&e▸ Chạm / Chuột phải để nâng cấp ◂"));
                display.setBillboard(Display.Billboard.CENTER);
                display.setBackgroundColor(Color.fromARGB(140, 15, 15, 25));
                display.setSeeThrough(false);
                display.setShadowed(true);
                display.addScoreboardTag("tnupgrader_holo");
            });
        } catch (Exception ignored) {
        }
    }

    private void removeHologram(Location blockLoc) {
        if (blockLoc.getWorld() == null) return;
        Location center = blockLoc.clone().add(0.5, 1.35, 0.5);
        for (Entity entity : blockLoc.getWorld().getNearbyEntities(center, 1.5, 2.0, 1.5)) {
            if (entity instanceof TextDisplay && entity.getScoreboardTags().contains("tnupgrader_holo")) {
                entity.remove();
            }
        }
    }

    public final void loadPlacedBlocks() {
        placedBlockKeys.clear();
        if (!blockFile.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(blockFile);
        List<String> list = cfg.getStringList("blocks");
        placedBlockKeys.addAll(list);
    }

    public synchronized void savePlacedBlocks() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("blocks", new ArrayList<>(placedBlockKeys));
        try {
            cfg.save(blockFile);
        } catch (IOException ignored) {
        }
    }

    public void cleanupAllHolograms() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(TextDisplay.class)) {
                if (entity.getScoreboardTags().contains("tnupgrader_holo")) {
                    entity.remove();
                }
            }
        }
    }
}
