package me.mrCookieSlime.Slimefun.Objects.SlimefunItem.multiblocks;

import io.github.thebusybiscuit.cscorelib2.inventory.InvUtils;
import io.github.thebusybiscuit.cscorelib2.protection.ProtectableAction;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunMachine;
import me.mrCookieSlime.Slimefun.Objects.handlers.MultiBlockInteractionHandler;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class MultiBlockMachine extends SlimefunMachine {

    private static final BlockFace[] outputFaces = {
            BlockFace.UP,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    public MultiBlockMachine(Category category, SlimefunItemStack item, ItemStack[] recipe, ItemStack[] machineRecipes, BlockFace trigger) {
        super(category, item, recipe, machineRecipes, trigger);
    }

    @Override
    public void register(SlimefunAddon addon) {
        addItemHandler(getInteractionHandler());
        super.register(addon);
    }

    protected MultiBlockInteractionHandler getInteractionHandler() {
        return (p, mb, b) -> {
            if (mb.equals(getMultiBlock())) {
                if (!isDisabled() && SlimefunPlugin.getProtectionManager().hasPermission(p, b.getLocation(), ProtectableAction.ACCESS_INVENTORIES) && Slimefun.hasUnlocked(p, this, true)) {
                    onInteract(p, b);
                }

                return true;
            } else return false;
        };
    }

    public abstract void onInteract(Player p, Block b);

    // Overloaded method for finding a potential output chest. Fallbacks to the old system of putting the adding back into the dispenser.
    // Optional last argument Inventory placeCheckerInv is for multiblock machines that create a dummy inventory to check if there's a space for the adding,
    // i.e. Enhanced crafting table
    protected Inventory findOutputInventory(ItemStack adding, Block dispBlock, Inventory dispInv) {
        return findOutputInventory(adding, dispBlock, dispInv, dispInv);
    }

    protected Inventory findOutputInventory(ItemStack product, Block dispBlock, Inventory dispInv, Inventory placeCheckerInv) {
        Inventory outputInv = findOutputChest(dispBlock, product);

        // This if-clause will trigger if no suitable output chest was found. It's functionally the same as the old fit check for the dispenser, only refactored.
        if (outputInv == null && InvUtils.fits(placeCheckerInv, product)) {
            return dispInv;
        } else {
            return outputInv;
        }
    }

    protected Inventory findOutputChest(Block b, ItemStack output) {
        for (BlockFace face : outputFaces) {
            Block potentialOutput = b.getRelative(face);

            if (potentialOutput.getType() == Material.CHEST) {
                String id = BlockStorage.checkID(potentialOutput);

                if (id != null && id.equals("OUTPUT_CHEST")) {
                    // Found the output chest! Now, let's check if we can fit the product in it.
                    Inventory inv = ((Chest) potentialOutput.getState()).getInventory();

                    if (InvUtils.fits(inv, output)) {
                        return inv;
                    }
                }
            }
        }

        return null;
    }

}
