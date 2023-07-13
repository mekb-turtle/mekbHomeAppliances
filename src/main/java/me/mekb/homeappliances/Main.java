package me.mekb.homeappliances;

import me.mekb.homeappliances.block.ChairBlock;
import me.mekb.homeappliances.block.TableBlock;
import me.mekb.homeappliances.entity.ChairEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Main implements ModInitializer {
    private static final String namespace = "mekbhomeappliances";

    public static EntityType<ChairEntity> CHAIR_ENTITY_TYPE = null;

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

        Registries.BLOCK.forEach((Block block) -> {
            String name = Registries.BLOCK.getId(block).getPath();
            if (name.endsWith("_planks")) {
                name = name.substring(0, name.length() - 6);

                FlammableBlockRegistry.Entry flammableBlock = flammable.get(block);

                // add table for every wood type
                Identifier tableID = new Identifier(namespace, name + "table");
                TableBlock tableBlock = new TableBlock(block);
                BlockItem tableItem = new BlockItem(tableBlock, new FabricItemSettings());
                Registry.register(Registries.BLOCK, tableID, tableBlock);
                Registry.register(Registries.ITEM, tableID, tableItem);
                if (flammableBlock != null) flammable.add(tableBlock, flammableBlock.getBurnChance(), flammableBlock.getSpreadChance());

                // add chair for every wood type
                Identifier chairID = new Identifier(namespace, name + "chair");
                ChairBlock chairBlock = new ChairBlock(block);
                BlockItem chairItem = new BlockItem(chairBlock, new FabricItemSettings());
                Registry.register(Registries.BLOCK, chairID, chairBlock);
                Registry.register(Registries.ITEM, chairID, chairItem);
                if (flammableBlock != null) flammable.add(chairBlock, flammableBlock.getBurnChance(), flammableBlock.getSpreadChance());

                // add to creative inventory tabs
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
                    content.add(tableItem);
                    content.add(chairItem);
                });
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> content.add(chairItem));
            }
        });
    }
}
