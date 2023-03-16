package com.altendin.peln.payloads;

public class TextGenerationParams {
    /* example of HuggingFace request params :
    do_sample:true
    early_stopping:false
    length_penalty:0
    max_new_tokens:20
    seed:13
    top_p:0.9 */

    private Boolean do_sample;
    private Boolean early_stopping;
    private Integer length_penalty;
    private Integer max_new_tokens;
    private Integer seed;
    private String top_p;

    public TextGenerationParams() {}

    public Boolean getDo_sample() {
        return do_sample;
    }

    public void setDo_sample(Boolean do_sample) {
        this.do_sample = do_sample;
    }

    public Boolean getEarly_stopping() {
        return early_stopping;
    }

    public void setEarly_stopping(Boolean early_stopping) {
        this.early_stopping = early_stopping;
    }

    public Integer getLength_penalty() {
        return length_penalty;
    }

    public void setLength_penalty(Integer length_penalty) {
        this.length_penalty = length_penalty;
    }

    public Integer getMax_new_tokens() {
        return max_new_tokens;
    }

    public void setMax_new_tokens(Integer max_new_tokens) {
        this.max_new_tokens = max_new_tokens;
    }

    public Integer getSeed() {
        return seed;
    }

    public void setSeed(Integer seed) {
        this.seed = seed;
    }

    public String getTop_p() {
        return top_p;
    }

    public void setTop_p(String top_p) {
        this.top_p = top_p;
    }
}
