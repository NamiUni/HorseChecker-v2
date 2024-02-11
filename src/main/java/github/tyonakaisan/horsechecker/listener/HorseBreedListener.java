package github.tyonakaisan.horsechecker.listener;

import com.google.inject.Inject;
import com.tyonakaisan.glowlib.glow.Glow;
import github.tyonakaisan.horsechecker.config.ConfigFactory;
import github.tyonakaisan.horsechecker.horse.Converter;
import github.tyonakaisan.horsechecker.horse.HorseFinder;
import github.tyonakaisan.horsechecker.manager.StateManager;
import github.tyonakaisan.horsechecker.message.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Objects;

@DefaultQualifier(NonNull.class)
public final class HorseBreedListener implements Listener {

    private final StateManager stateManager;
    private final ConfigFactory configFactory;
    private final Messages messages;
    private final HorseFinder horseFinder;

    @Inject
    public HorseBreedListener(
            final StateManager stateManager,
            final Messages messages,
            final ConfigFactory configFactory,
            final HorseFinder horseFinder
    ) {
        this.stateManager = stateManager;
        this.messages = messages;
        this.configFactory = configFactory;
        this.horseFinder = horseFinder;
    }

    @EventHandler
    public void onCanceledBreed(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();

        //toggleチェック
        //繫殖させるためのアイテムか
        if (!this.stateManager.state(player, "breed") || !this.isBreedItem(itemStack)) return;

        AbstractHorse horse = (AbstractHorse) event.getRightClicked();
        int maxHealth = (int) Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
        int health = (int) horse.getHealth();
        int age = horse.getAge();
        int loveMode = horse.getLoveModeTicks();
        var horseStats = Converter.convertHorseStats(horse);

        //繫殖クールタイム中&体力がMAXであればイベントキャンセル
        if (age > 0 && health == maxHealth) {
            player.sendActionBar(
                    this.messages.translatable(
                            Messages.Style.ERROR,
                            player,
                            "breeding.normal_cool_time",
                            TagResolver.builder()
                                    .tag("cool_time", Tag.selfClosingInserting(Component.text(horseStats.breedingCoolTime())))
                                    .build()));
            event.setCancelled(true);
            //繫殖モード中(ハートが出てる時)&体力がMAXであればイベントキャンセル
        } else if (loveMode > 0 && health == maxHealth) {
            player.sendActionBar(
                    this.messages.translatable(
                            Messages.Style.ERROR,
                            player,
                            "breeding.current_love_mode_time",
                            TagResolver.builder()
                                    .tag("cool_time", Tag.selfClosingInserting(Component.text(horseStats.loveModeTime())))
                                    .build()));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreeding(EntityBreedEvent event) {
        if (event.getBreeder() instanceof Player player) {
            if (!this.stateManager.state(player, "breed_notification")) return;

            var childrenHorse = (AbstractHorse) event.getEntity();
            var motherHorse = (AbstractHorse) event.getMother();
            var fatherHorse = (AbstractHorse) event.getFather();

            var childrenStats = Converter.convertHorseStats(childrenHorse);
            var motherStats = Converter.convertHorseStats(motherHorse);
            var fatherStats = Converter.convertHorseStats(fatherHorse);

            player.sendMessage(
                    this.messages.translatable(
                            Messages.Style.SUCCESS,
                            player,
                            "breeding.notification",
                            TagResolver.builder()
                                    .tag("call", Tag.styling(style ->
                                            style.clickEvent(ClickEvent.callback(audience -> {
                                                if (audience instanceof Player callPlayer) {
                                                    this.horseFinder.fromUuid(childrenHorse.getUniqueId(), callPlayer);
                                                    this.horseFinder.showing(motherHorse, callPlayer, Glow.Color.RED);
                                                    this.horseFinder.showing(fatherHorse, callPlayer, Glow.Color.BLUE);
                                                }
                                            }, builder -> builder.uses(3)))))
                                    .tag("hover", Tag.styling(style ->
                                            style.hoverEvent(HoverEvent.showText(Component.text()
                                                    .append(Converter.withParentsStatsMessageResolver(this.configFactory, childrenStats, motherStats, fatherStats))))))
                                    .build()));
        }
    }

    private boolean isBreedItem(ItemStack itemStack) {
        return itemStack.getType() == Material.GOLDEN_CARROT ||
                itemStack.getType() == Material.GOLDEN_APPLE ||
                itemStack.getType() == Material.ENCHANTED_GOLDEN_APPLE ||
                itemStack.getType() == Material.HAY_BLOCK;
    }
}
