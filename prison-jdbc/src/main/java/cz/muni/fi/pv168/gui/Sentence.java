package cz.muni.fi.pv168.gui;

import java.time.LocalDate;
import java.util.Objects;

/**
 *
 * @author Zita
 */
public class Sentence {
    private int prisonerId;
    private int cellId;

    public int getPrisonerId() {
        return prisonerId;
    }

    public Sentence(int prisonerId, int cellId, LocalDate startDay, LocalDate endDay, String punishment) {
        this.prisonerId = prisonerId;
        this.cellId = cellId;
        this.startDay = startDay;
        this.endDay = endDay;
        this.punishment = punishment;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.prisonerId);
        hash = 53 * hash + Objects.hashCode(this.cellId);
        hash = 53 * hash + Objects.hashCode(this.startDay);
        hash = 53 * hash + Objects.hashCode(this.endDay);
        hash = 53 * hash + Objects.hashCode(this.punishment);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Sentence other = (Sentence) obj;
        if (!Objects.equals(this.punishment, other.punishment)) {
            return false;
        }
        if (!Objects.equals(this.prisonerId, other.prisonerId)) {
            return false;
        }
        if (!Objects.equals(this.cellId, other.cellId)) {
            return false;
        }
        if (!Objects.equals(this.startDay, other.startDay)) {
            return false;
        }
        if (!Objects.equals(this.endDay, other.endDay)) {
            return false;
        }
        return true;
    }

    public void setPrisonerId(int prisonerId) {
        this.prisonerId = prisonerId;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public LocalDate getStartDay() {
        return startDay;
    }

    public void setStartDay(LocalDate startDay) {
        this.startDay = startDay;
    }

    public LocalDate getEndDay() {
        return endDay;
    }

    public void setEndDay(LocalDate endDay) {
        this.endDay = endDay;
    }

    public String getPunishment() {
        return punishment;
    }

    public void setPunishment(String punishment) {
        this.punishment = punishment;
    }
    private LocalDate startDay;
    private LocalDate endDay;
    private String punishment;
}
