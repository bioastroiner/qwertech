package com.kbi.qwertech.items.stats;

import com.kbi.qwertech.QwerTech;
import com.kbi.qwertech.items.behavior.Behavior_Slingshot;
import gregapi.data.MT;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.item.multiitem.tools.ToolStats;
import gregapi.render.IIconContainer;
import gregapi.render.TextureSet;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class QT_Tool_Slingshot extends ToolStats {

	@Override
	public boolean isMinableBlock(Block arg0, byte arg1) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isRangedWeapon()
	{
		return true;
	}
	
	@Override
	  public void onStatsAddedToTool(MultiItemTool aItem, int aID)
	  {
		  aItem.addItemBehavior(aID, new Behavior_Slingshot("random.bow", 50));
	  }
	public static final int slingshotTexID = TextureSet.addToAll(QwerTech.MODID, true, "slingshot");
	public static final int stringshotTexID = TextureSet.addToAll(QwerTech.MODID, true, "slingstring");
	@Override
	  public IIconContainer getIcon(boolean aIsToolHead, ItemStack aStack)
	  {
	    return aIsToolHead ? MultiItemTool.getPrimaryMaterial(aStack, MT.Steel).mTextureSetsItems.get(slingshotTexID) : MultiItemTool.getPrimaryMaterial(aStack, MT.Steel).mTextureSetsItems.get(stringshotTexID);
	  }
	  
		@Override
	  public short[] getRGBa(boolean aIsToolHead, ItemStack aStack)
	  {
	    return aIsToolHead ? MultiItemTool.getPrimaryMaterial(aStack, MT.Steel).mRGBaSolid : new short[] {255, 255, 255, 255};
	  }
	  
		@Override
	  public String getDeathMessage()
	  {
	    return "[VICTIM] was strangled by [KILLER]";
	  }
		
		
}
