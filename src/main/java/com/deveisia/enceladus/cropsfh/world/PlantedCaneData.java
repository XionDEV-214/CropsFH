package com.deveisia.enceladus.cropsfh.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

/**
 * Remembers positions of sugar cane that came from a player (placed by hand or grown on top
 * of such cane). Any sugar cane not in this set is treated as naturally generated.
 */
public class PlantedCaneData extends SavedData {
    private static final String NAME = "cropsfh_planted_cane";
    private static final String TAG_POSITIONS = "positions";

    private final LongOpenHashSet positions = new LongOpenHashSet();

    public static PlantedCaneData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(PlantedCaneData::load, PlantedCaneData::new, NAME);
    }

    public static PlantedCaneData load(CompoundTag tag) {
        PlantedCaneData data = new PlantedCaneData();
        for (long pos : tag.getLongArray(TAG_POSITIONS)) {
            data.positions.add(pos);
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putLongArray(TAG_POSITIONS, positions.toLongArray());
        return tag;
    }

    public void mark(BlockPos pos) {
        if (positions.add(pos.asLong())) {
            setDirty();
        }
    }

    public boolean isMarked(BlockPos pos) {
        return positions.contains(pos.asLong());
    }

    /**
     * True if the cane at (or just removed from) {@code pos}, or any cane segment below it, is player-planted.
     * The walk-down is a fallback for growth that did not go through the tracked grow event.
     */
    public boolean isPlayerPlanted(Level level, BlockPos pos) {
        if (isMarked(pos)) return true;
        BlockPos.MutableBlockPos cursor = pos.mutable();
        while (level.getBlockState(cursor.move(Direction.DOWN)).is(Blocks.SUGAR_CANE)) {
            if (isMarked(cursor)) return true;
        }
        return false;
    }
}
