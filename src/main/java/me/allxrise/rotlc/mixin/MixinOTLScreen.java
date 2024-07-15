package me.allxrise.rotlc.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

@Mixin(OpenToLanScreen.class)
public abstract class MixinOTLScreen extends Screen {

    @Accessor("ALLOW_COMMANDS_TEXT")
    public static Text getAllowCommandsText() {
        throw new AssertionError();
    }
    @Accessor("GAME_MODE_TEXT")
    public static Text getGameModeText() {
        throw new AssertionError();
    }
    @Accessor("OTHER_PLAYERS_TEXT")
    public static Text getOtherPlayersText() {
        throw new AssertionError();
    }

    @Mutable
    @Final
    @Shadow
    private final Screen parent;

    @Unique
    private static boolean isNO = false;

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

    @Unique
    public void addRemovedScreen() {
        this.addDrawableChild(CyclingButtonWidget.builder(GameMode::getSimpleTranslatableName)
                .values(new GameMode[] { GameMode.SURVIVAL, GameMode.SPECTATOR, GameMode.CREATIVE, GameMode.ADVENTURE })
                .initially(this.gameMode)
                .build(this.width / 2 - 155, 100, 150, 20, getGameModeText(), (button, gameMode) -> {
                    this.gameMode = gameMode;
                }));
        if (this.isNO) {
            var dis = new ButtonWidget(this.width / 2 + 5, 100, 150, 20, Text.literal("Haha, NO!"), (b) -> {
            });
            dis.active = false;
            this.addDrawableChild(dis);
        } else {
            this.addDrawableChild(
                    CyclingButtonWidget.onOffBuilder(this.allowCommands).build(this.width / 2 + 5, 100, 150,
                            20, getAllowCommandsText(), (button, allowCommands) -> {
                                this.isNO = true;
                                this.client.setScreen(this.parent);
                            }));
        }
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 155, this.height - 28, 150, 20,
                Text.translatable("lanServer.start"), (button) -> {
            this.client.setScreen((Screen) null);
            int i = NetworkUtils.findLocalPort();
            MutableText text;
            if (this.client.getServer().openToLan(this.gameMode, this.allowCommands, i)) {
                text = Text.translatable("commands.publish.started", new Object[] { i });
            } else {
                text = Text.translatable("commands.publish.failed");
            }

            this.client.inGameHud.getChatHud().addMessage(text);
            this.client.updateWindowTitle();
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 5, this.height - 28, 150, 20, ScreenTexts.CANCEL, (button) -> {
            this.client.setScreen(this.parent);
        }));
    }
}
