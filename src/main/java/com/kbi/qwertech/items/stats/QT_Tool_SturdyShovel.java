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
	Block blockToMine;
	//boolean LOCK = false;
	float finalSpeed=0;

	//public MovingObjectPosition MOP;

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

	// returns 1 if it can break if not just returns mining speed, otherwise 0 if the block isnt harvestble
//	public float breakCheck(EntityPlayer player, World world, int x, int y, int z, boolean doBreak)
//	{
//		Block aBlock = world.getBlock(x, y, z);
//		int aMetadata = world.getBlockMetadata(x, y, z);
//		if (aBlock.canHarvestBlock(player, aMetadata) && this.isMinableBlock(aBlock, (byte)aMetadata) && aBlock.getPlayerRelativeBlockHardness(player, world, x, y, z) > 0)
//		{
//			if (doBreak)
//			{
//				world.func_147480_a(x, y, z, true);
//				return 1;
//			}
//			return super.getMiningSpeed(aBlock, (byte)aMetadata);
//		}
//		return 0;
//	}
	@Override
	public boolean isMinableBlock(Block aBlock, byte aMetaData) {
		return super.isMinableBlock(aBlock, aMetaData);
	}

//	public void calculateSides(boolean[] pos, boolean[] loc)
//	{
//		if (MOP != null && MOP.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
//		{
//			switch (MOP.sideHit)
//			{
//				case 0: // bottom
//				case 1: // top
//				{
//					pos[0] = false;
//					pos[1] = true;
//					pos[2] = true;
//					break;
//				}
//				case 2: // east +
//				case 3: // west
//				{
//					pos[0] = true;
//					pos[2] = true;
//					break;
//				}
//				case 4: // north - Z
//				case 5: // south + Z
//				{
//					pos[0] = true;
//					pos[1] = true;
//					break;
//				}
//				default:
//				{
//					break;
//				}
//			}
//			if (MOP.hitVec.yCoord > MOP.blockY + 0.5)
//			{
//				loc[0] = true;
//			}
//			if (MOP.hitVec.zCoord > MOP.blockZ + 0.5)
//			{
//				loc[1] = true;
//			}
//			if (MOP.hitVec.xCoord > MOP.blockX + 0.5)
//			{
//				loc[2] = true;
//			}
//		}
//	}

//	public float checkBlocks(EntityPlayer aPlayer, int aX, int aY, int aZ, boolean chop)
//	{
//		boolean[] checkPos = new boolean[]{false, false, false}; //
//		boolean[] checkLoc = new boolean[]{false, false, false}; // y z x
//
//		calculateSides(checkPos, checkLoc);
//
//		float returnable = 0;
//		int x = aX;
//		int y = aY;
//		int z = aZ;
//		// happends when you mine from south or west
//		if (checkPos[0])
//		{
//			y = checkLoc[0] ? y + 1 : y - 1;
//			returnable = returnable + breakCheck(aPlayer, aPlayer.worldObj, x, y, z, chop);
//			if (checkPos[1])
//			{
//				z = checkLoc[1] ? z + 1 : z - 1;
//				returnable = returnable + breakCheck(aPlayer, aPlayer.worldObj, x, y, z, chop);
//			} else if (checkPos[2])
//			{
//				x = checkLoc[2] ? x + 1 : x - 1;
//				returnable = returnable + breakCheck(aPlayer, aPlayer.worldObj, x, y, z, chop);
//			}
//
//		}
//		x = aX;
//		y = aY;
//		z = aZ;
//		if (checkPos[1])
//		{
//			z = checkLoc[1] ? z + 1 : z - 1;
//			returnable = returnable + breakCheck(aPlayer, aPlayer.worldObj, x, y, z, chop);
//			if (checkPos[2])
//			{
//				x = checkLoc[2] ? x + 1 : x - 1;
//				returnable = returnable + breakCheck(aPlayer, aPlayer.worldObj, x, y, z, chop);
//			}
//
//		}
//		x = aX;
//		y = aY;
//		z = aZ;
//		// happends when you mine from bottom or top
//		if (checkPos[2])
//		{
//			x = checkLoc[2] ? x + 1 : x - 1;
//			returnable = returnable + breakCheck(aPlayer, aPlayer.worldObj, x, y, z, chop);
//		}
//		return returnable;
//	}

	@Override
	public int convertBlockDrops(List<ItemStack> aDrops, ItemStack aStack, EntityPlayer aPlayer, Block aBlock, long aAvailableDurability, int aX, int aY, int aZ, byte aMetaData, int aFortune, boolean aSilkTouch, BlockEvent.HarvestDropsEvent aEvent) {
		//if (LOCK) return 0;
		//LOCK = true;
		//int returnable = (int)checkBlocks(aPlayer, aX, aY, aZ, true);
//		doAOE();
		//LOCK = false;

		determinateAOE(aX,aY,aZ,aPlayer,aPlayer.worldObj);
		int blocksBroke = 1;
		if(!query.isEmpty()) {
			// this is unsafe causes ConcurrentModificationException to happen
			for (int[] q : query) {
				int x = q[0];
				int y = q[1];
				int z = q[2];
				aPlayer.worldObj.func_147480_a(x, y, z, true);
				blocksBroke++;
			}
		}
		return blocksBroke;
	}

	@Override
	public float getMiningSpeed(Block aBlock, byte aMetaData, float aDefault, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ)
	{
		finalSpeed=0;
		blockToMine=aBlock;
		//MOP = EntityHelperFunctions.getEntityLookTrace(aWorld, aPlayer, false, 5D);
		if(finalSpeed<super.getMiningSpeed(aBlock, aMetaData, aDefault, aPlayer, aWorld, aX, aY, aZ)){
			return super.getMiningSpeed(aBlock, aMetaData, aDefault, aPlayer, aWorld, aX, aY, aZ);
		}
		return finalSpeed;
	}

	private List<Float> determinateAOE(int x,int y, int z,EntityPlayer aPlayer,World aWorld){
		if(!query.isEmpty())query.clear();
		MovingObjectPosition ray = EntityHelperFunctions.getEntityLookTrace(aWorld, aPlayer, false, 5D);
		List<Float> speed = new ArrayList<>();
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
