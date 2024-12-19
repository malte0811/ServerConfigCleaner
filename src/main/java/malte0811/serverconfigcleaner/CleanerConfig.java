package malte0811.serverconfigcleaner;

import com.google.common.collect.ImmutableList;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CleanerConfig {
    public static final ModConfigSpec CONFIG_SPEC;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BAD_CONFIG_PATTERNS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> TRUE_PROBLEMATIC_CONFIGS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FALSE_POSITIVES;
    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> PROBLEMATIC_HASHES;

    static
    {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        BAD_CONFIG_PATTERNS = builder
                .comment("Patterns used to determine \"suspicious\" config keys. Capitalization is ignored.")
                .defineList("badConfigPatterns", ImmutableList.of(
                        "host", "username", "password", "secret", "token", "apikey", "webhook", "jdbc", "sql", "redis", "mongodb"
                ), obj -> true);
        TRUE_PROBLEMATIC_CONFIGS = builder
                .comment("Options to exclude from config syncing", "Format: modid:category.configKey")
                .defineListAllowEmpty("doNotSync", ImmutableList.of(), obj -> true);
        FALSE_POSITIVES = builder
                .comment(
                        "Options that are detected by the badConfigPatterns, but should still be synced",
                        "Format: modid:category.configKey; * allowed at the end of an entry"
                )
                .defineListAllowEmpty("falsePositives", ImmutableList.of(), obj -> true);
        PROBLEMATIC_HASHES = builder
                .comment(
                        "An \"obfuscated\" list of known options containing secrets.",
                        "Ignore unless you know what you are doing."
                )
                .defineListAllowEmpty("doNotSyncHashes", ImmutableList.of(358182576), obj -> true);

        CONFIG_SPEC = builder.build();
    }
}
