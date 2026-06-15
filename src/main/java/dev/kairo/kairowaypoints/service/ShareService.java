package dev.kairo.kairowaypoints.service;

import com.google.gson.Gson;
import dev.kairo.kairowaypoints.config.ConfigService;
import dev.kairo.kairowaypoints.model.ShareData;
import dev.kairo.kairowaypoints.model.Waypoint;
import dev.kairo.kairowaypoints.model.WaypointGroup;
import dev.kairo.kairowaypoints.model.WorldIdentity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class ShareService {
    private static final String PREFIX = "KWP1:";
    private final Gson gson;
    private final ConfigService config;

    public ShareService(Gson gson, ConfigService config) { this.gson = gson; this.config = config; }

    public String encode(Waypoint waypoint, WaypointGroup group) {
        String description = config.get().sharing.removeDescription ? "" : waypoint.description();
        ShareData data = new ShareData(1, waypoint.name(), waypoint.x(), waypoint.y(), waypoint.z(), waypoint.dimension(),
            waypoint.color(), waypoint.icon(), waypoint.type(), description, group.name());
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(gson.toJson(data).getBytes(StandardCharsets.UTF_8));
    }

    public ShareData decode(String code) {
        if (code == null || !code.startsWith(PREFIX) || code.length() > 32768) throw new IllegalArgumentException("invalid share code");
        try {
            String json = new String(Base64.getUrlDecoder().decode(code.substring(PREFIX.length())), StandardCharsets.UTF_8);
            ShareData data = gson.fromJson(json, ShareData.class);
            if (data == null || data.schemaVersion() != 1 || data.name() == null || data.name().isBlank()
                || data.name().length() > 64 || !Double.isFinite(data.x()) || !Double.isFinite(data.y()) || !Double.isFinite(data.z())
                || data.dimension() == null || !data.dimension().matches("[a-z0-9_.-]+:[a-z0-9_/.-]+")) {
                throw new IllegalArgumentException("invalid share code");
            }
            return data;
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("invalid share code", exception);
        }
    }

    public Waypoint toWaypoint(ShareData data, WorldIdentity world, WaypointGroup group) {
        Waypoint base = Waypoint.create(data.name(), data.x(), data.y(), data.z(),
            new WorldIdentity(world.scopeId(), world.displayName(), data.dimension()), group.id(), data.type());
        return base.edited(data.name(), data.description(), data.x(), data.y(), data.z(), group.id(), data.type(),
            data.color(), data.icon(), base.visibility(), false, false, false, null);
    }
}
