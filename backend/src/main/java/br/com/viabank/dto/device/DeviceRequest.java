package br.com.viabank.dto.device;
public record DeviceRequest(String userAgent, String language, String platform, Screen screen, String timezone) {
    public record Screen(Integer width, Integer height) {}
}
