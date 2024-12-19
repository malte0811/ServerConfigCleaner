package malte0811.serverconfigcleaner;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.*;

public class KeyChecker {
    private final List<String> problematicKeyParts;
    private final ConfigKeySet nonSyncedKeys;
    private final Set<Integer> nonSyncedHashes;
    private final ConfigKeySet falsePositives;

    public KeyChecker() {
        problematicKeyParts = new ArrayList<>(CleanerConfig.BAD_CONFIG_PATTERNS.get());
        nonSyncedKeys = ConfigKeySet.buildSetFromConfig(CleanerConfig.TRUE_PROBLEMATIC_CONFIGS);
        nonSyncedHashes = new HashSet<>(CleanerConfig.PROBLEMATIC_HASHES.get());
        falsePositives = ConfigKeySet.buildSetFromConfig(CleanerConfig.FALSE_POSITIVES);
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
            Preconditions.checkArgument(separator > 0, "Invalid key " + value + ", expected : separator");
            return new ConfigKey(value.substring(0, separator), value.substring(separator + 1));
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

    private static class ConfigKeySet {
        private final Set<ConfigKey> exactEntries;
        private final Map<String, List<String>> prefixes;

        private ConfigKeySet(Set<ConfigKey> exactEntries, Map<String, List<String>> prefixes) {
            this.exactEntries = exactEntries;
            this.prefixes = prefixes;
        }

        public boolean contains(ConfigKey key) {
            if (exactEntries.contains(key)) {
                return true;
            }
            List<String> falsePositivePrefixes = prefixes.getOrDefault(key.modid, ImmutableList.of());
            for (String prefix : falsePositivePrefixes) {
                if (key.key.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        }

        public static ConfigKeySet buildSetFromConfig(ForgeConfigSpec.ConfigValue<List<? extends String>> value) {
            Set<ConfigKey> exactEntries = new HashSet<>();
            Map<String, List<String>> prefixes = new HashMap<>();
            for (String element : value.get()) {
                ConfigKey key = ConfigKey.fromOwnConfig(element);
                if (key.key.contains("*")) {
                    Preconditions.checkArgument(key.key.endsWith("*"), key);
                    String cleanedKey = key.key.substring(0, key.key.length() - 1);
                    Preconditions.checkArgument(!cleanedKey.contains("*"), key);
                    prefixes.computeIfAbsent(key.modid, $ -> new ArrayList<>()).add(cleanedKey);
                } else {
                    exactEntries.add(key);
                }
            }
            return new ConfigKeySet(exactEntries, prefixes);
        }

    }
}
