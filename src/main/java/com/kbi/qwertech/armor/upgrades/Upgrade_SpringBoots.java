package com.kbi.qwertech.armor.upgrades;

import com.kbi.qwertech.QwerTech;
import com.kbi.qwertech.api.armor.MultiItemArmor;
import com.kbi.qwertech.api.armor.upgrades.UpgradeBase;
import com.kbi.qwertech.client.models.armor.ModelArmorSpring;
import com.kbi.qwertech.loaders.RegisterArmor;
import cpw.mods.fml.common.eventhandler.Event.Result;
import gregapi.data.CS;
import gregapi.data.LH;
import gregapi.data.MT;
import gregapi.oredict.OreDictMaterial;
import gregapi.render.IIconContainer;
import gregapi.util.UT;
import li.cil.oc.integration.gregtech.ModGregtech;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import javax.annotation.Nullable;
import java.util.List;

import static gregapi.data.CS.OUT;

public class Upgrade_SpringBoots extends UpgradeBase {

	public Upgrade_SpringBoots(OreDictMaterial mat) {
		setMaterial(mat);
	}
	
	@Override
	public boolean isValidInSlot(int slot)
	{
		return slot == 3;
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
	{
		return type != null ? "qwertech:textures/armor/blank.png" : "qwertech:textures/armor/upgrade/spring.png";
	}
	
	Object model;
	
	@Override
	public ModelBiped getArmorModel(ItemStack stack, EntityLivingBase entity, int slot)
	{
		if (slot != 3) return null;
		if (model == null)
		{
			model = new ModelArmorSpring();
		}
		return (ModelBiped)model;
	}
	
	@Override
	public boolean onFallingDamage(World world, EntityLivingBase entity, ItemStack stack, DamageSource source, float amount, LivingHurtEvent event)
	{
		// TODO it still hurts the plaer even tough no damage is taken
		event.ammount=0; // nullify fall damage
		event.setCanceled(true);
		return false;
	}
	
	@Override
	public int getRenderPasses() {
		return 1;
	}
	
	@Override
	public IIcon getIcon(ItemStack aStack, int aRenderPass)
	{
		return ((IIconContainer)RegisterArmor.iconTitle.get("qwertech:armor/boots/springs")).getIcon(aRenderPass);
	}
	
	@Override
	public short[] getRGBa(ItemStack aStack, int aRenderPass)
	{
		return aRenderPass == 0 ? this.getMaterial().mRGBaSolid : MT.Empty.mRGBaSolid;
	}
	boolean fell = false;
	float dist_fell=0;
	@Override
	public void onArmorTick(World world, EntityLivingBase player, ItemStack stack)
	{
		if (!player.isPotionActive(Potion.jump)) {player.addPotionEffect(new PotionEffect(Potion.jump.id, 100, this.getMaterial().mToolQuality, true));}
		if (player.onGround)
		{
			if (fell && !player.isSneaking())
			{
				if (player instanceof EntityPlayer)
				{
					//((EntityPlayer)player).jump();
					if(dist_fell==0){
						//((EntityPlayer)player).jump();
					} else {
						if(dist_fell*0.15f>1.2) player.motionY=1.2; else
						player.motionY = dist_fell*0.15f;
					}
				} else {
					player.setJumping(true);
				}
				UT.Sounds.send(world, "qwertech:armor.upgrade.spring", 0.4F, 1 + CS.RNGSUS.nextFloat(), player);
				fell=false;
				((MultiItemArmor)stack.getItem()).doDamage(stack, CS.RNGSUS.nextInt(10));
			}
		} else {fell=false;}
	}

	@Override
	public void onEntityJump(EntityPlayer player, ItemStack stack) {
		// TODO: dosent work properly, is called even when not jumping
	}

	@Override
	public void onEntityFall(EntityPlayer player, float distance, LivingFallEvent event, ItemStack stack) {
		dist_fell=0;
		World world = player.worldObj;
//		UT.Entities.sendchat(player,"Distance: " + distance);
//		UT.Entities.sendchat(player,"Motion: " + player.motionY);
		if(distance > 0.35f &&player.onGround/*&&!player.isSprinting()*/){
			fell = true;
			dist_fell=distance;
			//UT.Entities.sendchat(player,"New Motion: " + player.motionY);
			UT.Sounds.send(world, "qwertech:armor.upgrade.spring", 0.4F, 1 + CS.RNGSUS.nextFloat(), player);
			event.setCanceled(true);
		}
	}

	@Override
	public List<String> getAdditionalToolTips(List<String> aList, ItemStack aStack)
	{
		aList.add(LH.Chat.ITALIC + LH.Chat.CYAN + "Applies Jump Boost, Nullifies FallDamage and Bounces you back up");
		return aList;
	}

}
