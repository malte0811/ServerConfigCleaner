package malte0811.serverconfigcleaner;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CleanerConfig {
    public static final ForgeConfigSpec CONFIG_SPEC;

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
    private static final List<Integer> KNOWN_PROBLEMATIC_HASHES = ImmutableList.of(358182576, -1793003039, -310303834, 1537110965, -691466550, -1263699746, -1205332082);
    private static final List<String> DEFAULT_SUSPICIOUS_PATTERNS = ImmutableList.of(
        "host", "username", "password", "secret", "token", "apikey", "webhook", "jdbc", "sql", "redis", "mongodb", "database"
    );

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BAD_CONFIG_PATTERNS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TRUE_PROBLEMATIC_CONFIGS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> FALSE_POSITIVES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> PROBLEMATIC_HASHES;

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        BAD_CONFIG_PATTERNS = builder
                .comment("Patterns used to determine suspicious config keys. Capitalization is ignored.")
                .defineList("badConfigPatterns", DEFAULT_SUSPICIOUS_PATTERNS, obj -> true);
        TRUE_PROBLEMATIC_CONFIGS = builder
                .comment("Options to exclude from config syncing", "Format: modid:category.configKey")
                .defineList(ImmutableList.of("doNotSync"), ImmutableList::of, obj -> true);
        FALSE_POSITIVES = builder
                .comment(
                        "Options that are detected by the badConfigPatterns, but should still be synced",
                        "Format: modid:category.configKey; * allowed at the end of an entry"
                )
                .defineList(ImmutableList.of("falsePositives"), () -> DEFAULT_FALSE_POSITIVES, obj -> true);
        PROBLEMATIC_HASHES = builder
                .comment(
                        "A somewhat obfuscated list of known options containing secrets.",
                        "Ignore unless you know what you are doing."
                )
                .defineList(ImmutableList.of("doNotSyncHashes"), () -> KNOWN_PROBLEMATIC_HASHES, obj -> true);

        CONFIG_SPEC = builder.build();
    }
}
