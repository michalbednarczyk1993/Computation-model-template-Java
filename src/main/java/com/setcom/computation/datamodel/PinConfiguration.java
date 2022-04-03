package com.setcom.computation.datamodel;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.HashMap;

public class PinConfiguration {

    public final String pinName = null;
    public final String pinType = null;
    public final String isRequired = null;
    public final String accessType = null;
    public final DataMultiplicity dataMultiplicity = null;
    public final TokenMultiplicity tokenMultiplicity = null;
    public final HashMap<String, String> accessCredential = null;

    //TODO bez testów nie zrozumiem tego

    public PinConfiguration(JSONObject section) throws JSONException {
        this.pinName = section.getString("PinName");
        this.pinType = section.getString("PinType");
        this.isRequired = section.getString("IsRequired");
        this.accessType = section.getString("AccessType");
        this.dataMultiplicity = (DataMultiplicity)Enum.Parse(typeof(DataMultiplicity),
                section.getString("DataMultiplicity"), true);
        tokenMultiplicity = (TokenMultiplicity)Enum.Parse(typeof(TokenMultiplicity),
                section.getString("TokenMultiplicity"), true);
        this.accessCredential = new HashMap<>();
        for (JSONObject aSection : section.getJSONObject("AccessCredential").GetChildren())
            this.accessCredential.put(aSection.keys().next().toString(), aSection.toString());
    }
}
