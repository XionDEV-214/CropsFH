package com.deveisia.enceladus.cropsfh.machine;

import com.deveisia.enceladus.cropsfh.block.CropSupportBlockEntity;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import net.minecraft.nbt.CompoundTag;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CropHarvesterMachine extends TieredEnergyMachine implements IFancyUIMachine, IAutoOutputItem, IControllable, IDropSaveMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CropHarvesterMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    private final int range;
    private final int inventorySize;

    @Persisted
    @DescSynced
    protected final NotifiableItemStackHandler output;

    @Persisted
    @DescSynced
    protected Direction outputFacingItems = Direction.NORTH;

    @Persisted
    @DescSynced
    protected boolean autoOutputItems = false;

    @Persisted
    @DescSynced
    protected boolean allowInputFromOutputSideItems = false;

    public CropHarvesterMachine(IMachineBlockEntity info, int tier, int range, int inventorySize) {
        super(info, tier);
        this.range = range;
        this.inventorySize = inventorySize;
        this.output = new NotifiableItemStackHandler(this, inventorySize, IO.BOTH, IO.OUT);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            subscribeServerTick(this::harvestTick);
            subscribeServerTick(this::autoOutputTick);
        }
    }

    private void harvestTick() {
        if (getOffsetTimer() % 30 != 0) return;
        if (isFull()) return;
        if (!drainEnergy(true)) return;

        BlockPos center = getPos();
        boolean worked = false;
        for (int x = -(range / 2); x <= range / 2; x++) {
            for (int z = -(range / 2); z <= range / 2; z++) {
                BlockPos target = center.offset(x, 0, z);
                if (harvestToInventory(target)) {
                    worked = true;
                }
            }
        }

        if (worked) {
            drainEnergy(false);
        }
    }

    private boolean harvestToInventory(BlockPos pos) {
        if (!(getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return false;
        net.minecraft.world.level.block.entity.BlockEntity be = serverLevel.getBlockEntity(pos);
        if (!(be instanceof CropSupportBlockEntity support)) return false;
        if (!support.hasCrop() || !support.isMature()) return false;

        List<ItemStack> drops = support.getHarvestDrops();
        // Check capacity before harvesting so crops don't get destroyed if the inventory is full.
        for (ItemStack drop : drops) {
            if (!insertItem(drop, true).isEmpty()) return false;
        }

        support.harvestAndGetDrops();
        for (ItemStack drop : drops) {
            ItemStack remaining = insertItem(drop, false);
            if (!remaining.isEmpty()) {
                spawnItem(remaining, pos);
            }
        }
        return true;
    }

    private ItemStack insertItem(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack remaining = stack.copy();
        for (int i = 0; i < output.getSlots(); i++) {
            remaining = output.insertItemInternal(i, remaining, simulate);
            if (remaining.isEmpty()) break;
        }
        return remaining;
    }

    private void spawnItem(ItemStack stack, BlockPos pos) {
        if (getLevel() == null || getLevel().isClientSide || stack.isEmpty()) return;
        net.minecraft.world.entity.item.ItemEntity entity = new net.minecraft.world.entity.item.ItemEntity(
                getLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        getLevel().addFreshEntity(entity);
    }

    private void autoOutputTick() {
        if (!autoOutputItems) return;
        if (getLevel() == null || getLevel().isClientSide) return;
        output.exportToNearby(outputFacingItems);
    }

    private boolean drainEnergy(boolean simulate) {
        long energy = GTValues.V[getTier()];
        if (energyContainer.getEnergyStored() >= energy) {
            if (!simulate) energyContainer.changeEnergy(-energy);
            return true;
        }
        return false;
    }

    private boolean isFull() {
        for (int i = 0; i < output.getSlots(); i++) {
            ItemStack stack = output.getStackInSlot(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Widget createUIWidget() {
        int cols = (int) Math.sqrt(inventorySize);
        int height = 40 + cols * 18;
        WidgetGroup group = new WidgetGroup(0, 0, 176, height);
        group.addWidget(new LabelWidget(5, 5, getDefinition().getDescriptionId()));

        int startX = (176 - cols * 18) / 2;
        int startY = 22;
        for (int i = 0; i < inventorySize; i++) {
            int x = startX + (i % cols) * 18;
            int y = startY + (i / cols) * 18;
            group.addWidget(new SlotWidget(output, i, x, y, true, false).setBackground(GuiTextures.SLOT));
        }
        return group;
    }

    // IAutoOutputItem implementation
    @Override
    public boolean isAutoOutputItems() {
        return autoOutputItems;
    }

    @Override
    public void setAutoOutputItems(boolean autoOutputItems) {
        this.autoOutputItems = autoOutputItems;
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return allowInputFromOutputSideItems;
    }

    @Override
    public void setAllowInputFromOutputSideItems(boolean allow) {
        this.allowInputFromOutputSideItems = allow;
    }

    @Override
    public Direction getOutputFacingItems() {
        return outputFacingItems;
    }

    @Override
    public void setOutputFacingItems(Direction direction) {
        this.outputFacingItems = direction;
    }

    // IControllable implementation
    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
    }

    // IDropSaveMachine implementation
    @Override
    public void saveToItem(CompoundTag tag) {
        tag.put("inventory", output.storage.serializeNBT());
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        if (tag.contains("inventory")) {
            output.storage.deserializeNBT(tag.getCompound("inventory"));
        }
    }
}
