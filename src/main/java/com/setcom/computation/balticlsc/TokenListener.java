package com.setcom.computation.balticlsc;

public abstract class TokenListener {

    protected IJobRegistry registry;
    protected IDataHandler data;

    public TokenListener(JobRegistry registry, DataHandler data) {
        this.registry = registry;
        this.data = data;
    }

    //TODO #5 Create Java Doc
    // This is C# XML documentation format
    /// <param name="pinName"></param>
    public abstract void dataReceived(String pinName);

    /// <param name="pinName"></param>
    public abstract void optionalDataReceived(String pinName);

    public abstract void dataReady();

    public abstract void dataComplete();
}
