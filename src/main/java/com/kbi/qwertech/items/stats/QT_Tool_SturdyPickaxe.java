package com.kbi.qwertech.items.stats;

import com.kbi.qwertech.entities.EntityHelperFunctions;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.oredict.OreDictPrefix;
import gregapi.render.IIconContainer;
import gregtech.items.tools.early.GT_Tool_Pickaxe;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent;

import java.util.List;

import static com.kbi.qwertech.items.stats.ToolsVector.raytraceFromEntity;

public class QT_Tool_SturdyPickaxe extends GT_Tool_Pickaxe implements IAOE_Tool {
    @Override
    public boolean canCollect() {
        return true;
    }

    @Override
    public IIconContainer getIcon(boolean aIsToolHead, ItemStack aStack) {
        return aIsToolHead ? MultiItemTool.getPrimaryMaterial(aStack, MT.Steel).mTextureSetsItems.get(OreDictPrefix.get("toolHeadSturdyPickaxe").mIconIndexItem) : MultiItemTool.getSecondaryMaterial(aStack, MT.WOODS.Spruce).mTextureSetsItems.get(OP.stick.mIconIndexItem);
    }

    @Override
    public float getMaxDurabilityMultiplier() {
        return 1.2F;
    }

    @Override
    public int getToolDamagePerBlockBreak() {
        return 75;
    }

    @Override
    public int getToolDamagePerDropConversion() {
        return 75;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, int X, int Y, int Z, EntityPlayer player) {
        if(player.isSneaking()) return false;
        // IMPORTED FROM IMPACT IT WORKED BETTER THAN MINE!!
        Block block = player.worldObj.getBlock(X, Y, Z);
        int meta = player.worldObj.getBlockMetadata(X, Y, Z);

        if (block == null || !itemstack.hasTagCompound())
            return false;

        MovingObjectPosition mop = raytraceFromEntity(player.worldObj, player, false, 4.5d);
        if (mop == null)
            return false;
        int sideHit = mop.sideHit;
        // we successfully destroyed a block. time to do AOE!
        switch (sideHit) {
            case 0:
            case 1:
                breakBlock(player.worldObj, X, Y, Z, sideHit, player, X, Y, Z);
                break;
            case 2:
            case 3:
            case 4:
            case 5:
                breakBlock(player.worldObj, X, Y, Z, sideHit, player, X, Y, Z);
                breakBlock(player.worldObj, X, Y-1, Z, sideHit, player, X, Y, Z);
                break;
        }
        return false;
    }

    @Override
    public float getMiningSpeed(Block aBlock, byte aMetaData, float aDefault, EntityPlayer player, World aWorld, int X, int Y, int Z) {
        if(player.isSneaking()) return aDefault;
        Block block = player.worldObj.getBlock(X, Y, Z);
        int meta = player.worldObj.getBlockMetadata(X, Y, Z);
        float speed_all=aDefault;

        if (block == null)
            return aDefault;

        MovingObjectPosition mop = raytraceFromEntity(player.worldObj, player, false, 4.5d);
        if (mop == null)
            return aDefault;
        int sideHit = mop.sideHit;
        switch (sideHit) {
            case 0:
            case 1:
                speed_all+=getMiningSpeed(block, (byte) aWorld.getBlockMetadata(X,Y,Z));
                break;
            case 2:
            case 3:
            case 4:
            case 5:
                speed_all+=getMiningSpeed(block, (byte) aWorld.getBlockMetadata(X,Y,Z));
                speed_all+=getMiningSpeed(block, (byte) aWorld.getBlockMetadata(X,Y-1,Z));
                break;
        }
        // 1.8 means very small penalty if two blocks are the same type!
        return aDefault*1.8f/speed_all;
    }
    public void breakBlock(World world, int x, int y, int z, int sideHit, EntityPlayer playerEntity, int refX, int refY, int refZ) {
        if (world.isAirBlock(x, y, z)) return;

        if (!(playerEntity instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) playerEntity;

        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);

        Block refBlock = world.getBlock(refX, refY, refZ);
        float refStrength = ForgeHooks.blockStrength(refBlock, player, world, refX, refY, refZ);
        float strength = ForgeHooks.blockStrength(block, player, world, x, y, z);

        if (!ForgeHooks.canHarvestBlock(block, player, meta) || refStrength / strength > 10f) return;

        BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(world, player.theItemInWorldManager.getGameType(), player, x, y, z);
        if (event.isCanceled())
            return;

        if (player.capabilities.isCreativeMode) {
            block.onBlockHarvested(world, x, y, z, meta, player);
            if (block.removedByPlayer(world, player, x, y, z, false))
                block.onBlockDestroyedByPlayer(world, x, y, z, meta);
            if (!world.isRemote) {
                player.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, world));
            }
            return;
        }
        ItemStack currentItem = player.getCurrentEquippedItem();
        if (currentItem != null) {
            currentItem.func_150999_a(world, block, x, y, z, player);
        }
        if (!world.isRemote) {
            block.onBlockHarvested(world, x, y, z, meta, player);

            if (block.removedByPlayer(world, player, x, y, z, true)) {
                block.onBlockDestroyedByPlayer(world, x, y, z, meta);
                block.harvestBlock(world, player, x, y, z, meta);
                block.dropXpOnBlockBreak(world, x, y, z, event.getExpToDrop());
            }

            player.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, world));
        } else {
            world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
            if (block.removedByPlayer(world, player, x, y, z, true)) {
                block.onBlockDestroyedByPlayer(world, x, y, z, meta);
            }
            ItemStack itemstack = player.getCurrentEquippedItem();
            if (itemstack != null) {
                itemstack.func_150999_a(world, block, x, y, z, player);

                if (itemstack.stackSize == 0) {
                    player.destroyCurrentEquippedItem();
                }
            }
        }
    }
}