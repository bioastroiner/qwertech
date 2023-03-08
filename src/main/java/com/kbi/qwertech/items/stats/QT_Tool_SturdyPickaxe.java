package com.kbi.qwertech.items.stats;

import com.kbi.qwertech.entities.EntityHelperFunctions;
import gregtech.items.tools.early.GT_Tool_Pickaxe;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

import java.util.List;

public class QT_Tool_SturdyPickaxe extends GT_Tool_Pickaxe {
    int blocksBroke = 1;
    private boolean doAOE = false;

    @Override
    public float getMaxDurabilityMultiplier() {
        return 2F;
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
    public int convertBlockDrops(List<ItemStack> aDrops, ItemStack aStack, EntityPlayer aPlayer, Block aBlock, long aAvailableDurability, int aX, int aY, int aZ, byte aMetaData, int aFortune, boolean aSilkTouch, BlockEvent.HarvestDropsEvent aEvent) {
        //determinateAOE(aX,aY,aZ,aPlayer,aPlayer.worldObj);
        if (blocksBroke > 1) return 1;
        if (isBlockHarvestable(aX, aY - 1, aZ, aPlayer, aPlayer.worldObj) && doAOE) {
            blocksBroke++;
            aPlayer.worldObj.func_147480_a(aX, aY - 1, aZ, true);
        }
        return blocksBroke;
    }

    @Override
    public float getMiningSpeed(Block aBlock, byte aMetaData, float aDefault, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ) {
        return determinateAOE(aX, aY, aZ, aPlayer, aWorld, aDefault);
    }

    private float determinateAOE(int x, int y, int z, EntityPlayer aPlayer, World aWorld, float aDefault) {
        float speed_f = aDefault;
        doAOE = false;
        blocksBroke = 1;
        if (aPlayer.posY >= y) {
            doAOE = false;
            return speed_f;
        }
        MovingObjectPosition ray = EntityHelperFunctions.getEntityLookTrace(aWorld, aPlayer, false, 5D);
        try {
            if (ray.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                switch (ray.sideHit) {
                    case -1:
                    case 0:
                    case 1:
                        break;
                    case 2: // east
                    case 3: // west
                    case 4: // north -> only mine Vertically for tunnel down, mine horz (X+-) and vert in odd
                    case 5: // south
                        // do simple tunnel
                        if (isBlockHarvestable(x, y - 1, z, aPlayer, aWorld)) {
                            doAOE = true;
                            speed_f = Math.max(speed_f, super.getMiningSpeed(aWorld.getBlock(x, y, z), (byte) aWorld.getBlockMetadata(x, y, z)));
                        }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return speed_f;
    }

    private boolean isBlockHarvestable(int x, int y, int z, EntityPlayer mPlayer, World mWorld) {
        Block aBlock = mWorld.getBlock(x, y, z);
        return aBlock.canHarvestBlock(mPlayer, mWorld.getBlockMetadata(x, y, z)) && this.isMinableBlock(mWorld.getBlock(x, y, z), (byte) mWorld.getBlockMetadata(x, y, z));
    }
}