package com.altendin.peln.utils;

import com.altendin.peln.payloads.TextGenerationParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class PythonUtil {

    private static final Logger logger = LoggerFactory.getLogger(PythonUtil.class);

    @Value("${bloom.relativePath}")
    private String bloomRelativePath;

    public List<String> runScript(List<String> command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        List<String> results = new ArrayList<>();

        try {
            Process process = processBuilder.start();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("[" + command.get(1) + "] - " + line);
                results.add(line);
            }

        int processCode = process.waitFor();
        logger.info("Process finished with exit code : " + processCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return results;
    }

    public String resolvePythonScriptPath(String fileName) throws IOException {
        File scriptFile = new File(bloomRelativePath + fileName);
        return scriptFile.getCanonicalPath();
    }

    public void setParams(TextGenerationParams params, List<String> command) {
        Boolean doSample = params.getDo_sample();
        Boolean earlyStopping = params.getEarly_stopping();
        Integer lengthPenalty = params.getLength_penalty();
        Integer maxNewTokens = params.getMax_new_tokens();
        Integer seed = params.getSeed();
        String top_p = params.getTop_p();

        if(doSample != null) {
            if(doSample) {
                command.add("-ds");
            }
        }

        if(earlyStopping != null) {
            if(earlyStopping) {
                command.add("-es");
            }
        }

        if(lengthPenalty != null) {
            command.add("-lp");
            command.add(lengthPenalty.toString());
        }

        if(maxNewTokens != null) {
            command.add("-mnt");
            command.add(maxNewTokens.toString());
        }

        if(seed != null) {
            command.add("-s");
            command.add(seed.toString());
        }

        if(top_p != null) {
            command.add("-tp");
            command.add(top_p);
        }
    }
}
