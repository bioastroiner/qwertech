package com.kbi.qwertech;

import codechicken.nei.recipe.CatalystInfo;
import codechicken.nei.recipe.RecipeCatalysts;
import com.kbi.qwertech.api.data.*;
import com.kbi.qwertech.api.recipe.AnyQTTool;
import com.kbi.qwertech.api.recipe.RepairRecipe;
import com.kbi.qwertech.api.recipe.WoodSpecificCrafting;
import com.kbi.qwertech.api.recipe.listeners.OreProcessing_NonCrafting;
import com.kbi.qwertech.api.recipe.listeners.OreProcessing_QTTool;
import com.kbi.qwertech.api.recipe.managers.CraftingManagerCountertop;
import com.kbi.qwertech.api.registry.ArmorUpgradeRegistry;
import com.kbi.qwertech.blocks.BlockCorrugated;
import com.kbi.qwertech.blocks.BlockSoil;
import com.kbi.qwertech.client.render.QT_GUIHandler;
import com.kbi.qwertech.entities.projectile.EntityShuriken;
import com.kbi.qwertech.items.MultiItemTool_QT;
import com.kbi.qwertech.items.behavior.Dispenser_Shuriken;
import com.kbi.qwertech.items.stats.*;
import com.kbi.qwertech.loaders.*;
import com.kbi.qwertech.loaders.mod.ModLoadBase;
import com.kbi.qwertech.network.packets.PacketInventorySync;
import com.kbi.qwertech.tileentities.*;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.api.Abstract_Mod;
import gregapi.api.Abstract_Proxy;
import gregapi.block.ItemBlockBase;
import gregapi.block.metatype.BlockStones;
import gregapi.block.multitileentity.MultiTileEntityBlock;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.code.ICondition.And;
import gregapi.code.ModData;
import gregapi.config.ConfigCategories;
import gregapi.data.*;
import gregapi.data.CS.ModIDs;
import gregapi.item.multiitem.MultiItemRandom;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.item.multiitem.food.FoodStatDrink;
import gregapi.item.prefixitem.PrefixItem;
import gregapi.network.NetworkHandler;
import gregapi.old.Textures;
import gregapi.oredict.*;
import gregapi.recipes.AdvancedCraftingTool;
import gregapi.recipes.Recipe;
import gregapi.render.IIconContainer;
import gregapi.render.TextureSet;
import gregapi.util.CR;
import gregapi.util.OM;
import gregapi.util.ST;
import gregapi.util.UT;
import gregtech.loaders.b.Loader_OreProcessing;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;

