package cc.zork.ui.account;

import me.superskidder.IRCLoader;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;

public class GuiRegister extends GuiScreen {
    public static String status;

    @Override
    public void initGui() {
        super.initGui();
        final int var3 = this.height / 4 + 24;
        this.username = new GuiTextField(var3, this.mc.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
        this.password = new PasswordField(this.mc.fontRendererObj, this.width / 2 - 100, 100, 200, 20);
        this.password2 = new PasswordField(this.mc.fontRendererObj, this.width / 2 - 100, 140, 200, 20);
        this.key = new GuiTextField(var3, this.mc.fontRendererObj, this.width / 2 - 100, 180, 200, 20);

        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, 210, "Register"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, 278, "Back"));
    }

    private PasswordField password, password2;
    private GuiTextField username;
    private GuiTextField key;

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        try {
            super.keyTyped(character, key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (character == '\t') {
            if (!this.username.isFocused() && !this.password.isFocused()) {
                this.username.setFocused(true);
            } else {
                this.username.setFocused(this.password.isFocused());
                this.password.setFocused(!this.username.isFocused());
            }
        }
        if (character == '\r') {
            this.actionPerformed(this.buttonList.get(0));
        }
        this.username.textboxKeyTyped(character, key);
        this.password.textboxKeyTyped(character, key);
        this.password2.textboxKeyTyped(character, key);
        this.key.textboxKeyTyped(character, key);
    }

    @Override
    protected void mouseClicked(final int x, final int y, final int button) {
        try {
            super.mouseClicked(x, y, button);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.username.mouseClicked(x, y, button);
        this.password.mouseClicked(x, y, button);
        this.password2.mouseClicked(x, y, button);
        this.key.mouseClicked(x, y, button);

    }

    @Override
    public void updateScreen() {
        this.username.updateCursorCounter();
        this.password.updateCursorCounter();
        this.password2.updateCursorCounter();
        this.key.updateCursorCounter();

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.username.drawTextBox();
        this.password.drawTextBox();
        this.password2.drawTextBox();
        this.key.drawTextBox();

        this.drawCenteredString(this.mc.fontRendererObj, "Zork Regitser", this.width / 2, 20, -1);

        this.drawCenteredString(this.mc.fontRendererObj, status, this.width / 2, 29, -1);
        if (this.username.getText().isEmpty()) {
            this.drawString(this.mc.fontRendererObj, "Username", this.width / 2 - 96, 66, -7829368);
        }
        if (this.password.getText().isEmpty()) {
            this.drawString(this.mc.fontRendererObj, "Password", this.width / 2 - 96, 106, -7829368);
        }
        if (this.password2.getText().isEmpty()) {
            this.drawString(this.mc.fontRendererObj, "Confirm password", this.width / 2 - 96, 146, -7829368);
        }
        if (this.key.getText().isEmpty()) {
            this.drawString(this.mc.fontRendererObj, "Key", this.width / 2 - 96, 186, -7829368);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        try {
            switch (button.id) {
                case 1: {
                    if (username.getText().isEmpty() || password.getText().isEmpty()) {
                        status = "Username or password can't be empty";
                        return;
                    }
                    if (!password.getText().equals(password2.getText())) {
                        status = "passwords aren't same";
                        return;
                    }
                    if (key.getText().isEmpty()) {
                        status = "Please input the key";
                        return;
                    }
                    status = "Registering...";
                    IRCLoader.register(username.getText(), password.getText(), key.getText());
                    break;
                }
                case 2:
                    mc.displayGuiScreen(new GuiLogin(new GuiMainMenu()));
                    break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
