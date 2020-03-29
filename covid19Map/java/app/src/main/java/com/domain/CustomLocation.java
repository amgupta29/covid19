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
        "location",
        "timeSpan",
        "radius"
})
public class CustomLocation {

    @JsonProperty("location")
    private Location location;
    @JsonProperty("timeSpan")
    private Double timeSpan;
    @JsonProperty("radius")
    private Double radius;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("location")
    public Location getLocation() {
        return location;
    }

    @JsonProperty("location")
    public void setLocation(Location location) {
        this.location = location;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonProperty("timeSpan")
    public Double getTimeSpan() {
        return timeSpan;
    }

    @JsonProperty("timeSpan")
    public void setTimeSpan(Double timeSpan) {
        this.timeSpan = timeSpan;
    }

    @JsonProperty("radius")
    public Double getRadius() {
        return radius;
    }

    @JsonProperty("radius")
    public void setRadius(Double radius) {
        this.radius = radius;
    }
}