import java.io.File;
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
    public static final String MODID = Tags.MODID;
    public static final String MODNAME = Tags.MODNAME;
    public static final String VERSION = Tags.VERSION;
    public static ModData MOD_DATA = new ModData(MODID, MODNAME);
    public static QwerTech instance;

    @SidedProxy(modId = MODID, clientSide = "com.kbi.qwertech.ClientProxy", serverSide = "com.kbi.qwertech.ServerProxy")
    public static CommonProxy PROXY;
    public static HashMap<String, ItemStack> food;
    public static NBTTagCompound NullBT = null;
    public static BlockCorrugated corrugatedBlock;
    public static BlockSoil soilBlock;
    public static int knucklesTexID;
    public static int slingshotTexID;
    public static int stringshotTexID;
    public static int javelinHeadTexID;
    public static int stakeTexID;
    public static int batTexID;
    public static int batSpikeTexID;
    public static int maxChiselTex = 4;
    public static MultiTileEntityRegistry machines;
    public static MultiTileEntityRegistry armor_upgrade_desk;
    public static MultiTileEntityBlock metal;
    public static MultiTileEntityBlock wood;
    public static MultiTileEntityBlock air;
    private static MultiItemTool qwerTool;
    private static MultiItemRandom qwerFood;

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

    public void doConfigurations() {
        Configuration tMainConfig = new Configuration(new File(CS.DirectoriesGT.CONFIG_GT, "QwerTech_main.cfg"));
        tMainConfig.load();

        QTConfigs.doMobScrapesDrop = tMainConfig.get("entities", "DoMobScrapesDrop", true, "Add bonus loot from certain weapons").setShowInGui(true).getBoolean(true);
        QTConfigs.doMobsUseGear = tMainConfig.get("entities", "DoMobsUseGear", true, "Add more items to be held by naturally-spawning mobs").setShowInGui(true).getBoolean(true);
        QTConfigs.slingshotExplode = tMainConfig.get("entities", "CanThrownRocksExplode", true, "Add the chance for projectiles made from explosive materials to explode on impact").setShowInGui(true).getBoolean(true);
        QTConfigs.slingshotGlass = tMainConfig.get("entities", "CanRocksBreakGlass", true, "Add the ability for thrown rocks and chunks to break glass").setShowInGui(true).getBoolean(true);
        QTConfigs.canScrapePlayers = tMainConfig.get("entities", "CanPlayersBeScraped", true, "Add the chance for players to drop an item when hit by certain weapons").setShowInGui(true).getBoolean(true);
        QTConfigs.addCustomAI = tMainConfig.get("entities", "AddCustomAI", true, "Add custom AI to existing mobs").setShowInGui(true).getBoolean(true);
        QTConfigs.cowsOverheat = tMainConfig.get("entities", "HeatKillsCows", true, "Add temperature limits to cows (deserts, savannahs)").setShowInGui(true).getBoolean(true);
        QTConfigs.addDungeonTools = tMainConfig.get("worldgen", "AddDungeonTools", true, "Add GT and QT tools as rare dungeon items").setShowInGui(true).getBoolean(true);
        QTConfigs.announceFanfare = tMainConfig.get("achievements", "AnnounceFanfare", false, "If achievements annoy you, turn this off").setShowInGui(true).getBoolean(false);
        QTConfigs.chemicalXRandom = tMainConfig.get("recipes", "ElectrolyzeChemicalX", false, "Set to false to eliminate the Utonium Volatility Process").setShowInGui(true).getBoolean(true);

        tMainConfig.save();

        Configuration tCompat = new Configuration(new File(CS.DirectoriesGT.CONFIG_GT, "QwerTech_Compat.cfg"));
        tCompat.load();

        QTConfigs.overwriteJourneyMap = tCompat.get("journeymap", "OverwriteIcons", true, "Overwrite JourneyMap icons on load").setShowInGui(true).getBoolean(true);

        tCompat.save();

        Configuration tSections = new Configuration(new File(CS.DirectoriesGT.CONFIG_GT, "QwerTech_Modules.cfg"));
        tSections.load();

        QTConfigs.enableFrogs = tSections.get("entities", "enableFrogs", true, "Allow Frogs to exist").setShowInGui(true).getBoolean(true);
        QTConfigs.enableTurkeys = tSections.get("entities", "enableTurkeys", true, "Allow Turkeys to exist").setShowInGui(true).getBoolean(true);
        QTConfigs.enableChickens = tSections.get("entities", "enableChickens", true, "Turns baby Chickens into QwerTech chickens").setShowInGui(true).getBoolean(true);

        //QTConfigs.enable3DCrafting = tSections.get("crafting", "enable3D", true, "Allow use of the 3x3x3 crafting grid of T4 crafting anvils").setShowInGui(true).getBoolean(true);

        QTConfigs.enableTools = tSections.get("tools", "enableTools", true, "Allow the creation of QwerTech tools like maces and mattocks").setShowInGui(true).getBoolean(true);

        QTConfigs.enableArmor = tSections.get("armor", "enableArmor", true, "Allow the creation of QwerTech armor").setShowInGui(true).getBoolean(true);

        tSections.save();

        Configuration UI = new Configuration(new File(CS.DirectoriesGT.CONFIG_GT, "QwerTech_UI_Display.cfg"));
        UI.load();

        QTConfigs.effectAnchorX = UI.get("effects_custom", "effectAnchorX", 1, "If default is disabled, which screen edge the effect icons will adhere to: left (0), middle (1), or right (2)").setShowInGui(true).getInt(1);
        QTConfigs.effectAnchorY = UI.get("effects_custom", "effectAnchorY", 2, "If default is disabled, which screen edge the effect icons will adhere to: top (0), middle (1), or bottom (2)").setShowInGui(true).getInt(2);
        QTConfigs.effectOffsetX = UI.get("effects_custom", "effectOffsetX", -89, "If default is disabled, how far from the X edge the icons will be placed.").setShowInGui(true).getInt(-89);
        QTConfigs.effectOffsetY = UI.get("effects_custom", "effectOffsetY", -46, "If default is disabled, how far from the Y edge the icons will be placed.").setShowInGui(true).getInt(-46);
        QTConfigs.effectUseDefault = UI.get("effects", "useDefault", true, "Disable to manually change the positioning of the effect icons.").setShowInGui(true).getBoolean(true);
        QTConfigs.effectDefaultInUse = UI.get("effects", "defaultType", 0, "Which default position is currently in use. 0: above health").setShowInGui(true).getInt(0);

        QTConfigs.effectCenterX = UI.get("effects_custom", "effectCenterX", 0, "If default is disabled, which way the icons will render from the offset: to the right (0), centered (1) or to the left (2)").setShowInGui(true).getInt(0);
        QTConfigs.effectCenterY = UI.get("effects_custom", "effectCenterY", 0, "If default is disabled, which way the icons will render from the offset: below (0), centered (1) or above (2)").setShowInGui(true).getInt(0);
        QTConfigs.effectHorizontal = UI.get("effects_custom", "effectHorizontal", true, "If default is disabled, whether the icons will generate in a row left to right (true) or top to bottom (false)").setShowInGui(true).getBoolean(true);
        QTConfigs.effectRowLimit = UI.get("effects_custom", "effectRowLimit", 0, "If default is disabled, setting this to a value above 1 will generate a new row of icons after every so many icons.").setShowInGui(true).getInt(0);
        QTConfigs.effectBackgroundType = UI.get("effects", "effectOutline", 0, "-1 = no outline, 0 = rounded outline, 1 = square outline, 2 = circle outline").setShowInGui(true).getInt(0);

        UI.save();
    }

    @Override
    public void onModPreInit2(FMLPreInitializationEvent aEvent) {
        instance = this;

        QTMT.ChemicalX.toString();
        COLOR.put("Black", 0);
        NOTE.A2.get();
        MUSE.addSome();

        this.doConfigurations();

        QTI.NW_API = new NetworkHandler(MODID, "QWER", new PacketInventorySync());

        OreDictionary.registerOre("ingotCeramic", Items.brick);

        RecipeSorter.register("qwertech:tool", AnyQTTool.class, RecipeSorter.Category.SHAPELESS, "after:gregtech:tool");
        RecipeSorter.register("qwertech:repair", RepairRecipe.class, RecipeSorter.Category.SHAPELESS, "after:qwertech:tool");
        RecipeSorter.register("qwertech:wooded", WoodSpecificCrafting.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");

        new ArmorUpgradeRegistry();
        new RegisterMaterials();
        RegisterItems.run();

        new FoodStatDrink(UT.Fluids.create("tomatosauce", "Tomato Sauce", null, 1, 1000L, 300L, CS.FluidsGT.SIMPLE, CS.FluidsGT.FOOD), "Spaghetti, spaghetti, all over the place", 3, 0.4F, 20.0F, 350.0F, 0.5F, 0, 0, 0, 5, 0, EnumAction.drink, false, false, false, Potion.hunger.id, 100, 1, 20);
        new FoodStatDrink(UT.Fluids.create("mildsalsa", "Mild Salsa", null, 1, 1000L, 300L, CS.FluidsGT.SIMPLE, CS.FluidsGT.FOOD), "AKA Chunky Ketchup", 3, 0.4F, 20.0F, 310.0F, 0.3F, 0, 0, 0, 5, 0, EnumAction.drink, false, false, false, Potion.hunger.id, 100, 1, 20);
        new FoodStatDrink(UT.Fluids.create("salsa", "Medium Salsa", null, 1, 1000L, 300L, CS.FluidsGT.SIMPLE, CS.FluidsGT.FOOD), "With a little kick", 3, 0.4F, 20.0F, 310.0F, 0.5F, 0, 0, 0, 5, 0, EnumAction.drink, false, false, false, Potion.hunger.id, 100, 1, 20);
        new FoodStatDrink(UT.Fluids.create("hotsalsa", "Hot Salsa", null, 1, 1000L, 300L, CS.FluidsGT.SIMPLE, CS.FluidsGT.FOOD), "Vegan fire", 3, 0.4F, 20.0F, 310.0F, 0.7F, 0, 0, 0, 5, 0, EnumAction.drink, false, false, false, Potion.hunger.id, 100, 1, 20);
        new FoodStatDrink(UT.Fluids.create("sugarwater", "Sugarwater", null, 1, 1000L, 300L, CS.FluidsGT.SIMPLE, CS.FluidsGT.FOOD), "Sweet, yet unsatisfying", 1, 0.1F, 20.0F, 350.0F, 0.5F, 0, 0, 0, 20, 0, EnumAction.drink, false, false, false, Potion.moveSpeed.id, 100, 1, 20);

        UT.Fluids.create("dna", "DNA", null, 1, 1000L, 300L, CS.FluidsGT.SIMPLE, CS.FluidsGT.LIQUID);
        UT.Fluids.create("blood", "Blood", null, 1, 1000L, 300L, CS.FluidsGT.NONSTANDARD, CS.FluidsGT.LIQUID);

        corrugatedBlock = new BlockCorrugated(ItemBlockBase.class, "qt.block.corrugated", Material.iron, Block.soundTypeMetal, 16, new IIconContainer[]{new Textures.BlockIcons.CustomIcon("qwertech:wall")});
        LH.add("qt.block.corrugated.0.name", "Corrugated Iron Wall");
        LH.add("qt.block.corrugated.1.name", "Corrugated Aluminium Wall");
        LH.add("qt.block.corrugated.2.name", "Corrugated Gold Wall");
        LH.add("qt.block.corrugated.3.name", "Corrugated Steel Wall");
        LH.add("qt.block.corrugated.4.name", "Corrugated Bronze Wall");
        LH.add("qt.block.corrugated.5.name", "Corrugated Brass Wall");
        LH.add("qt.block.corrugated.6.name", "Corrugated Silver Wall");
        LH.add("qt.block.corrugated.7.name", "Corrugated Stainless Steel Wall");
        LH.add("qt.block.corrugated.8.name", "Corrugated Wrought Iron Wall");
        LH.add("qt.block.corrugated.9.name", "Corrugated Vinyl Wall");
        LH.add("qt.block.corrugated.10.name", "Corrugated Titanium Wall");
        LH.add("qt.block.corrugated.11.name", "Corrugated TungstenSteel Wall");
        LH.add("qt.block.corrugated.12.name", "Corrugated Invar Wall");
        LH.add("qt.block.corrugated.13.name", "Corrugated Tin Alloy Wall");
        LH.add("qt.block.corrugated.14.name", "Corrugated Galvanized Steel Wall");
        LH.add("qt.block.corrugated.15.name", "Corrugated Electrum Wall");

        soilBlock = new BlockSoil(ItemBlockBase.class, "qt.block.soil", Material.ground, Block.soundTypeGravel, 11, new IIconContainer[]{new Textures.BlockIcons.CustomIcon("qwertech:wall")});
        LH.add("qt.block.soil.0.name", "Potting Soil");
        LH.add("qt.block.soil.1.name", "Woodchip Mulch");
        LH.add("qt.block.soil.2.name", "Bark Mulch");
        LH.add("qt.block.soil.3.name", "Raw Compost");
        LH.add("qt.block.soil.4.name", "Compost");
        LH.add("qt.block.soil.5.name", "Pinestraw");
        LH.add("qt.block.soil.6.name", "Dry Pinestraw");
        LH.add("qt.block.soil.7.name", "Rotten Pinestraw");
        LH.add("qt.block.soil.8.name", "Fresh Leaves");
        LH.add("qt.block.soil.9.name", "Dry Leaves");
        LH.add("qt.block.soil.10.name", "Rotten Leaves");
        soilBlock.setTickRandomly(true);
        OreDictManager.INSTANCE.setTarget(OP.blockDust, QTMT.CompostRaw, ST.make(QwerTech.soilBlock, 1, 3));
        OreDictManager.INSTANCE.setTarget(OP.blockDust, QTMT.Compost, ST.make(QwerTech.soilBlock, 1, 4));

        machines = new MultiTileEntityRegistry("qwertech.machines");
        armor_upgrade_desk = new MultiTileEntityRegistry("qwertech.upgrade_desks");

        metal = MultiTileEntityBlock.getOrCreate(MODID, "iron", Material.iron, Block.soundTypeMetal, CS.TOOL_pickaxe, 0, 0, 15, false, false);
        wood = MultiTileEntityBlock.getOrCreate(MODID, "wood", Material.wood, Block.soundTypeWood, CS.TOOL_axe, 0, 0, 15, false, false);
        air = (MultiTileEntityBlock) MultiTileEntityBlock.getOrCreate(MODID, "air", Material.plants, Block.soundTypeSnow, "", -1, -1, -1, false, false).setTickRandomly(true).setLightOpacity(0);

        final OreDictPrefix mattockHead = OreDictPrefix.createPrefix("toolHeadMattock"); // This newly created OreDict Prefix is named "exampleprefix", so an Aluminium Item with this Prefix would be named "exampleprefixAluminium" in the OreDict.
        mattockHead.setCategoryName("Mattock Heads"); // That is what the Creative Tab of it would be named.
        mattockHead.setLocalItemName("", " Mattock Head"); // Generic Items will follow this naming Guideline, so for example "Small Aluminium Example" for an Aluminium Item with that Prefix.
        mattockHead.setCondition(new And(OP.toolHeadShovel, OP.toolHeadAxe, typemin(2))); // The Condition under which Items of this Prefix should generate in general. In this case TRUE to have ALL the Items.
        mattockHead.add(UNIFICATABLE, BURNABLE, RECYCLABLE, SCANNABLE, TOOL_HEAD, NEEDS_HANDLE).setStacksize(16).aspects(TC.INSTRUMENTUM, 2, TC.MESSIS, 1);// Items of this can be recycled for Resources.
        mattockHead.setMaterialStats(gregapi.data.CS.U * 3); // Any Item of this example Prefix has the value of 1 Material Unit (U), this is exactly equal to one Ingot/Dust/Gem.
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
        shuriken.setCondition(new And(HAS_TOOL_STATS,typemin(2)));
        shuriken.add(UNIFICATABLE, BURNABLE, RECYCLABLE, SCANNABLE).setStacksize(64, 16).aspects(TC.TELUM, 2, TC.MOTUS, 1);
        shuriken.setMaterialStats(OP.stick.mAmount);
        new PrefixItem(MOD_DATA, "qwertech.tools.shuriken", shuriken) {
            @Override public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) {
                OreDictItemData data = OreDictManager.INSTANCE.getItemData(item);UT.Sounds.send(world, "qwertech:metal.slide", 0.5F, (world.rand.nextInt(5) + 8) / 10F, (int) player.posX, (int) player.posY, (int) player.posZ);
                if (!world.isRemote) {EntityShuriken es = new EntityShuriken(world, player, 4F / 3F, data.mMaterial.mMaterial);world.spawnEntityInWorld(es);
                    if (!UT.Entities.hasInfiniteItems(player)) {item.stackSize = item.stackSize - 1;}}return item;}
            @Override public void run() {super.run();BlockDispenser.dispenseBehaviorRegistry.putObject(this, new Dispenser_Shuriken());}
            @Override public void addInformation(ItemStack aStack, EntityPlayer aPlayer, List aList, boolean aF3_H) {
                aList.add("Ken I toss this throwing star?"); OreDictMaterial mat = OreDictManager.INSTANCE.getItemData(aStack).mMaterial.mMaterial; float tCombat = 2 + ((mat.mToolQuality) / 2F);
                aList.add(LH.Chat.WHITE + "Attack Damage: " + LH.Chat.BLUE + "+" + tCombat + LH.Chat.RED + " (= " + ((tCombat + 1) / 2) + " Hearts)" + LH.Chat.GRAY);
                if (mat == MT.Ag && aPlayer.getDisplayName().toLowerCase().startsWith("bear989")) {aList.add(LH.Chat.BLINKING_RED + "Be careful with this, Mr. Bear!" + LH.Chat.GRAY);}
                else if ((mat == MT.Pb || mat == MT.Craponite) && aPlayer.getDisplayName().equalsIgnoreCase("crazyj1984")) {aList.add(LH.Chat.BLINKING_RED + "Careful not to cut yourself on this one, lass!" + LH.Chat.GRAY);}
                else if ((mat == MT.Diamond || mat == MT.Diamantine) && aPlayer.getDisplayName().equalsIgnoreCase("shadowkn1ght18") || aPlayer.getDisplayName().equalsIgnoreCase("netmc")) {aList.add(LH.Chat.BLINKING_RED + "I wouldn't throw this straight up if I were you..." + LH.Chat.GRAY);}
                else if (mat == MT.Ti && aPlayer.getDisplayName().equalsIgnoreCase("gregoriust") || aPlayer.getDisplayName().equalsIgnoreCase("speiger")) {aList.add(LH.Chat.BLINKING_RED + "How is this like a NullPointerException? You'll get mad if it's thrown at you." + LH.Chat.GRAY);}
                else if (mat == MT.Pt && aPlayer.getDisplayName().equalsIgnoreCase("qwertygiy") || aPlayer.getDisplayName().equalsIgnoreCase("ilrith")) {aList.add(LH.Chat.BLINKING_RED + "But it's so shiny!" + LH.Chat.GRAY);}
                else if (mat == MT.Al && aPlayer.getDisplayName().equalsIgnoreCase("andyafw")) {aList.add(LH.Chat.BLINKING_RED + "Might want to return this one to Wal-Mart.");}}
        };

        final OreDictPrefix sturdyPickaxeHead = OreDictPrefix.createPrefix("toolHeadSturdyPickaxe"); // This newly created OreDict Prefix is named "exampleprefix", so an Aluminium Item with this Prefix would be named "exampleprefixAluminium" in the OreDict.
        sturdyPickaxeHead.setCategoryName("Sturdy Pickaxe Heads"); // That is what the Creative Tab of it would be named.
        sturdyPickaxeHead.setLocalItemName("", " Sturdy Pickaxe Head"); // Generic Items will follow this naming Guideline, so for example "Small Aluminium Example" for an Aluminium Item with that Prefix.
        sturdyPickaxeHead.setCondition(new And(OP.toolHeadPickaxe,typemin(2))); // The Condition under which Items of this Prefix should generate in general. In this case TRUE to have ALL the Items.
        sturdyPickaxeHead.add(UNIFICATABLE, BURNABLE, RECYCLABLE, SCANNABLE, TOOL_HEAD, NEEDS_HANDLE).setStacksize(16).aspects(TC.INSTRUMENTUM, 2, TC.PERFODIO, 1);// Items of this can be recycled for Resources.
        sturdyPickaxeHead.setMaterialStats(gregapi.data.CS.U * 3); // Any Item of this example Prefix has the value of 1 Material Unit (U), this is exactly equal to one Ingot/Dust/Gem.
        new PrefixItem(MOD_DATA,"qwetech.tools.pickaxe", sturdyPickaxeHead);

        final OreDictPrefix sturdyAxeHead = OreDictPrefix.createPrefix("toolHeadSturdyAxe"); // This newly created OreDict Prefix is named "exampleprefix", so an Aluminium Item with this Prefix would be named "exampleprefixAluminium" in the OreDict.
        sturdyAxeHead.setCategoryName("Lumberaxe Heads"); // That is what the Creative Tab of it would be named.
        sturdyAxeHead.setLocalItemName("", " Lumber Axe Head"); // Generic Items will follow this naming Guideline, so for example "Small Aluminium Example" for an Aluminium Item with that Prefix.
        sturdyAxeHead.setCondition(new And(OP.toolHeadPickaxe,typemin(2))); // The Condition under which Items of this Prefix should generate in general. In this case TRUE to have ALL the Items.
        sturdyAxeHead.add(UNIFICATABLE, BURNABLE, RECYCLABLE, SCANNABLE, TOOL_HEAD, NEEDS_HANDLE).setStacksize(16).aspects(TC.INSTRUMENTUM, 2, TC.TELUM, 1);// Items of this can be recycled for Resources.
        sturdyAxeHead.setMaterialStats(gregapi.data.CS.U * 3); // Any Item of this example Prefix has the value of 1 Material Unit (U), this is exactly equal to one Ingot/Dust/Gem.
        new PrefixItem(MOD_DATA,"qwetech.tools.axe", sturdyAxeHead);

        final OreDictPrefix miningHammerHead = OreDictPrefix.createPrefix("toolHeadMiningHammer"); // This newly created OreDict Prefix is named "exampleprefix", so an Aluminium Item with this Prefix would be named "exampleprefixAluminium" in the OreDict.
        miningHammerHead.setCategoryName("Sturdy Mining Heads"); // That is what the Creative Tab of it would be named.
        miningHammerHead.setLocalItemName("", " Sturdy Mining Hammer Head"); // Generic Items will follow this naming Guideline, so for example "Small Aluminium Example" for an Aluminium Item with that Prefix.
        miningHammerHead.setCondition(new And(OP.toolHeadHammer,OP.toolHeadPickaxe,typemin(2))); // The Condition under which Items of this Prefix should generate in general. In this case TRUE to have ALL the Items.
        miningHammerHead.add(UNIFICATABLE, BURNABLE, RECYCLABLE, SCANNABLE, TOOL_HEAD, NEEDS_HANDLE).setStacksize(16).aspects(TC.INSTRUMENTUM, 10, TC.PERFODIO, 2);// Items of this can be recycled for Resources.
        miningHammerHead.setMaterialStats(gregapi.data.CS.U * 18); // Any Item of this example Prefix has the value of 1 Material Unit (U), this is exactly equal to one Ingot/Dust/Gem.
        new PrefixItem(MOD_DATA,"qwetech.tools.hammer", miningHammerHead);

        final OreDictPrefix excavatorHead = OreDictPrefix.createPrefix("toolHeadExcavator"); // This newly created OreDict Prefix is named "exampleprefix", so an Aluminium Item with this Prefix would be named "exampleprefixAluminium" in the OreDict.
        excavatorHead.setCategoryName("Excavating Heads"); // That is what the Creative Tab of it would be named.
        excavatorHead.setLocalItemName("", " Excavator Head"); // Generic Items will follow this naming Guideline, so for example "Small Aluminium Example" for an Aluminium Item with that Prefix.
        excavatorHead.setCondition(new And(OP.toolHeadShovel,typemin(2))); // The Condition under which Items of this Prefix should generate in general. In this case TRUE to have ALL the Items.
        excavatorHead.add(UNIFICATABLE, BURNABLE, RECYCLABLE, SCANNABLE, TOOL_HEAD, NEEDS_HANDLE).setStacksize(16).aspects(TC.INSTRUMENTUM, 10, TC.PERFODIO, 2);// Items of this can be recycled for Resources.
        excavatorHead.setMaterialStats(gregapi.data.CS.U * 7); // Any Item of this example Prefix has the value of 1 Material Unit (U), this is exactly equal to one Ingot/Dust/Gem.
        new PrefixItem(MOD_DATA,"qwetech.tools.excavator", excavatorHead);

        shuriken.addListener(new Loader_OreProcessing.OreProcessing_CraftFrom(1, null, new String[][]{{"X X", " f ", "X X"}, {" X ", "XfX", " X "}}, OP.stick, null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        maceHead.addListener(new Loader_OreProcessing.OreProcessing_CraftFrom(1, null, new String[][]{{"YSY", "SXS", "YSY"},{"SYS", "YXY", "SYS"}}, OP.gearGt,OP.bolt, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        mattockHead.addListener(new Loader_OreProcessing.OreProcessing_CraftFrom(1L, null, new String[][]{{"PXh","Y  ","f  "}}, OP.toolHeadAxe,OP.toolHeadShovel, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        sturdyAxeHead.addListener(new Loader_OreProcessing.OreProcessing_CraftFrom(1L, null, new String[][]{{"PPh","PX ","f  "}}, OP.toolHeadAxe,null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        sturdyPickaxeHead.addListener(new Loader_OreProcessing.OreProcessing_CraftFrom(1L, null, new String[][]{{"XPh","P  ","f  "}}, OP.toolHeadConstructionPickaxe,null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        miningHammerHead.addListener(new Loader_OreProcessing.OreProcessing_CraftFrom(1L, null, new String[][]{{"PPh","YX ","PPf"}}, OP.toolHeadHammer,OP.plateDense, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        excavatorHead.addListener(new Loader_OreProcessing.OreProcessing_CraftFrom(1L, null, new String[][]{{"PPP","PXP","f h"}}, OP.toolHeadShovel,null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
        //new PrefixBlock(QwerTech.MODID, QwerTech.MODID, "qwertech.blocks.stake", stake, null, null, null, null, net.minecraft.block.material.Material.wood, net.minecraft.block.Block.soundTypeLadder, null, 1.5F, 4.5F,   0,   0, 999, 0, 0, 0, 1, 1, 1, false, false, false, false, false, false, true, true, true, true, false, true, true, true, gregapi.oredict.OreDictMaterial.MATERIAL_ARRAY);

        if (QTConfigs.enableTools) {
            knucklesTexID = TextureSet.addToAll(QwerTech.MODID, true, "knuckles");
            slingshotTexID = TextureSet.addToAll(QwerTech.MODID, true, "slingshot");
            stringshotTexID = TextureSet.addToAll(QwerTech.MODID, true, "slingstring");
            javelinHeadTexID = TextureSet.addToAll(QwerTech.MODID, true, "javelinHead");
            stakeTexID = TextureSet.addToAll(QwerTech.MODID, true, "stake");
            batTexID = TextureSet.addToAll(QwerTech.MODID, true, "bat");
            batSpikeTexID = TextureSet.addToAll(QwerTech.MODID, true, "batSpike");

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
            OP.stickLong.addListener(new OreProcessing_QTTool(12, ConfigCategories.Recipes.gregtechtools + "." + "Bat", false, false, 0L, 0L, MT.Empty, new String[][]{{"yL"}, {"Ly"}}, null, null, null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
            //GameRegistry.addRecipe(new AdvancedCraftingTool(qwerTool, 12, OP.stickLong, MT.WoodTreated));
            qwerTool.addTool(14, "LumberAxe", "Jack Jack", new QT_Tool_Lumberaxe().setMaterialAmount(OP.toolHeadAxe.mAmount), "craftingToolAxe", TC.stack(TC.INSTRUMENTUM, 2), TC.stack(TC.ARBOR, 1), TC.stack(TC.MACHINA, 1), "axe");
            GameRegistry.addRecipe(new AdvancedCraftingTool(qwerTool, 14, sturdyAxeHead,MT.Steel));
            qwerTool.addTool(16, "Sturdy Pickaxe", "Mines a simple 1x2 tunnel, Pretty safe won't dig the tunnel if the block isn't right in front of you", new QT_Tool_SturdyPickaxe().setMaterialAmount(OP.toolHeadPickaxe.mAmount), "craftingToolPickaxe", TC.stack(TC.INSTRUMENTUM, 2), TC.stack(TC.PERDITIO, 1), "pickaxe");
            GameRegistry.addRecipe(new AdvancedCraftingTool(qwerTool, 16, sturdyPickaxeHead,MT.Steel));
            qwerTool.addTool(18, "Kazoo", "A true " + LH.Chat.ITALIC + "instrument" + LH.Chat.RESET + LH.Chat.GRAY + " of torture", new QT_Tool_Kazoo().setMaterialAmount(OP.stick.mAmount), "kazoo");
            OP.ring.addListener(new OreProcessing_QTTool(18, ConfigCategories.Recipes.gregtechtools + "." + "Kazoo", false, false, 0L, 0L, MT.Paper, new String[][]{{"XO ", " Sk"}}, null, ST.make(Items.paper, 1, 0), null, null, null, null, TD.Atomic.ANTIMATTER.NOT));
            qwerTool.addTool(20, "Mining Hammer", "Tinker's Hammer, Mines a 3x3 Area", new QT_TOOL_MiningHammer().setMaterialAmount(miningHammerHead.mAmount), "craftingToolPickaxe", TC.stack(TC.INSTRUMENTUM, 10), TC.stack(TC.PERDITIO, 7), "hammer");
            GameRegistry.addRecipe(new AdvancedCraftingTool(qwerTool, 20, miningHammerHead,MT.Steel));
            qwerTool.addTool(22, "Excavator", "Digs up a 3x3 Area", new QT_Tool_Excavator().setMaterialAmount(excavatorHead.mAmount), "craftingToolShovel", TC.stack(TC.INSTRUMENTUM, 10), TC.stack(TC.PERDITIO, 7), "shovel");
            GameRegistry.addRecipe(new AdvancedCraftingTool(qwerTool, 22, excavatorHead,MT.Steel));
        }
        if (QTConfigs.enableArmor) {
            MinecraftForge.EVENT_BUS.register(new RegisterArmor());
        }
    }

    @Override
    public void onModInit2(FMLInitializationEvent aEvent) {

        //MinecraftForge.EVENT_BUS.register(new RegisterMobs());
        new RegisterMobs();
        MinecraftForge.EVENT_BUS.register(new QT_GUIHandler());

        RegisterMaterials.instance.registerRecipes();

        RegisterLoot.init();

        ModLoadBase.runInit();

        // TODO what is this?! //CS.ToolsGT.sMetaTool.addItemBehavior(CS.ToolsGT.WRENCH, new Behavior_Slingshot("", 20, 40));

        CR.shaped(ST.make(soilBlock, 2, 8), CR.DEF, "AA", "AA", 'A', "treeLeaves");
        CR.shaped(ST.make(soilBlock, 1, 5), CR.DEF, "AA", 'A', ST.make(Blocks.leaves, 1, 1));
        CR.shaped(ST.make(soilBlock, 1, 1), CR.DEF, "AAA", "A A", "AAA", 'A', OP.scrapGt.mat(MT.Wood, 1));

        RM.Mixer.addRecipe2(true, 16L, 16L, ST.make(Blocks.sand, 1, 0), ST.make(soilBlock, 2, 4), (FluidStack) null, null, ST.make(soilBlock, 2, 0));
        RM.Mixer.addRecipe2(true, 16L, 16L, ST.make(CS.BlocksGT.Sands, 1, 0), ST.make(CS.BlocksGT.Diggables, 2, 0), (FluidStack) null, null, ST.make(soilBlock, 2, 0));

        RM.Crusher.addRecipe1(true, 16L, 16L, ST.make(Items.stick, 8, 0), (FluidStack) null, null, ST.make(soilBlock, 1, 1));
        RM.Crusher.addRecipe1(true, 16L, 16L, IL.Bark_Dry.get(8), (FluidStack) null, null, ST.make(soilBlock, 1, 2));

        CR.shaped(qwerTool.getToolWithStats(2, MT.Wood, MT.Wood), CR.DEF, "AAA", "BfB", " B ", 'A', ST.make(Items.string, 1, 0), 'B', OP.stick.mat(ANY.Wood, 1));
        CR.shaped(qwerTool.getToolWithStats(2, MT.Bone, MT.Bone), CR.DEF, "AAA", "BfB", " B ", 'A', ST.make(Items.string, 1, 0), 'B', ST.make(Items.bone, 1, 0));
        CR.shaped(qwerTool.getToolWithStats(2, MT.Rubber, MT.Rubber), CR.DEF, "AAA", "BfB", " B ", 'A', ST.make(Items.string, 1, 0), 'B', OP.stick.mat(MT.Rubber, 1));
        CR.shaped(qwerTool.getToolWithStats(2, MT.Blaze, MT.Blaze), CR.DEF, "AAA", "BfB", " B ", 'A', ST.make(Items.string, 1, 0), 'B', ST.make(Items.blaze_rod, 1, 0));

        if (OreDictionary.doesOreNameExist("cropOnion") && OreDictionary.doesOreNameExist("cropGarlic") && OreDictionary.doesOreNameExist("cropSpiceleaf")) {
            RM.Mixer.addRecipe2(true, 16L, 16L, OreDictionary.getOres("cropGarlic").get(0), OreDictionary.getOres("cropOnion").get(0), UT.Fluids.make("binnie.juicetomato", 500), UT.Fluids.make("mildsalsa", 500), (ItemStack[]) null);
            RM.Mixer.addRecipe2(true, 16L, 16L, OreDictionary.getOres("cropBellpepper").get(0), OreDictionary.getOres("cropSpiceleaf").get(0), UT.Fluids.make("mildsalsa", 500), UT.Fluids.make("salsa", 500), (ItemStack[]) null);
            RM.Mixer.addRecipe2(true, 16L, 16L, OreDictionary.getOres("cropChilipepper").get(0), OreDictionary.getOres("cropChilipepper").get(0), UT.Fluids.make("salsa", 500), UT.Fluids.make("hotsalsa", 500), (ItemStack[]) null);
        }

        RM.Mixer.addRecipe1(true, 16L, 16L, ST.make(Items.sugar, 1, 0), UT.Fluids.make("water", 1000), UT.Fluids.make("sugarwater", 1000), (ItemStack[]) null);
        RM.Mixer.addRecipe1(true, 16L, 16L, OP.dust.mat(MT.Wheat, 1), UT.Fluids.make("sugarwater", 250), null, IL.Food_Dough_Sugar.get(2));
        RM.Mixer.addRecipe1(true, 16L, 16L, OP.dust.mat(MT.Barley, 1), UT.Fluids.make("sugarwater", 250), null, IL.Food_Dough_Sugar.get(2));
        RM.Mixer.addRecipe1(true, 16L, 16L, OP.dust.mat(MT.Rye, 1), UT.Fluids.make("sugarwater", 250), null, IL.Food_Dough_Sugar.get(2));
        RM.Mixer.addRecipe1(true, 16L, 16L, OP.dust.mat(MT.Oat, 1), UT.Fluids.make("sugarwater", 250), null, IL.Food_Dough_Sugar.get(2));
        RM.Mixer.addRecipe1(true, 16L, 16L, OP.dust.mat(MT.Potato, 1), UT.Fluids.make("sugarwater", 250), null, IL.Food_Dough_Sugar.get(2));

//
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.toolHeadScrewdriver, OP.toolHeadScrewdriver}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.toolHeadScrewdriver, OP.toolHeadFile}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.toolHeadFile, OP.toolHeadFile}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.toolHeadScrewdriver, OP.toolHeadChisel}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.toolHeadFile, OP.toolHeadChisel}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.toolHeadChisel, OP.toolHeadChisel}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.screw, OP.toolHeadScrewdriver}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.screw, OP.toolHeadFile}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.screw, OP.toolHeadChisel}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.screw, OP.screw}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.toolHeadArrow, OP.toolHeadScrewdriver}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.toolHeadArrow, OP.toolHeadFile}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.toolHeadArrow, OP.toolHeadChisel}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.toolHeadArrow, OP.screw}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//        GameRegistry.addRecipe(new AnyQTTool(12L, new And(OP.stickLong, TD.Atomic.ANTIMATTER.NOT), false, null, new Object[]{OP.toolHeadArrow, OP.toolHeadArrow}, qwerTool.getToolWithStats(12, MT.NULL, MT.Empty)));
//
//        GameRegistry.addRecipe(new AnyQTTool(14L, new And(TD.Atomic.ANTIMATTER.NOT), true, new Object[]{OP.toolHeadAxe, OP.plate}, new Object[]{OP.stickLong}));
//
//        GameRegistry.addRecipe(new AnyQTTool(16L, new And(TD.Atomic.ANTIMATTER.NOT), true, new Object[]{OP.toolHeadShovel, OP.plate}, new Object[]{OP.stickLong}));
//
//        GameRegistry.addRecipe(new AnyQTTool(18L, new And(TD.Atomic.ANTIMATTER.NOT), true, new Object[]{OP.toolHeadPickaxe, OP.plate}, new Object[]{OP.stickLong}));

        OreDictMaterial[] wallmats = new OreDictMaterial[]{MT.Fe, MT.Al, MT.Au, MT.Steel, MT.Bronze, MT.Brass, MT.Ag, MT.StainlessSteel, MT.WroughtIron, MT.Plastic, MT.Ti, MT.TungstenSteel, MT.Invar, MT.TinAlloy, MT.SteelGalvanized, MT.Electrum};
        for (int q = 0; q < 16; q++) {
            RM.RollFormer.addRecipe1(true, 16, 768, OP.plateDouble.mat(wallmats[q], 1), ST.make(corrugatedBlock, 4, q));
            RM.RollFormer.addRecipe1(true, 16, 768, OP.plate.mat(wallmats[q], 1), ST.make(corrugatedBlock, 2, q));
            RM.RollingMill.addRecipe1(true, 16, 768, ST.make(corrugatedBlock, 2, q), OP.plate.mat(wallmats[q], 1));
            CR.shaped(ST.make(corrugatedBlock, 12, q), new Object[]{"PPP","PPP"," h ",'P', OP.plate.mat(wallmats[q], 1)});
        }

        OreDictMaterial[] upgradeDeskMats = new OreDictMaterial[]{MT.Bronze, MT.Co, MT.Au, MT.Obsidian, MT.Plastic, MT.Ag};
        for (int q = 0; q < upgradeDeskMats.length; q++) {
            OreDictMaterial mat = upgradeDeskMats[q];
            ItemStack desk = armor_upgrade_desk.add(mat.mNameLocal + " Upgrade Desk", "Upgrade Desks", 0 + q, 0, UpgradeDesk.class, 0, 16, metal, UT.NBT.make(NullBT, CS.NBT_MATERIAL, mat, CS.NBT_INV_SIZE, 1, CS.NBT_TEXTURE, "qwertech:metal", CS.NBT_HARDNESS, 3.0F, CS.NBT_RESISTANCE, 3.0F, CS.NBT_COLOR, UT.Code.getRGBInt(mat.fRGBaSolid)), "RfR", "RSR", "CCC", 'C', OP.plate.dat(mat), 'R', OP.stick.dat(ANY.Steel), 'S', OP.springSmall.dat(ANY.Steel));
            OreDictManager.INSTANCE.registerOre("upgradeDesk",desk);
        }

        for (int q = 1; q < WOOD.woodList.length; q++) {
            OreDictMaterial woodType = WOOD.woodList[q];
            if (woodType != null && !woodType.mHidden) {
                machines.add(woodType.mNameLocal + " Chest", "Chests", 1510 + q, 0, ChestTileEntity.class, 0, 64, wood, UT.NBT.make(NullBT, CS.NBT_MATERIAL, woodType, CS.NBT_INV_SIZE, 27, CS.NBT_TEXTURE, "woodenchest", CS.NBT_HARDNESS, 3.0F, CS.NBT_RESISTANCE, 3.0F, CS.NBT_COLOR, UT.Code.getRGBInt(woodType.fRGBaSolid)));
                CR.shapeless(ST.make(Blocks.chest, 1, 0), CR.DEF, new Object[]{machines.getItem(1510 + q)});
                CR.shaped(machines.getItem(1510 + q),CR.DEF,"rPa", "RSR", "PWP", 'P', OP.plank.mat(woodType,1), 'S', OP.stick.mat(woodType,1), 'W', OP.plank.mat(woodType,1), 'R', OP.ring.mat(woodType,1));
                CR.shaped(machines.getItem(1510 + q),CR.DEF,"rPs", "RSR", "PWP", 'P', OP.plank.mat(woodType,1), 'S', OP.stick.mat(woodType,1), 'W', OP.plank.mat(woodType,1), 'R', OP.ring.mat(woodType,1));

                OM.reg(OD.craftingChest, machines.getItem(1510 + q));
                OM.reg("craftingChestWood", machines.getItem(1510 + q));
                machines.add(woodType.mNameLocal + " Nesting Box", "Nesting Boxes", 1780 + q, 0, NestBoxTileEntity.class, 0, 16, wood, UT.NBT.make(NullBT, CS.NBT_MATERIAL, woodType, CS.NBT_INV_SIZE, 5, CS.NBT_TEXTURE, "qwertech:wood", CS.NBT_HARDNESS, 3.0F, CS.NBT_RESISTANCE, 3.0F, CS.NBT_COLOR, UT.Code.getRGBInt(woodType.fRGBaSolid)), "GGP", "PPP", 'P', "plank" + woodType.mNameInternal, 'G', IL.Grass.get(1));
            }
        }
        machines.add("Wooden Chest", "Chests", 1510, 0, ChestTileEntity.class, 0, 64, wood, UT.NBT.make(NullBT, CS.NBT_MATERIAL, MT.Wood, CS.NBT_INV_SIZE, 27, CS.NBT_TEXTURE, "woodenchest", CS.NBT_HARDNESS, 3.0F, CS.NBT_RESISTANCE, 3.0F, CS.NBT_COLOR, UT.Code.getRGBInt(MT.Wood.fRGBaSolid)));
        CR.shapeless(ST.make(Blocks.chest, 1, 0), CR.DEF, new Object[]{machines.getItem(1510)});
        OM.reg(OD.craftingChest, machines.getItem(1510));
        OM.reg("craftingChestWood", machines.getItem(1510));
        // TODO make config
        GameRegistry.addRecipe(new WoodSpecificCrafting(machines.getItem(1510), "PPP", "P P", "PPP", 'P', "plankWood"));
        // [SHammer,<ore:plateAnyWood>,<ore:craftingToolSawAxe>],[<ore:ringAnyWood>,<ore:stickAnyWood>,<ore:ringAnyWood>],[<ore:plateAnyWood>,<ore:beamWood>,<ore:plateAnyWood>]

        for (int q = 1; q < WOOD.woodList.length; q++) {
            OreDictMaterial woodType = WOOD.woodList[q];
            if (woodType != null && !woodType.mHidden) {
                machines.add(woodType.mNameLocal + " Nesting Box", "Nesting Boxes", 1780 + q, 0, NestBoxTileEntity.class, 0, 16, wood, UT.NBT.make(NullBT, CS.NBT_MATERIAL, woodType, CS.NBT_INV_SIZE, 5, CS.NBT_TEXTURE, "qwertech:wood", CS.NBT_HARDNESS, 3.0F, CS.NBT_RESISTANCE, 3.0F, CS.NBT_COLOR, UT.Code.getRGBInt(woodType.fRGBaSolid)), "GGP", "PPP", 'P', "plank" + woodType.mNameInternal, 'G', IL.Grass.get(1));
            }
        }
        machines.add("Nest (ground)", "Natural", 1770, -1, NestTileEntity.class, 0, 1, wood, UT.NBT.make(NullBT, CS.NBT_INV_SIZE, 5, CS.NBT_HARDNESS, 1.0F, CS.NBT_RESISTANCE, 1.0F));
        OreDictMaterial[] nestBoxMats = new OreDictMaterial[]{MT.Plastic, MT.Steel, MT.Bronze, MT.Brass, MT.Cu, MT.Ag, MT.Au, MT.Invar, MT.Electrum, MT.Concrete, MT.Asphalt, MT.Al, MT.Ti, MT.StainlessSteel, MT.SteelGalvanized, MT.Pt, MT.Ceramic};
        for (int q = 0; q < nestBoxMats.length; q++) {
            OreDictMaterial mat = nestBoxMats[q];
            machines.add(mat.mNameLocal + " Nesting Box", "Nesting Boxes", 2040 + q, 0, NestBoxTileEntity.class, 0, 16, metal, UT.NBT.make(NullBT, CS.NBT_MATERIAL, mat, CS.NBT_INV_SIZE, 5, CS.NBT_HARDNESS, 3.0F, CS.NBT_RESISTANCE, 3.0F, CS.NBT_COLOR, UT.Code.getRGBInt(mat.fRGBaSolid)), "GGP", "PPP", 'P', "plate" + mat.mNameInternal, 'G', IL.Grass.get(1));
        }
    }

    @Override
    public void onModPostInit2(FMLPostInitializationEvent aEvent) {
        for (int q = 0; q < CS.BlocksGT.stones.length; q++) {
            BlockStones block = (BlockStones) CS.BlocksGT.stones[q];
//            machines.add(block.mMaterial.mNameLocal + " Countertop", "Countertops", 667 + (q * 3), 0, CuttingBoardTileEntity.class, 0, 16, metal, UT.NBT.make(NullBT, CS.NBT_MATERIAL, block.mMaterial, CS.NBT_INV_SIZE, 9, CS.NBT_TEXTURE, q, "qt.metatex", 0, CS.NBT_HARDNESS, 3.0F, CS.NBT_RESISTANCE, 3.0F, CS.NBT_COLOR, UT.Code.getRGBInt(block.mMaterial.fRGBaSolid)), "S", "P", 'S', ST.make(block.mSlabs[CS.SIDE_DOWN], 1, 0), 'P', ST.make(block, 1, 0));
//            machines.add("Smooth " + block.mMaterial.mNameLocal + " Countertop", "Countertops", 667 + (q * 3) + 1, 0, CuttingBoardTileEntity.class, 0, 16, metal, UT.NBT.make(NullBT, CS.NBT_MATERIAL, block.mMaterial, CS.NBT_INV_SIZE, 9, CS.NBT_TEXTURE, q, "qt.metatex", 7, CS.NBT_HARDNESS, 3.0F, CS.NBT_RESISTANCE, 3.0F, CS.NBT_COLOR, UT.Code.getRGBInt(block.mMaterial.fRGBaSolid)), "S", "P", 'S', ST.make(block.mSlabs[CS.SIDE_DOWN], 1, 7), 'P', ST.make(block, 1, 7));
//            machines.add(block.mMaterial.mNameLocal + " Cobblestone Countertop", "Countertops", 667 + (q * 3) + 2, 0, CuttingBoardTileEntity.class, 0, 16, metal, UT.NBT.make(NullBT, CS.NBT_MATERIAL, block.mMaterial, CS.NBT_INV_SIZE, 9, CS.NBT_TEXTURE, q, "qt.metatex", 1, CS.NBT_HARDNESS, 3.0F, CS.NBT_RESISTANCE, 3.0F, CS.NBT_COLOR, UT.Code.getRGBInt(block.mMaterial.fRGBaSolid)), "S", "P", 'S', ST.make(block.mSlabs[CS.SIDE_DOWN], 1, 1), 'P', ST.make(block, 1, 1));
//
//            machines.add(block.mMaterial.mNameLocal + " Counterdrawers", "Counterdrawers", 1250 + (q * 3), 0, CountertopShelvesTileEntity.class, 0, 16, metal, UT.NBT.make(NullBT, CS.NBT_MATERIAL, block.mMaterial, CS.NBT_INV_SIZE, 9 + 18, CS.NBT_TEXTURE, q, "qt.metatex", 0, CS.NBT_HARDNESS, 3.0F, CS.NBT_RESISTANCE, 3.0F, CS.NBT_COLOR, UT.Code.getRGBInt(block.mMaterial.fRGBaSolid)), "GCG", "PHP", 'C', machines.getItem(667 + (q * 3)), 'P', ST.make(block, 1, 0), 'H', OD.craftingChest, 'G', OD.itemGlue);
//            CR.shaped(machines.getItem(1250 + (q * 3)), new Object[]{"GCG", "PHP", 'C', machines.getItem(667 + (q * 3)), 'P', ST.make(block, 1, 0), 'H', OD.craftingChest, 'G', OD.slimeball});
//            machines.add("Smooth " + block.mMaterial.mNameLocal + " Counterdrawers", "Counterdrawers", 1251 + (q * 3), 0, CountertopShelvesTileEntity.class, 0, 16, metal, UT.NBT.make(NullBT, CS.NBT_MATERIAL, block.mMaterial, CS.NBT_INV_SIZE, 9 + 18, CS.NBT_TEXTURE, q, "qt.metatex", 7, CS.NBT_HARDNESS, 3.0F, CS.NBT_RESISTANCE, 3.0F, CS.NBT_COLOR, UT.Code.getRGBInt(block.mMaterial.fRGBaSolid)), "GCG", "PHP", 'C', machines.getItem(668 + (q * 3)), 'P', ST.make(block, 1, 7), 'H', OD.craftingChest, 'G', OD.itemGlue);
//            CR.shaped(machines.getItem(1251 + (q * 3)), new Object[]{"GCG", "PHP", 'C', machines.getItem(668 + (q * 3)), 'P', ST.make(block, 1, 7), 'H', OD.craftingChest, 'G', OD.slimeball});
//            machines.add(block.mMaterial.mNameLocal + " Counterdrawers", "Counterdrawers", 1252 + (q * 3), 0, CountertopShelvesTileEntity.class, 0, 16, metal, UT.NBT.make(NullBT, CS.NBT_MATERIAL, block.mMaterial, CS.NBT_INV_SIZE, 9 + 18, CS.NBT_TEXTURE, q, "qt.metatex", 1, CS.NBT_HARDNESS, 3.0F, CS.NBT_RESISTANCE, 3.0F, CS.NBT_COLOR, UT.Code.getRGBInt(block.mMaterial.fRGBaSolid)), "GCG", "PHP", 'C', machines.getItem(669 + (q * 3)), 'P', ST.make(block, 1, 1), 'H', OD.craftingChest, 'G', OD.itemGlue);
//            CR.shaped(machines.getItem(1252 + (q * 3)), new Object[]{"GCG", "PHP", 'C', machines.getItem(669 + (q * 3)), 'P', ST.make(block, 1, 1), 'H', OD.craftingChest, 'G', OD.slimeball});
        }

        RegisterArmor.instance.addUpgrades();

        //CraftingManagerHammer.replacems.put(ST.make(Items.feather, 1, 0), "itemFeather");
        //CraftingManagerHammer.replacems.put(ST.make(Blocks.chest, 1, 0), "craftingChest");
        //CraftingManagerHammer.replacems.put(ST.make(Blocks.crafting_table, 1, 0), "craftingWorkBench");
        //CS.GT.mAfterPostInit.add(CraftingManagerHammer.getInstance());
        //CS.GT.mAfterPostInit.add(CraftingManager3D.getInstance());

        // NEI Catalyst Plugin for GT
        Recipe.RecipeMap.RECIPE_MAP_LIST.forEach(map->{
            for (int i = 0; i < map.mRecipeMachineList.toArray().length; i++) {
                RecipeCatalysts.addRecipeCatalyst(map.mNameNEI,new CatalystInfo((ItemStack) map.mRecipeMachineList.toArray()[i],i));
            }
        });
    }

    @Mod.EventHandler
    public void onModsLoaded(FMLLoadCompleteEvent event) {
        CraftingManagerCountertop.getInstance().run();
        ModLoadBase.runPostInit();
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
