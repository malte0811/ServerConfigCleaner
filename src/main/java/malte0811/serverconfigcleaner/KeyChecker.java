package malte0811.serverconfigcleaner;

import com.google.common.base.Preconditions;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;

public class KeyChecker {
    private final List<String> problematicKeyParts;
    private final Set<ConfigKey> nonSyncedKeys;
    private final Set<Integer> nonSyncedHashes;
    private final Set<ConfigKey> falsePositives;

    public KeyChecker() {
        problematicKeyParts = new ArrayList<>(CleanerConfig.BAD_CONFIG_PATTERNS.get());
        nonSyncedKeys = ConfigKey.buildSetFromConfig(CleanerConfig.TRUE_PROBLEMATIC_CONFIGS);
        nonSyncedHashes = new HashSet<>(CleanerConfig.PROBLEMATIC_HASHES.get());
        falsePositives = ConfigKey.buildSetFromConfig(CleanerConfig.FALSE_POSITIVES);
    }

    public boolean mayBeProblematic(String key) {
        String lowercaseKey = key.toLowerCase(Locale.ROOT);
        for (String pattern : problematicKeyParts) {
            if (lowercaseKey.contains(pattern.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public boolean markedFalsePositive(ConfigKey key) {
        return falsePositives.contains(key);
    }

    public boolean markedExcludedFromSync(ConfigKey key) {
        return nonSyncedKeys.contains(key) || nonSyncedHashes.contains(key.hashCode());
    }

    public static final class ConfigKey {
        private final String modid;
        private final String key;

        public static ConfigKey fromOwnConfig(String value) {
            int separator = value.indexOf(':');
            Preconditions.checkArgument(separator > 0, "Invalid key " + value);
            return new ConfigKey(value.substring(0, separator), value.substring(separator + 1));
        }

        public static Set<ConfigKey> buildSetFromConfig(ModConfigSpec.ConfigValue<List<? extends String>> value) {
            Set<ConfigKey> result = new HashSet<>();
            for (String element : value.get()) {
                result.add(fromOwnConfig(element));
            }
            return result;
        }

        public ConfigKey(String modid, String key) {
            this.modid = modid;
            this.key = key;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) { return true; }
            if (obj == null || obj.getClass() != this.getClass()) { return false; }
            ConfigKey that = (ConfigKey) obj;
            return Objects.equals(this.modid, that.modid) && Objects.equals(this.key, that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(modid, key);
        }

        @Override
        public String toString() {
            if (FMLLoader.isProduction()) {
                return "\"" + modid + ":" + key + "\"";
            } else {
                return modid + ":" + key + " (" + hashCode() + ")";
            }
        }
    }
}
