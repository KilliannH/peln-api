package com.altendin.peln.payloads;

import java.io.Serializable;

public class TextGenerationRequest implements Serializable {
    // No need to implement Serializable for child classes, io TextGenerationParams
    String inputs;
    TextGenerationParams params;

    TextGenerationRequest() {
        // Empty constructor for Serializable
    }
    TextGenerationRequest(TextGenerationParams params, String inputs) {}

    public String getInputs() {
        return inputs;
    }

    public void setInputs(String inputs) {
        this.inputs = inputs;
    }

    public TextGenerationParams getParams() {
        return params;
    }

    public void setParams(TextGenerationParams params) {
        this.params = params;
    }
}
