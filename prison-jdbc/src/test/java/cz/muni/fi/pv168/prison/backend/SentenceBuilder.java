package cz.muni.fi.pv168.prison.backend;

import java.time.LocalDate;

/**
 * Created by oldrichkonecny on 30.03.16.
 */
public class SentenceBuilder {

    private Long prisonerId;
    private Long cellId;
    private LocalDate startDay;
    private LocalDate endDay;
    private String punishment;

    public SentenceBuilder prisonerId(Long prisonerId) {
        this.prisonerId = prisonerId;
        return this;
    }

    public SentenceBuilder cellId(Long cellId) {
        this.cellId = cellId;
        return this;
    }

    public SentenceBuilder startDay(LocalDate startDay) {
        this.startDay = startDay;
        return this;
    }

    public SentenceBuilder endDay(LocalDate endDay) {
        this.endDay = endDay;
        return this;
    }

    public SentenceBuilder punishment(String punishment) {
        this.punishment = punishment;
        return this;
    }

    public Sentence build() {
        Sentence sentence = new Sentence();
        sentence.setPrisonerId(prisonerId);
        sentence.setCellId(cellId);
        sentence.setStartDay(startDay);
        sentence.setEndDay(endDay);
        sentence.setPunishment(punishment);
        return sentence;
    }


}
