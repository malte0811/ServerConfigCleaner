package malte0811.serverconfigcleaner;

import net.minecraft.client.Minecraft;

import static malte0811.serverconfigcleaner.ModMain.LOGGER;

public class ModClient {
    public static void checkABIST() {
        if (Minecraft.getInstance().hasSingleplayerServer()) { return; }

        String value = ABISTConfig.ABIST_TOKEN.get();
        if (value.equals(ABISTConfig.FAKE_SECRET)) {
            LOGGER.error("ABIST value did not get replaced correctly!");
        } else {
            LOGGER.info("ABIST passed");
        }
    }
}
