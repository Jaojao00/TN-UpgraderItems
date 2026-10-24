package vn.tainguyen.tnupgrader.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerProfile {

    private final UUID uuid;
    private String playerName;
    private UiTheme theme = UiTheme.GOLD;
    private boolean spinAnimation = true;
    private boolean soundEnabled = true;
    private boolean broadcastEnabled = true;

    private int totalAttempts = 0;
    private int totalWins = 0;
    private int totalLosses = 0;
    private int currentStreak = 0;
    private int bestStreak = 0;
    private double totalWageredValue = 0.0;
    private double totalWonValue = 0.0;
    private double pityBonus = 0.0;

    private final Set<String> favoriteItemIds = new LinkedHashSet<>();

    public PlayerProfile(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public UiTheme getTheme() {
        return theme;
    }

    public void setTheme(UiTheme theme) {
        this.theme = theme != null ? theme : UiTheme.GOLD;
    }

    public boolean isSpinAnimation() {
        return spinAnimation;
    }

    public void setSpinAnimation(boolean spinAnimation) {
        this.spinAnimation = spinAnimation;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public boolean isBroadcastEnabled() {
        return broadcastEnabled;
    }

    public void setBroadcastEnabled(boolean broadcastEnabled) {
        this.broadcastEnabled = broadcastEnabled;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    public int getTotalLosses() {
        return totalLosses;
    }

    public void setTotalLosses(int totalLosses) {
        this.totalLosses = totalLosses;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }

    public double getTotalWageredValue() {
        return totalWageredValue;
    }

    public void setTotalWageredValue(double totalWageredValue) {
        this.totalWageredValue = totalWageredValue;
    }

    public double getTotalWonValue() {
        return totalWonValue;
    }

    public void setTotalWonValue(double totalWonValue) {
        this.totalWonValue = totalWonValue;
    }

    public double getPityBonus() {
        return pityBonus;
    }

    public void setPityBonus(double pityBonus) {
        this.pityBonus = Math.max(0.0, pityBonus);
    }

    public Set<String> getFavoriteItemIds() {
        return favoriteItemIds;
    }

    public boolean toggleFavorite(String itemId) {
        if (favoriteItemIds.contains(itemId)) {
            favoriteItemIds.remove(itemId);
            return false;
        } else {
            favoriteItemIds.add(itemId);
            return true;
        }
    }

    public boolean isFavorite(String itemId) {
        return favoriteItemIds.contains(itemId);
    }

    public void recordResult(boolean win, double wageredV, double wonV) {
        this.totalAttempts++;
        this.totalWageredValue += wageredV;
        if (win) {
            this.totalWins++;
            this.totalWonValue += wonV;
            this.currentStreak++;
            if (this.currentStreak > this.bestStreak) {
                this.bestStreak = this.currentStreak;
            }
        } else {
            this.totalLosses++;
            this.currentStreak = 0;
        }
    }

    public double getWinRate() {
        if (totalAttempts <= 0) {
            return 0.0;
        }
        return (totalWins * 100.0) / totalAttempts;
    }
}
