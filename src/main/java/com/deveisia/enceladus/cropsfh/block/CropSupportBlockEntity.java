package com.deveisia.enceladus.cropsfh.block;

import com.deveisia.enceladus.cropsfh.Registration;
import com.deveisia.enceladus.cropsfh.config.CropsFHConfig;
import com.deveisia.enceladus.cropsfh.crop.CropPlantHelper;
import com.deveisia.enceladus.cropsfh.crop.CropSeedHelper;
import com.deveisia.enceladus.cropsfh.crop.CropStats;
import com.deveisia.enceladus.cropsfh.crop.CropStatsHelper;
import com.deveisia.enceladus.cropsfh.soil.SoilConfig;
import com.deveisia.enceladus.cropsfh.soil.SoilRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CropSupportBlockEntity extends BlockEntity {
    private static final String TAG_SEED = "seed";
    private static final String TAG_STATS = "stats";
    private static final String TAG_GROWTH_TICKS = "growthTicks";
    private static final String TAG_IS_CHILD = "isChild";

    private static final int CHILD_GROWTH_TICKS = 180 * 20;

    private ItemStack seed = ItemStack.EMPTY;
    private CropStats stats = CropStats.DEFAULT_SEED;
    private int growthTicks = 0;
    private boolean isChild = false;

    public CropSupportBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.CROP_SUPPORT_BE.get(), pos, state);
    }

    public boolean hasCrop() {
        return !seed.isEmpty();
    }

    public boolean isMature() {
        return hasCrop() && growthTicks >= getRequiredGrowthTicks();
    }

    public ItemStack getSeed() {
        return seed;
    }

    public CropStats getStats() {
        return stats;
    }

    public boolean isChild() {
        return isChild;
    }

    public void plantSeed(ItemStack seedStack) {
        if (hasCrop() || seedStack.isEmpty()) return;
        this.seed = seedStack.copyWithCount(1);
        this.stats = CropSeedHelper.getStats(seedStack);
        this.growthTicks = 0;
        this.isChild = false;
        setChanged();
        sendUpdate();
    }

    public void plantChild(ItemStack seedStack, CropStats childStats) {
        if (hasCrop() || seedStack.isEmpty()) return;
        this.seed = seedStack.copyWithCount(1);
        this.stats = childStats;
        this.growthTicks = 0;
        this.isChild = true;
        setChanged();
        sendUpdate();
    }

    public void removeCrop(boolean dropSeed) {
        if (!hasCrop()) return;
        if (dropSeed && level != null && !level.isClientSide) {
            dropItem(seed.copy());
        }
        this.seed = ItemStack.EMPTY;
        this.stats = CropStats.DEFAULT_SEED;
        this.growthTicks = 0;
        this.isChild = false;
        setChanged();
        sendUpdate();
    }

    public void harvest(Player player) {
        if (!isMature()) return;

        for (ItemStack drop : harvestAndGetDrops()) {
            if (player != null) {
                dropTowardPlayer(drop, player);
            } else {
                dropItem(drop);
            }
        }
    }

    public List<ItemStack> getHarvestDrops() {
        if (!isMature()) return new ArrayList<>();

        if (isChild) {
            RandomSource random = level != null ? level.random : RandomSource.create();
            int count = 2 + random.nextInt(7); // 2 - 8
            ItemStack seeds = seed.copyWithCount(count);
            CropSeedHelper.setStats(seeds, stats);
            return Collections.singletonList(seeds);
        }

        return calculateHarvestDrops();
    }

    public List<ItemStack> harvestAndGetDrops() {
        List<ItemStack> drops = getHarvestDrops();

        if (isChild) {
            removeCrop(false);
        } else {
            this.growthTicks = 0;
            setChanged();
            sendUpdate();
        }

        return drops;
    }


    public List<ItemStack> calculateHarvestDrops() {
        List<ItemStack> result = new ArrayList<>();
        if (!hasCrop()) return result;

        Block cropBlock = CropPlantHelper.getCropBlock(seed);
        if (cropBlock == null || !(level instanceof ServerLevel serverLevel)) return result;

        BlockState matureState = CropPlantHelper.getMatureState(cropBlock);
        LootParams.Builder builder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .withParameter(LootContextParams.BLOCK_STATE, matureState)
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
        List<ItemStack> baseDrops = matureState.getDrops(builder);

        SoilConfig soil = getSoil();
        int gain = stats.gain();
        double gainMultiplier = soil.gainMultiplier();

        for (ItemStack drop : baseDrops) {
            long finalCount = (long) Math.floor(drop.getCount() * gain * gainMultiplier);
            if (finalCount <= 0) finalCount = drop.getCount();
            ItemStack scaled = drop.copyWithCount((int) Math.min(finalCount, drop.getMaxStackSize()));
            // Preserve CropsFH stats on any seed-like drops so replanted seeds keep their quality.
            if (scaled.getItem() == seed.getItem()) {
                CropSeedHelper.setStats(scaled, stats);
            }
            result.add(scaled);
        }

        return result;
    }

    public void serverTick() {
        if (!hasCrop()) return;
        if (isMature()) return;

        int previous = growthTicks;
        growthTicks++;
        if (growthTicks > getRequiredGrowthTicks()) {
            growthTicks = getRequiredGrowthTicks();
        }
        if (growthTicks != previous) {
            setChanged();
            sendUpdate();
        }
    }

    public void randomTick(RandomSource random) {
        if (!hasCrop()) return;

        double chance = CropStatsHelper.getDegenerationChance(stats);
        if (random.nextDouble() < chance) {
            int step = getGrowthStepTicks();
            if (growthTicks > step) {
                growthTicks = Math.max(0, growthTicks - step);
                setChanged();
                sendUpdate();
            }
        }
    }

    public void attemptBreed() {
        if (hasCrop() || level == null || level.isClientSide) return;

        if (level.random.nextInt(CropsFHConfig.BREEDING_CHANCE.get()) != 0) return;

        List<CropSupportBlockEntity> parents = findBreedParents();
        if (parents.size() < 2) return;

        CropSupportBlockEntity parentA = parents.get(0);
        CropSupportBlockEntity parentB = parents.get(1);

        if (!CropPlantHelper.getSeedId(parentA.getSeed()).equals(CropPlantHelper.getSeedId(parentB.getSeed()))) {
            return;
        }

        CropStats base = selectBetterParent(parentA.getStats(), parentB.getStats());
        CropStats childStats = generateChildStats(base, level.random);

        level.setBlockAndUpdate(worldPosition, Registration.CROP_SUPPORT.get().defaultBlockState());
        BlockEntity be = level.getBlockEntity(worldPosition);
        if (be instanceof CropSupportBlockEntity child) {
            child.plantChild(parentA.getSeed().copyWithCount(1), childStats);
        }
    }

    private List<CropSupportBlockEntity> findBreedParents() {
        List<CropSupportBlockEntity> parents = new ArrayList<>();
        if (level == null) return parents;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = worldPosition.relative(dir);
            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be instanceof CropSupportBlockEntity support && support.hasCrop()) {
                parents.add(support);
            }
        }
        return parents;
    }

    private CropStats selectBetterParent(CropStats a, CropStats b) {
        double avgA = (a.growth() + a.gain() + a.resistance()) / 3.0;
        double avgB = (b.growth() + b.gain() + b.resistance()) / 3.0;
        return avgA >= avgB ? a : b;
    }

    private CropStats generateChildStats(CropStats base, RandomSource random) {
        int growth = base.growth() + (int) Math.round(CropStats.MAX_GROWTH * 0.01 * random.nextDouble());
        int gain = base.gain() + (int) Math.round(CropStats.MAX_GAIN * 0.01 * random.nextDouble());
        double resistance = base.resistance() + CropStats.MAX_RESISTANCE * 0.01 * random.nextDouble();
        return new CropStats(growth, gain, resistance);
    }

    private int getRequiredGrowthTicks() {
        if (isChild) return CHILD_GROWTH_TICKS;

        int baseSeconds = CropsFHConfig.BASE_GROWTH_TIME.get();
        int minSeconds = CropsFHConfig.MIN_GROWTH_TIME.get();

        double speedMultiplier = CropStatsHelper.getGrowthMultiplier(stats);
        SoilConfig soil = getSoil();
        speedMultiplier *= soil.growthSpeedModifier();

        double seconds = baseSeconds / speedMultiplier;
        seconds = Math.max(minSeconds, seconds);
        return (int) Math.round(seconds * 20);
    }


    private int getGrowthStepTicks() {
        // One "stage" worth of ticks. Simplified as 1/4 of required ticks.
        return Math.max(1, getRequiredGrowthTicks() / 4);
    }

    private SoilConfig getSoil() {
        if (level == null) return SoilRegistry.DEFAULT;
        Block soilBlock = level.getBlockState(worldPosition.below()).getBlock();
        return SoilRegistry.get(soilBlock);
    }

    public float getGrowthProgress() {
        int required = getRequiredGrowthTicks();
        return required <= 0 ? 1.0f : Math.min(1.0f, (float) growthTicks / (float) required);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    private void sendUpdate() {
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;
        net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> packet = getUpdatePacket();
        if (packet == null) return;
        for (net.minecraft.server.level.ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(worldPosition), false)) {
            player.connection.send(packet);
        }
    }

    private void dropItem(ItemStack stack) {
        if (level == null || level.isClientSide || stack.isEmpty()) return;
        Vec3 center = Vec3.atCenterOf(worldPosition);
        level.addFreshEntity(new ItemEntity(level, center.x, center.y, center.z, stack));
    }

    private void dropTowardPlayer(ItemStack stack, Player player) {
        if (level == null || level.isClientSide || stack.isEmpty()) return;
        ItemEntity entity = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, stack);
        entity.setDeltaMovement((player.getX() - worldPosition.getX()) * 0.05, 0.2, (player.getZ() - worldPosition.getZ()) * 0.05);
        level.addFreshEntity(entity);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_SEED, seed.save(new CompoundTag()));
        tag.put(TAG_STATS, CropStatsHelper.write(stats));
        tag.putInt(TAG_GROWTH_TICKS, growthTicks);
        tag.putBoolean(TAG_IS_CHILD, isChild);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.seed = ItemStack.of(tag.getCompound(TAG_SEED));
        this.stats = CropStatsHelper.read(tag.getCompound(TAG_STATS));
        this.growthTicks = tag.getInt(TAG_GROWTH_TICKS);
        this.isChild = tag.getBoolean(TAG_IS_CHILD);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag) {
        load(tag);
    }
}
