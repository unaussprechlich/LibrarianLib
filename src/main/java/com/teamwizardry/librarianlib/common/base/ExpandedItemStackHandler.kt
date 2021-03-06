package com.teamwizardry.librarianlib.common.base

import net.minecraft.item.ItemStack
import net.minecraftforge.items.ItemHandlerHelper
import net.minecraftforge.items.ItemStackHandler

/**
 * @author WireSegal
 * Created at 4:35 PM on 10/28/16.
 */
class ExpandedItemStackHandler : ItemStackHandler {

    var changedCallback: (ExpandedItemStackHandler.(Int) -> Unit)? = null
    var slotPredicate: (ExpandedItemStackHandler.(Int, ItemStack) -> Boolean)? = null

    constructor(size: Int = 1): super(size)
    constructor(stacks: Array<ItemStack>): super(stacks)

    override fun insertItem(slot: Int, stack: ItemStack?, simulate: Boolean): ItemStack? {
        if (stack == null || stack.stackSize == 0)
            return null

        validateSlotIndex(slot)

        val existing = this.stacks[slot]

        var limit = getStackLimit(slot, stack)
        val canInsert = canInsertIntoSlot(slot, stack)

        if (!canInsert) return stack

        if (existing != null) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack

            limit -= existing.stackSize
        }

        if (limit <= 0)
            return stack

        val reachedLimit = stack.stackSize > limit

        if (!simulate) {
            if (existing == null) {
                this.stacks[slot] = if (reachedLimit) ItemHandlerHelper.copyStackWithSize(stack, limit) else stack
            } else {
                existing.stackSize += if (reachedLimit) limit else stack.stackSize
            }
            onContentsChanged(slot)
        }

        return if (reachedLimit) ItemHandlerHelper.copyStackWithSize(stack, stack.stackSize - limit) else null
    }

    fun canInsertIntoSlot(slot: Int, stack: ItemStack): Boolean {
        return slotPredicate?.invoke(this, slot, stack) ?: true
    }

    override fun onContentsChanged(slot: Int) {
        changedCallback?.invoke(this, slot)
    }
}
