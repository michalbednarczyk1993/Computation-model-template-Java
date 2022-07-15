package com.setcom.computation.balticlsc;

import com.setcom.computation.datamodel.PinConfiguration;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.List;

public class ConfigurationHandle {
    public static List<PinConfiguration> GetPinsConfiguration(JSONObject configuration) throws JSONException {

        //TODO #1
        //        var pinsSections = configuration.getJSONArray("Pins").name
//        //var pinsSections = configuration.GetSection("Pins").GetChildren();
//        return pinsSections.Select(configurationSection => new PinConfiguration(configurationSection)).ToList();
        return null;
    }
}
