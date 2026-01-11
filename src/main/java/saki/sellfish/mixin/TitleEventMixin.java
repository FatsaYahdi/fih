package saki.sellfish.mixin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public class TitleEventMixin {

    // private static final String modID = "sellfish";

    private static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();
    private static boolean isProcessing = false;

    @Inject(method = "onTitle", at = @At("TAIL"))
    private void onTitleReceived(TitleS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || packet.text() == null)
            return;

        // Only start if we aren't already processing a title
        if (isProcessing)
            return;

        // String titleString = packet.text().getString();

        // Only trigger if the title actually contains gameplay instructions (L or R)
        if (!packet.text().getString().contains("L") && !packet.text().getString().contains("R"))
            return;

        boolean holdingRod = client.player.getMainHandStack().getItem() instanceof net.minecraft.item.FishingRodItem ||
                client.player.getOffHandStack().getItem() instanceof net.minecraft.item.FishingRodItem;

        if (holdingRod) {
            isProcessing = true; // Lock the mixin
            char[] characters = packet.text().getString().toCharArray();

            THREAD_POOL.submit(() -> {
                try {

                    for (char c : characters) {
                        if (c == 'R') {
                            client.execute(() -> {
                                KeyBinding.onKeyPressed(InputUtil.Type.MOUSE.createFromCode(1));
                            });
                            Thread.sleep(300);
                        } else if (c == 'L') {
                            client.execute(() -> {
                                KeyBinding.onKeyPressed(InputUtil.Type.MOUSE.createFromCode(0));
                            });
                            Thread.sleep(300);
                        }
                    }

                    // 2. Final re-throw check
                    Thread.sleep(200);
                    client.execute(() -> {
                        if (client.player.fishHook == null) {
                            client.interactionManager.interactItem(client.player, net.minecraft.util.Hand.MAIN_HAND);
                        }
                    });

                } catch (InterruptedException e) {
                    // e.printStackTrace();
                } finally {
                    isProcessing = false; // Unlock so the next title can be processed
                }
            });
        }
    }

}
