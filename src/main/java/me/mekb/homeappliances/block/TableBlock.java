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

public class TableBlock extends Block implements Waterloggable {
    public TableBlock(Block copyBlock) {
        super(FabricBlockSettings.copy(copyBlock).nonOpaque());
        // default state
        setDefaultState(getDefaultState()
                .with(NORTH_WEST_LEG, true).with(NORTH_EAST_LEG, true)
                .with(SOUTH_EAST_LEG, true).with(SOUTH_WEST_LEG, true)
                .with(NORTH_LEG, true).with(EAST_LEG, true)
                .with(SOUTH_LEG, true).with(WEST_LEG, true)
                .with(WATERLOGGED, false));
    }

    public static final BooleanProperty NORTH_WEST_LEG = BooleanProperty.of("north_west_leg");
    public static final BooleanProperty NORTH_EAST_LEG = BooleanProperty.of("north_east_leg");
    public static final BooleanProperty SOUTH_EAST_LEG = BooleanProperty.of("south_east_leg");
    public static final BooleanProperty SOUTH_WEST_LEG = BooleanProperty.of("south_west_leg");
    public static final BooleanProperty NORTH_LEG = BooleanProperty.of("north_leg");
    public static final BooleanProperty EAST_LEG  = BooleanProperty.of("east_leg");
    public static final BooleanProperty SOUTH_LEG = BooleanProperty.of("south_leg");
    public static final BooleanProperty WEST_LEG  = BooleanProperty.of("west_leg");
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder
                .add(NORTH_WEST_LEG).add(NORTH_EAST_LEG).add(SOUTH_EAST_LEG).add(SOUTH_WEST_LEG)
                .add(NORTH_LEG).add(EAST_LEG).add(SOUTH_LEG).add(WEST_LEG)
                .add(WATERLOGGED);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        // return true if block below is solid
        return Block.hasTopRim(world, pos.down());
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (state.canPlaceAt(world, pos)) return;
        // remove block with drop, if the block below was destroyed
        Block.dropStacks(state, world, pos, null);
        world.removeBlock(pos, false);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getStateForNeighborUpdate(
                getDefaultState().with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER),
                null, null, ctx.getWorld(), ctx.getBlockPos(), null);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) return state;

        Block block = state.getBlock();
        boolean north = world.getBlockState(pos.north()).isOf(block);
        boolean east  = world.getBlockState(pos.east()) .isOf(block);
        boolean south = world.getBlockState(pos.south()).isOf(block);
        boolean west  = world.getBlockState(pos.west()) .isOf(block);

        boolean north_west_leg = true, north_east_leg = true, south_east_leg = true, south_west_leg = true,
                north_leg = false, east_leg = false, south_leg = false, west_leg = false;

        // calculate what legs should exist for different connections
        if (north && east && south && west) {
            north_east_leg = north_west_leg = south_west_leg = south_east_leg = false;
            north_leg = east_leg = true; // this will never happen otherwise, this is so we can save another blockstate
        } else if ((!north || !south) && (!east || !west)) {
            if (north)
                north_east_leg = north_west_leg = false;
            if (east)
                south_east_leg = north_east_leg = false;
            if (south)
                south_west_leg = south_east_leg = false;
            if (west)
                north_west_leg = south_west_leg = false;
        } else if ((north ? 1 : 0) + (east ? 1 : 0) + (south ? 1 : 0) + (west ? 1 : 0) == 3) {
            north_east_leg = north_west_leg = south_west_leg = south_east_leg = false;
            if (north && south) {
                if (west)
                    east_leg = true;
                else
                    west_leg = true;
            } else {
                if (north)
                    south_leg = true;
                else
                    north_leg = true;
            }
        } else if (north) {
            // north + south
            north_east_leg = north_west_leg = south_west_leg = south_east_leg = false;
            east_leg = west_leg = true;
        } else {
            // east + west
            north_east_leg = north_west_leg = south_west_leg = south_east_leg = false;
            north_leg = south_leg = true;
        }

        // https://fabricmc.net/wiki/tutorial:waterloggable
        if (state.get(WATERLOGGED))
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
                .with(NORTH_WEST_LEG, north_west_leg).with(NORTH_EAST_LEG, north_east_leg)
                .with(SOUTH_EAST_LEG, south_east_leg).with(SOUTH_WEST_LEG, south_west_leg)
                .with(NORTH_LEG, north_leg).with(EAST_LEG, east_leg)
                .with(SOUTH_LEG, south_leg).with(WEST_LEG, west_leg);
    }

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
