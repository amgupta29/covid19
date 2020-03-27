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
        "symptom-cough",
        "longitude",
        "latitude",
        "symptom-headache",
        "symptom-fever",
        "diagnosis-influenze",
        "timestamp",
        "diagnosis-Covid-19-Positive",
        "diagnosis-Flu-Symptoms",
        "symptom-Feeling-breathless",
        "symptom-Persistent-pain-or-pressure-in-chest",
        "symptom-Severe-weakness"
})
public class Value {

    @JsonProperty("symptom-cough")
    private Boolean symptomCough;
    @JsonProperty("longitude")
    private Double longitude;
    @JsonProperty("latitude")
    private Double latitude;
    @JsonProperty("symptom-headache")
    private Boolean symptomHeadache;
    @JsonProperty("symptom-fever")
    private Boolean symptomFever;
    @JsonProperty("diagnosis-influenze")
    private Boolean diagnosisInfluenze;
    @JsonProperty("timestamp")
    private Long timestamp;
    @JsonProperty("diagnosis-Covid19-Positive")
    private Boolean diagnosisCovid19;
    @JsonProperty("diagnosis-Flu-Symptoms")
    private Boolean diagnosisFluSymptoms;
    @JsonProperty("symptom-Feeling-breathless")
    private Boolean symptomFeelingBreathless;
    @JsonProperty("symptom-Persistent-pain-or-pressure-in-chest")
    private Boolean symptomPersistentPainOrPressureInChest;
    @JsonProperty("symptom-Severe-weakness")
    private Boolean symptomSevereWeakness;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("symptom-cough")
    public Boolean getSymptomCough() {
        return symptomCough;
    }

    @JsonProperty("symptom-cough")
    public void setSymptomCough(Boolean symptomCough) {
        this.symptomCough = symptomCough;
    }

    @JsonProperty("longitude")
    public Double getLongitude() {
        return longitude;
    }

    @JsonProperty("longitude")
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @JsonProperty("latitude")
    public Double getLatitude() {
        return latitude;
    }

    @JsonProperty("latitude")
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @JsonProperty("symptom-headache")
    public Boolean getSymptomHeadache() {
        return symptomHeadache;
    }

    @JsonProperty("symptom-headache")
    public void setSymptomHeadache(Boolean symptomHeadache) {
        this.symptomHeadache = symptomHeadache;
    }

    @JsonProperty("symptom-fever")
    public Boolean getSymptomFever() {
        return symptomFever;
    }

    @JsonProperty("symptom-fever")
    public void setSymptomFever(Boolean symptomFever) {
        this.symptomFever = symptomFever;
    }

    @JsonProperty("diagnosis-influenze")
    public Boolean getDiagnosisInfluenze() {
        return diagnosisInfluenze;
    }

    @JsonProperty("diagnosis-influenze")
    public void setDiagnosisInfluenze(Boolean diagnosisInfluenze) {
        this.diagnosisInfluenze = diagnosisInfluenze;
    }

    @JsonProperty("timestamp")
    public Long getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("diagnosis-Covid19-Positive")
    public Boolean getDiagnosisCovid19() {
        return diagnosisCovid19;
    }

    @JsonProperty("diagnosis-Covid19-Positive")
    public void setDiagnosisCovid19(Boolean diagnosisCovid19) {
        this.diagnosisCovid19 = diagnosisCovid19;
    }

    @JsonProperty("diagnosis-Flu-Symptoms")
    public Boolean getDiagnosisFluSymptoms() {
        return diagnosisFluSymptoms;
    }

    @JsonProperty("diagnosis-Flu-Symptoms")
    public void setDiagnosisFluSymptoms(Boolean diagnosisFluSymptoms) {
        this.diagnosisFluSymptoms = diagnosisFluSymptoms;
    }

    @JsonProperty("symptom-Feeling-breathless")
    public Boolean getSymptomFeelingBreathless() {
        return symptomFeelingBreathless;
    }

    @JsonProperty("symptom-Feeling-breathless")
    public void setSymptomFeelingBreathless(Boolean symptomFeelingBreathless) {
        this.symptomFeelingBreathless = symptomFeelingBreathless;
    }

    @JsonProperty("symptom-Persistent-pain-or-pressure-in-chest")
    public Boolean getSymptomPersistentPainOrPressureInChest() {
        return symptomPersistentPainOrPressureInChest;
    }

    @JsonProperty("symptom-Persistent-pain-or-pressure-in-chest")
    public void setSymptomPersistentPainOrPressureInChest(Boolean symptomPersistentPainOrPressureInChest) {
        this.symptomPersistentPainOrPressureInChest = symptomPersistentPainOrPressureInChest;
    }

    @JsonProperty("symptom-Severe-weakness")
    public Boolean getSymptomSevereWeakness() {
        return symptomSevereWeakness;
    }

    @JsonProperty("symptom-Severe-weakness")
    public void setSymptomSevereWeakness(Boolean symptomSevereWeakness) {
        this.symptomSevereWeakness = symptomSevereWeakness;
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