package malte0811.serverconfigcleaner.mixin;

import malte0811.serverconfigcleaner.SyncCleaner;
import net.minecraftforge.network.packets.ConfigData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraftforge.network.tasks.SyncConfigTask")
public class ConfigSyncMixin {
    @Redirect(
            method = "run",
            at = @At(value = "NEW", target = "Lnet/minecraftforge/network/packets/ConfigData;"),
            remap = false
    )
    private static ConfigData redirectNewConfigData(String name, byte[] data) {
        return SyncCleaner.redirectNewConfigData(name, data);
    }
}
