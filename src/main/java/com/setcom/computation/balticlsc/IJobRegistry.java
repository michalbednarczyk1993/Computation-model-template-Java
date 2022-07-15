package com.setcom.computation.balticlsc;

import com.setcom.computation.datamodel.InputTokenMessage;
import com.setcom.computation.datamodel.Status;
import org.javatuples.Pair;

import java.util.List;

public interface IJobRegistry {
    ///
    /// <param name="pinName"></param>
    Status getPinStatus(String pinName) throws InterruptedException;

    ///
    /// <param name="pinName"></param>
    String getPinValue(String pinName) throws Exception;

    ///
    /// <param name="pinName"></param>
    List<String> getPinValues(String pinName) throws Exception;

    ///
    /// <param name="pinName"></param>
    Pair<List<String>, long[]> getPinValuesNDim(String pinName) throws InterruptedException;

    ///
    /// <param name="pinName"></param>
    List<InputTokenMessage> getPinTokens(String pinName);

    ///
    /// <param name="progress"></param>
    void setProgress(long progress);

    long getProgress();

    ///
    /// <param name="status"></param>
    void setStatus(Status status);

    ///
    /// <param name="name"></param>
    /// <param name="value"></param>
    void setVariable(String name, String value);

    ///
    /// <param name="name"></param>
    Object getVariable(String name);

    ///
    /// <param name="name"></param>
    String getEnvironmentVariable(String name);
}
