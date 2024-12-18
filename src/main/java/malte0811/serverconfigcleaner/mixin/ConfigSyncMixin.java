package malte0811.serverconfigcleaner.mixin;

import malte0811.serverconfigcleaner.SyncCleaner;
import net.neoforged.neoforge.network.ConfigSync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Collector;
import java.util.stream.Stream;

@Mixin(ConfigSync.class)
public class ConfigSyncMixin {
    @Redirect(
            method = "syncConfigs",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;collect(Ljava/util/stream/Collector;)Ljava/lang/Object;")
    )
    public <T, A, R>
    Object redirectCollect(Stream<T> instance, Collector<? super T, A, R> arCollector) {
        return SyncCleaner.cleanFromMixin(instance, arCollector);
    }
}
