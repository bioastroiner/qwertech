package com.kbi.qwertech.items.stats;

import com.kbi.qwertech.QwerTech;
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
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent;

import java.util.List;

public class QT_TOOL_MiningHammer extends GT_Tool_Pickaxe implements IAOE_Tool {
    // now i understand why qwer had this var, it makes the convertBlockDrops method not be called again while its still running causing an stack overflow error.
    boolean LOCK;

    public IIconContainer getIcon(boolean aIsToolHead, ItemStack aStack) {
        return aIsToolHead ? MultiItemTool.getPrimaryMaterial(aStack, MT.Steel).mTextureSetsItems.get(OreDictPrefix.get("toolHeadMiningHammer").mIconIndexItem) : MultiItemTool.getSecondaryMaterial(aStack, MT.WOODS.Spruce).mTextureSetsItems.get(OP.stick.mIconIndexItem);
    }

    @Override
    public float getMaxDurabilityMultiplier() {
        return 0.8F;
    }

    @Override
    public int getToolDamagePerBlockBreak() {
        return 50;
    }

    @Override
    public int getToolDamagePerDropConversion() {
        return 75;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, int X, int Y, int Z, EntityPlayer player) {
        LOCK = true;
        return false;
    }

    @Override
    public int convertBlockDrops(List<ItemStack> aDrops, ItemStack aStack, EntityPlayer aPlayer, Block aBlock, long aAvailableDurability, int aX, int aY, int aZ, byte aMetaData, int aFortune, boolean aSilkTouch, BlockEvent.HarvestDropsEvent aEvent) {
        if (!LOCK) return 0;
        LOCK = false;
        MovingObjectPosition ray = EntityHelperFunctions.getEntityLookTrace(aPlayer.worldObj, aPlayer, false, 5D);
        World aWorld = aPlayer.worldObj;
        int blocksBroke = 1;
        try {
            if (ray.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                switch (ray.sideHit) {
                    case 0: // bottom
                    case 1: // top
                        for (int i = -1; i < 2; i++)
                            for (int j = -1; j < 2; j++)
                                if (isBlockHarvestable(aX + i, aY, aZ + j, aPlayer, aWorld)) {
                                    aPlayer.worldObj.func_147480_a(aX + i, aY, aZ + j, true);
                                    blocksBroke++;
                                }
                        break;
                    case 2: // east
                    case 3: // west // IDK WHY IT MIXXED east-west with south-north, axisis are wierd, this is north and next is east
                        for (int i = -1; i < 2; i++)
                            for (int j = -1; j < 2; j++)
                                if (isBlockHarvestable(aX + i, aY + j, aZ, aPlayer, aWorld)) {
                                    aPlayer.worldObj.func_147480_a(aX + i, aY + j, aZ, true);
                                    blocksBroke++;
                                }
                        break;
                    case 4: // north -> only mine Vertically for tunnel down, mine horz (X+-) and vert in odd
                    case 5: // south
                        for (int i = -1; i < 2; i++)
                            for (int j = -1; j < 2; j++)
                                if (isBlockHarvestable(aX, aY + i, aZ + j, aPlayer, aWorld)) {
                                    aPlayer.worldObj.func_147480_a(aX, aY + i, aZ + j, true);
                                    blocksBroke++;
                                }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {

        }
        LOCK = false;
        return blocksBroke;
    }

    @Override
    public float getMiningSpeed(Block aBlock, byte aMetaData, float aDefault, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ) {
        float speed_f = aDefault;
        float speed_d = speed_f;
        MovingObjectPosition ray = EntityHelperFunctions.getEntityLookTrace(aWorld, aPlayer, false, 5D);
        try {
            if (ray.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                switch (ray.sideHit) {
                    case 0: // bottom
                    case 1: // top
                        for (int i = -1; i < 2; i++)
                            for (int j = -1; j < 2; j++)
                                if (isBlockHarvestable(aX + i, aY, aZ + j, aPlayer, aWorld)) {
                                    speed_f += super.getMiningSpeed(aWorld.getBlock(aX + i, aY, aZ + j), (byte) aWorld.getBlockMetadata(aX + i, aY, aZ + j));
                                }
                        break;
                    case 2: // east
                    case 3: // west // IDK WHY IT MIXXED east-west with south-north, axisis are wierd, this is north and next is east
                        for (int i = -1; i < 2; i++)
                            for (int j = -1; j < 2; j++)
                                if (isBlockHarvestable(aX + i, aY + j, aZ, aPlayer, aWorld)) {
                                    speed_f += super.getMiningSpeed(aWorld.getBlock(aX + i, aY + j, aZ), (byte) aWorld.getBlockMetadata(aX + i, aY + j, aZ));
                                }
                        break;
                    case 4:
                    case 5: // south
                        for (int i = -1; i < 2; i++)
                            for (int j = -1; j < 2; j++)
                                if (isBlockHarvestable(aX, aY + i, aZ + j, aPlayer, aWorld)) {
                                    speed_f += super.getMiningSpeed(aWorld.getBlock(aX, aY + i, aZ + j), (byte) aWorld.getBlockMetadata(aX, aY + i, aZ + j));
                                }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return speed_d * 2 / speed_f;
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


    private boolean isBlockHarvestable(int x, int y, int z, EntityPlayer mPlayer, World mWorld) {
        Block aBlock = mWorld.getBlock(x, y, z);
        return aBlock.canHarvestBlock(mPlayer, mWorld.getBlockMetadata(x, y, z)) && this.isMinableBlock(mWorld.getBlock(x, y, z), (byte) mWorld.getBlockMetadata(x, y, z));
    }
}
