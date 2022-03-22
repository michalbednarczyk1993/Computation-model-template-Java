package com.setcom.computation.BalticLSC;

import com.setcom.computation.DataModel.InputTokenMessage;
import com.setcom.computation.DataModel.Status;
import org.javatuples.Pair;

import java.util.List;

public interface IJobRegistry {
    ///
    /// <param name="pinName"></param>
    Status GetPinStatus(String pinName);

    ///
    /// <param name="pinName"></param>
    String GetPinValue(String pinName);

    ///
    /// <param name="pinName"></param>
    List<String> GetPinValues(String pinName);

    ///
    /// <param name="pinName"></param>
    Pair<List<String>, long[]> GetPinValuesNDim(String pinName);

    ///
    /// <param name="pinName"></param>
    List<InputTokenMessage> GetPinTokens(String pinName);

    ///
    /// <param name="progress"></param>
    void SetProgress(long progress);

    long GetProgress();

    ///
    /// <param name="status"></param>
    void SetStatus(Status status);

    ///
    /// <param name="name"></param>
    /// <param name="value"></param>
    void SetVariable(String name, String value);

    ///
    /// <param name="name"></param>
    Object GetVariable(String name);

    ///
    /// <param name="name"></param>
    String GetEnvironmentVariable(String name);
}
