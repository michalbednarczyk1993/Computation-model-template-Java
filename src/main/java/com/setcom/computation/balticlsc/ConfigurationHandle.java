package com.setcom.computation.balticlsc;

import com.setcom.computation.datamodel.PinConfiguration;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationHandle {
    public static List<PinConfiguration> GetPinsConfiguration(JSONObject configuration) throws JSONException {
        var pinsSections = configuration.getJSONArray("Pins");
        List<PinConfiguration> result = new ArrayList<>();
        for (int i = 0; i < pinsSections.length(); i++) {
            result.add(new PinConfiguration(pinsSections.getJSONObject(i)));
        }
        return result;
    }
}
