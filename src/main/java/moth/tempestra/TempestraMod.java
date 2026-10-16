package moth.tempestra;

import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.mod.ModContext;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

public final class TempestraMod {
    public static final String MOD_ID = "tempestra";
    public static final ModContext MOD = ButterflyApi.mod(MOD_ID, "Tempestra");
    public static final Logger LOGGER = MOD.logger();

    private TempestraMod() {
    }

    public static void initializeClient() {
        LOGGER.info("[Tempestra] Initializing Tempestra!");
    }

    public static Identifier id(String path) {
        return MOD.id(path);
    }
}
