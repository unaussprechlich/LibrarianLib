package com.teamwizardry.librarianlib.api.util.misc;

import net.minecraft.client.renderer.GlStateManager;

public class Color {

    public float r, g, b, a;

    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 1;
    }

    public static Color argb(int color) {
        float a = ((color >> 24) & 0xff) / 255f;
        float r = ((color >> 16) & 0xff) / 255f;
        float g = ((color >> 8) & 0xff) / 255f;
        float b = ((color) & 0xff) / 255f;
        return new Color(r, g, b, a);
    }

    public static Color rgba(int color) {
        float r = ((color >> 24) & 0xff) / 255f;
        float g = ((color >> 16) & 0xff) / 255f;
        float b = ((color >> 8) & 0xff) / 255f;
        float a = ((color) & 0xff) / 255f;
        return new Color(r, g, b, a);
    }

    public static Color rgb(int color) {
        float r = ((color >> 16) & 0xff) / 255f;
        float g = ((color >> 8) & 0xff) / 255f;
        float b = ((color) & 0xff) / 255f;
        return new Color(r, g, b);
    }

    public void glColor() {
        GlStateManager.color(r, g, b, a);
    }
    
    public int hexRGBA() {
    	return ((int)(r*255) << 24) | ((int)(g*255) << 16) | ((int)(b*255) << 8) | (int)(a*255);
    }
    
    public int hexARGB() {
    	return ((int)(a*255) << 24) | ((int)(r*255) << 16) | ((int)(g*255) << 8) | (int)(b*255);
    }

}
