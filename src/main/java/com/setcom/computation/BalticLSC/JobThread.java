package com.setcom.computation.BalticLSC;

import com.setcom.computation.DataModel.Status;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobThread {

    private String pinName;
    private TokenListener listener;
    private JobRegistry registry;
    private DataHandler handler;

    public JobThread(String pinName, TokenListener listener, JobRegistry registry, DataHandler handler)
    {
        this.pinName = pinName;
        this.listener = listener;
        this.registry = registry;
        this.handler = handler;
    }

    public void Run(){
        try
        {
            listener.DataReceived(pinName);
            if ("true" == registry.GetPinConfiguration(pinName).IsRequired)
                listener.OptionalDataReceived(pinName);
            Status pinAggregatedStatus = Status.COMPLETED;
            foreach (String pinName in registry.GetStrongPinNames())
            {
                Status pinStatus = registry.GetPinStatus(pinName);
                if (Status.WORKING == pinStatus)
                    pinAggregatedStatus = Status.WORKING;
                else if (Status.IDLE == pinStatus)
                {
                    pinAggregatedStatus = Status.IDLE;
                    break;
                }
            }

            if (Status.IDLE != pinAggregatedStatus)
                listener.DataReady();
            if (Status.COMPLETED == pinAggregatedStatus)
                listener.DataComplete();
        }
        catch (Exception e)
        {
            log.error($"Error of type {e.GetType()}: {e.Message}\n{e.StackTrace}");
            handler.FailProcessing(e.getMessage());
        }
    }
}
