package com.setcom.computation.balticlsc;

import com.setcom.computation.datamodel.PinConfiguration;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationHandle {
    public static List<PinConfiguration> getPinsConfiguration(JSONObject configuration) throws JSONException {
        List<PinConfiguration> result = new ArrayList<>();
        var pinSections = configuration.getJSONObject("Pins").keys();
        while (pinSections.hasNext()) {
            result.add(new PinConfiguration((JSONObject) pinSections.next()));
        }
        return result;
    }
}
