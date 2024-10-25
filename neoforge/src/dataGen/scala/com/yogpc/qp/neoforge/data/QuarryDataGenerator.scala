package com.yogpc.qp.neoforge.data

import com.yogpc.qp.QuarryPlus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.data.event.GatherDataEvent

import scala.annotation.static

object QuarryDataGenerator {
  @static
  @SubscribeEvent
  def onEvent(event: GatherDataEvent): Unit = {
    QuarryPlus.LOGGER.info("Start NeoForge data generation")
    event.getGenerator.addProvider(event.includeServer, new RecipeNeoForge(event.getGenerator.getPackOutput, event.getLookupProvider))
  }
}

@EventBusSubscriber(modid = QuarryPlus.modID, bus = EventBusSubscriber.Bus.MOD)
class QuarryDataGenerator {

}
