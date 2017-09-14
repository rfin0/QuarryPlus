/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *//*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.yogpc.qp.item

import net.minecraft.block.Block
import net.minecraft.enchantment.Enchantment
import net.minecraft.init.Enchantments
import net.minecraft.item.{ItemBlock, ItemStack}

class ItemBlockRefinery(b: Block) extends ItemBlock(b) with IEnchantableItem {
    override def canMove(is: ItemStack, enchantment: Enchantment): Boolean =
        enchantment == Enchantments.SILK_TOUCH || enchantment == Enchantments.FORTUNE || enchantment == Enchantments.EFFICIENCY

    override def isBookEnchantable(itemstack1: ItemStack, itemstack2: ItemStack) = false
}