package com.thatoneaiguy;

import com.thatoneaiguy.init.ParoxysmItems;
import com.thatoneaiguy.item.CrateCracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

public class ParoxsysmClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerModelProvider((resources, out) -> out.accept(new ModelIdentifier("paroxysm", "crate_cracker_off", "inventory")));
        ModelLoadingRegistry.INSTANCE.registerModelProvider((resources, out) -> out.accept(new ModelIdentifier("paroxysm", "crate_cracker_on", "inventory")));

        registerModelPredicateProviders();
    }

    public static void registerModelPredicateProviders() {
        ModelPredicateProviderRegistry.register(ParoxysmItems.CRATE_CRACKER, new Identifier("mode"), (itemStack, clientWorld, livingEntity, seed) -> {
            if (livingEntity == null) {
                return 0.0F;
            }
            return (float) CrateCracker.getMode(itemStack);
        });
    }
}
