package cn.mangofanfan.noruncommandconfirm.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.network.ClientPlayNetworkHandler")
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler {

    protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Shadow
    public abstract ClientPlayNetworkHandler.CommandRunResult parseCommand(String command);

    @Shadow
    public abstract void openConfirmRunCommandScreen(String command, String message, @Nullable Screen afterActionScreen);

    @Inject(method = "runClickEventCommand", at = @At("HEAD"), cancellable = true)
    public void runClickEventCommand(String command, @Nullable Screen afterActionScreen, CallbackInfo ci) {
        switch (this.parseCommand(command)) {
            case NO_ISSUES:
            case SIGNATURE_REQUIRED:
            case PERMISSIONS_REQUIRED:
                this.sendPacket(new CommandExecutionC2SPacket(command));
                this.client.setScreen(afterActionScreen);
                break;
            case PARSE_ERRORS:
                this.openConfirmRunCommandScreen(command, "multiplayer.confirm_command.parse_errors", afterActionScreen);
                break;
        }
        ci.cancel();
    }
}
