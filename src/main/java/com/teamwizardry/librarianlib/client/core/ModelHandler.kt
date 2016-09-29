package com.teamwizardry.librarianlib.client.core

import com.teamwizardry.librarianlib.LibrarianLib
import com.teamwizardry.librarianlib.LibrarianLog
import com.teamwizardry.librarianlib.client.util.JsonGenerationUtils
import com.teamwizardry.librarianlib.common.base.IExtraVariantHolder
import com.teamwizardry.librarianlib.common.base.IVariantHolder
import com.teamwizardry.librarianlib.common.base.block.IBlockColorProvider
import com.teamwizardry.librarianlib.common.base.block.IModBlockProvider
import com.teamwizardry.librarianlib.common.base.item.IItemColorProvider
import com.teamwizardry.librarianlib.common.base.item.IModItemProvider
import com.teamwizardry.librarianlib.common.core.ConfigHandler
import com.teamwizardry.librarianlib.common.core.serialize
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ModelBakery
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.io.File
import java.util.*

/**
 * @author WireSegal
 * Created at 2:12 PM on 3/20/16.
 */
object ModelHandler {

    // Easy access
    private val debug = LibrarianLib.DEV_ENVIRONMENT
    private var modName = ""
    private val namePad: String
        get() = Array(modName.length) { " " }.joinToString("")

    private var generatedFile = false

    private val variantCache = HashMap<String, MutableList<IVariantHolder>>()

    /**
     * This is Mod name -> (Variant name -> MRL), specifically for ItemMeshDefinitions.
     */
    @JvmField
    @SideOnly(Side.CLIENT)
    val resourceLocations = HashMap<String, HashMap<String, ModelResourceLocation>>()

    /**
     * Use this method to inject your item into the list to be loaded at the end of preinit and colorized at the end of init.
     */
    @JvmStatic
    fun registerVariantHolder(holder: IVariantHolder) {
        val name = Loader.instance().activeModContainer()?.modId ?: return
        variantCache.getOrPut(name) { mutableListOf() }.add(holder)
    }

    @SideOnly(Side.CLIENT)
    private fun addToCachedLocations(name: String, mrl: ModelResourceLocation) {
        resourceLocations.getOrPut(modName) { hashMapOf() }.put(name, mrl)
    }

    @SideOnly(Side.CLIENT)
    fun preInit() {
        for ((modid, holders) in variantCache) {
            modName = modid
            log("$modName | Registering models")
            for (holder in holders.sortedBy { (255 - it.variants.size).toChar() + if (it is IModBlockProvider) "b" else "I" + if (it is IModItemProvider) it.providedItem.registryName.resourcePath else "" }) {
                registerModels(holder)
            }
        }

        if (generatedFile && debug) {
            LibrarianLog.warn("")
            LibrarianLog.warn("*************************************************************")
            LibrarianLog.warn("* At least one was file generated by the model loader system.")
            LibrarianLog.warn("* Restart the client to lock in the changes.")
            LibrarianLog.warn("*************************************************************")
            FMLCommonHandler.instance().handleExit(0)
        }
    }

    @SideOnly(Side.CLIENT)
    fun init() {
        val itemColors = Minecraft.getMinecraft().itemColors
        val blockColors = Minecraft.getMinecraft().blockColors
        for ((modid, holders) in variantCache) {
            modName = modid
            log("$modName | Registering colors")
            for (holder in holders) {

                if (holder is IItemColorProvider && holder is IModItemProvider) {
                    val color = holder.getItemColor()
                    if (color != null) {
                        log("$namePad | Registering item color for ${holder.providedItem.registryName.resourcePath}")
                        itemColors.registerItemColorHandler(color, holder.providedItem)
                    }
                }

                if (holder is IModBlockProvider && holder is IBlockColorProvider) {
                    val color = holder.getBlockColor()
                    if (color != null) {
                        log("$namePad | Registering block color for ${holder.providedBlock.registryName.resourcePath}")
                        blockColors.registerBlockColorHandler(color, holder.providedBlock)
                    }
                }

            }
        }
    }

