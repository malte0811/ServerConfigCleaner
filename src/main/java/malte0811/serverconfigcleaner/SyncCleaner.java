package malte0811.serverconfigcleaner;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import malte0811.serverconfigcleaner.KeyChecker.ConfigKey;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec.ValueSpec;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static malte0811.serverconfigcleaner.ModMain.LOGGER;

public class SyncCleaner {
    private final TomlParser parser = new TomlParser();
    private final TomlWriter writer = new TomlWriter();
    private final KeyChecker checker = new KeyChecker();
    private final Map<String, ModConfig> configsByFile = collectServerConfigsByFile();

    public static <T, A, R>
    Object cleanFromMixin(Stream<T> instance, Collector<? super T, A, R> arCollector) {
        try {
            Map<String, byte[]> oldMap = (Map<String, byte[]>) instance.collect(arCollector);
            return new SyncCleaner().cleanAll(oldMap);
        } catch (Exception x) {
            LOGGER.error("Caught exception while cleaning configs", x);
            throw new RuntimeException(x);
        }
    }

    private Map<String, byte[]> cleanAll(Map<String, byte[]> oldMap) {
        Map<String, byte[]> newMap = new HashMap<>();

        for (Map.Entry<String, byte[]> fileAndBytes : oldMap.entrySet()) {
            String fileName = fileAndBytes.getKey();
            CommentedConfig sanitizedConfig = parser.parse(new ByteArrayInputStream(fileAndBytes.getValue()));

            if (cleanSingleConfig(fileName, sanitizedConfig)) {
                String newTOML = writer.writeToString(sanitizedConfig);
                newMap.put(fileName, newTOML.getBytes(StandardCharsets.UTF_8));
            } else {
                newMap.put(fileName, fileAndBytes.getValue());
            }
        }
        return newMap;
    }

    /**
     * Replace all config entries excluded by the config by their default value
     *
     * @param fileName        Config file corresponding to this config
     * @param sanitizedConfig The config to clean. After the call, this will contain the "safe" data to send to the
     *                        client
     * @return true if the config was modified
     */
    private boolean cleanSingleConfig(String fileName, CommentedConfig sanitizedConfig) {
        ModConfig modConfig = configsByFile.get(fileName);
        if (modConfig == null) {
            LOGGER.error(
                    "Did not find mod config corresponding to config {}. Potential secrets in this config will not be cleaned!",
                    fileName
            );
            return false;
        }

        MutableBoolean cleanedAny = new MutableBoolean();
        ConfigIterator.forEachConfigKey(sanitizedConfig, entryKey -> {
            ConfigKey key = new ConfigKey(modConfig.getModId(), entryKey);
            if (checker.markedExcludedFromSync(key)) {
                // Reset to the default value, since that should not contain any secrets
                Object defaultValue = modConfig.getSpec().get(entryKey);
                if (defaultValue instanceof ValueSpec) {
                    // This should always be the case, but checking first does not hurt
                    defaultValue = ((ValueSpec) defaultValue).getDefault();
                }
                sanitizedConfig.set(entryKey, defaultValue);
                cleanedAny.setTrue();
            }
        });
        if (cleanedAny.isFalse()) {
            if (modConfig.getModId().equals(ModMain.MODID)) {
                LOGGER.error("Expected at least one key to clean in our own config, but did not find any");
                throw new RuntimeException("Did not find own \"secret\" config!");
            }
            return false;
        }
        // Just in case there is a secret in some comment. We cannot just use sanitizedConfig.clearComments() since that
        // will cause a warning on the client due to mismatched comments.
        ConfigIterator.forEachConfigKey(sanitizedConfig, entryKey -> {
            var specValue = modConfig.getSpec().get(entryKey);
            if (specValue instanceof ValueSpec) {
                sanitizedConfig.setComment(entryKey, ((ValueSpec)specValue).getComment());
            }
        });
        return true;
    }

    private static Map<String, ModConfig> collectServerConfigsByFile() {
        Map<String, ModConfig> configs = new HashMap<>();
        for (ModConfig config : ModMain.getServerConfigs()) {
            configs.put(config.getFileName(), config);
        }
        return configs;
    }
}
