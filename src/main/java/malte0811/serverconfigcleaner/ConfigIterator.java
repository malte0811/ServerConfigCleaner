package malte0811.serverconfigcleaner;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

import java.util.function.Consumer;

public class ConfigIterator {
    public static void forEachConfigKey(UnmodifiableConfig config, Consumer<String> keyConsumer) {
        forEachConfigKeyRecursive(config, keyConsumer, "");
    }

    private static void forEachConfigKeyRecursive(
            UnmodifiableConfig config, Consumer<String> keyConsumer, String keyPrefix
    ) {
        for (UnmodifiableConfig.Entry entry : config.entrySet()) {
            String entryKey = keyPrefix + entry.getKey();
            if (entry.getRawValue() instanceof UnmodifiableConfig innerConfig) {
                forEachConfigKeyRecursive(innerConfig, keyConsumer, entryKey + ".");
            } else {
                keyConsumer.accept(entryKey);
            }
        }
    }
}
