package com.gathin.tvhome;
final class WeatherModel {
    static boolean valid(double t) { return !Double.isNaN(t) && !Double.isInfinite(t) && t >= -100 && t <= 65; }
    static long degrees(double c, boolean fahrenheit) { return Math.round(fahrenheit ? c * 9 / 5 + 32 : c); }
    static String condition(int code) {
        if (code == 0) return "Clear";
        if (code == 1) return "Mostly clear";
        if (code == 2) return "Partly cloudy";
        if (code == 3) return "Overcast";
        if (code == 45 || code == 48) return "Fog";
        if (code >= 51 && code <= 57) return "Drizzle";
        if (code >= 61 && code <= 67) return "Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Showers";
        if (code == 85 || code == 86) return "Snow showers";
        if (code >= 95 && code <= 99) return "Thunderstorms";
        return "Weather";
    }
}
