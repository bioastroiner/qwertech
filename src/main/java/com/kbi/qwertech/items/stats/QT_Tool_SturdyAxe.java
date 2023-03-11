package com.kbi.qwertech.items.stats;

import com.google.common.collect.Lists;
import com.kbi.qwertech.QwerTech;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import gnu.trove.set.hash.THashSet;
import gregapi.data.IL;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.item.multiitem.behaviors.Behavior_Tool;
import gregapi.oredict.OreDictPrefix;
import gregapi.render.IIconContainer;
import gregapi.render.TextureSet;
import gregapi.util.ST;
import gregapi.util.UT;
import gregapi.util.WD;
import gregtech.items.tools.early.GT_Tool_Axe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.IIcon;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import static gregapi.item.multiitem.MultiItemTool.getToolDamage;
import static gregapi.item.multiitem.MultiItemTool.getToolMaxDamage;

public class QT_Tool_SturdyAxe extends GT_Tool_Axe implements IAOE_Tool {

	public int breakRadius = 1;
	public int breakDepth = 0;
	@Override
	public float getMaxDurabilityMultiplier()
	{
		return 1.1F;
	}
	
	@Override
	public int getToolDamagePerDropConversion()
	{
	    return 20;
	}

	public int handle = TextureSet.addToAll(QwerTech.MODID, true, "chop_handle");
	@Override
	public IIconContainer getIcon(boolean aIsToolHead, ItemStack aStack) {return aIsToolHead ? MultiItemTool.getPrimaryMaterial(aStack, MT.Steel).mTextureSetsItems.get(OreDictPrefix.get("toolHeadSturdyAxe").mIconIndexItem) : MultiItemTool.getSecondaryMaterial(aStack, MT.WOODS.Spruce).mTextureSetsItems.get(handle);}

	public static boolean detectTree(World world, int pX, int pY, int pZ) {
		ChunkPosition pos = null;
		Stack<ChunkPosition> candidates = new Stack<>();
		candidates.add(new ChunkPosition(pX, pY, pZ));

		while (!candidates.isEmpty()) {
			ChunkPosition candidate = candidates.pop();
			int curX = candidate.chunkPosX, curY = candidate.chunkPosY, curZ = candidate.chunkPosZ;

			Block block = world.getBlock(curX, curY, curZ);
			if ((pos == null || candidate.chunkPosY > pos.chunkPosY) && block.isWood(world, curX, curY, curZ)) {
				pos = new ChunkPosition(curX, candidate.chunkPosY + 1, curZ);
				// go up
				while (world.getBlock(curX, pos.chunkPosY, curZ).isWood(world, curX, pos.chunkPosY, curZ)) {
					pos = new ChunkPosition(curX, pos.chunkPosY + 1, curZ);
				}
				// check if we still have a way diagonally up
				candidates.add(new ChunkPosition(curX + 1, pos.chunkPosY + 1, curZ));
				candidates.add(new ChunkPosition(curX, pos.chunkPosY + 1, curZ + 1));
				candidates.add(new ChunkPosition(curX - 1, pos.chunkPosY + 1, curZ));
				candidates.add(new ChunkPosition(curX, pos.chunkPosY + 1, curZ - 1));
			}
		}

		// not even one match, so there were no logs.
		if (pos == null) {
			return false;
		}

		// check if there were enough leaves around the last position
		// pos now contains the block above the topmost log
		// we want at least 5 leaves in the surrounding 26 blocks
		int d = 3;
		int leaves = 0;
		for (int offX = 0; offX < d; offX++) {
			for (int offY = 0; offY < d; offY++) {
				for (int offZ = 0; offZ < d; offZ++) {
					int xPos = pos.chunkPosX - 1 + offX, yPos = pos.chunkPosY - 1 + offY, zPos = pos.chunkPosZ - 1 + offZ;
					Block leaf = world.getBlock(xPos, yPos, zPos);
					if (leaf != null && leaf.isLeaves(world, xPos, yPos, zPos)) {
						if (++leaves >= 5) {
							return true;
						}
					}
				}
			}
		}

		// not enough leaves. sorreh
		return false;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player) {
		// only effective materials matter. We don't want to aoe when beraking dirt with a hammer.
		Block wood = player.worldObj.getBlock(x, y, z);
		World world = player.worldObj;
		int meta = player.worldObj.getBlockMetadata(x, y, z);

		if (wood == null || !stack.hasTagCompound() || player.isSneaking()) return false;

		if (wood.isWood(world, x, y, z) || wood.getMaterial() == Material.sponge)
			if (detectTree(world, x, y, z)) {
				TreeChopTask chopper = new TreeChopTask(this, stack, new ChunkPosition(x, y, z), player, 128);
				try {
					FMLCommonHandler.instance().bus().register(chopper);
				} catch (LinkageError l) {
//                    PlayerUtils.sendChatMessage(player, "Well, that's embarrassing, you missed!");
				}
				// custom block breaking code, don't call vanilla code
				return true;
			}

		return false;
	}

