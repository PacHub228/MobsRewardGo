package pac.hy_da.mobsrewardgo;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class MobsRewardGo extends JavaPlugin {

    private static MobsRewardGo instance;
    private NamespacedKey mobTierKey;

    @Override
    public void onEnable() {
        instance = this;
        this.mobTierKey = new NamespacedKey(this, "mob_tier");

        getServer().getPluginManager().registerEvents(new MobListener(), this);
        getLogger().info("MobsRewardGo запущен. Уведомления перенесены в чат.");
    }

    public static MobsRewardGo getInstance() {
        return instance;
    }

    public NamespacedKey getMobTierKey() {
        return mobTierKey;
    }
}