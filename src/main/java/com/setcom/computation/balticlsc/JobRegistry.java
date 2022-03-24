package com.setcom.computation.balticlsc;

import com.setcom.computation.datamodel.*;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.core.env.Environment;

import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.Semaphore;

@Slf4j
public class JobRegistry implements IJobRegistry {

    private final List<JobThread> jobThreads;

    private Dictionary<String, List<InputTokenMessage>> tokens;
    private Dictionary<String, Object> variables;
    private JobStatus status;
    private List<PinConfiguration> pins;

    private Semaphore semaphore;

    public JobRegistry(IConfiguration config){
        try
        {
            pins = ConfigurationHandle.GetPinsConfiguration(config);
        }
        catch (Exception e)
        {
            log.error("Error while parsing configuration.");
            throw e;
        }

        jobThreads = new List<JobThread>();
        tokens = new Dictionary<String, List<InputTokenMessage>>();
        foreach (PinConfiguration pc in _pins.FindAll(p => "input" == p.PinType))
        tokens[pc.PinName] = new List<InputTokenMessage>();
        variables = new Dictionary<String, Object>();
        jobInstanceUid = System.getenv("SYS_MODULE_INSTANCE_UID");
        status = new JobStatus()
        {
            //JobInstanceUid = Environment.GetEnvironmentVariable("SYS_MODULE_INSTANCE_UID")
        };
        semaphore = new Semaphore(1);
    }

    public short registerThread(JobThread thread) throws InterruptedException {
        semaphore.wait();
        try
        {
            jobThreads.add(thread);
            return 0;
        }
        finally
        {
            semaphore.release();
        }
    }

    ///
    /// <param name="pinName"></param>
    public Status GetPinStatus(String pinName) throws InterruptedException {

        semaphore.wait();
        try
        {
            if (0 == tokens.get(pinName).Count)
                return Status.IDLE;
            if (TokenMultiplicity.SINGLE == GetPinConfigurationInternal(pinName).TokenMultiplicity)
                return Status.COMPLETED;
            InputTokenMessage finalToken =
                    tokens.get(pinName).Find(t => !t.TokenSeqStack.ToList().Exists(s => !s.IsFinal));
            if (null != finalToken)
            {
                long maxCount = 1;
                for (SeqToken token : finalToken.tokenSeqStack)
                maxCount *= token.No + 1;
                if (tokens.get(pinName).Count == maxCount)
                    return Status.COMPLETED;
            }

            return Status.WORKING;
        }
        finally
        {
            semaphore.release();
        }
    }

    public String GetPinValue(String pinName)
    {
        List<String> values;
        long[] sizes;
        (values, sizes) = GetPinValuesNDim(pinName);
        if (null == values || 0 == values.size())
            return null;
        if (null == sizes && 1 == values.size())
            return values.get(0);
        throw new Exception("Improper call - more than one token exists for the pin");
    }

    public List<String> GetPinValues(String pinName)
    {
        List<String> values;
        long[] sizes;
        (values, sizes) = GetPinValuesNDim(pinName);
        if (1 == sizes?.Length)
        return values;
        throw new Exception("Improper call - more than one dimension exists for the pin");
    }