    @Override
    public void onLeftClick(EntityPlayer player, World world, int face, int x, int y, int z, Event.Result useBlock, Event.Result useItem) {

    }

    public void breakBlock(World world, int x, int y, int z, int sideHit, EntityPlayer playerEntity, int refX, int refY, int refZ) {
		if (world.isAirBlock(x, y, z)) return;

		if (!(playerEntity instanceof EntityPlayerMP)) return;

		ItemStack itemstack = playerEntity.getCurrentEquippedItem();
		if (itemstack != null && !playerEntity.capabilities.isCreativeMode) {
			long damage = getToolDamage(itemstack);
			long maxDamage = getToolMaxDamage(itemstack);
			if (damage >= maxDamage) {
				playerEntity.destroyCurrentEquippedItem();
			}
		}

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
		}
	}

	@Override
	public boolean isCrowbar()
	{
	    return true;
	}
	
	@Override
	public boolean isMinableBlock(Block aBlock, byte aMetaData)
	  {
	    if (((aBlock instanceof BlockRailBase)) || (aBlock.getMaterial() == Material.circuits) || (IL.TG_Ore_Cluster_1.equal(aBlock)) || (IL.TG_Ore_Cluster_2.equal(aBlock))) {
	      return true;
	    }
	    String tTool = aBlock.getHarvestTool(aMetaData);
	    if (UT.Code.stringValid(tTool)) {
	      return "crowbar".equalsIgnoreCase(tTool) || super.isMinableBlock(aBlock, aMetaData);
	    }
	    return super.isMinableBlock(aBlock, aMetaData);
	  }
	
	@Override
	public void onStatsAddedToTool(MultiItemTool aItem, int aID)
	{
	    aItem.addItemBehavior(aID, new Behavior_Tool("axe", "dig.wood", getToolDamagePerContainerCraft(), true));
	    aItem.addItemBehavior(aID, new Behavior_Tool("crowbar", "random.break", 100L, !canBlock()));
	}

	public static class TreeChopTask {

		public final World world;
		public final EntityPlayer player;
		public final ItemStack stack;
		public final QT_Tool_SturdyAxe axe;

		public final int blocksPerTick;

		public Queue<ChunkPosition> blocks = Lists.newLinkedList();
		public Set<ChunkPosition> visited = new THashSet<>();

		public TreeChopTask(QT_Tool_SturdyAxe axe, ItemStack stack, ChunkPosition start, EntityPlayer player, int blocksPerTick) {
			this.world         = player.getEntityWorld();
			this.player        = player;
			this.stack         = stack;
			this.blocksPerTick = blocksPerTick;
			this.axe           = axe;
			this.blocks.add(start);
		}

		private void queueCoordinate(int x, int y, int z) {
			ChunkPosition pos = new ChunkPosition(x, y, z);
			if (!visited.contains(pos)) {
				blocks.add(pos);
			}
		}

		@SubscribeEvent
		public void onWorldTick(TickEvent.WorldTickEvent event) {
			if (event.side.isClient()) {
				finish();
				return;
			}
			// only if same dimension
			if (event.world.provider.dimensionId != world.provider.dimensionId) {
				return;
			}

			// setup
			int left = blocksPerTick;

			// continue running
			ChunkPosition pos;
			while (left > 0) {
				// completely done or can't do our job anymore?!
				if (blocks.isEmpty()) {
					finish();
					return;
				}

				pos = blocks.remove();
				if (!visited.add(pos)) {
					continue;
				}
				int x = pos.chunkPosX, y = pos.chunkPosY, z = pos.chunkPosZ;

				Block block = world.getBlock(x, y, z);
				int meta = world.getBlockMetadata(x, y, z);

				// can we harvest the block and is effective?
				if (!block.isWood(world, x, y, z)) {
					continue;
				}

				// save its neighbors
				queueCoordinate(x + 1, y, z);
				queueCoordinate(x, y, z + 1);
				queueCoordinate(x - 1, y, z);
				queueCoordinate(x, y, z - 1);

				// also add the layer above.. stupid acacia trees
				for (int offX = 0; offX < 3; offX++) {
					for (int offZ = 0; offZ < 3; offZ++) {
						queueCoordinate(x - 1 + offX, y + 1, z - 1 + offZ);
					}
				}

				axe.breakBlock(player.worldObj, x, y, z, 0, player, x, y, z);

				left--;
			}
		}

		private void finish() {
			// goodbye cruel world
			FMLCommonHandler.instance().bus().unregister(this);
		}
	}
}
