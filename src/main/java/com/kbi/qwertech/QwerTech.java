package com.kbi.qwertech;

import com.kbi.qwertech.api.data.QTI;
import com.kbi.qwertech.api.recipe.AnyQTTool;
import com.kbi.qwertech.api.recipe.RepairRecipe;
import com.kbi.qwertech.api.recipe.listeners.OreProcessing_NonCrafting;
import com.kbi.qwertech.api.recipe.listeners.OreProcessing_QTTool;
import com.kbi.qwertech.api.registry.ArmorUpgradeRegistry;
import com.kbi.qwertech.client.render.QT_GUIHandler;
import com.kbi.qwertech.entities.projectile.EntityShuriken;
import com.kbi.qwertech.items.MultiItemTool_QT;
import com.kbi.qwertech.items.behavior.Dispenser_Shuriken;
import com.kbi.qwertech.items.stats.*;
import com.kbi.qwertech.loaders.RegisterArmor;
import com.kbi.qwertech.loaders.RegisterLoot;
import com.kbi.qwertech.network.packets.PacketInventorySync;
import com.kbi.qwertech.tileentities.UpgradeDesk;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.api.Abstract_Mod;
import gregapi.api.Abstract_Proxy;
import gregapi.block.multitileentity.MultiTileEntityBlock;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.code.ICondition.And;
import gregapi.code.ModData;
import gregapi.config.ConfigCategories;
import gregapi.data.*;
import gregapi.data.CS.ModIDs;
import gregapi.item.multiitem.MultiItemRandom;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.item.prefixitem.PrefixItem;
import gregapi.network.NetworkHandler;
import gregapi.oredict.*;
import gregapi.recipes.AdvancedCraftingTool;
import gregapi.util.CR;
import gregapi.util.ST;
import gregapi.util.UT;
import gregtech.loaders.b.Loader_OreProcessing;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;

import java.util.HashMap;
import java.util.List;

import static gregapi.data.TD.Prefix.*;
import static gregapi.data.TD.Properties.HAS_TOOL_STATS;
import static gregapi.oredict.OreDictMaterialCondition.typemin;

@InterfaceList(value = {
        @Interface(iface = "squeek.applecore.api.food.IEdible", modid = ModIDs.APC)
        , @Interface(iface = "ic2.api.item.IItemReactorPlanStorage", modid = ModIDs.IC2C)
        , @Interface(iface = "ic2.api.item.ISpecialElectricItem", modid = ModIDs.IC2)
        , @Interface(iface = "ic2.api.item.IElectricItemManager", modid = ModIDs.IC2)
        , @Interface(iface = "micdoodle8.mods.galacticraft.api.item.IItemElectric", modid = ModIDs.GC)
})
@Mod(modid = QwerTech.MODID, name = QwerTech.MODNAME, version = QwerTech.VERSION, dependencies = "required-after:gregapi_post; after:gregtech")
public final class QwerTech extends Abstract_Mod {
    public static final String MODID = "qwertech";
    public static final String MODNAME = "QwerTech";
    public static final String VERSION = "1.0.0-a.50";
    public static ModData MOD_DATA = new ModData(MODID, MODNAME);
    public static QwerTech instance;

    @SidedProxy(modId = MODID, clientSide = "com.kbi.qwertech.ClientProxy", serverSide = "com.kbi.qwertech.ServerProxy")
    public static CommonProxy PROXY;
    public static HashMap<String, ItemStack> food;
    public static NBTTagCompound NullBT = null;
    public static MultiTileEntityRegistry machines;
    public static MultiTileEntityBlock metal;
    public static MultiTileEntityBlock wood;
    public static MultiTileEntityBlock air;
    private static MultiItemTool qwerTool;

    // Do not change these 7 Functions. Just keep them this way.
    @Mod.EventHandler
    public final void onPreLoad(FMLPreInitializationEvent aEvent) {
        onModPreInit(aEvent);
    }

    @Mod.EventHandler
    public final void onLoad(FMLInitializationEvent aEvent) {
        onModInit(aEvent);
    }

    @Mod.EventHandler
    public final void onPostLoad(FMLPostInitializationEvent aEvent) {
        onModPostInit(aEvent);
    }

