package cz.muni.fi.pv168.prison.backend;

import java.time.LocalDate;

/**
 * This entity represents prisoner. Prisoner has id, name, surname and date of birth.
 */
public class Prisoner {

    private Long id;
    private String name;
    private String surname;
    private LocalDate born;

    public Prisoner() { }

    public Prisoner(String name, String surname, LocalDate born) {
        this.name = name;
        this.surname = surname;
        this.born = born;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public LocalDate getBorn() { return born; }
    public void setBorn(LocalDate born) { this.born = born; }

    @Override
    public String toString() {
        return "Prisoner{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", born=" + born +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Prisoner prisoner = (Prisoner) o;

        if (!id.equals(prisoner.id)) return false;
        if (name != null ? !name.equals(prisoner.name) : prisoner.name != null) return false;
        if (surname != null ? !surname.equals(prisoner.surname) : prisoner.surname != null) return false;
        return born != null ? born.equals(prisoner.born) : prisoner.born == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (born != null ? born.hashCode() : 0);
        return result;
    }
}
