package com.application.dto;

// import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AppSeriesDTO {
    private String displaySeries; // "2810001 - 2810050"
    private int startNo;
    private int endNo;
    private int availableCount; // From BalanceTrack
    private int masterStartNo; // From AdminApp
    private int masterEndNo; // From AdminApp

    // --- Constructor for JPQL Query (BalanceTrackRepository) ---
    // The Repository will fill the first 4 fields.
    // The Service will fill the Master fields later.
    public AppSeriesDTO(String displaySeries, int startNo, int endNo, int availableCount) {
        this.displaySeries = displaySeries;
        this.startNo = startNo;
        this.endNo = endNo;
        this.availableCount = availableCount;
    }
}