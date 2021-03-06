package io.github.thebusybiscuit.slimefun4.implementation.listeners;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideLayout;
import io.github.thebusybiscuit.slimefun4.core.guide.options.SlimefunGuideSettings;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class SlimefunGuideListener implements Listener {

    private final boolean giveOnFirstJoin;

    public SlimefunGuideListener(SlimefunPlugin plugin, boolean giveOnFirstJoin) {
        this.giveOnFirstJoin = giveOnFirstJoin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (giveOnFirstJoin && !e.getPlayer().hasPlayedBefore()) {
            Player p = e.getPlayer();

            if (!SlimefunPlugin.getWorldSettingsService().isWorldEnabled(p.getWorld())) {
                return;
            }

            SlimefunGuideLayout type = SlimefunPlugin.getCfg().getBoolean("guide.default-view-book") ? SlimefunGuideLayout.BOOK : SlimefunGuideLayout.CHEST;
            p.getInventory().addItem(SlimefunGuide.getItem(type));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerRightClickEvent e) {
        Player p = e.getPlayer();

        if (openGuide(e, SlimefunGuideLayout.BOOK) == Result.ALLOW) {
            if (p.isSneaking()) {
                SlimefunGuideSettings.openSettings(p, e.getItem());
            } else {
                SlimefunGuide.openGuide(p, SlimefunGuideLayout.BOOK);
            }
        } else if (openGuide(e, SlimefunGuideLayout.CHEST) == Result.ALLOW) {
            if (p.isSneaking()) {
                SlimefunGuideSettings.openSettings(p, e.getItem());
            } else {
                SlimefunGuide.openGuide(p, SlimefunGuideLayout.CHEST);
            }
        } else if (openGuide(e, SlimefunGuideLayout.CHEAT_SHEET) == Result.ALLOW) {
            // We rather just run the command here,
            // all necessary permission checks will be handled there.
            p.chat("/sf cheat");
        }
    }

    private Result openGuide(PlayerRightClickEvent e, SlimefunGuideLayout layout) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        if (SlimefunUtils.isItemSimilar(item, SlimefunGuide.getItem(layout), true)) {
            e.cancel();

            if (!SlimefunPlugin.getWorldSettingsService().isWorldEnabled(p.getWorld())) {
                SlimefunPlugin.getLocal().sendMessage(p, "messages.disabled-item", true);
                return Result.DENY;
            }

            return Result.ALLOW;
        }

        return Result.DEFAULT;
    }

}