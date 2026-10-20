package com.deveisia.enceladus.cropsfh.loot;

import com.deveisia.enceladus.cropsfh.Registration;
import com.deveisia.enceladus.cropsfh.world.PlantedCaneData;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/** Adds a Stickreed seed to the drops of naturally generated (not player-planted) sugar cane. */
public class StickreedSeedLootModifier extends LootModifier {
    public static final Supplier<Codec<StickreedSeedLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst)
                    .and(Codec.FLOAT.optionalFieldOf("chance", 0.05f).forGetter(m -> m.chance))
                    .apply(inst, StickreedSeedLootModifier::new)));

    /** Set while CropsFH itself rolls crop drops (harvest / JEI preview) so those never grant seeds. */
    public static final ThreadLocal<Boolean> SUPPRESS = ThreadLocal.withInitial(() -> false);

    private final float chance;

    public StickreedSeedLootModifier(LootItemCondition[] conditions, float chance) {
        super(conditions);
        this.chance = chance;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (SUPPRESS.get()) return loot;

        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
        if (state == null || origin == null || !state.is(Blocks.SUGAR_CANE)) return loot;

        if (context.getRandom().nextFloat() >= chance) return loot;

        BlockPos pos = BlockPos.containing(origin);
        if (PlantedCaneData.get(context.getLevel()).isPlayerPlanted(context.getLevel(), pos)) return loot;

        loot.add(new ItemStack(Registration.STICKREED_SEED.get()));
        return loot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
