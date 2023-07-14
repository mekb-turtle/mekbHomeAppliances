package me.mekb.homeappliances.block;

import me.mekb.homeappliances.Main;
import me.mekb.homeappliances.entity.ChairEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class ChairBlock extends HorizontalFacingBlock implements Waterloggable {
    public static final BooleanProperty SEATED = BooleanProperty.of("seated");
    public static final BooleanProperty CONNECTED_LEFT = BooleanProperty.of("connected_left");
    public static final BooleanProperty CONNECTED_RIGHT = BooleanProperty.of("connected_right");
    public static final EnumProperty<BlockHalf> HALF = Properties.BLOCK_HALF;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public ChairBlock(Block copyBlock) {
        super(FabricBlockSettings.copy(copyBlock).nonOpaque());
        // default state
        setDefaultState(getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(SEATED, false)
                .with(HALF, BlockHalf.BOTTOM)
                .with(WATERLOGGED, false)
                .with(CONNECTED_LEFT, false)
                .with(CONNECTED_RIGHT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder
                .add(FACING)
                .add(SEATED).add(HALF).add(WATERLOGGED)
                .add(CONNECTED_LEFT).add(CONNECTED_RIGHT);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getStateForConnectedChairs(
                this.getDefaultState()
                        .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()) // chair is facing towards player
                        .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER),
                ctx.getWorld(), ctx.getBlockPos());
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        // return false if block below isn't solid
        if (!Block.hasTopRim(world, pos.down())) return false;

        // return true if the block at the other half is also a chair or replaceable (can place a block there)
        BlockHalf half = state.get(HALF);
        BlockState otherHalfState = world.getBlockState(half.equals(BlockHalf.TOP) ? pos.down() : pos.up());
        return otherHalfState != null && (otherHalfState.isReplaceable() || (otherHalfState.isOf(state.getBlock()) && !otherHalfState.get(HALF).equals(half)));
    }

    private static BlockState getStateForConnectedChairs(BlockState state, WorldAccess world, BlockPos pos) {
        Direction facing = state.get(FACING);
        Block block = state.getBlock();
        BlockState blockStateRight = world.getBlockState(pos.offset(facing.rotateYClockwise()));
        BlockState blockStateLeft  = world.getBlockState(pos.offset(facing.rotateYCounterclockwise()));
        boolean connectedRight = blockStateRight != null && blockStateRight.isOf(block) && blockStateRight.get(FACING).equals(facing);
        boolean connectedLeft  = blockStateLeft  != null && blockStateLeft .isOf(block) && blockStateLeft .get(FACING).equals(facing);
        return state
                .with(CONNECTED_RIGHT, connectedRight)
                .with(CONNECTED_LEFT,  connectedLeft);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        // server side only function
        if (world.isClient) return;

        BlockHalf half = state.get(HALF);
        BlockPos otherHalfPos = half.equals(BlockHalf.TOP) ? pos.down() : pos.up();
        BlockState otherHalfState = world.getBlockState(otherHalfPos);
        Block block = state.getBlock();

        if (
                otherHalfState == null ||
                !state.isOf(block) || // if current block is not a chair
                !otherHalfState.isOf(block) || // or if other half is not a chair
                (otherHalfState.isOf(block) && otherHalfState.get(HALF).equals(half))) { // or if the other half is not the correct half that it should be
            // remove block without dropping
            world.removeBlock(pos, false);
            return;
        }

        // if the block below was destroyed
        if (half.equals(BlockHalf.BOTTOM) && !Block.hasTopRim(world, pos.down())) {
            // remove block with drop
            Block.dropStacks(state, world, pos, null);
            world.removeBlock(pos, false);
        }
    }

    // shapes

    private static final VoxelShape LEG_SHAPE = VoxelShapes.cuboid(0, 0, 0, 3.0/16.0, 8.0/16.0, 3.0/16.0);
    private static final VoxelShape NORTH_WEST_LEG_SHAPE = LEG_SHAPE.offset(2.0/16.0, 0, 2.0/16.0);
    private static final VoxelShape NORTH_EAST_LEG_SHAPE = LEG_SHAPE.offset(11.0/16.0, 0, 2.0/16.0);
    private static final VoxelShape SOUTH_WEST_LEG_SHAPE = LEG_SHAPE.offset(2.0/16.0, 0, 11.0/16.0);
    private static final VoxelShape SOUTH_EAST_LEG_SHAPE = LEG_SHAPE.offset(11.0/16.0, 0, 11.0/16.0);

    private static final VoxelShape SEAT_SHAPE = VoxelShapes.cuboid(1.0/16.0, 8.0/16.0, 1.0/16.0, 15.0/16.0, 11.0/16.0, 15.0/16.0);
    private static final VoxelShape SEAT_SHAPE_NORTH = VoxelShapes.cuboid(1.0/16.0, 8.0/16.0, 0, 15.0/16.0, 11.0/16.0, 1.0/16.0);
    private static final VoxelShape SEAT_SHAPE_EAST  = VoxelShapes.cuboid(15.0/16.0, 8.0/16.0, 1.0/16.0, 1, 11.0/16.0, 15.0/16.0);
    private static final VoxelShape SEAT_SHAPE_SOUTH = VoxelShapes.cuboid(1.0/16.0, 8.0/16.0, 15.0/16.0, 15.0/16.0, 11.0/16.0, 1);
    private static final VoxelShape SEAT_SHAPE_WEST  = VoxelShapes.cuboid(0, 8.0/16.0, 1.0/16.0, 1.0/16.0, 11.0/16.0, 15.0/16.0);

    private static final VoxelShape BACK_SHAPE_NORTH = VoxelShapes.cuboid(1.0/16.0, 11.0/16.0, 1.0/16.0, 15.0/16.0, 23.0/16.0, 4.0/16.0);
    private static final VoxelShape BACK_SHAPE_EAST  = VoxelShapes.cuboid(12.0/16.0, 11.0/16.0, 1.0/16.0, 15.0/16.0, 23.0/16.0, 15.0/16.0);
    private static final VoxelShape BACK_SHAPE_SOUTH = VoxelShapes.cuboid(1.0/16.0, 11.0/16.0, 12.0/16.0, 15.0/16.0, 23.0/16.0, 15.0/16.0);
    private static final VoxelShape BACK_SHAPE_WEST  = VoxelShapes.cuboid(1.0/16.0, 11.0/16.0, 1.0/16.0, 4.0/16.0, 23.0/16.0, 15.0/16.0);

    private static final VoxelShape BACK_SHAPE_NORTH_LEFT = VoxelShapes.cuboid(15.0/16.0, 11.0/16.0, 1.0/16.0, 16.0/16.0, 23.0/16.0, 4.0/16.0);
    private static final VoxelShape BACK_SHAPE_EAST_LEFT  = VoxelShapes.cuboid(12.0/16.0, 11.0/16.0, 15.0/16.0, 15.0/16.0, 23.0/16.0, 16.0/16.0);
    private static final VoxelShape BACK_SHAPE_SOUTH_LEFT = VoxelShapes.cuboid(0.0/16.0, 11.0/16.0, 12.0/16.0, 1.0/16.0, 23.0/16.0, 15.0/16.0);
    private static final VoxelShape BACK_SHAPE_WEST_LEFT  = VoxelShapes.cuboid(1.0/16.0, 11.0/16.0, 0.0/16.0, 4.0/16.0, 23.0/16.0, 1.0/16.0);

    private static final VoxelShape BACK_SHAPE_NORTH_RIGHT = VoxelShapes.cuboid(0.0/16.0, 11.0/16.0, 1.0/16.0, 1.0/16.0, 23.0/16.0, 4.0/16.0);
    private static final VoxelShape BACK_SHAPE_EAST_RIGHT  = VoxelShapes.cuboid(12.0/16.0, 11.0/16.0, 0.0/16.0, 15.0/16.0, 23.0/16.0, 1.0/16.0);
    private static final VoxelShape BACK_SHAPE_SOUTH_RIGHT = VoxelShapes.cuboid(15.0/16.0, 11.0/16.0, 12.0/16.0, 16.0/16.0, 23.0/16.0, 15.0/16.0);
    private static final VoxelShape BACK_SHAPE_WEST_RIGHT  = VoxelShapes.cuboid(1.0/16.0, 11.0/16.0, 15.0/16.0, 4.0/16.0, 23.0/16.0, 16.0/16.0);

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        VoxelShape shape = VoxelShapes.union(SEAT_SHAPE, NORTH_EAST_LEG_SHAPE, NORTH_WEST_LEG_SHAPE, SOUTH_EAST_LEG_SHAPE, SOUTH_WEST_LEG_SHAPE);
        boolean connectedLeft  = state.get(CONNECTED_LEFT);
        boolean connectedRight = state.get(CONNECTED_RIGHT);
        switch (state.get(FACING)) {
            // add the back part of the chair depending on which way it's facing
            case NORTH -> {
                shape = VoxelShapes.union(shape, BACK_SHAPE_SOUTH);
                if (connectedLeft)
                    shape = VoxelShapes.union(shape, BACK_SHAPE_SOUTH_LEFT, SEAT_SHAPE_WEST);
                if (connectedRight)
                    shape = VoxelShapes.union(shape, BACK_SHAPE_SOUTH_RIGHT, SEAT_SHAPE_EAST);
            }
            case EAST -> {
                shape = VoxelShapes.union(shape, BACK_SHAPE_WEST);
                if (connectedLeft)
                    shape = VoxelShapes.union(shape, BACK_SHAPE_WEST_LEFT, SEAT_SHAPE_NORTH);
                if (connectedRight)
                    shape = VoxelShapes.union(shape, BACK_SHAPE_WEST_RIGHT, SEAT_SHAPE_SOUTH);
            }
            case SOUTH -> {
                shape = VoxelShapes.union(shape, BACK_SHAPE_NORTH);
                if (connectedLeft)
                    shape = VoxelShapes.union(shape, BACK_SHAPE_NORTH_LEFT, SEAT_SHAPE_EAST);
                if (connectedRight)
                    shape = VoxelShapes.union(shape, BACK_SHAPE_NORTH_RIGHT, SEAT_SHAPE_WEST);
            }
            case WEST -> {
                shape = VoxelShapes.union(shape, BACK_SHAPE_EAST);
                if (connectedLeft)
                    shape = VoxelShapes.union(shape, BACK_SHAPE_EAST_LEFT, SEAT_SHAPE_SOUTH);
                if (connectedRight)
                    shape = VoxelShapes.union(shape, BACK_SHAPE_EAST_RIGHT, SEAT_SHAPE_NORTH);
            }
        }
        if (state.get(HALF).equals(BlockHalf.TOP)) {
            // shift down by one block if it's the top half
            shape = shape.offset(0, -1, 0);
        }
        return shape;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        // set the other half to the chair of the opposite half
        BlockHalf half = state.get(HALF);
        BlockPos otherHalfPos = half.equals(BlockHalf.TOP) ? pos.down() : pos.up();
        world.setBlockState(otherHalfPos, state.with(HALF, half.equals(BlockHalf.TOP) ? BlockHalf.BOTTOM : BlockHalf.TOP), Block.NOTIFY_ALL);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        // server side only function
        if (world.isClient) return;

        Block block = state.getBlock();

        if (!newState.isOf(block)) {
            // called when block is broken
            return;
        }

        // called when block still exists
        BlockHalf half = state.get(HALF);
        BlockPos otherHalfPos = half.equals(BlockHalf.BOTTOM) ? pos.up() : pos.down();
        BlockState otherHalfState = world.getBlockState(otherHalfPos);

        if (otherHalfState.isOf(block) && !otherHalfState.get(HALF).equals(half)) {
            // sync seated state and facing with other block
            BlockState newOtherHalfState = otherHalfState
                    .with(SEATED, newState.get(SEATED)).with(FACING, newState.get(FACING))
                    .with(CONNECTED_LEFT, newState.get(CONNECTED_LEFT)).with(CONNECTED_RIGHT, newState.get(CONNECTED_RIGHT));
            if (!otherHalfState.equals(newOtherHalfState)) {
                world.setBlockState(otherHalfPos, newOtherHalfState);
            }
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        // https://fabricmc.net/wiki/tutorial:waterloggable
        if (state.get(WATERLOGGED))
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));

        if (state.get(HALF).equals(BlockHalf.BOTTOM)) {
            Direction facing = state.get(FACING);
            Block block = state.getBlock();
            if (neighborPos.equals(pos.offset(facing.rotateYClockwise()))) {
                state = state.with(CONNECTED_RIGHT, neighborState != null && neighborState.isOf(block) && neighborState.get(FACING).equals(facing));
            }
            if (neighborPos.equals(pos.offset(facing.rotateYCounterclockwise()))) {
                state = state.with(CONNECTED_LEFT, neighborState != null && neighborState.isOf(block) && neighborState.get(FACING).equals(facing));
            }
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        // return full signal strength if someone is sitting in the chair
        return (state.get(HALF).equals(BlockHalf.BOTTOM) && state.get(SEATED)) ? 15 : 0;
    }

    private static boolean hasCollision(BlockView world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !state.getCollisionShape(world, pos).isEmpty();
    }

    public static boolean spaceInFront(BlockState state, BlockPos pos, BlockView world) {
        // get bottom position
        BlockHalf half = state.get(HALF);
        if (half.equals(BlockHalf.TOP)) {
            pos = pos.down();
            state = world.getBlockState(pos);
            if (!state.isOf(state.getBlock()) || state.get(HALF).equals(half)) return false;
        }

        Direction facing = state.get(FACING);
        BlockPos facingBlock = pos.offset(facing);
        BlockPos facingUpBlock = facingBlock.up();
        
        // return true if the two blocks in front of the chair
        return
                !hasCollision(world, facingBlock) &&
                !hasCollision(world, facingUpBlock);
    }

    private static void sit(PlayerEntity player, BlockState state, BlockPos pos, World world) {
        if (world.isClient) return;
        if (player == null) return;
        if (state.get(SEATED)) return;

        // get bottom position
        BlockHalf half = state.get(HALF);
        if (half.equals(BlockHalf.TOP)) {
            pos = pos.down();
            state = world.getBlockState(pos);
            if (!state.isOf(state.getBlock()) || state.get(HALF).equals(half)) return;
        }

        ChairEntity chairEntity = new ChairEntity(Main.CHAIR_ENTITY_TYPE, world);
        world.spawnEntity(chairEntity);
        chairEntity.setPosition(pos.toCenterPos());
        chairEntity.setPitch(0);
        Direction facing = state.get(FACING);
        float degrees = 0;
        switch (facing) {
            case WEST -> degrees = 90;
            case NORTH -> degrees = 180;
            case EAST -> degrees = 270;
        };
        chairEntity.setYaw(degrees);
        if (player.startRiding(chairEntity)) {
            world.setBlockState(pos, state.with(SEATED, true));
        } else {
            chairEntity.kill();
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        sit(player, state, pos, world);
        return ActionResult.SUCCESS;
    }
}
