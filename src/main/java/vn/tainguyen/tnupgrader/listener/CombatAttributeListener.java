package vn.tainguyen.tnupgrader.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.gui.UpgraderSession;
import vn.tainguyen.tnupgrader.manager.EquipmentManager;
import vn.tainguyen.tnupgrader.model.NativeEnchantment;
import vn.tainguyen.tnupgrader.model.UpgradeAttribute;
import vn.tainguyen.tnupgrader.util.TextUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class CombatAttributeListener implements Listener {

    private final TNUpgraderPlugin plugin;
    private final Set<Location> activeMultiBreakBlocks = new HashSet<>();

    public CombatAttributeListener(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
    }

    // ==========================================================================
    // 1. CHIẾN ĐẤU: THUỘC TÍNH & CỔ NGỮ VŨ KHÍ / GIÁP ĐỘC QUYỀN TN-UPGRADER
    // ==========================================================================
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }

        // A. Xử lý khi NGƯỜI CHƠI LÀ KẺ TẤN CÔNG (Vũ khí & Thuộc tính công)
        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        }
        if (attacker != null) {
            handleAttackerEffects(attacker, victim, event);
        }

        // B. Xử lý khi NGƯỜI CHƠI LÀ NGƯỜI BỊ ĐÁNH (Cổ ngữ Giáp trụ hộ thể)
        if (victim instanceof Player defender) {
            LivingEntity mobOrPlayerAttacker = null;
            if (event.getDamager() instanceof LivingEntity le) {
                mobOrPlayerAttacker = le;
            } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof LivingEntity le) {
                mobOrPlayerAttacker = le;
            }
            handleDefenderArmorRunes(defender, mobOrPlayerAttacker, event);
        }
    }

    private void handleAttackerEffects(Player attacker, LivingEntity victim, EntityDamageByEntityEvent event) {
        EquipmentManager eqManager = plugin.getEquipmentManager();
        EntityEquipment equip = attacker.getEquipment();
        if (equip == null) return;

        ItemStack mainHand = equip.getItemInMainHand();
        ItemStack[] activeGear = {
                mainHand,
                equip.getHelmet(),
                equip.getChestplate(),
                equip.getLeggings(),
                equip.getBoots(),
                equip.getItemInOffHand()
        };

        int critLevel = 0;
        int lifestealLevel = 0;
        int trueDamageLevel = 0;

        for (int i = 0; i < activeGear.length; i++) {
            ItemStack item = activeGear[i];
            if (item == null || item.getType().isAir()) continue;
            if (i > 0 && i < 5 && !isArmorPiece(item)) continue;
            critLevel += eqManager.getAttributeLevel(item, UpgradeAttribute.CRIT_STRIKE);
            lifestealLevel += eqManager.getAttributeLevel(item, UpgradeAttribute.LIFESTEAL);
            trueDamageLevel += eqManager.getAttributeLevel(item, UpgradeAttribute.TRUE_DAMAGE);
        }

        // --- Cổ Ngữ Vũ Khí Độc Quyền (Trên vũ khí cầm tay) ---
        if (!mainHand.getType().isAir()) {
            // 1. Trảm Quyết Đột Kích (EXECUTIONER)
            int execLvl = eqManager.getNativeEnchantLevel(mainHand, NativeEnchantment.EXECUTIONER);
            if (execLvl > 0) {
                double maxVictimHp = getMaxHealth(victim);
                if (victim.getHealth() / Math.max(1.0, maxVictimHp) <= 0.35) {
                    double multiplier = 1.0 + (execLvl * 0.12);
                    event.setDamage(event.getDamage() * multiplier);
                    victim.getWorld().spawnParticle(Particle.SWEEP_ATTACK, victim.getLocation().add(0, 1.0, 0), 3, 0.2, 0.2, 0.2, 0.0);
                    attacker.sendActionBar(TextUtil.color("&4&l🗡 TRẢM QUYẾT ĐỘT KÍCH! &c(+" + (execLvl * 12) + "% Sát thương kết liễu)"));
                }
            }

            // 2. Lôi Phạt Thiên Kích (THUNDER_STRIKE)
            int thunderLvl = eqManager.getNativeEnchantLevel(mainHand, NativeEnchantment.THUNDER_STRIKE);
            if (thunderLvl > 0 && rollChance(thunderLvl * 8 + 5)) {
                victim.getWorld().strikeLightningEffect(victim.getLocation());
                event.setDamage(event.getDamage() + (thunderLvl * 2.0));
                attacker.sendActionBar(TextUtil.color("&e&l⚡ LÔI PHẠT THIÊN KÍCH! &f(+" + (thunderLvl * 2) + " Sát thương sét)"));
            }

            // 3. Hàn Băng Phong Ấn (FROST_BITE)
            int frostLvl = eqManager.getNativeEnchantLevel(mainHand, NativeEnchantment.FROST_BITE);
            if (frostLvl > 0 && rollChance(frostLvl * 8 + 5)) {
                int durationTicks = (frostLvl + 2) * 20;
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, Math.min(3, frostLvl - 1)));
                victim.setFreezeTicks(Math.min(victim.getMaxFreezeTicks(), 100 + frostLvl * 25));
                victim.getWorld().spawnParticle(Particle.SNOWFLAKE, victim.getLocation().add(0, 1.0, 0), 18, 0.3, 0.4, 0.3, 0.04);
            }

            // 4. Độc Dược Hắc Ám (VENOMOUS)
            int venomLvl = eqManager.getNativeEnchantLevel(mainHand, NativeEnchantment.VENOMOUS);
            if (venomLvl > 0 && rollChance(venomLvl * 8 + 5)) {
                int durationTicks = (venomLvl + 2) * 20;
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, durationTicks, 0));
                if (venomLvl >= 3) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, durationTicks / 2, 0));
                }
                victim.getWorld().spawnParticle(Particle.WITCH, victim.getLocation().add(0, 1.0, 0), 15, 0.3, 0.4, 0.3, 0.03);
            }
        }

        // --- Thuộc Tính Bạo Kích (CRIT_STRIKE) ---
        if (critLevel > 0) {
            double critChance = critLevel * eqManager.getAttributePerLevel(UpgradeAttribute.CRIT_STRIKE);
            if (rollChance(critChance)) {
                event.setDamage(event.getDamage() * 1.5);
                victim.getWorld().spawnParticle(
                        Particle.CRIT,
                        victim.getLocation().add(0, 1.0, 0),
                        20, 0.35, 0.45, 0.35, 0.15
                );
                attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.85f);
                attacker.sendActionBar(TextUtil.color("&6&l💥 BẠO KÍCH HUỶ DIỆT! &e(x1.5 Sát thương)"));
            }
        }

        // --- Thuộc Tính Sát Thương Chuẩn (TRUE_DAMAGE) ---
        if (trueDamageLevel > 0) {
            double trueDmg = trueDamageLevel * eqManager.getAttributePerLevel(UpgradeAttribute.TRUE_DAMAGE);
            double newHealth = Math.max(0.0, victim.getHealth() - trueDmg);
            victim.setHealth(newHealth);
            victim.getWorld().spawnParticle(
                    Particle.SOUL_FIRE_FLAME,
                    victim.getLocation().add(0, 1.1, 0),
                    10, 0.25, 0.3, 0.25, 0.03
            );
        }

        // --- Thuộc Tính Hút Máu (LIFESTEAL) ---
        if (lifestealLevel > 0) {
            double lifestealPercent = lifestealLevel * eqManager.getAttributePerLevel(UpgradeAttribute.LIFESTEAL);
            double healAmount = event.getFinalDamage() * (lifestealPercent / 100.0);
            if (healAmount > 0.05) {
                double maxHp = getMaxHealth(attacker);
                attacker.setHealth(Math.min(maxHp, attacker.getHealth() + healAmount));
                attacker.getWorld().spawnParticle(
                        Particle.HEART,
                        attacker.getLocation().add(0, 1.8, 0),
                        2, 0.2, 0.2, 0.2, 0.01
                );
            }
        }
    }

    private void handleDefenderArmorRunes(Player defender, LivingEntity attacker, EntityDamageByEntityEvent event) {
        EquipmentManager eqManager = plugin.getEquipmentManager();
        EntityEquipment equip = defender.getEquipment();
        if (equip == null) return;

        ItemStack[] armors = {
                equip.getHelmet(),
                equip.getChestplate(),
                equip.getLeggings(),
                equip.getBoots(),
                equip.getItemInOffHand()
        };

        int vitalityLvl = 0;
        int moltenLvl = 0;
        int windStepLvl = 0;

        for (ItemStack piece : armors) {
            if (piece == null || piece.getType().isAir()) continue;
            vitalityLvl += eqManager.getNativeEnchantLevel(piece, NativeEnchantment.VITALITY_REGEN);
            moltenLvl += eqManager.getNativeEnchantLevel(piece, NativeEnchantment.MOLTEN_SHIELD);
            windStepLvl += eqManager.getNativeEnchantLevel(piece, NativeEnchantment.WIND_STEP);
        }

        // 1. Sinh Mệnh Tái Tạo (VITALITY_REGEN)
        if (vitalityLvl > 0 && rollChance(Math.min(60, vitalityLvl * 6 + 5))) {
            defender.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, Math.min(2, vitalityLvl / 3)));
            defender.getWorld().spawnParticle(Particle.HEART, defender.getLocation().add(0, 1.5, 0), 4, 0.3, 0.3, 0.3, 0.02);
        }

        // 2. Khiên Hỏa Diệm (MOLTEN_SHIELD)
        if (moltenLvl > 0) {
            defender.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 120, 0, true, false));
            if (attacker != null && rollChance(Math.min(75, moltenLvl * 10))) {
                attacker.setFireTicks((moltenLvl + 2) * 20);
            }
        }

        // 3. Bước Chân Thần Tốc (WIND_STEP)
        if (windStepLvl > 0) {
            double maxHp = getMaxHealth(defender);
            double afterHitHp = defender.getHealth() - event.getFinalDamage();
            if (afterHitHp > 0 && (afterHitHp / Math.max(1.0, maxHp)) <= 0.40) {
                if (!defender.hasPotionEffect(PotionEffectType.ABSORPTION)) {
                    int duration = (windStepLvl + 3) * 20;
                    defender.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1));
                    defender.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration, Math.min(2, windStepLvl / 2)));
                    defender.playSound(defender.getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.6f);
                    defender.sendActionBar(TextUtil.color("&a&l🌪 BƯỚC CHÂN THẦN TỐC! &f(Kích hoạt Lá Chắn Hộ Mệnh & Tăng Tốc)"));
                }
            }
        }
    }

    // ==========================================================================
    // 2. KHAI THÁC & CÔNG CỤ: MŨI KHOAN ĐỊA TÂM, BÚA 3X3, LÒ NUNG, THU HOẠCH
    // ==========================================================================
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (activeMultiBreakBlocks.contains(block.getLocation())) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType().isAir() || !tool.hasItemMeta()) {
            return;
        }

        EquipmentManager eqManager = plugin.getEquipmentManager();

        // 1. Thu Hoạch Mùa Vụ (HARVESTER - Tự động gieo lại hạt giống)
        int harvesterLvl = eqManager.getNativeEnchantLevel(tool, NativeEnchantment.HARVESTER);
        if (harvesterLvl > 0 && block.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() == ageable.getMaximumAge()) {
                Material cropType = block.getType();
                org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (block.getType().isAir()) {
                        block.setType(cropType);
                        if (block.getBlockData() instanceof Ageable newAge) {
                            newAge.setAge(0);
                            block.setBlockData(newAge);
                        }
                    }
                }, 2L);
            }
        }

        // Khi người chơi giữ phím Shift (Cúi người), tạm tắt đào lan để đào chính xác 1 khối
        if (player.isSneaking()) {
            return;
        }

        // 2. Mũi Khoan Địa Tâm (VEIN_MINER - Đào mạch quặng & chặt cây liên hoàn)
        int veinLvl = eqManager.getNativeEnchantLevel(tool, NativeEnchantment.VEIN_MINER);
        if (veinLvl > 0 && (isOreBlock(block.getType()) || isLogBlock(block.getType()))) {
            int maxBlocks = veinLvl * 2 + 1;
            List<Block> connected = findVeinBlocks(block, maxBlocks);
            for (Block target : connected) {
                if (!target.equals(block)) {
                    breakExtraBlockSafely(player, target);
                }
            }
            return;
        }

        // 3. Búa Thần Khai Sơn (DRILL_3X3 - Đào rộng 3x3)
        int drillLvl = eqManager.getNativeEnchantLevel(tool, NativeEnchantment.DRILL_3X3);
        if (drillLvl > 0 && isDrillableBlock(block.getType())) {
            int chance = drillLvl >= 3 ? 100 : (drillLvl * 35);
            if (rollChance(chance)) {
                for (Block neighbor : get3x3Blocks(player, block)) {
                    if (!neighbor.equals(block) && isDrillableBlock(neighbor.getType())) {
                        breakExtraBlockSafely(player, neighbor);
                    }
                }
            }
        }
    }

    // 4. Lò Nung Tự Động (AUTO_SMELT) & Từ Trường Hút Đồ (TELEKINESIS)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType().isAir() || !tool.hasItemMeta()) {
            return;
        }

        EquipmentManager eqManager = plugin.getEquipmentManager();
        int smeltLvl = eqManager.getNativeEnchantLevel(tool, NativeEnchantment.AUTO_SMELT);
        int telekinesisLvl = eqManager.getNativeEnchantLevel(tool, NativeEnchantment.TELEKINESIS);

        boolean doSmelt = smeltLvl > 0 && rollChance(smeltLvl >= 3 ? 100 : smeltLvl * 35);
        int fortuneLvl = tool.getEnchantmentLevel(Enchantment.FORTUNE);

        for (Item itemEntity : new ArrayList<>(event.getItems())) {
            ItemStack drop = itemEntity.getItemStack();
            if (doSmelt) {
                Material smelted = getSmeltedResult(drop.getType());
                if (smelted != null) {
                    int bonus = (fortuneLvl > 0 && isRawMetal(drop.getType()))
                            ? ThreadLocalRandom.current().nextInt(1, fortuneLvl + 2)
                            : 1;
                    drop.setType(smelted);
                    drop.setAmount(Math.min(64, drop.getAmount() * bonus));
                    itemEntity.setItemStack(drop);
                    event.getBlock().getWorld().spawnParticle(
                            Particle.FLAME,
                            event.getBlock().getLocation().add(0.5, 0.5, 0.5),
                            5, 0.2, 0.2, 0.2, 0.02
                    );
                    event.getBlock().getWorld().spawn(event.getBlock().getLocation().add(0.5, 0.5, 0.5), ExperienceOrb.class, orb -> orb.setExperience(1));
                }
            }
            if (telekinesisLvl > 0) {
                UpgraderSession.giveItemSafely(player, drop);
                itemEntity.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon.getType().isAir() || !weapon.hasItemMeta()) return;

        if (plugin.getEquipmentManager().getNativeEnchantLevel(weapon, NativeEnchantment.TELEKINESIS) > 0) {
            for (ItemStack drop : new ArrayList<>(event.getDrops())) {
                UpgraderSession.giveItemSafely(killer, drop);
            }
            event.getDrops().clear();
        }
    }

    // ==========================================================================
    // CÁC HÀM PHỤ TRỢ KHAI THÁC & CHIẾN ĐẤU
    // ==========================================================================
    private void breakExtraBlockSafely(Player player, Block target) {
        Location loc = target.getLocation();
        if (activeMultiBreakBlocks.contains(loc)) return;
        activeMultiBreakBlocks.add(loc);
        try {
            player.breakBlock(target);
        } finally {
            activeMultiBreakBlocks.remove(loc);
        }
    }

    private List<Block> findVeinBlocks(Block start, int maxCount) {
        List<Block> result = new ArrayList<>();
        Queue<Block> queue = new ArrayDeque<>();
        Set<Location> visited = new HashSet<>();
        Material targetType = start.getType();

        queue.add(start);
        visited.add(start.getLocation());

        while (!queue.isEmpty() && result.size() < maxCount) {
            Block current = queue.poll();
            result.add(current);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block rel = current.getRelative(dx, dy, dz);
                        if (!visited.contains(rel.getLocation()) && rel.getType() == targetType) {
                            visited.add(rel.getLocation());
                            queue.add(rel);
                            if (result.size() + queue.size() >= maxCount) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private List<Block> get3x3Blocks(Player player, Block center) {
        List<Block> blocks = new ArrayList<>();
        BlockFace face = player.getTargetBlockFace(6);
        if (face == null) face = BlockFace.NORTH;

        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                Block rel = switch (face) {
                    case UP, DOWN -> center.getRelative(a, 0, b);
                    case EAST, WEST -> center.getRelative(0, a, b);
                    default -> center.getRelative(a, b, 0);
                };
                blocks.add(rel);
            }
        }
        return blocks;
    }

    private boolean isOreBlock(Material mat) {
        String name = mat.name();
        return name.endsWith("_ORE") || mat == Material.ANCIENT_DEBRIS || mat == Material.GILDED_BLACKSTONE;
    }

    private boolean isLogBlock(Material mat) {
        String name = mat.name();
        return name.endsWith("_LOG") || name.endsWith("_STEM") || name.endsWith("_WOOD");
    }

    private boolean isDrillableBlock(Material mat) {
        String name = mat.name();
        return mat == Material.STONE || mat == Material.COBBLESTONE || mat == Material.DEEPSLATE
                || mat == Material.COBBLED_DEEPSLATE || mat == Material.NETHERRACK || mat == Material.END_STONE
                || mat == Material.ANDESITE || mat == Material.DIORITE || mat == Material.GRANITE
                || mat == Material.TUFF || mat == Material.DIRT || mat == Material.GRASS_BLOCK
                || mat == Material.SAND || mat == Material.GRAVEL || name.endsWith("_ORE");
    }

    private Material getSmeltedResult(Material input) {
        return switch (input) {
            case RAW_IRON, IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
            case RAW_GOLD, GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.GOLD_INGOT;
            case RAW_COPPER, COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_INGOT;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            case COBBLESTONE -> Material.STONE;
            case COBBLED_DEEPSLATE -> Material.DEEPSLATE;
            case SAND, RED_SAND -> Material.GLASS;
            case CLAY_BALL -> Material.BRICK;
            default -> null;
        };
    }

    private boolean isRawMetal(Material mat) {
        return mat == Material.RAW_IRON || mat == Material.RAW_GOLD || mat == Material.RAW_COPPER;
    }

    private double getMaxHealth(LivingEntity entity) {
        Attribute maxHpAttr = UpgradeAttribute.MAX_HEALTH.resolveBukkitAttribute();
        if (maxHpAttr != null) {
            AttributeInstance inst = entity.getAttribute(maxHpAttr);
            if (inst != null) {
                return inst.getValue();
            }
        }
        return 20.0;
    }

    private boolean rollChance(double percent) {
        return ThreadLocalRandom.current().nextDouble(0.0, 100.0) <= percent;
    }

    private boolean isArmorPiece(ItemStack item) {
        String name = item.getType().name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                || name.equals("ELYTRA") || name.equals("TURTLE_HELMET");
    }
}
