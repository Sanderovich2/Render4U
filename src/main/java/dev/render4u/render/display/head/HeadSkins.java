package dev.render4u.render.display.head;

import com.mojang.authlib.GameProfile;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.PlayerSkin;

final class HeadSkins {
    private static final int LIMIT = 128;
    private static final Map<UUID, Supplier<PlayerSkin>> CACHE = new LinkedHashMap<>(32, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, Supplier<PlayerSkin>> eldest) {
            return size() > LIMIT;
        }
    };

    private HeadSkins() {
    }

    static Supplier<PlayerSkin> lookup(GameProfile profile) {
        GameProfile safeProfile = profile != null ? profile : new GameProfile(new UUID(0L, 0L), "Player");
        UUID id = safeProfile.id() != null ? safeProfile.id() : UUID.nameUUIDFromBytes(safeProfile.name().getBytes());
        return CACHE.computeIfAbsent(id, k -> Minecraft.getInstance().getSkinManager().createLookup(safeProfile, false));
    }

    static void clear() {
        CACHE.clear();
    }
}
