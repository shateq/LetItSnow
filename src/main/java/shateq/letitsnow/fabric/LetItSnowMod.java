package shateq.letitsnow.fabric;

import net.fabricmc.api.ClientModInitializer;

public class LetItSnowMod implements ClientModInitializer {
    public static boolean enabled;

    @Override
    public void onInitializeClient() {
        // Everything happens in mixins here
        enabled = true;
    }
}
