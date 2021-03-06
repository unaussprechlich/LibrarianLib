package com.teamwizardry.librarianlib.client.gui.components

import com.google.common.collect.ImmutableList
import com.teamwizardry.librarianlib.client.gui.GuiComponent
import com.teamwizardry.librarianlib.client.gui.Option
import com.teamwizardry.librarianlib.common.util.math.BoundingBox2D
import com.teamwizardry.librarianlib.common.util.math.Vec2d
import net.minecraft.client.Minecraft
import java.awt.Color

class ComponentText @JvmOverloads constructor(posX: Int, posY: Int, var horizontal: ComponentText.TextAlignH = ComponentText.TextAlignH.LEFT, var vertical: ComponentText.TextAlignV = ComponentText.TextAlignV.TOP) : GuiComponent<ComponentText>(posX, posY) {

    /**
     * The text to draw
     */
    val text = Option<ComponentText, String>("-NULL TEXT-")
    /**
     * The color of the text
     */
    val color = Option<ComponentText, Color>(Color.BLACK)
    /**
     * The wrap width in pixels, -1 for no wrapping
     */
    val wrap = Option<ComponentText, Int>(-1)
    /**
     * Whether to set the font renderer's unicode and bidi flags
     */
    val unicode = Option<ComponentText, Boolean>(false)
    /**
     * Whether to render a shadow behind the text
     */
    val shadow = Option<ComponentText, Boolean>(false)

    init {
        this.color.setValue(Color.BLACK)
    }

    /**
     * Set the text value and unset the function
     */
    fun `val`(str: String): ComponentText {
        text.setValue(str)
        text.noFunc()
        return this
    }

    /**
     * Set the callback to create the text for

     * @param func
     * *
     * @return
     */
    fun func(func: (ComponentText) -> String): ComponentText {
        text.func(func)
        return this
    }

    override fun drawComponent(mousePos: Vec2d, partialTicks: Float) {
        val fr = Minecraft.getMinecraft().fontRendererObj

        val fullText = text.getValue(this)
        val colorHex = color.getValue(this).rgb
        val enableFlags = unicode.getValue(this)
        val dropShadow = shadow.getValue(this)

        if (enableFlags) {
            fr.bidiFlag = true
            fr.unicodeFlag = true
        }

        val x = pos.xi
        var y = pos.yi

        val lines: List<String>

        val wrap = this.wrap.getValue(this)
        if (wrap == -1) {
            lines = ImmutableList.of(fullText)
        } else {
            lines = fr.listFormattedStringToWidth(fullText, wrap)!!
        }


        val height = lines.size * fr.FONT_HEIGHT
        if (vertical == TextAlignV.MIDDLE) {
            y -= height / 2
        } else if (vertical == TextAlignV.BOTTOM) {
            y -= height
        }

        var i = 0
        for (line in lines) {

            var lineX = x
            val lineY = y + i * fr.FONT_HEIGHT

            val textWidth = fr.getStringWidth(line)
            if (horizontal == TextAlignH.CENTER) {
                lineX -= textWidth / 2
            } else if (horizontal == TextAlignH.RIGHT) {
                lineX -= textWidth
            }

            fr.drawString(line, lineX.toFloat(), lineY.toFloat(), colorHex, dropShadow)

            i++
        }

        if (enableFlags) {
            fr.bidiFlag = false
            fr.unicodeFlag = false
        }
    }

    override val contentSize: BoundingBox2D
        get() {
            val wrap = this.wrap.getValue(this)

            val size: Vec2d

            val fr = Minecraft.getMinecraft().fontRendererObj

            val enableFlags = unicode.getValue(this)

            if (enableFlags) {
                fr.unicodeFlag = true
                fr.bidiFlag = true
            }

            if (wrap == -1) {
                size = Vec2d(fr.getStringWidth(text.getValue(this)).toDouble(), fr.FONT_HEIGHT.toDouble())
            } else {
                val wrapped = fr.listFormattedStringToWidth(text.getValue(this), wrap)
                size = Vec2d(wrap.toDouble(), (wrapped.size * fr.FONT_HEIGHT).toDouble())
            }

            if (enableFlags) {
                fr.unicodeFlag = false
                fr.bidiFlag = false
            }

            return BoundingBox2D(Vec2d.ZERO, size)
        }

    enum class TextAlignH {
        LEFT, CENTER, RIGHT
    }

    enum class TextAlignV {
        TOP, MIDDLE, BOTTOM
    }

}
