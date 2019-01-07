package io.github.alphahelixdev.alpary.game;

import io.github.alphahelixdev.alpary.Alpary;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface SpectatorHandler {

    void addSpectator(Player p, String prefix, Location spawn);

    void removeSpectator(Player p);

    class GameSpectatorHandler implements SpectatorHandler {
        private final Map<UUID, String> prefix = new HashMap<>();
        private final PlayerHandler playerHandler;

        public GameSpectatorHandler(PlayerHandler playerHandler) {
            this.playerHandler = playerHandler;
        }

        public void addSpectator(Player player, String prefix, Location spawn) {
            if (!this.prefix.containsKey(player.getUniqueId()))
                this.prefix.put(player.getUniqueId(), prefix);

            resetStats(player);
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.setPlayerListName(prefix + player.getDisplayName());
            player.setDisplayName(prefix + player.getDisplayName());
            player.setCanPickupItems(false);
            player.setCustomName(prefix + player.getDisplayName());
            player.setCustomNameVisible(true);
            for (PotionEffect eff : player.getActivePotionEffects())
                player.removePotionEffect(eff.getType());

            player.teleport(spawn);

            for (Player p : this.playerHandler.getPlayerSet())
                p.hidePlayer(Alpary.getInstance(), player);

            playerHandler.removeAlivePlayer(player);
        }

        @Override
        public void removeSpectator(Player player) {
            resetStats(player);
            player.setGameMode(GameMode.SURVIVAL);
            player.setAllowFlight(false);
            player.setCanPickupItems(true);

            for (PotionEffect eff : player.getActivePotionEffects())
                player.removePotionEffect(eff.getType());

            if (this.prefix.containsKey(player.getUniqueId())) {
                String prefix = this.prefix.get(player.getUniqueId());

                player.setCustomName(player.getDisplayName().replaceFirst(prefix, ""));
                player.setPlayerListName(player.getDisplayName().replaceFirst(prefix, ""));
                player.setDisplayName(player.getDisplayName().replaceFirst(prefix, ""));
                player.setCustomNameVisible(true);
            }

            for (Player p : playerHandler.getPlayerSet())
                p.showPlayer(Alpary.getInstance(), player);
        }

        private void resetStats(Player p) {
            p.setFoodLevel(20);
            p.setLevel(0);
            p.setExp(0);
            p.setHealthScale(20);
            p.setHealth(20);
            p.setTotalExperience(0);
        }
    }
}
