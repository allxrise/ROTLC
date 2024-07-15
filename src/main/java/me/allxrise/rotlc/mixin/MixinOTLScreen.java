package me.allxrise.rotlc.mixin;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.server.command.PublishCommand;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
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

    @Shadow
    private int port;

    @Mutable
    @Shadow
    private TextFieldWidget portField;

    @Invoker("updatePort")
    private Text updatePort(String portText) {
        throw new AssertionError();
    }

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
        //        if (this.isNO) {
//            // this.width / 2 + 5, 100, 150, 20, Text.literal("Haha, NO!"), (b) -> {}
//            var dis = ButtonWidget.builder(Text.literal("Haha, NO!"), (b) -> {}).dimensions(this.width / 2 + 5, 100, 150, 20).build();
//            dis.active = false;
//            this.addDrawableChild(dis);
//        } else {
//            this.addDrawableChild(
//                    CyclingButtonWidget.onOffBuilder(this.allowCommands).build(this.width / 2 + 5, 100, 150,
//                            20, getAllowCommandsText(), (button, allowCommands) -> {
//                                this.isNO = true;
//                                this.client.setScreen(this.parent);
//                            }));
//        }
        IntegratedServer integratedServer = this.client.getServer();
        this.gameMode = integratedServer.getDefaultGameMode();
        this.allowCommands = integratedServer.getSaveProperties().areCommandsAllowed();
        this.addDrawableChild(CyclingButtonWidget.builder(GameMode::getSimpleTranslatableName).values(new GameMode[]{GameMode.SURVIVAL, GameMode.SPECTATOR, GameMode.CREATIVE, GameMode.ADVENTURE}).initially(this.gameMode).build(this.width / 2 - 155, 100, 150, 20, getGameModeText(), (button, gameMode) -> {
            this.gameMode = gameMode;
        }));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.allowCommands).build(this.width / 2 + 5, 100, 150, 20, getAllowCommandsText(), (button, allowCommands) -> {
            this.allowCommands = allowCommands;
        }));
        ButtonWidget buttonWidget = ButtonWidget.builder(Text.translatable("lanServer.start"), (button) -> {
            this.client.setScreen((Screen)null);
            MutableText text;
            if (integratedServer.openToLan(this.gameMode, this.allowCommands, this.port)) {
                text = PublishCommand.getStartedText(this.port);
            } else {
                text = Text.translatable("commands.publish.failed");
            }

            this.client.inGameHud.getChatHud().addMessage(text);
            this.client.updateWindowTitle();
        }).dimensions(this.width / 2 - 155, this.height - 28, 150, 20).build();
        this.portField = new TextFieldWidget(this.textRenderer, this.width / 2 - 75, 160, 150, 20, Text.translatable("lanServer.port"));
        this.portField.setChangedListener((portText) -> {
            Text text = this.updatePort(portText);
            this.portField.setPlaceholder(Text.literal("" + this.port).formatted(Formatting.DARK_GRAY));
            if (text == null) {
                this.portField.setEditableColor(14737632);
                this.portField.setTooltip((Tooltip)null);
                buttonWidget.active = true;
            } else {
                this.portField.setEditableColor(16733525);
                this.portField.setTooltip(Tooltip.of(text));
                buttonWidget.active = false;
            }

        });
        this.portField.setPlaceholder(Text.literal("" + this.port).formatted(Formatting.DARK_GRAY));
        this.addDrawableChild(this.portField);
        this.addDrawableChild(buttonWidget);
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 + 5, this.height - 28, 150, 20).build());
    }
}
