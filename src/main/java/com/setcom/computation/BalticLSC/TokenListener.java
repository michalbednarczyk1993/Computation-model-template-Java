package com.setcom.computation.BalticLSC;

public abstract class TokenListener {

    protected IJobRegistry registry;
    protected IDataHandler data;

    public TokenListener(JobRegistry registry, DataHandler data) {
        this.registry = registry;
        this.data = data;
    }

    //TODO what is that "<param name" for?
    /// <param name="pinName"></param> // C# element
    public abstract void DataReceived(String pinName);

    /// <param name="pinName"></param>
    public abstract void OptionalDataReceived(String pinName);

    public abstract void DataReady();

    public abstract void DataComplete();
}
