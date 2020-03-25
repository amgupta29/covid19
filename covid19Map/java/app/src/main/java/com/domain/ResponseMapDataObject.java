package com.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "operationStatus",
        "operationResponse"
})
public class ResponseMapDataObject {

    @JsonProperty("operationStatus")
    private String operationStatus;
    @JsonProperty("operationResponse")
    private List<OperationResponse> operationResponse = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("operationStatus")
    public String getOperationStatus() {
        return operationStatus;
    }

    @JsonProperty("operationStatus")
    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus;
    }

    @JsonProperty("operationResponse")
    public List<OperationResponse> getOperationResponse() {
        return operationResponse;
    }

    @JsonProperty("operationResponse")
    public void setOperationResponse(List<OperationResponse> operationResponse) {
        this.operationResponse = operationResponse;
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