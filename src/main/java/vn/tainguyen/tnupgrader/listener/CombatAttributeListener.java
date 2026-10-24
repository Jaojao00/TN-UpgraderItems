package vn.tainguyen.tnupgrader.listener;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import vn.tainguyen.tnupgrader.TNUpgraderPlugin;
import vn.tainguyen.tnupgrader.manager.EquipmentManager;
import vn.tainguyen.tnupgrader.model.UpgradeAttribute;
import vn.tainguyen.tnupgrader.util.TextUtil;

import java.util.concurrent.ThreadLocalRandom;

public class CombatAttributeListener implements Listener {

    private final TNUpgraderPlugin plugin;

    public CombatAttributeListener(TNUpgraderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }

        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        }
        if (attacker == null) {
            return;
        }

        EquipmentManager eqManager = plugin.getEquipmentManager();
        EntityEquipment equip = attacker.getEquipment();
        if (equip == null) return;

        ItemStack[] activeGear = {
                equip.getItemInMainHand(),
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
            // Đối với vũ khí ở ô giáp hoặc ngược lại, chỉ tính vũ khí ở tay chính và giáp ở 4 ô giáp
            if (i > 0 && i < 5 && !isArmorPiece(item)) continue;
            critLevel += eqManager.getAttributeLevel(item, UpgradeAttribute.CRIT_STRIKE);
            lifestealLevel += eqManager.getAttributeLevel(item, UpgradeAttribute.LIFESTEAL);
            trueDamageLevel += eqManager.getAttributeLevel(item, UpgradeAttribute.TRUE_DAMAGE);
        }

        // 1. Bạo Kích Huỷ Diệt (CRIT_STRIKE)
        if (critLevel > 0) {
            double critChance = critLevel * eqManager.getAttributePerLevel(UpgradeAttribute.CRIT_STRIKE);
            if (ThreadLocalRandom.current().nextDouble(0.0, 100.0) <= critChance) {
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

        // 2. Sát Thương Chuẩn Xuyên Giáp (TRUE_DAMAGE)
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

        // 3. Hút Máu Chiến Đấu (LIFESTEAL)
        if (lifestealLevel > 0) {
            double lifestealPercent = lifestealLevel * eqManager.getAttributePerLevel(UpgradeAttribute.LIFESTEAL);
            double healAmount = event.getFinalDamage() * (lifestealPercent / 100.0);
            if (healAmount > 0.05) {
                Attribute maxHpAttr = UpgradeAttribute.MAX_HEALTH.resolveBukkitAttribute();
                double maxHp = 20.0;
                if (maxHpAttr != null) {
                    AttributeInstance inst = attacker.getAttribute(maxHpAttr);
                    if (inst != null) {
                        maxHp = inst.getValue();
                    }
                }
                double nextHp = Math.min(maxHp, attacker.getHealth() + healAmount);
                attacker.setHealth(nextHp);
                attacker.getWorld().spawnParticle(
                        Particle.HEART,
                        attacker.getLocation().add(0, 1.8, 0),
                        2, 0.2, 0.2, 0.2, 0.01
                );
            }
        }
    }

    private boolean isArmorPiece(ItemStack item) {
        String name = item.getType().name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                || name.equals("ELYTRA") || name.equals("TURTLE_HELMET");
    }
}
