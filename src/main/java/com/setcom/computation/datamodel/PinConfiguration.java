package com.setcom.computation.datamodel;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PinConfiguration {

    public final String pinName;
    public final String pinType;
    public final String isRequired;
    public final String accessType;
    public final DataMultiplicity dataMultiplicity;
    public final TokenMultiplicity tokenMultiplicity;
    public final Map<String, String> accessCredential;

    public PinConfiguration(JSONObject section) throws JSONException {
        this.pinName = section.getString("PinName");
        this.pinType = section.getString("PinType");
        this.isRequired = section.getString("IsRequired");
        this.accessType = section.getString("AccessType");
        this.dataMultiplicity = DataMultiplicity.valueOf(section.getString("DataMultiplicity"));
        this.tokenMultiplicity = TokenMultiplicity.valueOf(section.getString("TokenMultiplicity"));
        this.accessCredential = new HashMap<>();
        for (int i = 0; i < section.names().length(); i++) {
            this.accessCredential.put(section.names().getString(i), section.get(section.names().getString(i)).toString());
        }
    }
}
