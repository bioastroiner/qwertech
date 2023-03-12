package com.kbi.qwertech.loaders;

import codechicken.nei.recipe.CatalystInfo;
import codechicken.nei.recipe.RecipeCatalysts;
import com.kbi.qwertech.QwerTech;
import com.kbi.qwertech.api.armor.IArmorStats;
import com.kbi.qwertech.api.armor.MultiItemArmor;
import com.kbi.qwertech.api.armor.upgrades.IArmorUpgrade;
import com.kbi.qwertech.api.data.QTI;
import com.kbi.qwertech.api.registry.ArmorUpgradeRegistry;
import com.kbi.qwertech.armor.*;
import com.kbi.qwertech.armor.upgrades.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregapi.code.ICondition;
import gregapi.data.*;
import gregapi.item.multiitem.MultiItemRandom;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictMaterialCondition;
import gregapi.oredict.event.IOreDictListenerEvent;
import gregapi.recipes.Recipe;
import gregapi.util.CR;
import gregapi.util.ST;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent.SetArmorModel;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static gregapi.data.CS.*;
import static gregapi.data.CS.F;
import static gregapi.data.LH.Chat.GOLD;
import static gregapi.data.LH.Chat.GREEN;

public class RegisterArmor {
	
	private static MultiItemArmor armor;
	public static HashMap<String, Object> iconTitle = new HashMap();
	private static List<String> types = new ArrayList();
	public static RegisterArmor instance;

	public static final Recipe.RecipeMap RM_UPGRADE= new Recipe.RecipeMap(null,
			"qt.recipe.upgrade",
			"Upgrade Your Armors",
			null, 0,
			1,
			RES_PATH_GUI+"machines/Boxinator",/*IN-OUT-MIN-ITEM=*/ 2, 1, 0,
			/*IN-OUT-MIN-FLUID=*/ 0, 0, 0,/*MIN*/ 1,/*AMP=*/ 1,
			"",1, "" , F, T, F, F, F, F, F);


	public static MultiItemRandom qt_armor_upgrades;
	
