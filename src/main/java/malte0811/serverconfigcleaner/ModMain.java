package malte0811.serverconfigcleaner;

import com.electronwill.nightconfig.core.Config;
import com.google.common.base.Preconditions;
import malte0811.serverconfigcleaner.KeyChecker.ConfigKey;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod(ModMain.MODID)
public class ModMain {
    public static final Logger LOGGER = LogManager.getLogger("ServerConfigCleaner");
    public static final String MODID = "serverconfigcleaner";

    public ModMain() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CleanerConfig.CONFIG_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ABISTConfig.CONFIG_SPEC);
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onConfigChanged);

        ModLoadingContext.get().registerExtensionPoint(
                ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (s, b) -> true)
        );
    }

    private void onConfigChanged(ModConfig.ModConfigEvent ev) {
        if (ev.getConfig().getSpec() == CleanerConfig.CONFIG_SPEC) {
            checkAllCategorized();
        } else if (!isProduction()) {
            Preconditions.checkState(ev.getConfig().getSpec() == ABISTConfig.CONFIG_SPEC);
            // Automatic Built In Self Test: On the server, always set the test config value to a "secret". On the
            // client, check that we never see that value when connecting to a dedicated server.
            if (FMLLoader.getDist().isClient()) {
                ModClient.checkABIST();
            } else {
                Config childConfig = ObfuscationReflectionHelper.getPrivateValue(
                        ForgeConfigSpec.class, ABISTConfig.CONFIG_SPEC, "childConfig"
                );
                childConfig.set(ABISTConfig.ABIST_TOKEN.getPath(), ABISTConfig.FAKE_SECRET);
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
        Map<ModConfig.Type, Set<ModConfig>> configSets = ObfuscationReflectionHelper.getPrivateValue(
                ConfigTracker.class, ConfigTracker.INSTANCE, "configSets"
        );
        return configSets.get(ModConfig.Type.SERVER);
    }

    public static boolean isProduction() {
        // TODO is this correct?
        return FMLLoader.getNaming().equals("srg");
    }
}
