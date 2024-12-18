package malte0811.serverconfigcleaner;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ABISTConfig {
    public static final String FAKE_SECRET = "VerySecretString";

    public static ModConfigSpec CONFIG_SPEC;
    public static ModConfigSpec.ConfigValue<String> ABIST_TOKEN;

    static
    {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("category");
        ABIST_TOKEN = builder
                .comment("This value is not used for anything, it is only used to detect whether the mod is working")
                .define("abistToken", "", obj -> true);
        builder.pop();
        CONFIG_SPEC = builder.build();
    }
}
