package io.github.thebusybiscuit.slimefun4.implementation.items.cargo;

import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import io.github.thebusybiscuit.cscorelib2.materials.MaterialCollections;
import io.github.thebusybiscuit.cscorelib2.protection.ProtectableAction;
import io.github.thebusybiscuit.cscorelib2.skull.SkullItem;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunBlockHandler;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.UnregisterReason;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class AdvancedCargoOutputNode extends SlimefunItem {

    private static final int[] border = {0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 17, 18, 22, 23, 24, 26, 27, 31, 32, 33, 34, 35, 36, 40, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
    private final int[] slots = {19, 20, 21, 28, 29, 30, 37, 38, 39};

    public AdvancedCargoOutputNode(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, ItemStack recipeOutput) {
        super(category, item, recipeType, recipe, recipeOutput);

        new BlockMenuPreset(getID(), "&c高级货运输出节点") {

            @Override
            public void init() {
                constructMenu(this);
            }

            @Override
            public void newInstance(BlockMenu menu, Block b) {
                if (!BlockStorage.hasBlockInfo(b) || BlockStorage.getLocationInfo(b.getLocation(), "filter-type") == null || BlockStorage.getLocationInfo(b.getLocation(), "filter-type").equals("whitelist")) {
                    menu.replaceExistingItem(15, new CustomItem(Material.WHITE_WOOL, "&7模式: &r白名单", "", "&e> 单击切换至黑名单模式"));
                    menu.addMenuClickHandler(15, (p, slot, item, action) -> {
                        BlockStorage.addBlockInfo(b, "filter-type", "blacklist");
                        newInstance(menu, b);
                        return false;
                    });
                } else {
                    menu.replaceExistingItem(15, new CustomItem(Material.BLACK_WOOL, "&7模式: &8黑名单", "", "&e> 单击切换至白名单模式"));
                    menu.addMenuClickHandler(15, (p, slot, item, action) -> {
                        BlockStorage.addBlockInfo(b, "filter-type", "whitelist");
                        newInstance(menu, b);
                        return false;
                    });
                }

                if (!BlockStorage.hasBlockInfo(b) || BlockStorage.getLocationInfo(b.getLocation(), "filter-durability") == null || BlockStorage.getLocationInfo(b.getLocation(), "filter-durability").equals("false")) {
                    menu.replaceExistingItem(16, new CustomItem(Material.STONE_SWORD, "&7判断子ID/耐久度: &4\u2718", "", "&e> 单击启用检查耐久度是否匹配"));
                    menu.addMenuClickHandler(16, (p, slot, item, action) -> {
                        BlockStorage.addBlockInfo(b, "filter-durability", "true");
                        newInstance(menu, b);
                        return false;
                    });
                } else {
                    ItemStack is = new ItemStack(Material.GOLDEN_SWORD);
                    Damageable dmg = (Damageable) is.getItemMeta();
                    dmg.setDamage(20);
                    is.setItemMeta((ItemMeta) dmg);
                    menu.replaceExistingItem(16, new CustomItem(is, "&7判断子ID/耐久度: &2\u2714", "", "&e> 单击关闭检查耐久度是否匹配"));
                    menu.addMenuClickHandler(16, (p, slot, item, action) -> {
                        BlockStorage.addBlockInfo(b, "filter-durability", "false");
                        newInstance(menu, b);
                        return false;
                    });
                }

                if (!BlockStorage.hasBlockInfo(b) || BlockStorage.getLocationInfo(b.getLocation(), "filter-lore") == null || BlockStorage.getLocationInfo(b.getLocation(), "filter-lore").equals("true")) {
                    menu.replaceExistingItem(25, new CustomItem(Material.MAP, "&7匹配 Lore: &2\u2714", "", "&e> 单击修改是否匹配Lore"));
                    menu.addMenuClickHandler(25, (p, slot, item, action) -> {
                        BlockStorage.addBlockInfo(b, "filter-lore", "false");
                        newInstance(menu, b);
                        return false;
                    });
                } else {
                    menu.replaceExistingItem(25, new CustomItem(Material.MAP, "&7匹配 Lore: &4\u2718", "", "&e> 单击修改是否匹配Lore"));
                    menu.addMenuClickHandler(25, (p, slot, item, action) -> {
                        BlockStorage.addBlockInfo(b, "filter-lore", "true");
                        newInstance(menu, b);
                        return false;
                    });
                }

                menu.replaceExistingItem(41, new CustomItem(SkullItem.fromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjI1OTliZDk4NjY1OWI4Y2UyYzQ5ODg1MjVjOTRlMTlkZGQzOWZhZDA4YTM4Mjg0YTE5N2YxYjcwNjc1YWNjIn19fQ=="), "&b信道", "", "&e> 单击将信道ID减一"));
                menu.addMenuClickHandler(41, (p, slot, item, action) -> {
                    int channel = Integer.parseInt(BlockStorage.getLocationInfo(b.getLocation(), "frequency")) - 1;
                    if (channel < 0) {
                        if (SlimefunPlugin.getThirdPartySupportService().isChestTerminalInstalled()) channel = 16;
                        else channel = 15;
                    }
                    BlockStorage.addBlockInfo(b, "frequency", String.valueOf(channel));
                    newInstance(menu, b);
                    return false;
                });

                int channel = ((!BlockStorage.hasBlockInfo(b) || BlockStorage.getLocationInfo(b.getLocation(), "frequency") == null) ? 0 : (Integer.parseInt(BlockStorage.getLocationInfo(b.getLocation(), "frequency"))));

                if (channel == 16) {
                    menu.replaceExistingItem(42, new CustomItem(SkullItem.fromHash("7a44ff3a5f49c69cab676bad8d98a063fa78cfa61916fdef3e267557fec18283"), "&b信道 ID: &3" + (channel + 1)));
                    menu.addMenuClickHandler(42, ChestMenuUtils.getEmptyClickHandler());
                } else {
                    menu.replaceExistingItem(42, new CustomItem(MaterialCollections.getAllWoolColors().get(channel), "&b信道 ID: &3" + (channel + 1)));
                    menu.addMenuClickHandler(42, ChestMenuUtils.getEmptyClickHandler());
                }

                menu.replaceExistingItem(43, new CustomItem(SkullItem.fromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzJmOTEwYzQ3ZGEwNDJlNGFhMjhhZjZjYzgxY2Y0OGFjNmNhZjM3ZGFiMzVmODhkYjk5M2FjY2I5ZGZlNTE2In19fQ=="), "&b信道", "", "&e> 单击将信道ID加一"));
                menu.addMenuClickHandler(43, (p, slot, item, action) -> {
                    int channeln = Integer.parseInt(BlockStorage.getLocationInfo(b.getLocation(), "frequency")) + 1;

                    if (SlimefunPlugin.getThirdPartySupportService().isChestTerminalInstalled()) {
                        if (channeln > 16) channeln = 0;
                    } else {
                        if (channeln > 15) channeln = 0;
                    }

                    BlockStorage.addBlockInfo(b, "frequency", String.valueOf(channeln));
                    newInstance(menu, b);
                    return false;
                });
            }

            @Override
            public boolean canOpen(Block b, Player p) {
                return p.hasPermission("slimefun.cargo.bypass") || SlimefunPlugin.getProtectionManager().hasPermission(p, b.getLocation(), ProtectableAction.ACCESS_INVENTORIES);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                return new int[0];
            }
        };

        registerBlockHandler(getID(), new SlimefunBlockHandler() {

            @Override
            public void onPlace(Player p, Block b, SlimefunItem item) {
                BlockStorage.addBlockInfo(b, "owner", p.getUniqueId().toString());
                BlockStorage.addBlockInfo(b, "index", "0");
                BlockStorage.addBlockInfo(b, "frequency", "0");
                BlockStorage.addBlockInfo(b, "filter-type", "whitelist");
                BlockStorage.addBlockInfo(b, "filter-lore", "true");
                BlockStorage.addBlockInfo(b, "filter-durability", "false");
            }

            @Override
            public boolean onBreak(Player p, Block b, SlimefunItem item, UnregisterReason reason) {
                BlockMenu inv = BlockStorage.getInventory(b);

                if (inv != null) {
                    for (int slot : slots) {
                        if (inv.getItemInSlot(slot) != null) {
                            b.getWorld().dropItemNaturally(b.getLocation(), inv.getItemInSlot(slot));
                            inv.replaceExistingItem(slot, null);
                        }
                    }
                }
                return true;
            }
        });
    }

    private void constructMenu(BlockMenuPreset preset) {
        for (int i : border) {
            preset.addItem(i, new CustomItem(Material.CYAN_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());
        }

        preset.addItem(2, new CustomItem(Material.PAPER, "&3物品", "", "&b放入所有你想添加至", "&b黑名单/白名单的物品"), ChestMenuUtils.getEmptyClickHandler());
    }
}
