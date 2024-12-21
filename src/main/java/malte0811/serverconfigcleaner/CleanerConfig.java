package malte0811.serverconfigcleaner;

import com.google.common.collect.ImmutableList;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CleanerConfig {
    public static final ModConfigSpec CONFIG_SPEC;

    private static final List<String> DEFAULT_FALSE_POSITIVES = ImmutableList.of(
            "rftoolsbuilder:scanner.locatorUsePerTickHostile",
            "tombstone:player_death.ghostly_shape_duration",
            // mahoutsukai for some reason puts essentially all config values in "secret", even though none of them are
            // secret in any sense
            "mahoutsukai:secret.*",
            "create:logistics.seatHostileMobs",
            "tombstone:general.ghostly_shape_duration",
            "skinnedlanterns:lanterns.ghost_soul_lantern",
            "skinnedlanterns:lanterns.ghost_lantern",
            "goblinsanddungeons:general.Super Secret Settings",
            "moblassos:hostile_damage_rate",
            "moblassos:hostile_mob_health",
            "moblassos:hostile_lasso_time"
    );
    // For hopefully obvious reasons, the list of config values these hashes correspond to is not public (there is an
    // internal list). All of them are caught by the default patterns.
    public static final List<Integer> KNOWN_PROBLEMATIC_HASHES = ImmutableList.of(
        358182576, -1793003039, -310303834, 1537110965, -691466550, -1263699746, -1205332082, -222630614, 181611141
    );
    private static final List<String> DEFAULT_SUSPICIOUS_PATTERNS = ImmutableList.of(
        "host", "username", "password", "secret", "token", "apikey", "webhook", "jdbc", "sql", "redis", "mongodb", "database"
    );

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BAD_CONFIG_PATTERNS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> TRUE_PROBLEMATIC_CONFIGS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FALSE_POSITIVES;
    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> PROBLEMATIC_HASHES;

    static
    {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        BAD_CONFIG_PATTERNS = builder
                .comment("Patterns used to determine suspicious config keys. Capitalization is ignored.")
                .defineList("badConfigPatterns", DEFAULT_SUSPICIOUS_PATTERNS, obj -> true);
        TRUE_PROBLEMATIC_CONFIGS = builder
                .comment("Options to exclude from config syncing", "Format: modid:category.configKey")
                .defineListAllowEmpty("doNotSync", ImmutableList.of(), obj -> true);
        FALSE_POSITIVES = builder
                .comment(
                        "Options that are detected by the badConfigPatterns, but should still be synced",
                        "Format: modid:category.configKey; * allowed at the end of an entry"
                )
                .defineListAllowEmpty("falsePositives", DEFAULT_FALSE_POSITIVES, obj -> true);
        PROBLEMATIC_HASHES = builder
                .comment(
                        "A somewhat obfuscated list of known options containing secrets.",
                        "Ignore unless you know what you are doing."
                )
                .defineListAllowEmpty("doNotSyncHashes", ImmutableList.of(), obj -> true);

        CONFIG_SPEC = builder.build();
    }
}