    @Mod.EventHandler
    public final void onServerStarting(FMLServerStartingEvent aEvent) {
        onModServerStarting(aEvent);
    }

    @Mod.EventHandler
    public final void onServerStarted(FMLServerStartedEvent aEvent) {
        onModServerStarted(aEvent);
    }

    @Mod.EventHandler
    public final void onServerStopping(FMLServerStoppingEvent aEvent) {
        onModServerStopping(aEvent);
    }

    @Mod.EventHandler
    public final void onServerStopped(FMLServerStoppedEvent aEvent) {
        onModServerStopped(aEvent);
    }

    @Override
    public String getModID() {
        return MODID;
    }

    @Override
    public String getModName() {
        return MODNAME;
    }

    @Override
    public String getModNameForLog() {
        return "Qwertech";
    }

    @Override
    public Abstract_Proxy getProxy() {
        return PROXY;
    }

    @Override
    public void onModPreInit2(FMLPreInitializationEvent aEvent) {
        instance = this;

        QTI.NW_API = new NetworkHandler(MODID, "QWER", new PacketInventorySync());

        OreDictionary.registerOre("ingotCeramic", Items.brick);

        RecipeSorter.register("qwertech:tool", AnyQTTool.class, RecipeSorter.Category.SHAPELESS, "after:gregtech:tool");
        RecipeSorter.register("qwertech:repair", RepairRecipe.class, RecipeSorter.Category.SHAPELESS, "after:qwertech:tool");
        machines = new MultiTileEntityRegistry("qwertech.machines");
        metal = MultiTileEntityBlock.getOrCreate(MODID, "iron", Material.iron, Block.soundTypeMetal, CS.TOOL_pickaxe, 0, 0, 15, false, false);
        final OreDictPrefix mattockHead = OreDictPrefix.createPrefix("toolHeadMattock");
        mattockHead.setCategoryName("Mattock Heads");
        mattockHead.setLocalItemName("", " Mattock Head");
        mattockHead.setCondition(new And(OP.toolHeadShovel, OP.toolHeadAxe, typemin(2)));
        mattockHead.add(UNIFICATABLE, BURNABLE, RECYCLABLE, SCANNABLE, TOOL_HEAD, NEEDS_HANDLE).setStacksize(16).aspects(TC.INSTRUMENTUM, 2, TC.MESSIS, 1);
        mattockHead.setMaterialStats(gregapi.data.CS.U * 3);
        new PrefixItem(MOD_DATA, "qwertech.tools.mattock", mattockHead);

        final OreDictPrefix maceHead = OreDictPrefix.createPrefix("toolHeadMace");
        maceHead.setCategoryName("Mace Heads");
        maceHead.setLocalItemName("", " Mace Head");
        maceHead.setCondition(new And(HAS_TOOL_STATS, OreDictMaterialCondition.typemin(2)));
        maceHead.add(UNIFICATABLE, BURNABLE, RECYCLABLE, SCANNABLE, TOOL_HEAD, NEEDS_HANDLE).setStacksize(16).aspects(TC.TELUM, 2, TC.PERDITIO, 1);
        maceHead.setMaterialStats(gregapi.data.CS.U * 5);
        new PrefixItem(MOD_DATA, "qwertech.tools.mace", maceHead);

        final OreDictPrefix shuriken = OreDictPrefix.createPrefix("shuriken");
        shuriken.setCategoryName("Shuriken");
        shuriken.setLocalItemName("", " Shuriken");
        shuriken.setCondition(new And(HAS_TOOL_STATS, typemin(2)));
        shuriken.add(UNIFICATABLE, BURNABLE, RECYCLABLE, SCANNABLE).setStacksize(64, 16).aspects(TC.TELUM, 2, TC.MOTUS, 1);
        shuriken.setMaterialStats(OP.stick.mAmount);
        new PrefixItem(MOD_DATA, "qwertech.tools.shuriken", shuriken) {
            @Override
            public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) {
                OreDictItemData data = OreDictManager.INSTANCE.getItemData(item);
                UT.Sounds.send(world, "qwertech:metal.slide", 0.5F, (world.rand.nextInt(5) + 8) / 10F, (int) player.posX, (int) player.posY, (int) player.posZ);
                if (!world.isRemote) {
                    EntityShuriken es = new EntityShuriken(world, player, 4F / 3F, data.mMaterial.mMaterial);
                    world.spawnEntityInWorld(es);
                    if (!UT.Entities.hasInfiniteItems(player)) {
                        item.stackSize = item.stackSize - 1;
                    }
                }
                return item;
            }

            @Override
            public void run() {
                super.run();
                BlockDispenser.dispenseBehaviorRegistry.putObject(this, new Dispenser_Shuriken());
            }

            @Override
            public void addInformation(ItemStack aStack, EntityPlayer aPlayer, List aList, boolean aF3_H) {
                aList.add("Ken I toss this throwing star?");
                OreDictMaterial mat = OreDictManager.INSTANCE.getItemData(aStack).mMaterial.mMaterial;
                float tCombat = 2 + ((mat.mToolQuality) / 2F);
                aList.add(LH.Chat.WHITE + "Attack Damage: " + LH.Chat.BLUE + "+" + tCombat + LH.Chat.RED + " (= " + ((tCombat + 1) / 2) + " Hearts)" + LH.Chat.GRAY);
                if (mat == MT.Ag && aPlayer.getDisplayName().toLowerCase().startsWith("bear989")) {
                    aList.add(LH.Chat.BLINKING_RED + "Be careful with this, Mr. Bear!" + LH.Chat.GRAY);
                } else if ((mat == MT.Pb || mat == MT.Craponite) && aPlayer.getDisplayName().equalsIgnoreCase("crazyj1984")) {
                    aList.add(LH.Chat.BLINKING_RED + "Careful not to cut yourself on this one, lass!" + LH.Chat.GRAY);
                } else if ((mat == MT.Diamond || mat == MT.Diamantine) && aPlayer.getDisplayName().equalsIgnoreCase("shadowkn1ght18") || aPlayer.getDisplayName().equalsIgnoreCase("netmc")) {
                    aList.add(LH.Chat.BLINKING_RED + "I wouldn't throw this straight up if I were you..." + LH.Chat.GRAY);
                } else if (mat == MT.Ti && aPlayer.getDisplayName().equalsIgnoreCase("gregoriust") || aPlayer.getDisplayName().equalsIgnoreCase("speiger")) {
                    aList.add(LH.Chat.BLINKING_RED + "How is this like a NullPointerException? You'll get mad if it's thrown at you." + LH.Chat.GRAY);
                } else if (mat == MT.Pt && aPlayer.getDisplayName().equalsIgnoreCase("qwertygiy") || aPlayer.getDisplayName().equalsIgnoreCase("ilrith")) {
                    aList.add(LH.Chat.BLINKING_RED + "But it's so shiny!" + LH.Chat.GRAY);
                } else if (mat == MT.Al && aPlayer.getDisplayName().equalsIgnoreCase("andyafw")) {
                    aList.add(LH.Chat.BLINKING_RED + "Might want to return this one to Wal-Mart.");
                }
            }
        };

