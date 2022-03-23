package com.setcom.computation.balticlsc;

import com.setcom.computation.datamodel.PinConfiguration;

import java.util.List;

public class ConfigurationHandle {
    public static List<PinConfiguration> GetPinsConfiguration(IConfiguration configuration)
    {
        var pinsSections = configuration.GetSection("Pins").GetChildren();
        return pinsSections.Select(configurationSection => new PinConfiguration(configurationSection)).ToList();
    }
}
