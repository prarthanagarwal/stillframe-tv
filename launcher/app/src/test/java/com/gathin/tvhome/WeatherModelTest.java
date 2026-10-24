package com.gathin.tvhome;
import org.junit.Test;
import static org.junit.Assert.*;
public class WeatherModelTest {
    @Test public void validatesUntrustedTemperature() {
        assertFalse(WeatherModel.valid(Double.NaN)); assertFalse(WeatherModel.valid(Double.POSITIVE_INFINITY));
        assertFalse(WeatherModel.valid(200)); assertTrue(WeatherModel.valid(28.4));
    }
    @Test public void convertsAndMapsCurrentWeather() {
        assertEquals(32, WeatherModel.degrees(0, true)); assertEquals(29, WeatherModel.degrees(28.6, false));
        assertEquals("Rain", WeatherModel.condition(63)); assertEquals("Thunderstorms", WeatherModel.condition(95));
        assertEquals("Weather", WeatherModel.condition(-1));
    }
}
