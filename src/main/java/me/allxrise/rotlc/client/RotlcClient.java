package me.allxrise.rotlc.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;

@Environment(EnvType.CLIENT)
public class RotlcClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LogManager.getLogger("ROTLC").info("ROTLC started! Beep-boop!");
    }
}
