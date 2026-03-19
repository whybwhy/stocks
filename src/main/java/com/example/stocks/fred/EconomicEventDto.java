package com.example.stocks.fred;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Supabase economic_events 테이블 행 매핑.
 */
public class EconomicEventDto {

    private Long id;
    private String eventDate;
    private String eventName;
    private String description;
    private String source;
    private Integer fredReleaseId;
    private Boolean notifiedD1;
    private Boolean notifiedD0;
    private String createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @JsonProperty("event_date")
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    @JsonProperty("event_name")
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    @JsonProperty("fred_release_id")
    public Integer getFredReleaseId() { return fredReleaseId; }
    public void setFredReleaseId(Integer fredReleaseId) { this.fredReleaseId = fredReleaseId; }

    @JsonProperty("notified_d1")
    public Boolean getNotifiedD1() { return notifiedD1; }
    public void setNotifiedD1(Boolean notifiedD1) { this.notifiedD1 = notifiedD1; }

    @JsonProperty("notified_d0")
    public Boolean getNotifiedD0() { return notifiedD0; }
    public void setNotifiedD0(Boolean notifiedD0) { this.notifiedD0 = notifiedD0; }

    @JsonProperty("created_at")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
