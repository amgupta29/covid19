package com.domain;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "modificationDate",
        "submissionDate",
        "uuid",
        "value",
        "submitter"
})
public class OperationResponse {

    @JsonProperty("modificationDate")
    private String modificationDate;
    @JsonProperty("submissionDate")
    private String submissionDate;
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("value")
    private Value value;
    @JsonProperty("submitter")
    private Submitter submitter;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("modificationDate")
    public String getModificationDate() {
        return modificationDate;
    }

    @JsonProperty("modificationDate")
    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    @JsonProperty("submissionDate")
    public String getSubmissionDate() {
        return submissionDate;
    }

    @JsonProperty("submissionDate")
    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = submissionDate;
    }

    @JsonProperty("uuid")
    public String getUuid() {
        return uuid;
    }

    @JsonProperty("uuid")
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @JsonProperty("value")
    public Value getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(Value value) {
        this.value = value;
    }

    @JsonProperty("submitter")
    public Submitter getSubmitter() {
        return submitter;
    }

    @JsonProperty("submitter")
    public void setSubmitter(Submitter submitter) {
        this.submitter = submitter;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}