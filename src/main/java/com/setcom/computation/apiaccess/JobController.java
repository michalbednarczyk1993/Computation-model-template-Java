package com.setcom.computation.apiaccess;

import com.setcom.computation.balticlsc.DataHandler;
import com.setcom.computation.balticlsc.JobRegistry;
import com.setcom.computation.balticlsc.JobThread;
import com.setcom.computation.balticlsc.TokenListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;


[ApiController]
            [Route("/")]
@Slf4j
public class JobController extends ControllerBase {
    private final JobRegistry registry;
    private final DataHandler handler;
    private final TokenListener listener;

    public JobController(JobRegistry registry, DataHandler handler, TokenListener listener) {
        this.registry = registry;
        this.handler = handler;
        this.listener = listener;
    }

        [HttpPost]
            [Route("token")]
    public IActionResult ProcessTokenMessage([FromBody] Object value) {
        try {
            log.info("Token message received: " + value);
            var inputToken = JsonConvert.DeserializeObject<InputTokenMessage>(value.ToString() ?? "");
            registry.RegisterToken(inputToken);
            try {
                String retMessage;
                short result = handler.CheckConnection(inputToken.PinName,
                        JsonConvert.DeserializeObject<Dictionary<String, String>>(inputToken.Values));
                switch (result) {
                    case 0:
                        JobThread jobThread = new JobThread(inputToken.PinName, listener, registry, handler);
                        registry.RegisterThread(jobThread);
                        var task = new Task(() => jobThread.Run());
                        task.Start();
                        return Ok();
                    case -1:
                        retMessage = "No response " + inputToken.PinName.toString();
                        log.error(retMessage);
                        return NotFound(retMessage);
                    case -2:
                        retMessage = String.format("Unauthorized ,%s", inputToken.PinName);
                        log.error(retMessage);
                        return Unauthorized(retMessage);
                    case -3:
                        retMessage = String.format("Invalid path %s",inputToken.PinName);
                        log.error(retMessage);
                        return Unauthorized(retMessage);
                }

                return BadRequest();
            }
            catch (Exception e)
            {
                log.error(String.format(
                        "Error of type %s: %s\n %s",e.getClass(), e.getMessage(), Arrays.toString(e.getStackTrace())));
                return Ok(e);
            }
        }
        catch (Exception e)
        {
            log.error("Corrupted token: " + e.getMessage());
            return BadRequest(e);
        }
    }

        [HttpGet]
            [Route("/status")]
    public IActionResult GetStatus()
    {
        return Ok(_registry.GetJobStatus());
    }
}
