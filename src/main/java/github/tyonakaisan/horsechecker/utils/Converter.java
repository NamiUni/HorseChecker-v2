package github.tyonakaisan.horsechecker.utils;

import github.tyonakaisan.horsechecker.horse.HorseStatsRecord;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractHorse;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Objects;

@DefaultQualifier(NonNull.class)
public final class Converter {

    public HorseStatsRecord convertHorseStats(AbstractHorse horse) {
        var rank = HorseRank.calcEvaluateRankString(
                Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).getValue(),
                Objects.requireNonNull(horse.getAttribute(Attribute.HORSE_JUMP_STRENGTH)).getValue()
        );
        return new HorseStatsRecord(getSpeed(horse), getHorseJump(horse), getMaxHealth(horse), getOwnerName(horse), rank);
    }

    private double jumpStrengthToJumpHeight(double strength) {
        return -0.1817584952 * strength * strength * strength + 3.689713992 * strength * strength + 2.128599134 * strength - 0.343930367;
    }

    private double genericSpeedToBlocPerSec(double speed) {
        return 42.162962963 * speed;
    }

    private int ageToBreedingCoolTime(double age) {
        return (int) (age / 20);
    }

    private int loveModeTicksToLoveModeTime(double loveModeTicks) {
        return (int) (loveModeTicks / 20);
    }

    public int getMaxHealth(AbstractHorse horse) {
        return (int) Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
    }

    public double getSpeed(AbstractHorse horse) {
        return genericSpeedToBlocPerSec(Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).getValue());
    }

    public double getHorseJump(AbstractHorse horse) {
        return jumpStrengthToJumpHeight(Objects.requireNonNull(horse.getAttribute(Attribute.HORSE_JUMP_STRENGTH)).getValue());
    }

    public double getAge(AbstractHorse horse) {
        return horse.getAge();
    }

    public String getOwnerName(AbstractHorse horse) {
        if (horse.getOwner() == null) {
            return "no owner";
        } else {
            return "owned by <#ffa500>" + horse.getOwner().getName() + "</#ffa500>";
        }
    }

    public int getBreedingCoolTime(AbstractHorse horse) {
        return ageToBreedingCoolTime(getAge(horse));
    }

    public int getLoveModeTime(AbstractHorse horse) {
        return loveModeTicksToLoveModeTime(horse.getLoveModeTicks());
    }

}
