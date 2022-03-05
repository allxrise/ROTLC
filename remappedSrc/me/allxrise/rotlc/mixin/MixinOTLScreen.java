package me.allxrise.rotlc.mixin;

import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenToLanScreen.class)
public abstract class MixinOTLScreen extends Screen {

    @Final
    @Shadow
    private static final Text GAME_MODE_TEXT = new TranslatableText("selectWorld.gameMode");
    @Mutable
    @Final
    @Shadow
    private final Screen parent;

    private final boolean isNO = false;
    @Shadow
    private GameMode gameMode = GameMode.SURVIVAL;
    @Shadow
    private boolean allowCommands;

    protected MixinOTLScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    public void removeCheat(CallbackInfo ci) {
        addRemovedScreen();
        ci.cancel();
    }

    public void addRemovedScreen() {
        this.addDrawableChild(CyclingButtonWidget.builder(GameMode::getSimpleTranslatableName).values(GameMode.SURVIVAL, GameMode.SPECTATOR, GameMode.CREATIVE, GameMode.ADVENTURE).initially(this.gameMode).build(this.width / 2 - 155, 100, 150, 20, GAME_MODE_TEXT, (button, gameMode) -> {
            this.gameMode = gameMode;
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 5, 100, 150, 20, Text.of("Allow Cheats : NO!"), (button) -> {
        })).active = false;
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableText("lanServer.start"), button -> {
            this.client.setScreen(null);
            int i = NetworkUtils.findLocalPort();
            TranslatableText text = this.client.getServer().openToLan(this.gameMode, false, i) ? new TranslatableText("commands.publish.started", i) : new TranslatableText("commands.publish.failed");
            this.client.inGameHud.getChatHud().addMessage(text);
            this.client.updateWindowTitle();
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 5, this.height - 28, 150, 20, ScreenTexts.CANCEL, button -> this.client.setScreen(this.parent)));
    }
}
