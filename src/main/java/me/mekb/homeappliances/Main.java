package me.mekb.homeappliances;

import me.mekb.homeappliances.block.ChairBlock;
import me.mekb.homeappliances.block.CoffeeTableBlock;
import me.mekb.homeappliances.block.TableBlock;
import me.mekb.homeappliances.entity.ChairEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

public class Main implements ModInitializer {
    private static final String namespace = "mekbhomeappliances";

    public static EntityType<ChairEntity> CHAIR_ENTITY_TYPE = null;

    private static final List<String> woodTypes = Arrays.asList(
            "oak", "spruce", "birch", "jungle",
            "acacia", "dark_oak", "mangrove", "cherry",
            "bamboo", "crimson", "warped"
    );

    @Override
    public void onInitialize() {
        CHAIR_ENTITY_TYPE = Registry.register(
                Registries.ENTITY_TYPE,
                new Identifier(namespace, "chair"),
                FabricEntityTypeBuilder
                        .create(SpawnGroup.MISC, ChairEntity::new)
                        .dimensions(EntityDimensions.fixed(1.0f/16.0f, 1.0f/16.0f)).build()
        );

        FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();

        for (String name : woodTypes) {
            Identifier blockID = new Identifier("minecraft", name + "_planks");
            Block block = Registries.BLOCK.get(blockID);
            if (block != Blocks.AIR) {
                FlammableBlockRegistry.Entry flammableBlock = flammable.get(block);

                // add table for every wood type
                Identifier tableID = new Identifier(namespace, name + "_table");
                TableBlock tableBlock = new TableBlock(block);
                BlockItem tableItem = new BlockItem(tableBlock, new FabricItemSettings());
                Registry.register(Registries.BLOCK, tableID, tableBlock);
                Registry.register(Registries.ITEM, tableID, tableItem);
                if (flammableBlock != null)
                    flammable.add(tableBlock, flammableBlock.getBurnChance(), flammableBlock.getSpreadChance());

                // add coffee table for every wood type
                Identifier coffeeTableID = new Identifier(namespace, name + "_coffee_table");
                CoffeeTableBlock coffeeTableBlock = new CoffeeTableBlock(block);
                BlockItem coffeeTableItem = new BlockItem(coffeeTableBlock, new FabricItemSettings());
                Registry.register(Registries.BLOCK, coffeeTableID, coffeeTableBlock);
                Registry.register(Registries.ITEM, coffeeTableID, coffeeTableItem);
                if (flammableBlock != null)
                    flammable.add(coffeeTableBlock, flammableBlock.getBurnChance(), flammableBlock.getSpreadChance());

                // add chair for every wood type
                Identifier chairID = new Identifier(namespace, name + "_chair");
                ChairBlock chairBlock = new ChairBlock(block);
                BlockItem chairItem = new BlockItem(chairBlock, new FabricItemSettings());
                Registry.register(Registries.BLOCK, chairID, chairBlock);
                Registry.register(Registries.ITEM, chairID, chairItem);
                if (flammableBlock != null)
                    flammable.add(chairBlock, flammableBlock.getBurnChance(), flammableBlock.getSpreadChance());

                // add to creative inventory tabs
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
                    content.add(tableItem);
                    content.add(coffeeTableItem);
                    content.add(chairItem);
                });
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> content.add(chairItem));
            }
        }
    }
}
