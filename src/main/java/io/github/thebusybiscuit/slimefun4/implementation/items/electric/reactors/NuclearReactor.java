package io.github.thebusybiscuit.slimefun4.implementation.items.electric.reactors;

import io.github.thebusybiscuit.cscorelib2.skull.SkullItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactive;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AReactor;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineFuel;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * The {@link NuclearReactor} is an implementation of {@link AReactor} that uses
 * any {@link Radioactive} material to generate energy.
 * It needs water coolant as well as a steady supply of Reactor Coolant Cells
 *
 * @author TheBusyBiscuit
 * @see NetherStarReactor
 */
public abstract class NuclearReactor extends AReactor {

    public NuclearReactor(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Override
    public String getInventoryTitle() {
        return SlimefunItems.NUCLEAR_REACTOR.getItemMeta().getDisplayName();
    }

    @Override
    protected void registerDefaultFuelTypes() {
        registerFuel(new MachineFuel(1200, SlimefunItems.URANIUM, SlimefunItems.NEPTUNIUM));
        registerFuel(new MachineFuel(600, SlimefunItems.NEPTUNIUM, SlimefunItems.PLUTONIUM));
        registerFuel(new MachineFuel(1500, SlimefunItems.BOOSTED_URANIUM, null));
    }

    @Override
    public ItemStack getFuelIcon() {
        return SlimefunItems.URANIUM;
    }

    @Override
    public ItemStack getProgressBar() {
        return SkullItem.fromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTNhZDhlZTg0OWVkZjA0ZWQ5YTI2Y2EzMzQxZjYwMzNiZDc2ZGNjNDIzMWVkMWVhNjNiNzU2NTc1MWIyN2FjIn19fQ==");
    }

    @Override
    public ItemStack getCoolant() {
        return SlimefunItems.REACTOR_COOLANT_CELL;
    }

    @Override
    public void extraTick(Location l) {
        // This machine does not need to perform anything while ticking
        // The Nether Star Reactor uses this method to generate the Wither Effect
    }

}