        final OreDictPrefix sturdyPickaxeHead = OreDictPrefix.createPrefix("toolHeadSturdyPickaxe"); // This newly created OreDict Prefix is named "exampleprefix", so an Aluminium Item with this Prefix would be named "exampleprefixAluminium" in the OreDict.
        sturdyPickaxeHead.setCategoryName("Sturdy Pickaxe Heads"); // That is what the Creative Tab of it would be named.
        sturdyPickaxeHead.setLocalItemName("", " Sturdy Pickaxe Head"); // Generic Items will follow this naming Guideline, so for example "Small Aluminium Example" for an Aluminium Item with that Prefix.
        sturdyPickaxeHead.setCondition(new And(OP.toolHeadPickaxe, typemin(2))); // The Condition under which Items of this Prefix should generate in general. In this case TRUE to have ALL the Items.
        sturdyPickaxeHead.add(UNIFICATABLE, BURNABLE, RECYCLABLE, SCANNABLE, TOOL_HEAD, NEEDS_HANDLE).setStacksize(16).aspects(TC.INSTRUMENTUM, 2, TC.PERFODIO, 1);// Items of this can be recycled for Resources.
        sturdyPickaxeHead.setMaterialStats(gregapi.data.CS.U * 3); // Any Item of this example Prefix has the value of 1 Material Unit (U), this is exactly equal to one Ingot/Dust/Gem.
        new PrefixItem(MOD_DATA, "qwetech.tools.pickaxe", sturdyPickaxeHead);