	public RegisterArmor()
	{
		instance = this;
		MT.Bronze.addEnchantmentForArmors(Enchantment.protection, 1);
		MT.Steel.addEnchantmentForArmors(Enchantment.fireProtection, 1);
		MT.BlackBronze.addEnchantmentForArmors(Enchantment.protection, 2);
		MT.BlueSteel.addEnchantmentForArmors(Enchantment.blastProtection, 3);
		MT.ObsidianSteel.addEnchantmentForArmors(Enchantment.blastProtection, 2);
		MT.RedSteel.addEnchantmentForArmors(Enchantment.projectileProtection, 3);
		MT.BlackSteel.addEnchantmentForArmors(Enchantment.protection, 3);
		MT.BlackSteel.addEnchantmentForArmors(Enchantment.thorns, 1);
		MT.DamascusSteel.addEnchantmentForArmors(Enchantment.featherFalling, 2);
		MT.DamascusSteel.addEnchantmentForArmors(Enchantment.unbreaking, 1);
		MT.TungstenSteel.addEnchantmentForArmors(Enchantment.unbreaking, 2);
		MT.TungstenSteel.addEnchantmentForArmors(Enchantment.fireProtection, 3);
		MT.Ti.addEnchantmentForArmors(Enchantment.thorns, 2);
		MT.Ti.addEnchantmentForArmors(Enchantment.fireProtection, 3);
		MT.TungstenCarbide.addEnchantmentForArmors(Enchantment.fireProtection, 3);
		MT.TungstenCarbide.addEnchantmentForArmors(Enchantment.unbreaking, 2);
		MT.TungstenSintered.addEnchantmentForArmors(Enchantment.fireProtection, 3);
		MT.TungstenSintered.addEnchantmentForArmors(Enchantment.unbreaking, 3);
		MT.Ag.addEnchantmentForArmors(Enchantment.aquaAffinity, 1);
		MT.Ag.addEnchantmentForArmors(Enchantment.respiration, 1);
		MT.Cr.addEnchantmentForArmors(Enchantment.fireProtection, 2);
		MT.SteelGalvanized.addEnchantmentForArmors(Enchantment.fireProtection, 2);
		MT.SteelGalvanized.addEnchantmentForArmors(Enchantment.blastProtection, 1);
		MT.StainlessSteel.addEnchantmentForArmors(Enchantment.fireProtection, 3);
		MT.StainlessSteel.addEnchantmentForArmors(Enchantment.blastProtection, 2);
		MT.Thaumium.addEnchantmentForArmors(Enchantment.aquaAffinity, 2);
		MT.Manasteel.addEnchantmentForArmors(Enchantment.aquaAffinity, 2);
		MT.IronWood.addEnchantmentForArmors(Enchantment.respiration, 1);

		addType("helmet/tcgoggles");
		addType("chainmail");
		addType("plate");
		addType("boots/springs");
		addType("lube");
		addType("slime");
		addType("curvedPlate");
		addType("helmet/monocle");
		addType("boots/spur");
		addType("helmet/feather");
		
		try
		{
			registerIcons();
		} catch (Throwable t)
		{
			//Just serverside probably
		}
		
		armor = new MultiItemArmor(MD.QT.mID, "qt.armor");
		QTI.qwerArmor.set(armor);
		armor.addArmor(0, "Chain Helmet", "Rattles worse than bones", new HelmetBase().setMaterialAmount(CS.U * 5), "armorHelmet");
		armor.addArmor(1, "Chainmail Shirt", "Thy mother was a hamster!", new ChestBase().setMaterialAmount(CS.U * 9), "armorChest");
		armor.addArmor(2, "Chainmail Leggings", "Thy father smelt of elderberries!", new PantBase().setMaterialAmount(CS.U * 8), "armorLegs");
		armor.addArmor(3, "Chain Boots", "Linked to the Past", new BootBase().setMaterialAmount(CS.U * 4), "armorBoots");
		armor.addArmor(4, "Plated Helmet", "Always wear your helmet", new HelmetPlate().setMaterialAmount(CS.U * 8), "armorHelmet");
		armor.addArmor(5, "Chestplate", "The classic look", new ChestPlate().setMaterialAmount(CS.U * 14), "armorChest");
		armor.addArmor(6, "Plated Pants", "Not to be confused with pleated pants", new PantPlate().setMaterialAmount(CS.U * 14), "armorLegs");
		armor.addArmor(7, "Plated Boots", "Clanky and clunky", new BootPlate().setMaterialAmount(CS.U * 6), "armorBoots");

		OP.chain.addListener(e -> { if(new ICondition.And(MT.Wood.NOT, OreDictMaterialCondition.typemin(2)).isTrue(e.mMaterial)){
		CR.shaped(RegisterArmor.armor.getArmorWithStats(0, e.mMaterial),CR.DEF,"CCC","ChC","   ",'C',OP.chain.mat(e.mMaterial,1),'D',OP.plateDouble.mat(e.mMaterial,1));
		CR.shaped( RegisterArmor.armor.getArmorWithStats(1, e.mMaterial),CR.DEF,"ChC","CCC","CCC",'C',OP.chain.mat(e.mMaterial,1),'D',OP.plateDouble.mat(e.mMaterial,1));
		CR.shaped( RegisterArmor.armor.getArmorWithStats(2, e.mMaterial),CR.DEF,"CCC","ChC","C C",'C',OP.chain.mat(e.mMaterial,1),'D',OP.plateDouble.mat(e.mMaterial,1));
		CR.shaped( RegisterArmor.armor.getArmorWithStats(3, e.mMaterial),CR.DEF,"   ","ChC","C C",'C',OP.chain.mat(e.mMaterial,1),'D',OP.plateDouble.mat(e.mMaterial,1));
		}});

		OP.plateCurved.addListener(e -> {if(new ICondition.And(MT.Wood.NOT, OreDictMaterialCondition.typemin(2)).isTrue(e.mMaterial)){
			CR.shaped(RegisterArmor.armor.getArmorWithStats(4, e.mMaterial),CR.DEF,"DDD","ChC","   ",'C',OP.plateCurved.mat(e.mMaterial,1),'D',OP.plateDouble.mat(e.mMaterial,1));
			CR.shaped( RegisterArmor.armor.getArmorWithStats(5, e.mMaterial),CR.DEF,"DhD","DDD","CDC",'C',OP.plateCurved.mat(e.mMaterial,1),'D',OP.plateDouble.mat(e.mMaterial,1));
			CR.shaped( RegisterArmor.armor.getArmorWithStats(6, e.mMaterial),CR.DEF,"DDD","DhD","D D",'C',OP.plateCurved.mat(e.mMaterial,1),'D',OP.plateDouble.mat(e.mMaterial,1));
			CR.shaped( RegisterArmor.armor.getArmorWithStats(7, e.mMaterial),CR.DEF,"   ","DhD","C C",'C',OP.plateCurved.mat(e.mMaterial,1),'D',OP.plateDouble.mat(e.mMaterial,1));
		}});

//		armor.addArmor(8, "Galoshes", "", new BootWet().setMaterialAmount(CS.U * 4), "armorBoots");
//		armor.addArmor(9, "Rainhat", "", new HelmetWet().setMaterialAmount(CS.U * 5), "armorHelmet");
//		armor.addArmor(10, "Raincoat", "", new ChestWet().setMaterialAmount(CS.U * 8), "armorChest");
		
		//addUpgrades();
		
		//FMLCommonHandler.instance().bus().register(this);

		//RM.DidYouKnow.addFakeRecipe(true,new ItemStack[]{},new ItemStack[]{});
	}
	
