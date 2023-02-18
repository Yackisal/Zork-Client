package cc.zork.ui.font;

import net.minecraft.client.Minecraft;

import java.awt.*;
import java.io.InputStream;

public class FontManager {

    public static UFontRenderer normal = new UFontRenderer(getFont("harmony.ttf", 20), 20, true);
    public static UFontRenderer normal_bold = new UFontRenderer(getFont("harmony_bold.ttf", 20), 20, true);
    public static UFontRenderer big_bold = new UFontRenderer(getFont("harmony_bold.ttf", 24), 24, true);

    public static UFontRenderer biggest = new UFontRenderer(getFont("harmony.ttf", 48), 48, true);
    public static UFontRenderer big = new UFontRenderer(getFont("harmony.ttf", 24), 24, true);
    public static UFontRenderer small = new UFontRenderer(getFont("harmony.ttf", 17), 17, true);
    public static UFontRenderer tiny = new UFontRenderer(getFont("harmony.ttf", 14), 14, true);
    public static UFontRenderer button = new UFontRenderer(getFont("ArialBold.ttf", 18), 18, true);
    public static UFontRenderer title = new UFontRenderer(getFont("ArialBold.ttf", 20), 20, true);


    public static Font getFont(String name, int size) {
        Font font;
        try {
            InputStream is = FontManager.class.getResourceAsStream("/assets/minecraft/client/font/" + name);
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception ex) {
            System.out.println("Error loading font " + name);
            font = new Font("Arial", Font.PLAIN, size);
        }
        return font;
    }
}