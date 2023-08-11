package me.mekb.homeappliances.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.util.ArrayList;

public class CoffeeTableBlock extends TemplateTableBlock {
    // shapes

    private static final VoxelShape TOP_SHAPE = VoxelShapes.cuboid(0, 8.0/16.0, 0, 1, 11.0/16.0, 1);

    private static final VoxelShape LEG_SHAPE = VoxelShapes.cuboid(0, 0, 0, 3.0/16.0, 8.0/16.0, 3.0/16.0);
    private static final VoxelShape NORTH_WEST_LEG_SHAPE = LEG_SHAPE.offset(1.0/16.0, 0, 1.0/16.0);
    private static final VoxelShape NORTH_EAST_LEG_SHAPE = LEG_SHAPE.offset(12.0/16.0, 0, 1.0/16.0);
    private static final VoxelShape SOUTH_EAST_LEG_SHAPE = LEG_SHAPE.offset(12.0/16.0, 0, 12.0/16.0);
    private static final VoxelShape SOUTH_WEST_LEG_SHAPE = LEG_SHAPE.offset(1.0/16.0, 0, 12.0/16.0);
    private static final VoxelShape NORTH_LEG_SHAPE = LEG_SHAPE.offset(6.5/16.0, 0, 1.0/16.0);
    private static final VoxelShape EAST_LEG_SHAPE = LEG_SHAPE.offset(12.0/16.0, 0, 6.5/16.0);
    private static final VoxelShape SOUTH_LEG_SHAPE = LEG_SHAPE.offset(6.5/16.0, 0, 12.0/16.0);
    private static final VoxelShape WEST_LEG_SHAPE = LEG_SHAPE.offset(1.0/16.0, 0, 6.5/16.0);
    private static final VoxelShape MIDDLE_LEG_SHAPE = LEG_SHAPE.offset(6.5/16.0, 0, 6.5/16.0);

    public CoffeeTableBlock(Block copyBlock) {
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