        final OreDictPrefix sturdyAxeHead = OreDictPrefix.createPrefix("toolHeadSturdyAxe"); // This newly created OreDict Prefix is named "exampleprefix", so an Aluminium Item with this Prefix would be named "exampleprefixAluminium" in the OreDict.
        sturdyAxeHead.setCategoryName("Lumberaxe Heads"); // That is what the Creative Tab of it would be named.
        sturdyAxeHead.setLocalItemName("", " Lumber Axe Head"); // Generic Items will follow this naming Guideline, so for example "Small Aluminium Example" for an Aluminium Item with that Prefix.
        sturdyAxeHead.setCondition(new And(OP.toolHeadPickaxe, typemin(2))); // The Condition under which Items of this Prefix should generate in general. In this case TRUE to have ALL the Items.
        sturdyAxeHead.add(UNIFICATABLE, BURNABLE, RECYCLABLE, SCANNABLE, TOOL_HEAD, NEEDS_HANDLE).setStacksize(16).aspects(TC.INSTRUMENTUM, 2, TC.TELUM, 1);// Items of this can be recycled for Resources.
        sturdyAxeHead.setMaterialStats(gregapi.data.CS.U * 3); // Any Item of this example Prefix has the value of 1 Material Unit (U), this is exactly equal to one Ingot/Dust/Gem.
        new PrefixItem(MOD_DATA, "qwetech.tools.axe", sturdyAxeHead);

        final OreDictPrefix miningHammerHead = OreDictPrefix.createPrefix("toolHeadMiningHammer"); // This newly created OreDict Prefix is named "exampleprefix", so an Aluminium Item with this Prefix would be named "exampleprefixAluminium" in the OreDict.
        miningHammerHead.setCategoryName("Sturdy Mining Heads"); // That is what the Creative Tab of it would be named.
        miningHammerHead.setLocalItemName("", " Sturdy Mining Hammer Head"); // Generic Items will follow this naming Guideline, so for example "Small Aluminium Example" for an Aluminium Item with that Prefix.
        miningHammerHead.setCondition(new And(OP.toolHeadHammer, OP.toolHeadPickaxe, typemin(2))); // The Condition under which Items of this Prefix should generate in general. In this case TRUE to have ALL the Items.
        miningHammerHead.add(UNIFICATABLE, BURNABLE, RECYCLABLE, SCANNABLE, TOOL_HEAD, NEEDS_HANDLE).setStacksize(16).aspects(TC.INSTRUMENTUM, 10, TC.PERFODIO, 2);// Items of this can be recycled for Resources.
        miningHammerHead.setMaterialStats(gregapi.data.CS.U * 18); // Any Item of this example Prefix has the value of 1 Material Unit (U), this is exactly equal to one Ingot/Dust/Gem.
        new PrefixItem(MOD_DATA, "qwetech.tools.hammer", miningHammerHead);

        final OreDictPrefix excavatorHead = OreDictPrefix.createPrefix("toolHeadExcavator"); // This newly created OreDict Prefix is named "exampleprefix", so an Aluminium Item with this Prefix would be named "exampleprefixAluminium" in the OreDict.
        excavatorHead.setCategoryName("Excavating Heads"); // That is what the Creative Tab of it would be named.
        excavatorHead.setLocalItemName("", " Excavator Head"); // Generic Items will follow this naming Guideline, so for example "Small Aluminium Example" for an Aluminium Item with that Prefix.
        excavatorHead.setCondition(new And(OP.toolHeadShovel, typemin(2))); // The Condition under which Items of this Prefix should generate in general. In this case TRUE to have ALL the Items.
        excavatorHead.add(UNIFICATABLE, BURNABLE, RECYCLABLE, SCANNABLE, TOOL_HEAD, NEEDS_HANDLE).setStacksize(16).aspects(TC.INSTRUMENTUM, 10, TC.PERFODIO, 2);// Items of this can be recycled for Resources.
        excavatorHead.setMaterialStats(gregapi.data.CS.U * 7); // Any Item of this example Prefix has the value of 1 Material Unit (U), this is exactly equal to one Ingot/Dust/Gem.
        new PrefixItem(MOD_DATA, "qwetech.tools.excavator", excavatorHead);