	public void addUpgrades()
	{
		IArmorUpgrade upgrade1 = new Upgrade_SpringBoots(MT.Steel);
		upgrade1.setMaterialAmount(OP.springSmall.mAmount * 2);
		upgrade1.addUpgradeStack(ST.make(qt_armor_upgrades,1, 0));
		IArmorUpgrade upgrade2 = new Upgrade_SpringBoots(MT.StainlessSteel);
		upgrade2.setMaterialAmount(OP.springSmall.mAmount * 2);
		upgrade2.addUpgradeStack(ST.make(qt_armor_upgrades, 1, 1));
		IArmorUpgrade upgrade3 = new Upgrade_SpringBoots(MT.Brass);
		upgrade3.setMaterialAmount(OP.springSmall.mAmount * 2);
		upgrade3.addUpgradeStack(ST.make(qt_armor_upgrades,1, 2));
		IArmorUpgrade upgrade4 = new Upgrade_SpringBoots(MT.Al);
		upgrade4.setMaterialAmount(OP.springSmall.mAmount * 2);
		upgrade4.addUpgradeStack(ST.make(qt_armor_upgrades, 1, 3));
		IArmorUpgrade upgrade5 = new Upgrade_SpringBoots(MT.Thaumium);
		upgrade5.setMaterialAmount(OP.springSmall.mAmount * 2);
		upgrade5.addUpgradeStack(ST.make(qt_armor_upgrades, 1, 4));

		ArmorUpgradeRegistry.instance.addUpgrade(0, upgrade1);
		ArmorUpgradeRegistry.instance.addUpgrade(1, upgrade2);
		ArmorUpgradeRegistry.instance.addUpgrade(2, upgrade3);
		ArmorUpgradeRegistry.instance.addUpgrade(3, upgrade4);
		ArmorUpgradeRegistry.instance.addUpgrade(4, upgrade5);

		IArmorUpgrade upgrade = new Upgrade_Lubricant();
		upgrade.addUpgradeStack(IL.Bottle_Lubricant.get(1));
		ArmorUpgradeRegistry.instance.addUpgrade(5, upgrade);
		
		upgrade = new Upgrade_Slime();
		upgrade.addUpgradeStack(ST.make(Items.slime_ball, 1, 0));
		ArmorUpgradeRegistry.instance.addUpgrade(6, upgrade);
		
		upgrade = new Upgrade_Plate().setEnchantment(Enchantment.protection);
		upgrade.setMaterial(MT.Bronze);
		upgrade.addUpgradeStack(OP.plateCurved.mat(MT.Bronze, 1));
		ArmorUpgradeRegistry.instance.addUpgrade(7, upgrade);
		
		upgrade = new Upgrade_Plate().setEnchantment(Enchantment.protection);
		upgrade.setMaterial(MT.Steel);
		upgrade.addUpgradeStack(OP.plateCurved.mat(MT.Steel, 1));
		ArmorUpgradeRegistry.instance.addUpgrade(8, upgrade);
		
		upgrade = new Upgrade_Plate().setEnchantment(Enchantment.protection);
		upgrade.setMaterial(MT.BlackSteel);
		upgrade.addUpgradeStack(OP.plateCurved.mat(MT.BlackSteel, 1));
		ArmorUpgradeRegistry.instance.addUpgrade(9, upgrade);
		
		upgrade = new Upgrade_Plate().setEnchantment(Enchantment.blastProtection);
		upgrade.setMaterial(MT.ObsidianSteel);
		upgrade.addUpgradeStack(OP.plateCurved.mat(MT.ObsidianSteel, 1));
		ArmorUpgradeRegistry.instance.addUpgrade(10, upgrade);
		
		upgrade = new Upgrade_Plate().setEnchantment(Enchantment.blastProtection);
		upgrade.setMaterial(MT.BlueSteel);
		upgrade.addUpgradeStack(OP.plateCurved.mat(MT.BlueSteel, 1));
		ArmorUpgradeRegistry.instance.addUpgrade(11, upgrade);
		
		upgrade = new Upgrade_Plate().setEnchantment(Enchantment.blastProtection);
		upgrade.setMaterial(MT.SteelGalvanized);
		upgrade.addUpgradeStack(OP.plateCurved.mat(MT.SteelGalvanized, 1));
		ArmorUpgradeRegistry.instance.addUpgrade(12, upgrade);
		
		upgrade = new Upgrade_Plate().setEnchantment(Enchantment.fireProtection);
		upgrade.setMaterial(MT.StainlessSteel);
		upgrade.addUpgradeStack(OP.plateCurved.mat(MT.StainlessSteel, 1));
		ArmorUpgradeRegistry.instance.addUpgrade(13, upgrade);
		
		upgrade = new Upgrade_Plate().setEnchantment(Enchantment.fireProtection);
		upgrade.setMaterial(MT.TungstenSteel);
		upgrade.addUpgradeStack(OP.plateCurved.mat(MT.TungstenSteel, 1));
		ArmorUpgradeRegistry.instance.addUpgrade(14, upgrade);
		
		upgrade = new Upgrade_Plate().setEnchantment(Enchantment.fireProtection);
		upgrade.setMaterial(MT.Ti);
		upgrade.addUpgradeStack(OP.plateCurved.mat(MT.Ti, 1));
		ArmorUpgradeRegistry.instance.addUpgrade(15, upgrade);
		
		upgrade = new Upgrade_Shuriken();
		List<ItemStack> shurikens = OreDictionary.getOres("shurikenAnyIronOrSteel");
		for (ItemStack item : shurikens)
		{
			upgrade.addUpgradeStack(item);
		}
		ArmorUpgradeRegistry.instance.addUpgrade(16, upgrade);
		
		upgrade = new Upgrade_Magnifier();
		upgrade.addUpgradeStack(ST.make(CS.ToolsGT.sMetaTool, 1, 62));// Magnifying Glass
		upgrade.addUpgradeStack(OP.lens.mat(MT.Glass,1));
		upgrade.addUpgradeStack(OP.lens.mat(MT.Sapphire,1));
		upgrade.addUpgradeStack(OP.lens.mat(MT.Ruby,1));
		upgrade.addUpgradeStack(OP.lens.mat(MT.Diamond,1));
		upgrade.addUpgradeStack(OP.lens.mat(MT.Amethyst,1));
		ArmorUpgradeRegistry.instance.addUpgrade(17, upgrade);
		
		upgrade = new Upgrade_Feather();
		upgrade.addUpgradeStack(ST.make(Items.feather, 1, 0));
		ArmorUpgradeRegistry.instance.addUpgrade(18, upgrade);
		
		upgrade = new Upgrade_Feather();
		upgrade.addUpgradeStack(QTI.turkeyFeather.get(1));
		((Upgrade_Feather)upgrade).setRGBa(MT.WoodPolished.mRGBaSolid);
		ArmorUpgradeRegistry.instance.addUpgrade(19, upgrade);

		upgrade = new Upgrade_ThaumicGoggles();
		Item thaumicGoggles = GameRegistry.findItem(MD.TC.mName, "ItemGoggles");
		if (thaumicGoggles != null)
		{
			upgrade.addUpgradeStack(ST.make(thaumicGoggles, 1, CS.W));
		}
		ArmorUpgradeRegistry.instance.addUpgrade(20, upgrade);

		//for (OreDictMaterial mat:MultiItemArmor.example_materials)
		for (OreDictMaterial mat:new OreDictMaterial[]{MT.NULL})
			armor.mArmorStats.forEach((id,stats)->{
				ItemStack armorStack = armor.getArmorWithStats(id,mat);
				for(ItemStack upgradeStack:ArmorUpgradeRegistry.upgradeItems.keySet()){
					if(ArmorUpgradeRegistry.instance.getUpgrade(upgradeStack).isCompatibleWith(armorStack)) {
						ItemStack upgradedArmorStack = MultiItemArmor.addUpgrade(armorStack.copy(), ArmorUpgradeRegistry.instance.getUpgradeID(upgradeStack));
						RM_UPGRADE.addFakeRecipe(false,
								ST.array(armorStack.copy().setStackDisplayName(GOLD+"Any Material"),upgradeStack),
								ST.array(upgradedArmorStack.copy().setStackDisplayName(GREEN+"You Can Have Multiple Upgrades!")),
								null,null,null,null,0,0,0);
						//RM.DidYouKnow.addFakeRecipe(false,new ItemStack[]{upgrade_},new ItemStack[]{null},null,null,null,null,0,0,0);
					}
				}
			});
		for (int i = 0; i < QwerTech.armor_upgrade_desk.mLastRegisteredID+1; i++) {
			// Upgrade Desks
			RM_UPGRADE.mRecipeMachineList.add(QwerTech.armor_upgrade_desk.getItem(i).setStackDisplayName(GOLD+LH.Chat.ITALIC+"Right Click with Armor Then Apply an Upgrade"));
			RecipeCatalysts.addRecipeCatalyst(RM_UPGRADE.mNameNEI,new CatalystInfo(QwerTech.armor_upgrade_desk.getItem(i),i));
		}
	}
	
