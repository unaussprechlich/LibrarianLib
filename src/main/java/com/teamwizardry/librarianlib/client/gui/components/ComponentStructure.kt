package com.teamwizardry.librarianlib.client.gui.components

import com.teamwizardry.librarianlib.client.gui.GuiComponent
import com.teamwizardry.librarianlib.client.gui.Option
import com.teamwizardry.librarianlib.client.gui.mixin.gl.GlMixin
import com.teamwizardry.librarianlib.client.structure.StructureRenderUtil
import com.teamwizardry.librarianlib.common.structure.Structure
import com.teamwizardry.librarianlib.common.util.math.Vec2d
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import java.awt.Color

class ComponentStructure(posX: Int, posY: Int, var structure: Structure?) : GuiComponent<ComponentStructure>(posX, posY) {

    val color = Option<ComponentStructure, Color>(Color.WHITE)

    init {
        GlMixin.transform(this).func { c -> Vec3d(this.pos.x, this.pos.y, 0.0) }
        initStructure()
    }

    override fun drawComponent(mousePos: Vec2d, partialTicks: Float) {
        GlStateManager.translate(this.pos.x, this.pos.y, 0.0)
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
        val buf = bufferInts
        buf ?: return

        val tessellator = Tessellator.getInstance()
        val vb = tessellator.buffer
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM)

        vb.addVertexData(buf)

        tessellator.draw()

        GlStateManager.translate(-this.pos.x, -this.pos.y, 0.0)
    }

    fun initStructure() {
        bufferInts = null
        val tmp = structure ?: return
        bufferInts = StructureRenderUtil.render(tmp, color.getValue(this), 1f)
    }

    companion object {
        private var bufferInts: IntArray? = null
    }

}
