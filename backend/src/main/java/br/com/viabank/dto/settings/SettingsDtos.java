package br.com.viabank.dto.settings;
public final class SettingsDtos {
    private SettingsDtos() {}
    public record SettingsResponse(boolean compactMode, boolean notifications, boolean hideBalance) {}
    public record UpdateSettingsRequest(boolean compactMode, boolean notifications, boolean hideBalance) {}
}
