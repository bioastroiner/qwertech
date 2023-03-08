package com.kbi.qwertech;

import com.kbi.qwertech.api.data.QTI;
import com.kbi.qwertech.loaders.RegisterArmor;
import com.kbi.qwertech.loaders.RegisterMobs;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregapi.api.Abstract_Mod;
import gregapi.api.Abstract_Proxy;
import gregapi.data.CS;
import gregapi.data.OP;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;
import gregapi.util.UT;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.brewing.PotionBrewEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.minecart.MinecartCollisionEvent;
import net.minecraftforge.event.entity.minecart.MinecartEvent;
import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.terraingen.*;
import net.minecraftforge.event.world.*;


/**
 * @author Max Mustermann
 * <p>
 * An example implementation for a Common Proxy using my System.
 */
public class CommonProxy extends Abstract_Proxy {
    // Insert your common implementation of Stuff here
    public static int wallRenderID;

    @Override
    public void onProxyBeforePreInit(Abstract_Mod aMod, FMLPreInitializationEvent aEvent) {/**/}

    @Override
    public void onProxyBeforeInit(Abstract_Mod aMod, FMLInitializationEvent aEvent) {/**/}

    @Override
    public void onProxyBeforePostInit(Abstract_Mod aMod, FMLPostInitializationEvent aEvent) {/**/}

    @Override
    public void onProxyBeforeServerStarting(Abstract_Mod aMod, FMLServerStartingEvent aEvent) {/**/}

    @Override
    public void onProxyBeforeServerStarted(Abstract_Mod aMod, FMLServerStartedEvent aEvent) {/**/}

    @Override
    public void onProxyBeforeServerStopping(Abstract_Mod aMod, FMLServerStoppingEvent aEvent) {/**/}

    @Override
    public void onProxyBeforeServerStopped(Abstract_Mod aMod, FMLServerStoppedEvent aEvent) {/**/}

    @Override
    public void onProxyAfterPreInit(Abstract_Mod aMod, FMLPreInitializationEvent aEvent) {/**/}

    @Override
    public void onProxyAfterInit(Abstract_Mod aMod, FMLInitializationEvent aEvent) {/**/}

    @Override
    public void onProxyAfterPostInit(Abstract_Mod aMod, FMLPostInitializationEvent aEvent) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        //this allows it to read game events for other classes without having to be added unnecessarily to all the game building events.
    }

    @Override
    public void onProxyAfterServerStarting(Abstract_Mod aMod, FMLServerStartingEvent aEvent) {/**/}

    @Override
    public void onProxyAfterServerStarted(Abstract_Mod aMod, FMLServerStartedEvent aEvent) {/**/}

    @Override
    public void onProxyAfterServerStopping(Abstract_Mod aMod, FMLServerStoppingEvent aEvent) {/**/}

    @Override
    public void onProxyAfterServerStopped(Abstract_Mod aMod, FMLServerStoppedEvent aEvent) {/**/}

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event) {
        RegisterMobs.instance.onAdded(event);
    }

    @SubscribeEvent
    public void livingDrops(LivingDropsEvent event) {
        RegisterMobs.instance.onDrop(event);
    }

    @SubscribeEvent
    public void livingUpdated(LivingEvent.LivingUpdateEvent event) {
        RegisterArmor.instance.updateEntityArmor(event);
    }

    @SubscribeEvent
    public void livingHurt(LivingHurtEvent event) {
        RegisterMobs.instance.onLivingHurt(event);
    }

    @SubscribeEvent
    public void checkSpawn(LivingSpawnEvent.CheckSpawn event) {
        RegisterMobs.instance.checkSpawn(event);
    }

    @SubscribeEvent
    public void specialSpawn(LivingSpawnEvent.SpecialSpawn event) {
        RegisterMobs.instance.specialSpawn(event);
    }

    @SubscribeEvent
    public void onPlayerEntityInteraction(EntityInteractEvent event) {
        RegisterMobs.instance.onInteracted(event);
    }

    @SubscribeEvent
    public void onPlayerWorldInteraction(PlayerInteractEvent event) {
        RegisterArmor.instance.onClickedWearingArmor(event);
    }

    @SubscribeEvent
    public void onPlayerItemUseFinish(PlayerUseItemEvent.Finish event) {
        if (event.item != null && event.item.getItem() == CS.ToolsGT.sMetaTool && event.item.getItemDamage() == CS.ToolsGT.WRENCH) {
            if (event.entityPlayer != null) {
                int previousSlot = event.entityPlayer.inventory.currentItem - 1;
                if (previousSlot > -1) {
                    ItemStack previous = event.entityPlayer.inventory.getStackInSlot(previousSlot);
                    if (previous != null) {
                        if (previous.getItem() instanceof MultiItemTool) {
                            if (MultiItemTool.getToolDamage(previous) == 0) {
                                OreDictMaterial mat = MultiItemTool.getPrimaryMaterial(previous);
                                ItemStack stack = previous;
                                if (previous.getItem() == CS.ToolsGT.sMetaTool) {
                                    switch (previous.getItemDamage()) {
                                        case CS.ToolsGT.SWORD:
                                            stack = OP.toolHeadSword.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.CHISEL:
                                            stack = OP.toolHeadChisel.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.FILE:
                                            stack = OP.toolHeadFile.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.HARDHAMMER:
                                        case CS.ToolsGT.SOFTHAMMER:
                                            stack = OP.toolHeadHammer.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.PICKAXE:
                                            stack = OP.toolHeadPickaxe.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.GEM_PICK:
                                            stack = OP.toolHeadPickaxeGem.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.CONSTRUCTION_PICK:
                                            stack = OP.toolHeadConstructionPickaxe.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.AXE:
                                            stack = OP.toolHeadAxe.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.DOUBLE_AXE:
                                            stack = OP.toolHeadAxeDouble.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.BUTCHERYKNIFE:
                                            stack = OP.plate.mat(mat, 4);
                                            break;
                                        case CS.ToolsGT.HOE:
                                            stack = OP.toolHeadHoe.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.PLOW:
                                            stack = OP.toolHeadPlow.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.SAW:
                                            stack = OP.toolHeadSaw.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.SCREWDRIVER:
                                            stack = OP.toolHeadScrewdriver.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.SENSE:
                                            stack = OP.toolHeadSense.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.SHOVEL:
                                            stack = OP.toolHeadShovel.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.SPADE:
                                            stack = OP.toolHeadSpade.mat(mat, 1);
                                            break;
                                        case CS.ToolsGT.UNIVERSALSPADE:
                                            stack = OP.toolHeadUniversalSpade.mat(mat, 1);
                                            break;
                                        default:
                                            return;
                                    }
                                } else if (previous.getItem() == QTI.qwerTool.getItem()) {
                                    switch (previous.getItemDamage()) {
                                        case 0:
                                            stack = OreDictPrefix.get("toolHeadMattock").mat(mat, 1);
                                            break;
                                        case 6:
                                            stack = OreDictPrefix.get("toolHeadMace").mat(mat, 1);
                                            break;
                                        default:
                                            return;
                                    }
                                }
                                if (stack != previous) {
                                    CS.ToolsGT.sMetaTool.doDamage(event.item, 20);
                                    UT.Sounds.send(event.entityPlayer.worldObj, "qwertech:metal.slide", 0.5F, (CS.RNGSUS.nextInt(5) + 8) / 10F, (int) event.entityPlayer.posX, (int) event.entityPlayer.posY, (int) event.entityPlayer.posZ);
                                    event.entityPlayer.inventory.setInventorySlotContents(previousSlot, stack);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}