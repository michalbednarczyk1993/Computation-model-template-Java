package com.setcom.computation.datamodel;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.HashMap;

public class PinConfiguration {

    public final String pinName;
    public final String pinType;
    public final String isRequired;
    public final String accessType;
    public final DataMultiplicity dataMultiplicity;
    public final TokenMultiplicity tokenMultiplicity;
    public final HashMap<String, String> accessCredential;

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
            this.accessCredential.put(aSection.Key,aSection.Value);
    }
}
