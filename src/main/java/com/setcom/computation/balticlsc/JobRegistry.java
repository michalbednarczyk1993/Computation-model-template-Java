package com.setcom.computation.balticlsc;

import com.setcom.computation.datamodel.*;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JobRegistry implements IJobRegistry {

    private volatile List<JobThread> jobThreads;
    private final HashMap<String, List<InputTokenMessage>> tokens;
    private final HashMap<String, Object> variables;
    private final JobStatus status;
    private static volatile List<PinConfiguration> pins;

    private final Semaphore semaphore;

    @Autowired
    public JobRegistry(JSONObject config) throws JSONException {
        try {
            pins = ConfigurationHandle.GetPinsConfiguration(config);
        } catch (JSONException e) {
            log.error("Error while parsing configuration.");
            throw e;
        }

        jobThreads = new ArrayList<>();
        tokens = new HashMap<>();
        variables = new HashMap<>();

        for (PinConfiguration pc : pins) {
            if (pc.pinType.equals("input")) {
                tokens.put(pc.pinName, new LinkedList<>());
            }
        }

        status = new JobStatus();
        status.setJobInstanceUid(System.getenv("SYS_MODULE_INSTANCE_UID"));
        semaphore = new Semaphore(1);
    }

    public void registerThread(JobThread thread) throws InterruptedException {
        semaphore.wait();
        try {
            jobThreads.add(thread);
        } finally {
            semaphore.release();
        }
    }

    /**
     *
     * @param pinName
     * @return
     * @throws InterruptedException
     */
    public Status GetPinStatus(String pinName) throws InterruptedException {
        semaphore.wait();
        try {
            if (0 == tokens.get(pinName).size())
                return Status.IDLE;
            if (TokenMultiplicity.SINGLE == GetPinConfigurationInternal(pinName).tokenMultiplicity)
                return Status.COMPLETED;

            InputTokenMessage finalToken = tokens.get(pinName).
                    stream().filter((t)-> t.tokenSeqStack.stream().allMatch((s)-> s.isFinal)).findFirst().orElseThrow();

            long maxCount = 1;
            for (SeqToken token : finalToken.tokenSeqStack)
            maxCount *= token.no + 1;
            if (tokens.get(pinName).size() == maxCount)
                return Status.COMPLETED;
            return Status.WORKING;
        } finally {
            semaphore.release();
        }
    }

    public String GetPinValue(String pinName) throws Exception {
        Pair<List<String>, long[]> pair = GetPinValuesNDim(pinName);
        List<String> values = pair.getValue0();
        long[] sizes = pair.getValue1();

        if (null == values || 0 == values.size())
            return null;
        if (null == sizes && 1 == values.size())
            return values.get(0);
        throw new Exception("Improper call - more than one token exists for the pin");
    }

    public List<String> GetPinValues(String pinName) throws Exception {
        Pair<List<String>, long[]> pair = GetPinValuesNDim(pinName);
        List<String> values = pair.getValue0();
        long[] sizes = pair.getValue1();

        if (sizes != null && sizes.length == 1)
            return values;
        throw new Exception("Improper call - more than one dimension exists for the pin");
    }

    /**
     *
     * @param pinName
     * @return
     */
    public Pair<List<String>, long[]> GetPinValuesNDim(String pinName) {
        try {
            semaphore.wait();

            if (0 == tokens.get(pinName).size())
                return new Pair<>(null, null);

            // Single token pin:
            if (TokenMultiplicity.SINGLE == GetPinConfigurationInternal(pinName).tokenMultiplicity)
                return new Pair<>(new List<String>(tokens.get(pinName).stream().findFirst().orElse().values), null);


            // Multiple token pin:
            long[] maxTableCounts = new long[tokens.get(pinName).stream().findFirst().orElse().tokenSeqStack.Count()];
            for (InputTokenMessage message : tokens.get(pinName))
            for (int i = 0; i < message.tokenSeqStack.size(); i++)
                if (maxTableCounts[i] < message.tokenSeqStack.get(i).no)
                    maxTableCounts[i] = message.tokenSeqStack.get(i).no;


            long allTokenCount = 1;
            for (long index : maxTableCounts)
            allTokenCount *= index + 1;

            String[] result = new String[(int) allTokenCount];

            for (InputTokenMessage message : tokens.get(pinName)) {
                long index = 0;
                long product = 1;
                for (int i = 0; i < message.tokenSeqStack.size(); i++) {
                    index += maxTableCounts[i] * product;
                    product *= maxTableCounts[i];
                }

                result[(int) index] = message.values;
            }

            return new Pair<> (Arrays.stream(result).collect(Collectors.toList()), maxTableCounts);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        } finally {
            semaphore.release();
        }
        return null;
    }

    public List<InputTokenMessage> GetPinTokens(String pinName) {
        try {
            semaphore.wait();
            return tokens.get(pinName);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        } finally {
            semaphore.release();
        }
        return null;
    }

    /**
     *
     * @param progress
     */
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

    /**
     *
     * @param status
     */
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

    /**
     *
     * @param name
     * @param value
     */
    public void SetVariable(String name, String value) {
        try {
            semaphore.wait();
            variables.(name) = value;
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
