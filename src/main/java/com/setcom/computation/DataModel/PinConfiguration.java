package com.setcom.computation.DataModel;

public class PinConfiguration {

    public final String pinName;
    public final String pinType;
    public final String isRequired;
    public final String accessType;
    public final DataMultiplicity dataMultiplicity;
    public final TokenMultiplicity tokenMultiplicity;
    public final Dictionary<String, String> accessCredential;

    public PinConfiguration(IConfigurationSection section)
    {
        this.pinName = sec
        tion.GetValue<String>("PinName");
        this.pinType = section.GetValue<String>("PinType");
        this.isRequired = section.GetValue<string>("IsRequired");
        this.accessType = section.GetValue<string>("AccessType");
        this.dataMultiplicity = (DataMultiplicity)Enum.Parse(typeof(DataMultiplicity),
                section.GetValue<string>("DataMultiplicity"), true);
        TokenMultiplicity = (TokenMultiplicity)Enum.Parse(typeof(TokenMultiplicity),
                section.GetValue<string>("TokenMultiplicity"), true);
        this.accessCredential = new Dictionary<string, string>();
        foreach (IConfigurationSection aSection in section.GetSection("AccessCredential").GetChildren())
        this.accessCredential.Add(aSection.Key,aSection.Value);
    }
}
