package cz.muni.fi.pv168.prison.backend;

/**
 * This entity represents cell. Cell has its id, capacity and number of floor.
 * One cell would contain zero or more prisoners up to its capacity.
 */
public class Cell {

    private Long id;
    private int floor;
    private int capacity;

    public Cell() { }

    public Cell(int floor, int capacity) {
        this.floor = floor;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    @Override
    public String toString() {
        return "Cell{" +
                "id=" + id +
                ", floor=" + floor +
                ", capacity=" + capacity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cell cell = (Cell) o;

        if (floor != cell.floor) return false;
        if (capacity != cell.capacity) return false;
        return id.equals(cell.id);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + floor;
        result = 31 * result + capacity;
        return result;
    }
}