    ///
    /// <param name="pinName"></param>
    public Pair<List<String>, long[]> GetPinValuesNDim(String pinName) {
        try
        {
            semaphore.wait();

            if (0 == tokens.get(pinName).size())
                return new Pair<>(null, null);

            // Single token pin:

            if (TokenMultiplicity.SINGLE == GetPinConfigurationInternal(pinName).TokenMultiplicity)
                return (new List<String>() {tokens.get(pinName).FirstOrDefault().Values}, null);

            // Multiple token pin:

            long[] maxTableCounts = new long[tokens.get(pinName).FirstOrDefault().TokenSeqStack.Count()];
            for (InputTokenMessage message : tokens.get(pinName))
            for (int i = 0; i < message.tokenSeqStack.Count(); i++) // TODO Enumeration nie ma niczego co by pasowało do tego. Poszukać innego zasobu.
                if (maxTableCounts[i] < message.tokenSeqStack.ToList()[i].No)
                    maxTableCounts[i] = message.tokenSeqStack.ToList()[i].No;

            long allTokenCount = 1;
            for (long index : maxTableCounts)
            allTokenCount *= index + 1;

            String[] result = new String[allTokenCount];

            for (InputTokenMessage message : tokens.get(pinName))
            {
                long index = 0;
                long product = 1;
                for (int i = 0; i < message.tokenSeqStack.Count(); i++)
                {
                    index += maxTableCounts[i] * product;
                    product *= maxTableCounts[i];
                }

                result[index] = message.values;
            }

            return new Pair<result.ToList(), maxTableCounts>();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        finally
        {
            semaphore.release();
        }
    }

    ///
    /// <param name="pinName"></param>
    public List<InputTokenMessage> GetPinTokens(String pinName)
    {
        try
        {
            semaphore.wait();
            return tokens.get(pinName);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        finally
        {
            semaphore.release();
        }
    }

    ///
    /// <param name="progress"></param>
    public void SetProgress(long progress) {
        try {
            semaphore.wait();
            status.jobProgress = progress;
            if (0 <= progress && Status.COMPLETED != status.status)
                status.status = Status.WORKING;
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        } finally {
            semaphore.release();
        }
    }

    public long GetProgress() {
        try {
            semaphore.wait();
            return status.jobProgress;
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            return -1;
        } finally {
            semaphore.release();
        }
    }

    ///
    /// <param name="status"></param>
    public void SetStatus(Status status) {
        try {
            semaphore.wait();
            this.status.status = status;
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        } finally {
            semaphore.release();
        }
    }

    public JobStatus getJobStatus() {
        try {
            semaphore.wait();
            return status;
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            return null;
        } finally {
            semaphore.release();
        }
    }

    ///
    /// <param name="name"></param>
    /// <param name="value"></param>
    public void SetVariable(String name, Object value) {
        try {
            semaphore.wait();
            variables[name] = value;
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        } finally {
            semaphore.release();
        }
    }

    ///
    /// <param name="name"></param>
    public Object GetVariable(String name) {
        try {
            semaphore.wait();
            return variables[name];
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            return null;
        } finally {
            semaphore.release();
        }
    }

    public String GetEnvironmentVariable(String name) {
        return Environment.GetEnvironmentVariable(name);
    }

    public PinConfiguration GetPinConfiguration(String pinName) {
        try
        {
            semaphore.wait();
            return GetPinConfigurationInternal(pinName);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            return null;
        } finally {
            semaphore.release();
        }
    }

    private PinConfiguration GetPinConfigurationInternal(String pinName)
    {
        return pins.Find(x => x.PinName == pinName);
    }

    public List<String> GetStrongPinNames()
    {
        try {
            semaphore.wait();
            return pins.FindAll(p => "true" == p.IsRequired).Select(p => p.PinName).ToList();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            return null;
        } finally {
            semaphore.release();
        }
    }

    public String GetBaseMsgUid() {
        try {
            semaphore.wait();
            return tokens.Values.ToList().Find(ltm => 0 != ltm.Count)?.FirstOrDefault()?.MsgUid;
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        } finally {
            semaphore.release();
        }
    }

    public List<String> GetAllMsgUids() {
        try {
            semaphore.wait();
            return tokens.Values.ToList().SelectMany(it => it).
            Select(it => it.MsgUid).ToList();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            return null;
        } finally {
            semaphore.release();
        }
    }

    public void ClearMessages(List<String> msgUids)
    {
        for (String msgUid : msgUids)
        {
            List<InputTokenMessage> tokens = tokens.Values.ToList().
                    Find(l => l.Exists(it => msgUid == it.MsgUid));
            if (null != tokens)
            {
                InputTokenMessage message = tokens.Find(it => msgUid == it.MsgUid);
                tokens.Remove(message);
            }
        }
    }

    public void RegisterToken(InputTokenMessage msg) {
        tokens.get(msg.pinName).add(msg);
    }
}
