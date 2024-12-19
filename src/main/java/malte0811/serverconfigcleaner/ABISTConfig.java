package malte0811.serverconfigcleaner;

import net.minecraftforge.common.ForgeConfigSpec;

public class ABISTConfig {
    public static final String FAKE_SECRET = "VerySecretString";

    public static ForgeConfigSpec CONFIG_SPEC;
    public static ForgeConfigSpec.ConfigValue<String> ABIST_TOKEN;

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("category");
        ABIST_TOKEN = builder
                .comment("This value is not used for anything, it is only used to detect whether the mod is working")
                .define("abistToken", "", obj -> true);
        builder.pop();
        CONFIG_SPEC = builder.build();
    }
}
