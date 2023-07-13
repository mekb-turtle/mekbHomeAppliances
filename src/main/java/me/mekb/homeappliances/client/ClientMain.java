package me.mekb.homeappliances.client;

import me.mekb.homeappliances.Main;
import me.mekb.homeappliances.entity.ChairEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ClientMain implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Main.CHAIR_ENTITY_TYPE, ChairEntity.ChairEntityRenderer::new);
    }
}