        shuriken.addListener(new Loader_OreProcessing.OreProcessing_CraftFrom(1, null, new String[][]{{"X X", " f ", "X X"}, {" X ", "XfX", " X "}}, OP.stick, null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        maceHead.addListener(new Loader_OreProcessing.OreProcessing_CraftFrom(1, null, new String[][]{{"YSY", "SXS", "YSY"}, {"SYS", "YXY", "SYS"}}, OP.gearGt, OP.bolt, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        mattockHead.addListener(new Loader_OreProcessing.OreProcessing_CraftFrom(1L, null, new String[][]{{"PXh", "Y  ", "f  "}}, OP.toolHeadAxe, OP.toolHeadShovel, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        sturdyAxeHead.addListener(new Loader_OreProcessing.OreProcessing_CraftFrom(1L, null, new String[][]{{"PPh", "PX ", "f  "}}, OP.toolHeadAxe, null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        sturdyPickaxeHead.addListener(new Loader_OreProcessing.OreProcessing_CraftFrom(1L, null, new String[][]{{"XPh", "P  ", "f  "}}, OP.toolHeadConstructionPickaxe, null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        miningHammerHead.addListener(new Loader_OreProcessing.OreProcessing_CraftFrom(1L, null, new String[][]{{"PPh", "YX ", "PPf"}}, OP.toolHeadHammer, OP.plateDense, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        excavatorHead.addListener(new Loader_OreProcessing.OreProcessing_CraftFrom(1L, null, new String[][]{{"PPP", "PXP", "f h"}}, OP.toolHeadShovel, null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        //new PrefixBlock(QwerTech.MODID, QwerTech.MODID, "qwertech.blocks.stake", stake, null, null, null, null, net.minecraft.block.material.Material.wood, net.minecraft.block.Block.soundTypeLadder, null, 1.5F, 4.5F,   0,   0, 999, 0, 0, 0, 1, 1, 1, false, false, false, false, false, false, true, true, true, true, false, true, true, true, gregapi.oredict.OreDictMaterial.MATERIAL_ARRAY);


        qwerTool = new MultiItemTool_QT(MODID, "qwertech.tools");
        QTI.qwerTool.set(qwerTool);
        qwerTool.addTool(0, "Mattock", "Tills soil and chops logs", new QT_Tool_Mattock().setMaterialAmount(mattockHead.mAmount), "craftingToolAxe", "craftingToolHoe", TC.stack(TC.INSTRUMENTUM, 2L), TC.stack(TC.METO, 2L), TC.stack(TC.ARBOR, 2L), "toolMattock");
        GameRegistry.addRecipe(new AdvancedCraftingTool(qwerTool, 0, mattockHead, MT.Bronze));
        qwerTool.addTool(2, "Slingshot", "Rock and roll", new QT_Tool_Slingshot().setMaterialAmount(CS.U3 + (CS.U / 2)), TC.stack(TC.INSTRUMENTUM, 2L), TC.stack(TC.TELUM, 2L), TC.stack(TC.TERRA, 2L), "toolSlingshot");
        OP.stick.addListener(new OreProcessing_QTTool(2, ConfigCategories.Recipes.gregtechtools + "." + "Slingshot", true, false, 0L, 0L, null, new String[][]{{"XXX", "SfS", " S "}}, null, new ItemStack(Items.string), null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        qwerTool.addTool(4, "Knuckles", "Hit it!", new QT_Tool_Knuckles().setMaterialAmount(OP.ring.mAmount * 4), TC.stack(TC.TELUM, 1), TC.stack(TC.PERDITIO, 1), "toolKnuckles");
        OP.ring.addListener(new OreProcessing_QTTool(4, ConfigCategories.Recipes.gregtechtools + "." + "Knuckles", false, false, 0L, 0L, MT.Empty, new String[][]{{"OOO", "Oh ", "   "}}, null, null, null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        qwerTool.addTool(6, "Mace", "Club with teeth", new QT_Tool_Mace().setMaterialAmount(maceHead.mAmount), TC.stack(TC.TELUM, 3), TC.stack(TC.PERDITIO, 2), "toolMace");
        GameRegistry.addRecipe(new AdvancedCraftingTool(qwerTool, 6, maceHead, MT.Steel));
        qwerTool.addTool(8, "Spear", "Stabby McStabface", new QT_Tool_Javelin().setMaterialAmount(OP.stickLong.mAmount + OP.toolHeadArrow.mAmount), TC.stack(TC.TELUM, 2), TC.stack(TC.MOTUS, 1), "toolSpear");
        GameRegistry.addRecipe(new AdvancedCraftingTool(qwerTool, 8, OP.toolHeadArrow, MT.Steel));
        qwerTool.addTool(10, "Stake", "GIT ME MY POKIN' STICK, MARTHA", new QT_Tool_Stake().setMaterialAmount(OP.stick.mAmount), TC.stack(TC.TELUM, 2), TC.stack(TC.MOTUS, 1), "toolSharpStick");
        OP.stick.addListener(new OreProcessing_QTTool(10, ConfigCategories.Recipes.gregtechtools + "." + "Stake", true, false, 0L, 0L, null, new String[][]{{"kS"}, {"Sk"}}, null, null, null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        OP.stick.addListener(new OreProcessing_NonCrafting(RM.Sharpening, (ItemStack) null, qwerTool.getToolWithStats(10, MT.Empty, MT.Empty), TD.Atomic.ANTIMATTER.NOT));
        qwerTool.addTool(12, "Bat", "Underwood Light Switch", new QT_Tool_Bat().setMaterialAmount(OP.stick.mAmount), TC.stack(TC.TELUM, 2), "toolBat");
        //OP.stickLong.addListener(new OreProcessing_QTTool(12, ConfigCategories.Recipes.gregtechtools + "." + "Bat", false, false, 0L, 0L, MT.Empty, new String[][]{{"yL"}, {"Ly"}}, null, null, null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        GameRegistry.addRecipe(new AdvancedCraftingTool(qwerTool, 12, OP.stickLong, MT.WoodTreated));
        qwerTool.addTool(14, "LumberAxe", "Jack Jack", new QT_Tool_Lumberaxe().setMaterialAmount(OP.toolHeadAxe.mAmount), "craftingToolAxe", TC.stack(TC.INSTRUMENTUM, 2), TC.stack(TC.ARBOR, 1), TC.stack(TC.MACHINA, 1), "axe");
        GameRegistry.addRecipe(new AdvancedCraftingTool(qwerTool, 14, sturdyAxeHead, MT.Steel));
        qwerTool.addTool(16, "Sturdy Pickaxe", "Mines a simple 1x2 tunnel, Pretty safe won't dig the tunnel if the block isn't right in front of you", new QT_Tool_SturdyPickaxe().setMaterialAmount(OP.toolHeadPickaxe.mAmount), "craftingToolPickaxe", TC.stack(TC.INSTRUMENTUM, 2), TC.stack(TC.PERDITIO, 1), "pickaxe");
        GameRegistry.addRecipe(new AdvancedCraftingTool(qwerTool, 16, sturdyPickaxeHead, MT.Steel));
        qwerTool.addTool(18, "Kazoo", "A true " + LH.Chat.ITALIC + "instrument" + LH.Chat.RESET + LH.Chat.GRAY + " of torture", new QT_Tool_Kazoo().setMaterialAmount(OP.stick.mAmount), "kazoo");
        OP.ring.addListener(new OreProcessing_QTTool(18, ConfigCategories.Recipes.gregtechtools + "." + "Kazoo", false, false, 0L, 0L, MT.Paper, new String[][]{{"XO ", " Sk"}}, null, ST.make(Items.paper, 1, 0), null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        qwerTool.addTool(20, "Mining Hammer", "Tinker's Hammer, Mines a 3x3 Area", new QT_TOOL_MiningHammer().setMaterialAmount(miningHammerHead.mAmount), "craftingToolPickaxe", TC.stack(TC.INSTRUMENTUM, 10), TC.stack(TC.PERDITIO, 7), "hammer");
        GameRegistry.addRecipe(new AdvancedCraftingTool(qwerTool, 20, miningHammerHead, MT.Steel));
        qwerTool.addTool(22, "Excavator", "Digs up a 3x3 Area", new QT_Tool_Excavator().setMaterialAmount(excavatorHead.mAmount), "craftingToolShovel", TC.stack(TC.INSTRUMENTUM, 10), TC.stack(TC.PERDITIO, 7), "shovel");
        GameRegistry.addRecipe(new AdvancedCraftingTool(qwerTool, 22, miningHammerHead, MT.Steel));


        MinecraftForge.EVENT_BUS.register(new RegisterArmor());
        new ArmorUpgradeRegistry();
        RegisterArmor.qt_armor_upgrades = new MultiItemRandom(QwerTech.MODID, "qwertech.armor.upgrades") {
            @Override
            public void addItems() {
                addItem(0, "Dual Steel Springs", "Boingy boingy boingy");
                addItem(1, "Dual Stainless Steel Springs", "Boingy boingy boingy");
                addItem(2, "Dual Brass Springs", "Boingy boingy boingy");
                addItem(3, "Dual Aluminium Springs", "Boingy boingy boingy");
                addItem(4, "Dual Thaumium Springs", "Boingy boingy boingy");

                CR.shapeless(ST.make(this, 1, 0), CR.DEF, new Object[]{"springSmallSteel", "springSmallSteel"});
                CR.shapeless(ST.make(this, 1, 1), CR.DEF, new Object[]{"springSmallStainlessSteel", "springSmallStainlessSteel"});
                CR.shapeless(ST.make(this, 1, 2), CR.DEF, new Object[]{"springSmallBrass", "springSmallBrass"});
                CR.shapeless(ST.make(this, 1, 3), CR.DEF, new Object[]{"springSmallAluminium", "springSmallAluminium"});
                CR.shapeless(ST.make(this, 1, 4), CR.DEF, new Object[]{"springSmallThaumium", "springSmallThaumium"});
            }
        };

        RegisterArmor.instance.addUpgrades();
    }

    @Override
    public void onModInit2(FMLInitializationEvent aEvent) {

        MinecraftForge.EVENT_BUS.register(new QT_GUIHandler());
        RegisterLoot.init();
        OreDictMaterial[] upgradeDeskMats = new OreDictMaterial[]{MT.Bronze, MT.Co, MT.Au, MT.Obsidian, MT.Plastic, MT.Ag};
        for (int q = 0; q < upgradeDeskMats.length; q++) {
            OreDictMaterial mat = upgradeDeskMats[q];
            machines.add(mat.mNameLocal + " Upgrade Desk", "Upgrade Desks", 401 + q, 0, UpgradeDesk.class, 0, 16, metal, UT.NBT.make(NullBT, CS.NBT_MATERIAL, mat, CS.NBT_INV_SIZE, 1, CS.NBT_TEXTURE, "qwertech:metal", CS.NBT_HARDNESS, 3.0F, CS.NBT_RESISTANCE, 3.0F, CS.NBT_COLOR, UT.Code.getRGBInt(mat.fRGBaSolid)), "RfR", "RSR", "CCC", 'C', OP.plate.dat(mat), 'R', OP.stick.dat(ANY.Steel), 'S', OP.springSmall.dat(ANY.Steel));
        }
    }

    @Override
    public void onModPostInit2(FMLPostInitializationEvent aEvent) {


    }

    @Mod.EventHandler
    public void onModsLoaded(FMLLoadCompleteEvent event) {
    }

    @Override
    public void onModServerStarting2(FMLServerStartingEvent aEvent) {
    }

    @Override
    public void onModServerStarted2(FMLServerStartedEvent aEvent) {
    }

    @Override
    public void onModServerStopping2(FMLServerStoppingEvent aEvent) {

    }

    @Override
    public void onModServerStopped2(FMLServerStoppedEvent aEvent) {


    }
}
