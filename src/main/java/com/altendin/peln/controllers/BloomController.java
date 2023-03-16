package com.altendin.peln.controllers;

import com.altendin.peln.payloads.MessageResponse;
import com.altendin.peln.payloads.TextGenerationParams;
import com.altendin.peln.payloads.TextGenerationRequest;
import com.altendin.peln.payloads.TextGenerationResponse;
import com.altendin.peln.payloads.errors.GenericError;
import com.altendin.peln.repositories.UserRepository;
import com.altendin.peln.utils.PythonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@CrossOrigin
public class BloomController {

    @Autowired
    private PythonUtil pythonUtil;

    @Autowired
    private UserRepository userRepository;

    @Value("${bloom.pythonScriptName}")
    private String pythonScriptName;

    @Value("${bloom.pythonGlobalVariable}")
    private String pythonGlobalVariable;

    @Operation(summary = "Generate text", description = "Use bloom model to generate text", tags = "Post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Text generated",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TextGenerationResponse.class))}),
    })
    @PostMapping("/models/bigscience/bloom")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    TextGenerationResponse complete(@RequestBody TextGenerationRequest textGenerationRequest) throws IOException, InterruptedException {
        String inputs = textGenerationRequest.getInputs();
        TextGenerationParams params = textGenerationRequest.getParams();

        // we initialize the python command
        List<String> command = new ArrayList<> (
            Arrays.asList(pythonGlobalVariable, pythonUtil.resolvePythonScriptPath(pythonScriptName))
        );

        // we add params
        pythonUtil.setParams(params, command);

        command.add("-i");
        command.add(inputs);

        // omit filename to get basedir path
        command.add(pythonUtil.resolvePythonScriptPath(""));

        // Run the script
        String result = String.join(";", pythonUtil.runScript(command));

        return new TextGenerationResponse(result);
    }
}
