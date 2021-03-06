package com.teamwizardry.librarianlib.client.book.gui

import com.teamwizardry.librarianlib.client.book.data.DataNode
import com.teamwizardry.librarianlib.client.book.data.DataNodeParsers
import com.teamwizardry.librarianlib.client.book.util.BookSectionOther
import com.teamwizardry.librarianlib.client.book.util.LinkParser
import com.teamwizardry.librarianlib.client.gui.GuiComponent
import com.teamwizardry.librarianlib.client.gui.components.ComponentGrid
import com.teamwizardry.librarianlib.client.gui.components.ComponentSprite
import com.teamwizardry.librarianlib.client.gui.mixin.ButtonMixin
import java.awt.Color

class PageIndex(section: BookSectionOther, node: DataNode, tag: String) : GuiBook(section) {

    init {

        val icons = node["icons"].asList()

        val normalColor = Color(Integer.parseInt(node["normalColor"].asStringOr("0"), 16))
        val hoverColor = Color(Integer.parseInt(node["hoverColor"].asStringOr("00BFFF"), 16))
        val pressColor = Color(0x191970)

        val size = 32
        val sep = (GuiBook.PAGE_WIDTH - size * 3) / 2
        val grid = ComponentGrid(0, 0, size + sep, size + sep, 3)

        for (icon in icons) {

            val iconNormalColor = if (icon["normalColor"].exists()) Color(Integer.parseInt(icon["normalColor"].asString(), 16)) else normalColor
            val iconHoverColor = if (icon["hoverColor"].exists()) Color(Integer.parseInt(icon["hoverColor"].asString(), 16)) else hoverColor


            val sprite = ComponentSprite(DataNodeParsers.parseSprite(icon["icon"]), 0, 0, size, size)

            ButtonMixin(sprite) { sprite.color.setValue(iconNormalColor) }
            sprite.BUS.hook(ButtonMixin.ButtonStateChangeEvent::class.java) { event ->
                when (event.newState) {
                    ButtonMixin.EnumButtonState.NORMAL -> sprite.color.setValue(iconNormalColor)
                    ButtonMixin.EnumButtonState.DISABLED -> sprite.color.setValue(pressColor)
                    ButtonMixin.EnumButtonState.HOVER -> sprite.color.setValue(iconHoverColor)
                }
            }
            sprite.BUS.hook(ButtonMixin.ButtonClickEvent::class.java) {
                val link = LinkParser.parse(icon["link"].asStringOr("/"))
                openPageRelative(link.path, link.tag)
            }

            sprite.BUS.hook(GuiComponent.MouseInEvent::class.java) { event ->
                addTextSlider(sprite, event.component.pos.yi, icon.get("text").asStringOr("<NULL>"))
            }
            sprite.BUS.hook(GuiComponent.MouseOutEvent::class.java) { event ->
                removeSlider(sprite)
            }

            grid.add(sprite)
        }

        contents.add(grid)
    }
}
