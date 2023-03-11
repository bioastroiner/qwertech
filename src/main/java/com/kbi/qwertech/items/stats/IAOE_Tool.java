package com.kbi.qwertech.items.stats;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IAOE_Tool {
    public boolean onBlockStartBreak(ItemStack itemstack, int X, int Y, int Z, EntityPlayer player);

    void onLeftClick(EntityPlayer player, World world, int face, int x, int y, int z, Event.Result useBlock, Event.Result useItem);
}
