package com.altendin.peln.payloads;

public class TextGenerationResponse {
    String generated_text;

    public TextGenerationResponse(String generated_text) {
        this.generated_text = generated_text;
    }

    public String getGenerated_text() {
        return generated_text;
    }
    public void setGenerated_text(String generated_text) {
        this.generated_text = generated_text;
    }
}
