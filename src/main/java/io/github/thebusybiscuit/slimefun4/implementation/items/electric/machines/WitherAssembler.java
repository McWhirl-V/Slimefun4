package io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines;

import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import io.github.thebusybiscuit.cscorelib2.math.DoubleHandler;
import io.github.thebusybiscuit.cscorelib2.protection.ProtectableAction;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunBlockHandler;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SimpleSlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.UnregisterReason;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.api.energy.ChargableBlock;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WitherAssembler extends SimpleSlimefunItem<BlockTicker> implements EnergyNetComponent {

    private static final int ENERGY_CONSUMPTION = 4096;

    private final int[] border = {0, 2, 3, 4, 5, 6, 8, 12, 14, 21, 23, 30, 32, 39, 40, 41};
    private final int[] skullBorder = {9, 10, 11, 18, 20, 27, 29, 36, 37, 38};
    private final int[] sandBorder = {15, 16, 17, 24, 26, 33, 35, 42, 43, 44};

    private int lifetime = 0;

    public WitherAssembler(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);

        new BlockMenuPreset(getID(), item.getItemMeta().getDisplayName()) {

            @Override
            public void init() {
                constructMenu(this);
            }

            @Override
            public void newInstance(BlockMenu menu, Block b) {
                if (!BlockStorage.hasBlockInfo(b) || BlockStorage.getLocationInfo(b.getLocation(), "enabled") == null || BlockStorage.getLocationInfo(b.getLocation(), "enabled").equals("false")) {
                    menu.replaceExistingItem(22, new CustomItem(new ItemStack(Material.GUNPOWDER), "&7是否启用: &4\u2718", "", "&e> 单击启用机器"));
                    menu.addMenuClickHandler(22, (p, slot, item, action) -> {
                        BlockStorage.addBlockInfo(b, "enabled", "true");
                        newInstance(menu, b);
                        return false;
                    });
                } else {
                    menu.replaceExistingItem(22, new CustomItem(new ItemStack(Material.REDSTONE), "&7是否启用: &2\u2714", "", "&e> 单击关闭机器"));
                    menu.addMenuClickHandler(22, (p, slot, item, action) -> {
                        BlockStorage.addBlockInfo(b, "enabled", "false");
                        newInstance(menu, b);
                        return false;
                    });
                }

                double offset = (!BlockStorage.hasBlockInfo(b) || BlockStorage.getLocationInfo(b.getLocation(), "offset") == null) ? 3.0F : Double.valueOf(BlockStorage.getLocationInfo(b.getLocation(), "offset"));

                menu.replaceExistingItem(31, new CustomItem(new ItemStack(Material.PISTON), "&7生成高度: &3比机器高 " + offset + " 格方块", "", "&r左键: &7+0.1", "&r右键: &7-0.1"));
                menu.addMenuClickHandler(31, (p, slot, item, action) -> {
                    double offsetv = DoubleHandler.fixDouble(Double.parseDouble(BlockStorage.getLocationInfo(b.getLocation(), "offset")) + (action.isRightClicked() ? -0.1F : 0.1F));
                    BlockStorage.addBlockInfo(b, "offset", String.valueOf(offsetv));
                    newInstance(menu, b);
                    return false;
                });
            }

            @Override
            public boolean canOpen(Block b, Player p) {
                return p.hasPermission("slimefun.inventory.bypass") || SlimefunPlugin.getProtectionManager().hasPermission(p, b.getLocation(), ProtectableAction.ACCESS_INVENTORIES);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (flow == ItemTransportFlow.INSERT) return getInputSlots();
                else return new int[0];
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
                if (flow == ItemTransportFlow.INSERT) {
                    if (SlimefunUtils.isItemSimilar(item, new ItemStack(Material.SOUL_SAND), true))
                        return getSoulSandSlots();
                    else return getWitherSkullSlots();
                } else return new int[0];
            }
        };

        registerBlockHandler(getID(), new SlimefunBlockHandler() {

            @Override
            public void onPlace(Player p, Block b, SlimefunItem item) {
                BlockStorage.addBlockInfo(b, "offset", "3.0");
                BlockStorage.addBlockInfo(b, "enabled", "false");
            }

            @Override
            public boolean onBreak(Player p, Block b, SlimefunItem item, UnregisterReason reason) {
                if (reason == UnregisterReason.EXPLODE) return false;
                BlockMenu inv = BlockStorage.getInventory(b);

                if (inv != null) {
                    for (int slot : getSoulSandSlots()) {
                        if (inv.getItemInSlot(slot) != null) {
                            b.getWorld().dropItemNaturally(b.getLocation(), inv.getItemInSlot(slot));
                            inv.replaceExistingItem(slot, null);
                        }
                    }

                    for (int slot : getWitherSkullSlots()) {
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
            preset.addItem(i, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i : skullBorder) {
            preset.addItem(i, new CustomItem(Material.BLACK_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i : sandBorder) {
            preset.addItem(i, new CustomItem(Material.BROWN_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());
        }

        preset.addItem(1, new CustomItem(Material.WITHER_SKELETON_SKULL, "&7凋零骷髅头颅槽", "", "&r这里可以放入凋零骷髅头颅"), ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(7, new CustomItem(Material.SOUL_SAND, "&7灵魂沙槽", "", "&r这里可以放入灵魂沙"), ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(13, new CustomItem(Material.CLOCK, "&7冷却时间: &b30 秒", "", "&r这台机器需要等待半分钟才能操作", "&r所以请给它点时间!"), ChestMenuUtils.getEmptyClickHandler());
    }

    public int[] getInputSlots() {
        return new int[]{19, 28, 25, 34};
    }

    public int[] getWitherSkullSlots() {
        return new int[]{19, 28};
    }

    public int[] getSoulSandSlots() {
        return new int[]{25, 34};
    }

    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return 4096;
    }

    @Override
    public BlockTicker getItemHandler() {
        return new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, Config data) {
                if (BlockStorage.getLocationInfo(b.getLocation(), "enabled").equals("false")) return;

                if (lifetime % 60 == 0) {
                    if (ChargableBlock.getCharge(b) < ENERGY_CONSUMPTION) return;

                    int soulsand = 0;
                    int skulls = 0;

                    BlockMenu menu = BlockStorage.getInventory(b);

                    for (int slot : getSoulSandSlots()) {
                        if (SlimefunUtils.isItemSimilar(menu.getItemInSlot(slot), new ItemStack(Material.SOUL_SAND), true)) {
                            soulsand = soulsand + menu.getItemInSlot(slot).getAmount();

                            if (soulsand > 3) {
                                soulsand = 4;
                                break;
                            }
                        }
                    }

                    for (int slot : getWitherSkullSlots()) {
                        if (SlimefunUtils.isItemSimilar(menu.getItemInSlot(slot), new ItemStack(Material.WITHER_SKELETON_SKULL), true)) {
                            skulls = skulls + menu.getItemInSlot(slot).getAmount();

                            if (skulls > 2) {
                                skulls = 3;
                                break;
                            }
                        }
                    }

                    if (soulsand > 3 && skulls > 2) {
                        for (int slot : getSoulSandSlots()) {
                            if (SlimefunUtils.isItemSimilar(menu.getItemInSlot(slot), new ItemStack(Material.SOUL_SAND), true)) {
                                int amount = menu.getItemInSlot(slot).getAmount();

                                if (amount >= soulsand) {
                                    menu.consumeItem(slot, soulsand);
                                    break;
                                } else {
                                    soulsand = soulsand - amount;
                                    menu.replaceExistingItem(slot, null);
                                }
                            }
                        }

                        for (int slot : getWitherSkullSlots()) {
                            if (SlimefunUtils.isItemSimilar(menu.getItemInSlot(slot), new ItemStack(Material.WITHER_SKELETON_SKULL), true)) {
                                int amount = menu.getItemInSlot(slot).getAmount();

                                if (amount >= skulls) {
                                    menu.consumeItem(slot, skulls);
                                    break;
                                } else {
                                    skulls = skulls - amount;
                                    menu.replaceExistingItem(slot, null);
                                }
                            }
                        }

                        ChargableBlock.addCharge(b, -ENERGY_CONSUMPTION);
                        double offset = Double.parseDouble(BlockStorage.getLocationInfo(b.getLocation(), "offset"));

                        Slimefun.runSync(() -> b.getWorld().spawnEntity(new Location(b.getWorld(), b.getX() + 0.5D, b.getY() + offset, b.getZ() + 0.5D), EntityType.WITHER));
                    }
                }
            }

            @Override
            public void uniqueTick() {
                lifetime++;
            }

            @Override
            public boolean isSynchronized() {
                return false;
            }
        };
    }

}