	private void addType(String type)
	{
		types.add(type);
	}
	
	private void registerIcons()
	{
		iconTitle.put("qwertech:kazoo", new ArmorIcon("qwertech:kazoo"));
		for (String type : types)
		{
			if (type.contains("/"))
			{
				iconTitle.put("qwertech:armor/" + type, new ArmorIcon("qwertech:armor/" + type));
			} else {
				iconTitle.put("qwertech:armor/helmet/" + type, new ArmorIcon("qwertech:armor/helmet/" + type));
				iconTitle.put("qwertech:armor/chestplate/" + type, new ArmorIcon("qwertech:armor/chestplate/" + type));
				iconTitle.put("qwertech:armor/leggings/" + type, new ArmorIcon("qwertech:armor/leggings/" + type));
				iconTitle.put("qwertech:armor/boots/" + type, new ArmorIcon("qwertech:armor/boots/" + type));
			}
		}
		/*iconTitle.put("weightNone", new ArmorIcon("qwertech:armorui/armor/weightNone"));
		iconTitle.put("weightLittle", new ArmorIcon("qwertech:armorui/armor/weightLittle"));
		iconTitle.put("weightLight", new ArmorIcon("qwertech:armorui/armor/weightLight"));
		iconTitle.put("weightMuch", new ArmorIcon("qwertech:armorui/armor/weightMuch"));
		iconTitle.put("weightSignificant", new ArmorIcon("qwertech:armorui/armor/weightSignificant"));
		iconTitle.put("noShield", new ArmorIcon("qwertech:armorui/armor/noShield"));
		iconTitle.put("woodShield", new ArmorIcon("qwertech:armorui/armor/woodShield"));
		iconTitle.put("bronzeShield", new ArmorIcon("qwertech:armorui/armor/bronzeShield"));
		iconTitle.put("blueMetalShield", new ArmorIcon("qwertech:armorui/armor/blueMetalShield"));
		iconTitle.put("purpleShield", new ArmorIcon("qwertech:armorui/armor/purpleShield"));
		iconTitle.put("brokenWoodShield", new ArmorIcon("qwertech:armorui/armor/brokenWoodShield"));
		iconTitle.put("brokenBronzeShield", new ArmorIcon("qwertech:armorui/armor/brokenBronzeShield"));
		iconTitle.put("brokenBlueMetalShield", new ArmorIcon("qwertech:armorui/armor/brokenBlueMetalShield"));
		iconTitle.put("brokenPurpleShield", new ArmorIcon("qwertech:armorui/armor/brokenPurpleShield"));*/
	}
	
