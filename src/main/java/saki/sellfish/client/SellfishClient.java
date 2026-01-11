package saki.sellfish.client;

import net.fabricmc.api.ClientModInitializer;
// import java.util.HashSet;
// import java.util.LinkedHashMap;
// import java.util.Map;
// import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.TimeUnit;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
// import net.fabricmc.fabric.api.event.player.UseItemCallback;
// import net.minecraft.client.MinecraftClient;
// import net.minecraft.client.network.ClientPlayerEntity;
// import net.minecraft.client.network.PlayerListEntry;
// import net.minecraft.item.FishingRodItem;
// import net.minecraft.item.ItemStack;
// import net.minecraft.text.Text;
// import net.minecraft.util.ActionResult;
// import net.minecraft.util.Hand;
// import net.minecraft.util.TypedActionResult;
// import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
// import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
// import net.minecraft.text.Text;
// import net.minecraft.client.network.ClientPlayNetworkHandler;

import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
// import net.minecraft.client.network.PlayerListEntry;
// import net.minecraft.client.option.KeyBinding;
// import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SellfishClient implements ClientModInitializer {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger("sellfish");

    @Override
    public void onInitializeClient() {

        LOGGER.info("Mod initialized");

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof FishingRodItem && player.fishHook != null) {
                CompletableFuture.delayedExecutor(500, TimeUnit.MILLISECONDS).execute(() -> {
                    if (stack.getItem() instanceof FishingRodItem && player.fishHook == null)
                        useRod();
                });
            }
            return TypedActionResult.pass(stack);
        });
    }

    private void useRod() {
        if (client.player == null || client.interactionManager == null)
            return;

        Hand hand = Hand.MAIN_HAND;
        ItemStack mainHandStack = client.player.getMainHandStack();

        if (!(mainHandStack.getItem() instanceof FishingRodItem))
            return;

        ActionResult actionResult = client.interactionManager.interactItem(client.player, hand);
        if (actionResult.isAccepted()) {
            if (actionResult.isAccepted()) {
                ClientPlayerEntity player = client.player;
                if (player != null) {
                    player.swingHand(hand);
                }
            }
            client.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
        }
    }

}
