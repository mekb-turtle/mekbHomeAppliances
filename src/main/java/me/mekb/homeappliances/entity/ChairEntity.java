package me.mekb.homeappliances.entity;

import me.mekb.homeappliances.block.ChairBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class ChairEntity extends Entity {
    public ChairEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public static class ChairEntityRenderer extends EntityRenderer<ChairEntity> {
        public ChairEntityRenderer(EntityRendererFactory.Context ctx) {
            super(ctx);
        }

        @Override
        public Identifier getTexture(ChairEntity entity) {
            return null;
        }
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity entity) {
        Vec3d pos = getPos();
        World world = getWorld();

        BlockPos blockPos = getBlockPos();
        BlockState state = world.getBlockState(blockPos);

        if (state.getBlock() instanceof ChairBlock) {
            Direction facing = state.get(ChairBlock.FACING);
            if (ChairBlock.spaceInFront(state, blockPos, world)) {
                // move player in front of the chair if there is space there
                pos = pos.offset(Direction.DOWN, 0.5).offset(facing, 0.738);
            } else {
                // move player on top of the chair
                pos = pos.offset(Direction.UP, 3.0 / 16.0).offset(facing, 1.0 / 20.0);
            }
        }

        return pos;
    }

    @Override
    public void tick() {
        super.tick();

        World world = getWorld();
        if (world.isClient) return;

        BlockPos pos = getBlockPos();
        BlockState state = world.getBlockState(pos);

        // if block has been removed
        if (!(state.getBlock() instanceof ChairBlock)) {
            // remove entity
            this.discard();
            return;
        }

        List<Entity> passengerList = this.getPassengerList();
        if (passengerList.isEmpty()) {
            // set seated state to false if no passengers
            world.setBlockState(pos, state.with(ChairBlock.SEATED, false));
            // and remove entity
            this.discard();
        }
    }

    @Override
    protected void initDataTracker() {

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }
}