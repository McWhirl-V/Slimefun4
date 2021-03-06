package io.github.thebusybiscuit.slimefun4.core.commands.subcommands;

import io.github.thebusybiscuit.slimefun4.core.commands.SlimefunCommand;
import io.github.thebusybiscuit.slimefun4.core.commands.SubCommand;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class DebugFishCommand extends SubCommand {

    DebugFishCommand(SlimefunPlugin plugin, SlimefunCommand cmd) {
        super(plugin, cmd);
    }

    @Override
    public String getName() {
        return "debug_fish";
    }


    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        if (sender instanceof Player && sender.isOp()) {
            ((Player) sender).getInventory().addItem(SlimefunItems.DEBUG_FISH);
        } else {
            SlimefunPlugin.getLocal().sendMessage(sender, "messages.no-permission", true);
        }
    }

}
