package malte0811.serverconfigcleaner;

import com.google.common.base.Preconditions;
import malte0811.serverconfigcleaner.KeyChecker.ConfigKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.NetworkConstants;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@Mod(ModMain.MODID)
public class ModMain {
    public static final Logger LOGGER = LoggerFactory.getLogger("ServerConfigCleaner");
    public static final String MODID = "serverconfigcleaner";

    public ModMain(IEventBus modBus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CleanerConfig.CONFIG_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ABISTConfig.CONFIG_SPEC);
        modBus.addListener(ModConfigEvent.Reloading.class, this::onConfigChanged);
        modBus.addListener(ModConfigEvent.Loading.class, this::onConfigChanged);

        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (s, b) -> true)
        );
    }

    private void onConfigChanged(ModConfigEvent ev) {
        if (ev.getConfig().getSpec() == CleanerConfig.CONFIG_SPEC) {
            checkAllCategorized();
        } else if (!FMLLoader.isProduction()) {
            Preconditions.checkState(ev.getConfig().getSpec() == ABISTConfig.CONFIG_SPEC);
            // Automatic Built In Self Test: On the server, always set the test config value to a "secret". On the
            // client, check that we never see that value when connecting to a dedicated server.
            if (FMLLoader.getDist().isClient()) {
                ModClient.checkABIST();
            } else {
                ABISTConfig.ABIST_TOKEN.set(ABISTConfig.FAKE_SECRET);
            }
        }
    }

    /**
     * Check that all "suspicious" server config entries have been manually marked as either false positives or true
     * "dangerous" values that should not be synced.
     */
    private void checkAllCategorized() {
        KeyChecker checker = new KeyChecker();
        Set<ConfigKey> uncategorized = new HashSet<>();
        MutableBoolean foundOwn = new MutableBoolean(false);
        for (ModConfig serverConfig : getServerConfigs()) {
            String modId = serverConfig.getModId();
            ConfigIterator.forEachConfigKey(serverConfig.getSpec(), entryKey -> {
                ConfigKey key = new ConfigKey(modId, entryKey);
                boolean excludedFromSync = checker.markedExcludedFromSync(key);
                boolean falsePositive = checker.markedFalsePositive(key);
                boolean heuristicallyProblematic = checker.mayBeProblematic(entryKey);
                if (excludedFromSync && falsePositive) {
                    throw new RuntimeException(
                            "Config key " + key + " is marked as both excluded from sync and a false positive?"
                    );
                } else if (heuristicallyProblematic) {
                    if (!excludedFromSync && !falsePositive) {
                        uncategorized.add(key);
                    }
                    if (modId.equals(MODID)) {
                        foundOwn.setTrue();
                    }
                }
            });
        }
        if (!uncategorized.isEmpty()) {
            throw new RuntimeException(
                    "Potentially secret config keys found: " + uncategorized + ". Please mark them as either false " +
                            "positives or excluded from sync in the " + MODID + "-common.toml"
            );
        }
        if (foundOwn.isFalse()) {
            throw new RuntimeException("Did not find our own \"secret\" config value, something is wrong");
        }
        LOGGER.info("Checked for secrets in server configs");
    }

    public static Set<ModConfig> getServerConfigs() {
        return ConfigTracker.INSTANCE.configSets().get(ModConfig.Type.SERVER);
    }
}
