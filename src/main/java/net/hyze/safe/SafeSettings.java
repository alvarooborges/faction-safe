package net.hyze.safe;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import net.hyze.core.shared.misc.utils.Position;
import net.hyze.core.shared.misc.utils.Vector3D;
import net.hyze.core.shared.world.location.SerializedLocation;
import net.hyze.core.spigot.misc.utils.WorldCuboid;

import java.util.Map;

@Getter
@Builder
public class SafeSettings {

    @Builder.Default
    private final boolean allowFriend = true;

    @Builder.Default
    private final int limit = 3;

    @Singular
    public final Map<Vector3D, Integer> triggers;

    @Builder.Default
    private final int cashPrice = 1500;

    @Builder.Default
    private final int coinsPrice = 5000000;

    @NonNull
    private final WorldCuboid cuboid;

    @NonNull
    private final String appId;

    @NonNull
    private final Position spawn;

}
