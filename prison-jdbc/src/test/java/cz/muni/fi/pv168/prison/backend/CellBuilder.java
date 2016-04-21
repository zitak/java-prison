package cz.muni.fi.pv168.prison.backend;

/**
 * Created by oldrichkonecny on 30.03.16.
 */
public class CellBuilder {

    private Long id;
    private int floor;
    private int capacity;

    public CellBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public CellBuilder floor(int floor) {
        this.floor = floor;
        return this;
    }

    public CellBuilder capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public Cell build() {
        Cell cell = new Cell();
        cell.setId(id);
        cell.setCapacity(capacity);
        cell.setFloor(floor);
        return cell;
    }

}
