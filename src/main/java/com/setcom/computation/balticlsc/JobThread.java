package com.setcom.computation.balticlsc;

import com.setcom.computation.datamodel.Status;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class JobThread {

    private final String pinName;
    private final TokenListener listener;
    private final JobRegistry registry;
    private final DataHandler handler;

    public JobThread(String pinName, TokenListener listener, JobRegistry registry, DataHandler handler)
    {
        this.pinName = pinName;
        this.listener = listener;
        this.registry = registry;
        this.handler = handler;
    }

    public void run(){
        try
        {
            listener.dataReceived(pinName);
            if ("true".equals(registry.GetPinConfiguration(pinName).isRequired))
                listener.optionalDataReceived(pinName);
            Status pinAggregatedStatus = Status.COMPLETED;
            for(String pinName : registry.GetStrongPinNames())
            {
                Status pinStatus = registry.GetPinStatus(pinName);
                if (Status.WORKING == pinStatus)
                    pinAggregatedStatus = Status.WORKING;
                else if (Status.IDLE == pinStatus)
                {
                    pinAggregatedStatus = Status.IDLE;
                    break;
                }
            };

            if (Status.IDLE != pinAggregatedStatus)
                listener.dataReady();
            if (Status.COMPLETED == pinAggregatedStatus)
                listener.dataComplete();
        }
        catch (Exception e)
        {
            log.error("Error of type" + e.getClass() +": " + e.getMessage() + "\n"+ Arrays.toString(e.getStackTrace()));
            handler.FailProcessing(e.getMessage());
        }
    }
}
