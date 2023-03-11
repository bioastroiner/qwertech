package com.kbi.qwertech;

import com.kbi.qwertech.api.data.QTI;
import com.kbi.qwertech.api.registry.ArmorUpgradeRegistry;
import com.kbi.qwertech.client.render.QT_Armor_Renderer;
import com.kbi.qwertech.client.render.QT_Machine_Renderer;
import com.kbi.qwertech.client.render.entity.projectile.RenderEntityBall;
import com.kbi.qwertech.client.render.entity.projectile.RenderEntityFoil;
import com.kbi.qwertech.client.render.entity.projectile.RenderEntityRock;
import com.kbi.qwertech.client.render.entity.projectile.RenderEntityShuriken;
import com.kbi.qwertech.entities.projectile.EntityBall;
import com.kbi.qwertech.entities.projectile.EntityFoil;
import com.kbi.qwertech.entities.projectile.EntityRock;
import com.kbi.qwertech.entities.projectile.EntityShuriken;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregapi.api.Abstract_Mod;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.data.LH;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public final class ClientProxy extends CommonProxy {


    @Override
    public void onProxyBeforePreInit(Abstract_Mod aMod, FMLPreInitializationEvent aEvent) {
    }

    @Override
    public void onProxyAfterInit(Abstract_Mod aMod, FMLInitializationEvent aEvent) {
        MinecraftForgeClient.registerItemRenderer(QTI.qwerArmor.getItem(), new QT_Armor_Renderer());
        MinecraftForgeClient.registerItemRenderer(MultiTileEntityRegistry.getRegistry("qwertech.machines").getItem(401).getItem(), new QT_Machine_Renderer());

        RenderingRegistry.registerEntityRenderingHandler(EntityFoil.class,
                new RenderEntityFoil());
        RenderingRegistry.registerEntityRenderingHandler(EntityBall.class,
                new RenderEntityBall());
        RenderingRegistry.registerEntityRenderingHandler(EntityRock.class,
                new RenderEntityRock());
        RenderingRegistry.registerEntityRenderingHandler(EntityShuriken.class,
                new RenderEntityShuriken());
    }


    @Override
    public void onProxyAfterPostInit(Abstract_Mod aMod, FMLPostInitializationEvent aEvent) {
        super.onProxyAfterPostInit(aMod, aEvent);
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (event.itemStack == null || event.entity == null || event.isCanceled()) return;
        if (ArmorUpgradeRegistry.instance.getUpgrade(event.itemStack) != null) {
            event.toolTip.add(LH.Chat.GOLD + "Can be used to upgrade armor");
        }
    }
}