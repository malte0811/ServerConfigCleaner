function initializeCoreMod() {
    return {
        'Clean server config before sync': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraftforge.fml.config.ConfigTracker'
            },
            'transformer': function(clazz) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode')
                var opcodes = Java.type('org.objectweb.asm.Opcodes');
                var method = undefined;
                for (var i = 0; i < clazz.methods.size(); ++i) {
                    var candidate = clazz.methods.get(i);
                    if (candidate.name == 'syncConfigs' && candidate.desc == '(Z)Ljava/util/List;') {
                        method = candidate;
                        break;
                    }
                }
                if (!method) {
                    throw "Did not find method to inject into";
                }
                var callback = ASMAPI.buildMethodCall(
                    "malte0811/serverconfigcleaner/SyncCleaner",
                    "cleanFromMixin",
                    "(Ljava/util/stream/Stream;Ljava/util/stream/Collector;)Ljava/lang/Object;",
                    ASMAPI.MethodType.STATIC
                );
                for (var i = 0; i < method.instructions.size(); ++i) {
                    var node = method.instructions.get(i);
                    if (node.opcode == opcodes.INVOKEINTERFACE && node.name == 'collect') {
                        method.instructions.set(node, callback);
                        if (ASMAPI.log) {
                            ASMAPI.log("INFO", "Inserted cleaning callback", {});
                        }
                        return clazz;
                    }
                }
                throw "Failed to find collect call to redirect";
            }
        }
    }
}
