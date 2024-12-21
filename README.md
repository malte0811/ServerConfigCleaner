### ServerConfigCleaner

NeoForge and MinecraftForge sync the entire content of all "server configs" to the client. Since this is not clear from
the naming[^1], some mods ended up putting secret values in these configs, e.g. Discord bot tokens. If you run a server
using a mod with this issue, this allows any user that can join your server to access these values. This mod avoids this
by removing sensitive values before the config is sent to the client.

#### Versions

Since this mod covers a wide range of Minecraft versions and two modloaders, the versioning scheme is somewhat
complicated. See the table below for the correct version for your loader and Minecraft version.

|Loader        |Minecraft version|Mod version suffix|
|--------------|-----------------|------------------|
|MinecraftForge|1.13.2 - 1.16.5  |`-1.13.2`         |
|MinecraftForge|1.17.x - 1.20.1  |`-1.17.1`         |
|NeoForge      |1.20.1           |`-1.17.1`         |
|MinecraftForge|1.20.2+          |`-1.20.2-mcf`     |
|NeoForge      |1.20.2 - 1.20.5  |`-1.20.2-neo`     |
|NeoForge      |1.21.0+          |`-1.21.0-neo`     |


#### How it works

This mod works in two stages: First, it detects config options that potentially contain sensitive information during
launch. Since this is done using a very rough heuristic, the options that are found have to be manually classified in
the `config/serverconfigcleaner-common.toml` config file:
1. Most of them will be false positives, harmless options that happen to match the heuristic. For example, the option
   `logistics.seatHostileMobs` in [Create](https://www.curseforge.com/minecraft/mc-mods/create) would be marked as a
   potentially sensitive option since it contains the string `Host`. These config values have to be added to the
   `falsePositives` list.
2. Some of them may be options that contain actual sensitive values. These options have to be added to the `doNotSync`
   list in the same format, e.g. `doNotSync = ["mymod:discord.token"]`.

To continue the launch, all found values have to be classified in the ServerConfigCleaner config. The mod ships with a
list of known false positives and known "problematic" options (the latter not in plaintext). Therefore, you should
usually not have to do anything at this stage. If you have to add values manually, see below on getting them added to
the default lists.

In the second stage, the mod intercepts the config sync on the server. If a value was marked as sensitive in the config
after the first stage, the value will be replaced by the default value before being synced to the client. Since the
default value for sensitive options is generally a meaningless constant value provided by the developer (e.g. `INSERT
BOT TOKEN HERE`), this can be sent to the client without revealing any secret information.

#### Reporting missed values

If you run into false positives that are not in the default list in the config, please create an issue listing them.

If you find any server config options that contain sensitive information and are not part of the default set of
sensitive options, please **do not** create an issue with these options. Instead, please get in touch with either
malte0811 (Discord: `malte0811`; e-mail available on GitHub profile) or ThatGravyBoat (Discord: `thatgravyboat`).

[^1]: Unless you know that the config types are named by which side determines the final value.
