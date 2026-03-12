package pac.hy_da.mobsrewardgo;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MobListener implements Listener {

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getType() != EntityType.ZOMBIE && entity.getType() != EntityType.SKELETON) return;

        double roll = ThreadLocalRandom.current().nextDouble();
        String tier;

        if (roll <= 0.05) { // 5% Легендарный
            tier = "LEGENDARY";
            setupCool(entity);
            notifyNearbyPlayers(entity, "легендарного");
        } else if (roll <= 0.20) { // 15% Улучшенный
            tier = "IMPROVED";
            setupImproved(entity);
            notifyNearbyPlayers(entity, "улучшенного");
        } else {
            tier = "ORDINARY";
        }

        entity.getPersistentDataContainer().set(MobsRewardGo.getInstance().getMobTierKey(), PersistentDataType.STRING, tier);
    }

    private void setupImproved(LivingEntity entity) {
        double hp = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() + 10.0;
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
        entity.setHealth(hp);
    }

    private void setupCool(LivingEntity entity) {
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(200.0); // 100 сердец
        entity.setHealth(200.0);
    }

    private void notifyNearbyPlayers(LivingEntity entity, String levelName) {
        String message = ChatColor.YELLOW + "Рядом с вами моб " + ChatColor.RED + levelName +
                ChatColor.YELLOW + " уровня! Если вы его убьёте то можете получить какой-то хороший предмет!";

        // Ищем игроков в радиусе 30 блоков
        for (Entity e : entity.getNearbyEntities(30, 30, 30)) {
            if (e instanceof Player) {
                e.sendMessage(message);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity)) return;
        LivingEntity damager = (LivingEntity) event.getDamager();

        String tier = damager.getPersistentDataContainer().get(MobsRewardGo.getInstance().getMobTierKey(), PersistentDataType.STRING);
        if ("LEGENDARY".equals(tier)) {
            event.setDamage(event.getDamage() * 2.5);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        String tier = entity.getPersistentDataContainer().get(MobsRewardGo.getInstance().getMobTierKey(), PersistentDataType.STRING);
        if (tier == null) return;

        event.getDrops().clear();
        List<ItemStack> drops = new ArrayList<>();

        switch (tier) {
            case "ORDINARY":
                addRandomDrops(drops, getSlagPool(), ThreadLocalRandom.current().nextInt(1, 3));
                break;
            case "IMPROVED":
                addRandomDrops(drops, getMediumPool(), ThreadLocalRandom.current().nextInt(1, 3));
                break;
            case "LEGENDARY":
                double chance = Math.min(0.1 + (entity.getTicksLived() / 20000.0), 1.0);
                if (ThreadLocalRandom.current().nextDouble() <= chance) {
                    addRandomDrops(drops, getBestPool(), 1);
                }
                break;
        }

        for (ItemStack item : drops) {
            entity.getWorld().dropItemNaturally(entity.getLocation(), item);
        }
    }

    private void addRandomDrops(List<ItemStack> list, Material[] pool, int count) {
        for (int i = 0; i < count; i++) {
            list.add(new ItemStack(pool[ThreadLocalRandom.current().nextInt(pool.length)]));
        }
    }

    private Material[] getSlagPool() {
        return new Material[]{Material.ROTTEN_FLESH, Material.SPIDER_EYE, Material.COBWEB};
    }

    private Material[] getMediumPool() {
        return new Material[]{Material.CARROT, Material.CHICKEN, Material.DRIED_KELP,
                Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.GLASS_BOTTLE};
    }

    private Material[] getBestPool() {
        return new Material[]{Material.DIAMOND, Material.IRON_INGOT, Material.GOLD_INGOT,
                Material.LAVA_BUCKET, Material.ENDER_PEARL, Material.BUCKET,
                Material.BEEF, Material.COOKED_BEEF};
    }
}