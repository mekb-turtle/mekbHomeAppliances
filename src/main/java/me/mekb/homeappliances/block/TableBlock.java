package me.mekb.homeappliances.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.ArrayList;

public class TableBlock extends TemplateTableBlock {
    // shapes

    private static final VoxelShape TOP_SHAPE = VoxelShapes.cuboid(0, 13.0/16.0, 0, 1, 1, 1);

    private static final VoxelShape LEG_SHAPE = VoxelShapes.cuboid(0, 0, 0, 3.0/16.0, 13.0/16.0, 3.0/16.0);
    private static final VoxelShape NORTH_WEST_LEG_SHAPE = LEG_SHAPE.offset(1.0/16.0, 0, 1.0/16.0);
    private static final VoxelShape NORTH_EAST_LEG_SHAPE = LEG_SHAPE.offset(12.0/16.0, 0, 1.0/16.0);
    private static final VoxelShape SOUTH_EAST_LEG_SHAPE = LEG_SHAPE.offset(12.0/16.0, 0, 12.0/16.0);
    private static final VoxelShape SOUTH_WEST_LEG_SHAPE = LEG_SHAPE.offset(1.0/16.0, 0, 12.0/16.0);
    private static final VoxelShape NORTH_LEG_SHAPE = LEG_SHAPE.offset(6.5/16.0, 0, 1.0/16.0);
    private static final VoxelShape EAST_LEG_SHAPE = LEG_SHAPE.offset(12.0/16.0, 0, 6.5/16.0);
    private static final VoxelShape SOUTH_LEG_SHAPE = LEG_SHAPE.offset(6.5/16.0, 0, 12.0/16.0);
    private static final VoxelShape WEST_LEG_SHAPE = LEG_SHAPE.offset(1.0/16.0, 0, 6.5/16.0);
    private static final VoxelShape MIDDLE_LEG_SHAPE = LEG_SHAPE.offset(6.5/16.0, 0, 6.5/16.0);

    public TableBlock(Block copyBlock) {
        super(copyBlock);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        ArrayList<VoxelShape> shape = new ArrayList<>();
        // add legs
        if (state.get(NORTH_LEG) && state.get(EAST_LEG)) {
            shape.add(MIDDLE_LEG_SHAPE);
        } else {
            if (state.get(NORTH_WEST_LEG)) shape.add(NORTH_WEST_LEG_SHAPE);
            if (state.get(NORTH_EAST_LEG)) shape.add(NORTH_EAST_LEG_SHAPE);
            if (state.get(SOUTH_EAST_LEG)) shape.add(SOUTH_EAST_LEG_SHAPE);
            if (state.get(SOUTH_WEST_LEG)) shape.add(SOUTH_WEST_LEG_SHAPE);
            if (state.get(NORTH_LEG)) shape.add(NORTH_LEG_SHAPE);
            if (state.get(EAST_LEG)) shape.add(EAST_LEG_SHAPE);
            if (state.get(SOUTH_LEG)) shape.add(SOUTH_LEG_SHAPE);
            if (state.get(WEST_LEG)) shape.add(WEST_LEG_SHAPE);
        }
        return shape.stream().reduce(TOP_SHAPE, VoxelShapes::union);
    }
}
