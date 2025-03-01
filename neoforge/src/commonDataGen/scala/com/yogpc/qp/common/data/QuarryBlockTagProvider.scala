package com.yogpc.qp.common.data

import com.yogpc.qp.{PlatformAccess, QuarryPlus}
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.tags.BlockTags
import net.neoforged.neoforge.common.data.BlockTagsProvider

import java.util.concurrent.CompletableFuture

class QuarryBlockTagProvider(output: PackOutput, lookupProvider: CompletableFuture[HolderLookup.Provider])
  extends BlockTagsProvider(output, lookupProvider, QuarryPlus.modID) {

  override def addTags(provider: HolderLookup.Provider): Unit = {
    val holder = PlatformAccess.getAccess.registerObjects()

    tag(BlockTags.MINEABLE_WITH_PICKAXE)
      .add(holder.quarryBlock().get())
      .add(holder.advQuarryBlock().get())

    tag(markerBlockTag)
      .add(holder.markerBlock().get())
      .add(holder.flexibleMarkerBlock().get())
      .add(holder.chunkMarkerBlock().get())
  }
}