    @SideOnly(Side.CLIENT)
    fun registerModels(holder: IVariantHolder) {
        if (holder is IModItemProvider && holder.getCustomMeshDefinition() != null)
            ModelLoader.setCustomMeshDefinition(holder.providedItem, holder.getCustomMeshDefinition())
        else
            registerModels(holder, holder.variants, false)

        if (holder is IExtraVariantHolder)
            registerModels(holder, holder.extraVariants, true)
    }

    @SideOnly(Side.CLIENT)
    fun registerModels(holder: IVariantHolder, variants: Array<out String>, extra: Boolean) {
        if (holder is IModBlockProvider && !extra) {
            val mapper = holder.getStateMapper()
            if (mapper != null)
                ModelLoader.setCustomStateMapper(holder.providedBlock, mapper)

            if (debug && ConfigHandler.generateJson) {
                val modelPath = JsonGenerationUtils.getPathForBlockModel(holder.providedBlock)
                val modelFile = File(modelPath)
                modelFile.parentFile.mkdirs()
                if (modelFile.createNewFile()) {
                    val obj = JsonGenerationUtils.generateBaseBlockModel(holder.providedBlock)
                    modelFile.writeText(obj.serialize())
                    log("$namePad | Creating file for block ${holder.providedBlock.registryName.resourcePath}")
                    generatedFile = true
                }

                val statePath = JsonGenerationUtils.getPathForBlockstate(holder.providedBlock)
                val stateFile = File(statePath)
                stateFile.parentFile.mkdirs()
                if (stateFile.createNewFile()) {
                    val obj = JsonGenerationUtils.generateBaseBlockState(holder.providedBlock, mapper)
                    stateFile.writeText(obj.serialize())
                    log("$namePad | Creating file for blockstate of ${holder.providedBlock.registryName.resourcePath}")
                    generatedFile = true
                }
            }
        }

        if (holder is IModItemProvider) {
            val item = holder.providedItem
            for (variant in variants.withIndex()) {

                if (variant.index == 0) {
                    var print = "$namePad | Registering "

                    if (variant.value != item.registryName.resourcePath || variants.size != 1 || extra)
                        print += "${if (extra) "extra " else ""}variant${if (variants.size == 1) "" else "s"} of "

                    print += if (item is IModBlockProvider) "block" else "item"
                    print += " ${item.registryName.resourcePath}"
                    log(print)
                }

                if ((variant.value != item.registryName.resourcePath || variants.size != 1))
                    log("$namePad |  Variant #${variant.index + 1}: ${variant.value}")

                if (debug && ConfigHandler.generateJson) {
                    val path = JsonGenerationUtils.getPathForItemModel(holder.providedItem, variant.value)
                    val file = File(path)
                    file.parentFile.mkdirs()
                    if (file.createNewFile()) {
                        val obj = JsonGenerationUtils.generateBaseItemModel(item, variant.value)
                        file.writeText(obj.serialize())
                        log("$namePad | Creating file for variant of ${holder.providedItem.registryName.resourcePath}")
                        generatedFile = true
                    }
                }

                val model = ModelResourceLocation(ResourceLocation(modName, variant.value).toString(), "inventory")
                if (!extra) {
                    ModelLoader.setCustomModelResourceLocation(item, variant.index, model)
                    addToCachedLocations(getKey(item, variant.index), model)
                } else {
                    ModelBakery.registerItemVariants(item, model)
                    addToCachedLocations(variant.value, model)
                }
            }
        }

    }

    private fun getKey(item: Item, meta: Int): String {
        return "i_" + item.registryName + "@" + meta
    }

    fun log(text: String) {
        if (debug) LibrarianLog.info(text)
    }
}
