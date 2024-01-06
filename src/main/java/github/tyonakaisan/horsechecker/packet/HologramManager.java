package github.tyonakaisan.horsechecker.packet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.tyonakaisan.horsechecker.horse.HorseStatsData;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;

@Singleton
@DefaultQualifier(NonNull.class)
public final class HologramManager {

    private final Map<String, HologramData> hologramMap = new HashMap<>();

    private final Server server;

    @Inject
    public HologramManager(
            final Server server
    ) {
        this.server = server;
    }

    public Set<String> getHologramNames() {
        return Collections.unmodifiableSet(this.hologramMap.keySet());
    }

    public HologramData getHologramData(String hologramId) {
        return this.hologramMap.get(hologramId);
    }

    public void createHologram(HorseStatsData statsData, Component text) {
        var hologramId = statsData.uuid().toString();
        if (this.hologramMap.containsKey(hologramId)) return;
        var hologramData = new HologramData(hologramId, text, statsData.location(), statsData.rankData());

        this.hologramMap.put(hologramId, hologramData);
    }

    public void deleteHologram(HorseStatsData horseStatsData) {
        this.server.forEachAudience(audience -> {
            if (audience instanceof Player player) this.hideHologram(horseStatsData, player);
        });
        var horseUuid = horseStatsData.uuid().toString();
        this.hologramMap.remove(horseUuid);
    }

    public void hideHologram(HorseStatsData statsData, Player player) {
        var hologramId = statsData.uuid().toString();
        Optional.ofNullable(this.hologramMap.get(hologramId)).ifPresent(hologramData -> {
            hologramData.updateLocation(statsData.location());
            hologramData.hideFrom(player);
        });
    }

    public void showHologram(String hologramId, Player player, int vehicleId) {
        Optional.ofNullable(this.hologramMap.get(hologramId)).ifPresent(hologramData -> hologramData.showFrom(player, vehicleId));
    }

    public void changeHologramText(String hologramId, Component text) {
        Optional.ofNullable(this.hologramMap.get(hologramId)).ifPresent(hologramData -> hologramData.updateText(text));
    }
}
