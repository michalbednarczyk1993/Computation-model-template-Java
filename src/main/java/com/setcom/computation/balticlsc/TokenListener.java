package com.setcom.computation.balticlsc;

public abstract class TokenListener {

    protected IJobRegistry registry;
    protected IDataHandler data;

    public TokenListener(IJobRegistry registry, IDataHandler data) {
        this.registry = registry;
        this.data = data;
    }

    /**
     * DataReceived(pinName: string) – called when any single data item (token) has arrived at the
     * pin with the provided name.
     *
     * @param pinName
     */
    public abstract void dataReceived(String pinName);

    /**
     * OptionalDataReceived(pinName: string) – called additionally when the particular pin is
     * optional.
     *
     * @param pinName
     */
    public abstract void optionalDataReceived(String pinName);

    /**
     * DataReady() – called additionally when all the non-optional input pins have received at least
     * one data item (token); can be used for modules with many input pins to determine when the
     * computations can be started.
     */
    public abstract void dataReady();

    /**
     * DataComplete() – called additionally when all the non-optional input pins have received all the
     * expected data items (tokens); no additional data items should since be delivered to the non optional
     * input pins; to be used mainly in case when multiple token input pins exist in the module.
     */
    public abstract void dataComplete();
}
