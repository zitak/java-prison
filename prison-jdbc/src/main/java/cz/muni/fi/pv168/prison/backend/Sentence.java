package cz.muni.fi.pv168.prison.backend;

import java.time.LocalDate;

/**
 * This entity represents sentence. Sentence holds information about prisoner and cell, where prisoner is.
 * Also contains start day and end day. May hold some information about punishment.
 */
public class Sentence {

    private Long prisonerId;
    private Long cellId;
    private LocalDate startDay;
    private LocalDate endDay;
    private String punishment;

    public Sentence(Long prisonerId, Long cellId, LocalDate startDay, LocalDate endDay, String punishment) {
        this.prisonerId = prisonerId;
        this.cellId = cellId;
        this.startDay = startDay;
        this.endDay = endDay;
        this.punishment = punishment;
    }

    public Sentence() { }

    public Long getPrisonerId() { return prisonerId; }
    public void setPrisonerId(Long prisonerId) { this.prisonerId = prisonerId; }

    public Long getCellId() { return cellId; }
    public void setCellId(Long cellId) { this.cellId = cellId; }

    public LocalDate getStartDay() { return startDay; }
    public void setStartDay(LocalDate startDay) { this.startDay = startDay; }

    public LocalDate getEndDay() { return endDay; }
    public void setEndDay(LocalDate endDay) { this.endDay = endDay; }

    public String getPunishment() { return punishment; }
    public void setPunishment(String punishment) { this.punishment = punishment; }

    @Override
    public String toString() {
        return "Sentence{" +
                "prisonerId=" + prisonerId +
                ", cellId=" + cellId +
                ", startDay=" + startDay +
                ", endDay=" + endDay +
                ", punishment='" + punishment + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sentence sentence = (Sentence) o;

        if (!prisonerId.equals(sentence.prisonerId)) return false;
        if (!cellId.equals(sentence.cellId)) return false;
        if (!startDay.equals(sentence.startDay)) return false;
        if (!endDay.equals(sentence.endDay)) return false;
        return punishment != null ? punishment.equals(sentence.punishment) : sentence.punishment == null;
    }

    @Override
    public int hashCode() {
        int result = prisonerId.hashCode();
        result = 31 * result + cellId.hashCode();
        result = 31 * result + startDay.hashCode();
        result = 31 * result + endDay.hashCode();
        result = 31 * result + (punishment != null ? punishment.hashCode() : 0);
        return result;
    }
}
