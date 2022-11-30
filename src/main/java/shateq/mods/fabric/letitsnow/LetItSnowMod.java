package shateq.mods.fabric.letitsnow;

import net.fabricmc.api.ClientModInitializer;

public class LetItSnowMod implements ClientModInitializer {
    public static boolean enabled;

    @Override
    public void onInitializeClient() {
        enabled = true;
    }

    // Render snow
}
