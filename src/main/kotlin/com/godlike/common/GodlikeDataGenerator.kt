package com.godlike.common

import com.godlike.common.items.ModItems
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.models.model.ModelTemplate
import net.minecraft.resources.ResourceLocation
import java.util.*

// TODO NOT USED RIGHT NOW BECAUSE OF AN ISSUE LOADING STUFF ON THE SERVER SIDE
object GodlikeDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		fabricDataGenerator.createPack().addProvider(::ModModelProvider)
	}

	private class ModModelProvider(output: FabricDataOutput) : FabricModelProvider(output) {
		override fun generateBlockStateModels(blockStateModelGenerator: BlockModelGenerators?) {
			// NOOP
		}

		override fun generateItemModels(itemModelGenerator: ItemModelGenerators) {
//			itemModelGenerator.generateFlatItem(ModItems.SIMPLE_TK_FOCUS, ModelTemplate(Optional.of(ResourceLocation(MOD_ID, "item/tk_focus")), Optional.empty()))
//			itemModelGenerator.generateFlatItem(ModItems.ELEVATED_TK_FOCUS, ModelTemplate(Optional.of(ResourceLocation(MOD_ID, "item/tk_focus")), Optional.empty()))
//			itemModelGenerator.generateFlatItem(ModItems.MAGNIFICENT_TK_FOCUS, ModelTemplate(Optional.of(ResourceLocation(MOD_ID, "item/tk_focus")), Optional.empty()))
//			itemModelGenerator.generateFlatItem(ModItems.SUPREME_TK_FOCUS, ModelTemplate(Optional.of(ResourceLocation(MOD_ID, "item/tk_focus")), Optional.empty()))
//			itemModelGenerator.generateFlatItem(ModItems.GODLIKE_TK_FOCUS, ModelTemplate(Optional.of(ResourceLocation(MOD_ID, "item/tk_focus")), Optional.empty()))
		}

	}
}