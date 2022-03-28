package com.setcom.computation.balticlsc;

public abstract class TokenListener {

    protected IJobRegistry registry;
    protected IDataHandler data;

    public TokenListener(JobRegistry registry, DataHandler data) {
        this.registry = registry;
        this.data = data;
    }

    // This is C# XML documentation format
    /// <param name="pinName"></param>
    public abstract void DataReceived(String pinName);

    /// <param name="pinName"></param>
    public abstract void OptionalDataReceived(String pinName);

    public abstract void DataReady();

    public abstract void DataComplete();
}