	public HashMap<EntityLivingBase, Double> entities = new HashMap();
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void setArmorModel(SetArmorModel event)
	{
		if (event.stack != null && event.stack.getItem() instanceof MultiItemArmor)
		{
			/*
			 * Draw our own upgrades, with blackjack and cookers 
			 */
			IArmorUpgrade[] upgrades = MultiItemArmor.getUpgrades(event.stack);
			GL11.glPushMatrix();
			GL11.glColor4f(1F, 1F, 1F, 1F);
			for (int q = 0; q < upgrades.length; q++)
			{
				IArmorUpgrade upgrade = upgrades[q];
				if (upgrade != null)
				{
					ModelBiped drawIt = upgrade.getArmorModel(event.stack, event.entityLiving, 3 - event.slot);
					if (drawIt != null)
					{
						//System.out.println("GOTIT");
						drawIt.onGround = event.entityLiving.getSwingProgress(event.partialRenderTick);
						drawIt.isRiding = event.entity.isRiding();
						drawIt.isChild = event.entityLiving.isChild();
						short[] j = upgrade.getRGBa(event.stack, 0);
						//UPDATE THIS PART LATER D00D
						
						
						if (j != null && j.length == 4)
						{
							GL11.glColor4f(((float)j[0])/255F, ((float)j[1])/255F, ((float)j[2])/255F, ((float)j[3])/255F);
						}
						String texture = upgrade.getArmorTexture(event.stack, event.entityLiving, event.slot, null);
						if (texture != null) {
							RenderManager.instance.renderEngine.bindTexture(new ResourceLocation(texture));
							drawIt.setLivingAnimations(event.entityLiving, event.entityLiving.limbSwing - event.entityLiving.limbSwingAmount, event.entityLiving.prevLimbSwingAmount + (event.entityLiving.limbSwingAmount - event.entityLiving.prevLimbSwingAmount), event.partialRenderTick);
							drawIt.render(event.entityLiving, event.entityLiving.limbSwing - event.entityLiving.limbSwingAmount, event.entityLiving.prevLimbSwingAmount + (event.entityLiving.limbSwingAmount - event.entityLiving.prevLimbSwingAmount), event.entityLiving.ticksExisted, event.entityLiving.rotationYawHead - event.entityLiving.renderYawOffset, event.entityLiving.prevRotationPitch + (event.entityLiving.rotationPitch - event.entityLiving.prevRotationPitch), 0.0625F);
						}
						GL11.glColor4f(1F, 1F, 1F, 1F);
					}
				}
			}
			GL11.glPopMatrix();
		}
	}
	
