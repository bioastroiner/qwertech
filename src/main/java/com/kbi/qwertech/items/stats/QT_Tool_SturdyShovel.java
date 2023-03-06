package com.kbi.qwertech.items.stats;

import com.kbi.qwertech.entities.EntityHelperFunctions;
import gregtech.items.tools.early.GT_Tool_Shovel;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class QT_Tool_SturdyShovel extends GT_Tool_Shovel {

	List<int[]> query = new ArrayList<>();
	private boolean query_iterated=false;
	float finalSpeed=0;

	@Override
	public float getMaxDurabilityMultiplier()
	{
		return 2F;
	}

	@Override
	public int getToolDamagePerBlockBreak()
	{
		return 75;
	}

	@Override
	public int getToolDamagePerDropConversion()
	{
		return 75;
	}

	@Override
	public boolean isMinableBlock(Block aBlock, byte aMetaData) {
		return super.isMinableBlock(aBlock, aMetaData);
	}

	@Override
	public int convertBlockDrops(List<ItemStack> aDrops, ItemStack aStack, EntityPlayer aPlayer, Block aBlock, long aAvailableDurability, int aX, int aY, int aZ, byte aMetaData, int aFortune, boolean aSilkTouch, BlockEvent.HarvestDropsEvent aEvent) {
		determinateAOE(aX,aY,aZ,aPlayer,aPlayer.worldObj);
		int blocksBroke = 1;
		if(!query.isEmpty()) {
			// this is unsafe causes ConcurrentModificationException to happen
			for (int[] q : query) {
				query_iterated=true;
				int x = q[0];
				int y = q[1];
				int z = q[2];
				aPlayer.worldObj.func_147480_a(x, y, z, true);
				blocksBroke++;
			}
			query_iterated=false;
			query.clear();
		}
		return blocksBroke;
	}

	@Override
	public float getMiningSpeed(Block aBlock, byte aMetaData, float aDefault, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ)
	{
		finalSpeed=0;
		if(finalSpeed<super.getMiningSpeed(aBlock, aMetaData, aDefault, aPlayer, aWorld, aX, aY, aZ)){
			return super.getMiningSpeed(aBlock, aMetaData, aDefault, aPlayer, aWorld, aX, aY, aZ);
		}
		return finalSpeed;
	}

	private List<Float> determinateAOE(int x,int y, int z,EntityPlayer aPlayer,World aWorld){
		List<Float> speed = new ArrayList<>();speed.add(0f);
		if(query_iterated)return speed;
		if(!query.isEmpty())query.clear();
		MovingObjectPosition ray = EntityHelperFunctions.getEntityLookTrace(aWorld, aPlayer, false, 5D);
		if (ray.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK){
			switch(ray.sideHit){
				case 0:
				case 1: break;
				case 2: // east
				case 3: // west
				case 4: // north -> only mine Vertically for tunnel down, mine horz (X+-) and vert in odd
				case 5: // south
					// do simple tunnel
					if(isBlockHarvestable(x,y-1,z,aPlayer,aWorld)){
						query.add(new int[]{x, y-1, z});
					}
					// no need to check the first block if you are in #checkForDrops you are already sure its breakble
//					if(isBlockHarvestable(x,y,z,aPlayer,aWorld)){
//						query.add(new int[]{x, y, z});
//						if(isBlockHarvestable(x,y-1,z,aPlayer,aWorld)){
//							query.add(new int[]{x, y-1, z});
//						}
//					}
			}
		}
		float max=0f;
		for (float f:speed) {
			max=Math.max(f,max);
		}
		finalSpeed=max;
		return speed;
	}

	private boolean isBlockHarvestable(int x, int y, int z,EntityPlayer mPlayer,World mWorld) {
		Block aBlock=mWorld.getBlock(x, y, z);
		return aBlock.canHarvestBlock(mPlayer, mWorld.getBlockMetadata(x, y, z)) && this.isMinableBlock(mWorld.getBlock(x, y, z), (byte) mWorld.getBlockMetadata(x, y, z)) && aBlock.getPlayerRelativeBlockHardness(mPlayer, mWorld, x, y, z) > 0;
	}


}
