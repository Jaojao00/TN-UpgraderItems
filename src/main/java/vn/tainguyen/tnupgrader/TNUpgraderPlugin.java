package vn.tainguyen.tnupgrader;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import vn.tainguyen.tnupgrader.command.UpgraderCommand;
import vn.tainguyen.tnupgrader.config.ConfigManager;
import vn.tainguyen.tnupgrader.data.ProfileManager;
import vn.tainguyen.tnupgrader.gui.EquipmentUpgraderGui;
import vn.tainguyen.tnupgrader.gui.MainUpgraderGui;
import vn.tainguyen.tnupgrader.gui.RewardBrowserGui;
import vn.tainguyen.tnupgrader.gui.SubMenusGui;
import vn.tainguyen.tnupgrader.gui.UpgraderSession;
import vn.tainguyen.tnupgrader.hook.ExcellentEnchantsHook;
import vn.tainguyen.tnupgrader.hook.FloodgateHook;
import vn.tainguyen.tnupgrader.hook.MythicMobsHook;
import vn.tainguyen.tnupgrader.listener.BlockAndToolListener;
import vn.tainguyen.tnupgrader.listener.CombatAttributeListener;
import vn.tainguyen.tnupgrader.listener.GuiListener;
import vn.tainguyen.tnupgrader.manager.EquipmentManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TNUpgraderPlugin extends JavaPlugin {

    private MythicMobsHook mythicMobsHook;
    private ExcellentEnchantsHook excellentEnchantsHook;
    private FloodgateHook floodgateHook;

    private EquipmentManager equipmentManager;
    private ConfigManager configManager;
    private ProfileManager profileManager;

    private MainUpgraderGui mainUpgraderGui;
    private RewardBrowserGui rewardBrowserGui;
    private EquipmentUpgraderGui equipmentUpgraderGui;
    private SubMenusGui subMenusGui;
    private BlockAndToolListener blockAndToolListener;

    private final Map<UUID, UpgraderSession> sessionManager = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 1. Khởi tạo các Hook tích hợp (MythicMobs, ExcellentEnchants, Floodgate/PE)
        this.mythicMobsHook = new MythicMobsHook(this);
        this.excellentEnchantsHook = new ExcellentEnchantsHook(this);
        this.floodgateHook = new FloodgateHook(this);

        // 2. Khởi tạo các Manager dữ liệu & cấu hình
        this.equipmentManager = new EquipmentManager(this);
        this.configManager = new ConfigManager(this);
        this.profileManager = new ProfileManager(this);

        // 3. Đăng ký công thức chế tạo Khối Đài Nâng Cấp & Sổ Tay Nâng Cấp Di Động
        this.configManager.registerRecipes();

        // 4. Khởi tạo các bộ dựng Giao diện (GUI)
        this.mainUpgraderGui = new MainUpgraderGui(this);
        this.rewardBrowserGui = new RewardBrowserGui(this);
        this.equipmentUpgraderGui = new EquipmentUpgraderGui(this);
        this.subMenusGui = new SubMenusGui(this);

        // 5. Đăng ký Listeners
        this.blockAndToolListener = new BlockAndToolListener(this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(this.blockAndToolListener, this);
        Bukkit.getPluginManager().registerEvents(new CombatAttributeListener(this), this);

        // 6. Đăng ký Lệnh (/upgrader, /tnupgrader, /nangcap, /nc)
        PluginCommand cmd = getCommand("upgrader");
        if (cmd != null) {
            UpgraderCommand executor = new UpgraderCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        getLogger().info("============================================================");
        getLogger().info(" TN-UpgraderItems v" + getDescription().getVersion() + " đã khởi động thành công!");
        getLogger().info(" Tác giả (Author): Tài Nguyễn | Nền tảng: Paper 1.21+");
        getLogger().info(" Đã nạp " + configManager.getRewardList().size() + " phần thưởng vào kho Nâng Cấp!");
        getLogger().info("============================================================");
    }

    @Override
    public void onDisable() {
        // Trả lại toàn bộ vật phẩm đang đặt cược cho người chơi khi tắt/reload server
        for (UpgraderSession session : sessionManager.values()) {
            Player player = session.getPlayer();
            if (player != null && player.isOnline()) {
                session.returnAllItems(player);
                player.closeInventory();
            }
        }
        sessionManager.clear();

        if (profileManager != null) {
            profileManager.saveAll();
        }
        if (blockAndToolListener != null) {
            blockAndToolListener.savePlacedBlocks();
        }
    }

    public UpgraderSession getOrCreateSession(Player player) {
        return sessionManager.computeIfAbsent(
                player.getUniqueId(),
                uuid -> new UpgraderSession(this, uuid)
        );
    }

    public void openUpgraderFor(Player player) {
        UpgraderSession session = getOrCreateSession(player);
        mainUpgraderGui.open(player, session);
    }

    public Map<UUID, UpgraderSession> getSessionManager() {
        return sessionManager;
    }

    public MythicMobsHook getMythicMobsHook() {
        return mythicMobsHook;
    }

    public ExcellentEnchantsHook getExcellentEnchantsHook() {
        return excellentEnchantsHook;
    }

    public FloodgateHook getFloodgateHook() {
        return floodgateHook;
    }

    public EquipmentManager getEquipmentManager() {
        return equipmentManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public MainUpgraderGui getMainUpgraderGui() {
        return mainUpgraderGui;
    }

    public RewardBrowserGui getRewardBrowserGui() {
        return rewardBrowserGui;
    }

    public EquipmentUpgraderGui getEquipmentUpgraderGui() {
        return equipmentUpgraderGui;
    }

    public SubMenusGui getSubMenusGui() {
        return subMenusGui;
    }

    public BlockAndToolListener getBlockAndToolListener() {
        return blockAndToolListener;
    }
}