	public void onClickedWearingArmor(PlayerInteractEvent event)
	{
		for (int q = 1; q < 5; q++)
		{
			ItemStack armor = event.entityPlayer.getEquipmentInSlot(q);
			if (armor != null && armor.getItem() instanceof MultiItemArmor)
			{
				((MultiItemArmor)armor.getItem()).onClickedWearing(armor, q, event.world, event.entityPlayer, event.action, event.x, event.y, event.z, event.face, event);
			}
		}
	}
	
	//@SubscribeEvent
	public void updateEntityArmor(LivingUpdateEvent event)
	{
		try {
			EntityLivingBase entity = event.entityLiving;
			for (int q = 1; q < 5; q++) {
				ItemStack stack = entity.getEquipmentInSlot(q);
				if (stack != null && stack.getItem() instanceof MultiItemArmor) {
					((MultiItemArmor) stack.getItem()).onArmorTicked(entity.worldObj, entity, stack);
				}
			}
			if (event.entity.worldObj.getWorldTime() % 100 == 0) {
				if (entities.containsKey(entity)) {
					entities.clear();
				}
				double totalWeight = 0;
				for (int q = 1; q < 5; q++) {
					ItemStack stack = entity.getEquipmentInSlot(q);
					if (stack != null && stack.getItem() instanceof MultiItemArmor) {
						IArmorStats tStats = ((MultiItemArmor) stack.getItem()).getArmorStats(stack);
						if (tStats != null) {
							OreDictMaterial mat = MultiItemArmor.getPrimaryMaterial(stack);
							totalWeight = totalWeight + mat.getWeight(tStats.getMaterialAmount() / 500);
						}
					}
				}
				if (totalWeight > 0 && !(entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode)) {
					//System.out.println("Adding weight " + totalWeight);
					entities.put(entity, totalWeight);
				}
			} else {
				if (entities.containsKey(entity)) {
					double weightToApply = entities.get(entity);
					if (!entity.onGround) {
						weightToApply = weightToApply * 0.5;
					}
					double speedEffect = 1;
					double jumpEffect = 0;
					if (weightToApply < 20) {
						//do nothing, you're good
					} else if (weightToApply < 30) {
						speedEffect = 0.9;
					} else if (weightToApply < 50) {
						speedEffect = 0.8;
					} else if (weightToApply < 70) {
						speedEffect = 0.7;
						jumpEffect = -0.01;
					} else if (weightToApply < 90) {
						speedEffect = 0.5;
						jumpEffect = -0.02;
					} else if (weightToApply < 150) {
						speedEffect = 0.3;
						jumpEffect = -0.03;
					} else {
						speedEffect = 0.1;
						jumpEffect = -0.05;
					}
					if (entity.motionY < 0) {
						entity.motionY = entity.motionY + jumpEffect;
					}
					entity.motionX = entity.motionX * speedEffect;
					entity.motionZ = entity.motionZ * speedEffect;
				}
			}
		} catch (Exception e) {
			System.out.println("Error while calculating or applying armor weight for entity");
			e.printStackTrace();
		}
	}
}
