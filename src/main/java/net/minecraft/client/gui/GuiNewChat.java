package net.minecraft.client.gui;

import com.google.common.collect.Lists;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

import cc.zork.event.Shader2DEvent;
import cc.zork.event.Subscribe;
import cc.zork.features.ui.BetterChat;
import cc.zork.manager.EventManager;
import cc.zork.manager.ModuleManager;
import cc.zork.ui.font.FontManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import cc.zork.utils.math.Type;
import cc.zork.utils.render.RoundedUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiNewChat extends Gui {
    private static final Logger logger = LogManager.getLogger();
    private final Minecraft mc;
    private final List<String> sentMessages = Lists.<String>newArrayList();
    private final List<ChatLine> chatLines = Lists.<ChatLine>newArrayList();
    private final List<ChatLine> drawnChatLines = Lists.<ChatLine>newArrayList();
    private int scrollPos;
    private boolean isScrolled;
    private int realCout;

    public GuiNewChat(Minecraft mcIn) {
        this.mc = mcIn;
    }

    {
        EventManager.Companion.registerListener(this);
    }

    @Subscribe
    public void drawShader(Shader2DEvent s2) {
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int limit = this.drawnChatLines.size();
            ScaledResolution sr = new ScaledResolution(mc);
            if (limit > 0) {
                float scale = this.getChatScale();
                int width = MathHelper.ceiling_float_int((float) this.getChatWidth() / scale);
                int k = 0;
                if (mc.currentScreen instanceof GuiChat)
                    k = 12;
                if (ModuleManager.getMod(BetterChat.class).getStage() && realCout > 0 && scale == 1) {
                    RoundedUtil.drawRound(2, sr.getScaledHeight() - 36 - realCout * 9 - k, (width + 4) * scale, realCout * 9 + 8, 3, true, new Color(0, 0, 0, 100));
                }
            }
        }
    }

    public void drawChat(int updateCounter) {
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int lineCount = this.getLineCount();
            boolean flag = false;
            int j = 0;
            int limit = this.drawnChatLines.size();
            float opacity = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (limit > 0) {
                if (this.getChatOpen()) {
                    flag = true;
                }

                float scale = this.getChatScale();
                int width = MathHelper.ceiling_float_int((float) this.getChatWidth() / scale);
                GlStateManager.pushMatrix();
                if (flag)
                    GlStateManager.translate(0.0F, -12.0F, 0.0F);
                GlStateManager.translate(2.0F, 20.0F, 0.0F);
                GlStateManager.scale(scale, scale, 1.0F);
                if (ModuleManager.getMod(BetterChat.class).getStage() && realCout > 0) {
                    RoundedUtil.drawRound(0, -8 - realCout * 9, width + 4, 8 + realCout * 9, 3, true, new Color(0, 0, 0, 50));
                }
                realCout = 0;
                for (int count = 0; count + this.scrollPos < this.drawnChatLines.size() && count < lineCount; ++count) {
                    ChatLine chatline = this.drawnChatLines.get(count + this.scrollPos);
                    if (chatline != null) {
                        int j1 = updateCounter - chatline.getUpdatedCounter();
                        double alpha = (double) j1 / 200.0D;
                        alpha = 1.0D - alpha;
                        alpha = alpha * 10.0D;
                        alpha = MathHelper.clamp_double(alpha, 0.0D, 1.0D);
                        alpha = alpha * alpha;
                        int ralpha = (int) (255.0D * alpha);
                        chatline.animation.update();

                        if (flag) {
                            ralpha = 255;
                        }

                        ralpha = (int) ((float) ralpha * opacity);
                        ++j;
                        int i2 = 0;
                        int j2 = -count * 9;
                        if ((chatline.animation.getValue() > 30 && (ModuleManager.getMod(BetterChat.class).getStage() || ralpha > 4)) || flag) {
                            if (ModuleManager.getMod(BetterChat.class).getStage()) {
                                String s = chatline.getChatComponent().getFormattedText();
                                GlStateManager.enableBlend();
                                FontManager.small.drawStringWithShadow(s, (float) i2 - (float) (100 - (flag ? 100 : chatline.animation.getValue())), (float) (j2 - 9), new Color(255, 255, 255, flag ? 255 : (int) (chatline.animation.getValue() / 100f * 255)).getRGB());
                            } else {
                                drawRect(i2, j2 - 9, i2 + width + 4, j2, ralpha / 2 << 24);
                                String s = chatline.getChatComponent().getFormattedText();
                                GlStateManager.enableBlend();
                                this.mc.fontRendererObj.drawStringWithShadow(s, (float) i2, (float) (j2 - 8), 16777215 + (ralpha << 24));
                            }
                            realCout = count;
                            GlStateManager.disableAlpha();
                            GlStateManager.disableBlend();
                        }
                        if (j1 < 200) {
                            chatline.animation.start(chatline.animation.getValue(), 100, 0.5f, Type.EASE_IN_OUT_QUAD);
                        } else {
                            chatline.animation.start(chatline.animation.getValue(), 0, 0.5f, Type.EASE_IN_OUT_QUAD);
                        }
                    }
                }

                if (flag) {
                    int fontheight = this.mc.fontRendererObj.FONT_HEIGHT;
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int l2 = limit * fontheight + limit;
                    int i3 = j * fontheight + j;
                    int j3 = this.scrollPos * i3 / limit;
                    int k1 = i3 * i3 / l2;

                    if (l2 != i3) {
                        int k3 = j3 > 0 ? 170 : 96;
                        int l3 = this.isScrolled ? 13382451 : 3355562;
                        drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                        drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }

    public void clearChatMessages() {
        this.drawnChatLines.clear();
        this.chatLines.clear();
        this.sentMessages.clear();
    }

    public void printChatMessage(IChatComponent chatComponent) {
        this.printChatMessageWithOptionalDeletion(chatComponent, 0);
    }

    public void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId) {
        this.setChatLine(chatComponent, chatLineId, this.mc.ingameGUI.getUpdateCounter(), false);
        logger.info("[CHAT] " + chatComponent.getUnformattedText());
    }

    private void setChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly) {
        if (chatLineId != 0) {
            this.deleteChatLine(chatLineId);
        }

        int i = MathHelper.floor_float((float) this.getChatWidth() / this.getChatScale());
        List<IChatComponent> list = GuiUtilRenderComponents.splitText(chatComponent, i, this.mc.fontRendererObj, false, false);
        boolean flag = this.getChatOpen();

        for (IChatComponent ichatcomponent : list) {
            if (flag && this.scrollPos > 0) {
                this.isScrolled = true;
                this.scroll(1);
            }

            this.drawnChatLines.add(0, new ChatLine(updateCounter, ichatcomponent, chatLineId));
        }

        while (this.drawnChatLines.size() > 100) {
            this.drawnChatLines.remove(this.drawnChatLines.size() - 1);
        }

        if (!displayOnly) {
            this.chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId));

            while (this.chatLines.size() > 100) {
                this.chatLines.remove(this.chatLines.size() - 1);
            }
        }
    }

    public void refreshChat() {
        this.drawnChatLines.clear();
        this.resetScroll();

        for (int i = this.chatLines.size() - 1; i >= 0; --i) {
            ChatLine chatline = (ChatLine) this.chatLines.get(i);
            this.setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
        }
    }

    public List<String> getSentMessages() {
        return this.sentMessages;
    }

    public void addToSentMessages(String message) {
        if (this.sentMessages.isEmpty() || !((String) this.sentMessages.get(this.sentMessages.size() - 1)).equals(message)) {
            this.sentMessages.add(message);
        }
    }

    public void resetScroll() {
        this.scrollPos = 0;
        this.isScrolled = false;
    }

    public void scroll(int amount) {
        this.scrollPos += amount;
        int i = this.drawnChatLines.size();

        if (this.scrollPos > i - this.getLineCount()) {
            this.scrollPos = i - this.getLineCount();
        }

        if (this.scrollPos <= 0) {
            this.scrollPos = 0;
            this.isScrolled = false;
        }
    }

    public IChatComponent getChatComponent(int mouseX, int mouseY) {
        if (!this.getChatOpen()) {
            return null;
        } else {
            ScaledResolution scaledresolution = new ScaledResolution(this.mc);
            int i = scaledresolution.getScaleFactor();
            float f = this.getChatScale();
            int j = mouseX / i - 3;
            int k = mouseY / i - 27;
            if (mc.currentScreen instanceof GuiChat)
                k -= 12;
            j = MathHelper.floor_float((float) j / f);
            k = MathHelper.floor_float((float) k / f);

            if (j >= 0 && k >= 0) {
                int l = Math.min(this.getLineCount(), this.drawnChatLines.size());

                if (j <= MathHelper.floor_float((float) this.getChatWidth() / this.getChatScale()) && k < this.mc.fontRendererObj.FONT_HEIGHT * l + l) {
                    int i1 = k / this.mc.fontRendererObj.FONT_HEIGHT + this.scrollPos;

                    if (i1 >= 0 && i1 < this.drawnChatLines.size()) {
                        ChatLine chatline = (ChatLine) this.drawnChatLines.get(i1);
                        int j1 = 0;

                        for (IChatComponent ichatcomponent : chatline.getChatComponent()) {
                            if (ichatcomponent instanceof ChatComponentText) {
                                j1 += this.mc.fontRendererObj.getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText) ichatcomponent).getChatComponentText_TextValue(), false));

                                if (j1 > j) {
                                    return ichatcomponent;
                                }
                            }
                        }
                    }

                    return null;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    public boolean getChatOpen() {
        return this.mc.currentScreen instanceof GuiChat;
    }

    public void deleteChatLine(int id) {
        Iterator<ChatLine> iterator = this.drawnChatLines.iterator();

        while (iterator.hasNext()) {
            ChatLine chatline = (ChatLine) iterator.next();

            if (chatline.getChatLineID() == id) {
                iterator.remove();
            }
        }

        iterator = this.chatLines.iterator();

        while (iterator.hasNext()) {
            ChatLine chatline1 = (ChatLine) iterator.next();

            if (chatline1.getChatLineID() == id) {
                iterator.remove();
                break;
            }
        }
    }

    public int getChatWidth() {
        return calculateChatboxWidth(this.mc.gameSettings.chatWidth);
    }

    public int getChatHeight() {
        return calculateChatboxHeight(this.getChatOpen() ? this.mc.gameSettings.chatHeightFocused : this.mc.gameSettings.chatHeightUnfocused);
    }

    public float getChatScale() {
        return this.mc.gameSettings.chatScale;
    }

    public static int calculateChatboxWidth(float scale) {
        int i = 320;
        int j = 40;
        return MathHelper.floor_float(scale * (float) (i - j) + (float) j);
    }

    public static int calculateChatboxHeight(float scale) {
        int i = 180;
        int j = 20;
        return MathHelper.floor_float(scale * (float) (i - j) + (float) j);
    }

    public int getLineCount() {
        return this.getChatHeight() / 9;
    }
}
