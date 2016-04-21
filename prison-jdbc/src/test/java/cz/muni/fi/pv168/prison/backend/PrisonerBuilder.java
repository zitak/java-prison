package cz.muni.fi.pv168.prison.backend;

import java.time.LocalDate;
import java.time.Month;

/**
 * This is builder for the {@link Prisoner} class to make tests better readable.
 */
public class PrisonerBuilder {

    private Long id;
    private String name;
    private String surname;
    private LocalDate born;

    public PrisonerBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public PrisonerBuilder name(String name) {
        this.name = name;
        return this;
    }

    public PrisonerBuilder surname(String surname) {
        this.surname = surname;
        return this;
    }

    public PrisonerBuilder born(LocalDate born) {
        this.born = born;
        return this;
    }

    public PrisonerBuilder born(int year, Month month, int day) {
        this.born = LocalDate.of(year, month, day);
        return this;
    }

    public Prisoner build() {
        Prisoner prisoner = new Prisoner();
        prisoner.setId(id);
        prisoner.setName(name);
        prisoner.setSurname(surname);
        prisoner.setBorn(born);
        return prisoner;
    }

}
