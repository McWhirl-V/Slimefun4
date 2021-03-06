package io.github.thebusybiscuit.slimefun4.implementation.items.tools;

import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SimpleSlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockBreakHandler;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PickaxeOfContainment extends SimpleSlimefunItem<BlockBreakHandler> {

    public PickaxeOfContainment(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Override
    public BlockBreakHandler getItemHandler() {
        return new BlockBreakHandler() {

            @Override
            public boolean isPrivate() {
                return false;
            }

            @Override
            public boolean onBlockBreak(BlockBreakEvent e, ItemStack item, int fortune, List<ItemStack> drops) {
                if (isItem(item)) {
                    if (!Slimefun.hasUnlocked(e.getPlayer(), PickaxeOfContainment.this, true)) {
                        return true;
                    }

                    Block b = e.getBlock();

                    if (b.getType() != Material.SPAWNER) {
                        return true;
                    }

                    // If the spawner's BlockStorage has BlockInfo, then it's not a vanilla spawner and
                    // should not give a broken spawner.
                    ItemStack spawner = SlimefunItems.BROKEN_SPAWNER.clone();
                    if (BlockStorage.hasBlockInfo(b)) {
                        spawner = SlimefunItems.REPAIRED_SPAWNER.clone();
                    }

                    ItemMeta im = spawner.getItemMeta();
                    List<String> lore = im.getLore();

                    for (int i = 0; i < lore.size(); i++) {
                        if (lore.get(i).contains("<类型>")) {
                            lore.set(i, lore.get(i).replace("<类型>", ChatUtils.humanize(((CreatureSpawner) b.getState()).getSpawnedType().toString())));
                        }
                    }

                    im.setLore(lore);
                    spawner.setItemMeta(im);
                    b.getLocation().getWorld().dropItemNaturally(b.getLocation(), spawner);
                    e.setExpToDrop(0);
                    e.setDropItems(false);
                    return true;
                } else {
                    if (e.getBlock().getType() == Material.SPAWNER) e.setDropItems(false);
                    return false;
                }
            }
        };
    }

}
