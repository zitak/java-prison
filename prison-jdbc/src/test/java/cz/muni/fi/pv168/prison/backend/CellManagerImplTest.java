package cz.muni.fi.pv168.prison.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.common.ValidationException;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.sql.SQLException;
import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Testing cell manager impl.
 */
public class CellManagerImplTest {

    private CellManagerImpl manager;
    private DataSource dataSource;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("memory:prisonmgr-test");
        dataSource.setCreateDatabase("create");
        return dataSource;
    }

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        DBUtils.executeSqlScript(dataSource,CellManager.class.getResource("createTables.sql"));
        manager = new CellManagerImpl();
        manager.setDataSource(dataSource);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(dataSource,CellManager.class.getResource("dropTables.sql"));
    }


    private CellBuilder sampleSmallCellBuilder() {
        return new CellBuilder()
                .id(null)
                .floor(1)
                .capacity(1);
    }

    private CellBuilder sampleBigCellBuilder() {
        return new CellBuilder()
                .id(null)
                .floor(3)
                .capacity(4);
    }



    @Test
    public void testCreateCell() {
        Cell cell = sampleSmallCellBuilder().build();
        manager.createCell(cell);

        Long cellId = cell.getId();
        assertThat(cellId).isNotNull();

        assertThat(manager.getCellById(cellId))
                .isNotSameAs(cell)
                .isEqualToComparingFieldByField(cell);
    }

    @Test
    public void testFindAllCells() {

        assertThat(manager.findAllCells()).isEmpty();

        Cell c1 = sampleSmallCellBuilder().build();
        Cell c2 = sampleBigCellBuilder().build();

        manager.createCell(c1);
        manager.createCell(c2);

        assertThat(manager.findAllCells())
                .usingFieldByFieldElementComparator()
                .containsOnly(c1,c2);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNull() {
        manager.createCell(null);
    }

    @Test
    public void testCreateCellWithExistingId() {
        Cell cell = sampleSmallCellBuilder().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        manager.createCell(cell);
    }

    @Test
    public void testCreateCellWithNegativeCapacity() {
        Cell cell = sampleSmallCellBuilder().capacity(-1).build();
        expectedException.expect(ValidationException.class);
        manager.createCell(cell);
    }

    @Test
    public void testCreateCellWithZeroCapacity() {
        Cell cell = sampleSmallCellBuilder().capacity(0).build();
        expectedException.expect(ValidationException.class);
        manager.createCell(cell);
    }

    @Test
    public void testUpdateCellManagerFloor() {
        Cell cellForUpdate = sampleSmallCellBuilder().build();
        Cell anotherCell = sampleBigCellBuilder().build();
        manager.createCell(cellForUpdate);
        manager.createCell(anotherCell);

        cellForUpdate.setFloor(10);

        manager.updateCell(cellForUpdate);
        assertThat(manager.getCellById(cellForUpdate.getId()))
                .isEqualToComparingFieldByField(cellForUpdate);
        assertThat(manager.getCellById(anotherCell.getId()))
                .isEqualToComparingFieldByField(anotherCell);
    }

    @Test
    public void testUpdateCellManagerCapacity() {
        Cell cellForUpdate = sampleSmallCellBuilder().build();
        Cell anotherCell = sampleBigCellBuilder().build();
        manager.createCell(cellForUpdate);
        manager.createCell(anotherCell);

        cellForUpdate.setCapacity(10);

        manager.updateCell(cellForUpdate);
        assertThat(manager.getCellById(cellForUpdate.getId()))
                .isEqualToComparingFieldByField(cellForUpdate);
        assertThat(manager.getCellById(anotherCell.getId()))
                .isEqualToComparingFieldByField(anotherCell);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateNullCell() {
        manager.updateCell(null);
    }

    @Test
    public void testUpdateCellWithNullId() {
        Cell cell = sampleBigCellBuilder().id(null).build();
        expectedException.expect(ValidationException.class);
        manager.updateCell(cell);
    }

    @Test
    public void testUpdateCellWithZeroCapacity() {
        Cell cell = sampleBigCellBuilder().capacity(0).build();
        expectedException.expect(ValidationException.class);
        manager.updateCell(cell);
    }

    @Test
    public void testUpdateCellWithNegativeCapacity() {
        Cell cell = sampleBigCellBuilder().capacity(-1).build();
        expectedException.expect(ValidationException.class);
        manager.updateCell(cell);
    }

    @Test
    public void testDeleteCell() {

        Cell c1 = sampleSmallCellBuilder().build();
        Cell c2 = sampleBigCellBuilder().build();
        manager.createCell(c1);
        manager.createCell(c2);

        assertThat(manager.getCellById(c1.getId())).isNotNull();
        assertThat(manager.getCellById(c2.getId())).isNotNull();

        manager.deleteCell(c1);

        assertThat(manager.getCellById(c1.getId())).isNull();
        assertThat(manager.getCellById(c2.getId())).isNotNull();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteNullCell() {
        manager.deleteCell(null);
    }

    @Test
    public void testDeleteCellWithNullId() {
        Cell cell = sampleBigCellBuilder().id(null).build();
        expectedException.expect(ValidationException.class);
        manager.updateCell(cell);
    }

    @Test
    public void testDeleteCellWithNonExistingId() {
        Cell cell = sampleBigCellBuilder().build();
        manager.createCell(cell);
        cell.setId(2L);

        expectedException.expect(IllegalEntityException.class);
        manager.deleteCell(cell);
    }





    @Test
    public void getAllCells() {
        assertTrue(manager.findAllCells().isEmpty());

        Cell c1 = newCell(2, 2);
        Cell c2 = newCell(3, 3);

        manager.createCell(c1);
        manager.createCell(c2);

        List<Cell> expected = Arrays.asList(c1, c2);
        List<Cell> actual = manager.findAllCells();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals("saved and retrieved cells differ", expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test

    public void updateCell() {
        Cell cell = newCell(2, 2);
        Cell c2 = newCell(3, 3);
        manager.createCell(cell);
        manager.createCell(c2);
        Long cellId = cell.getId();

        cell = manager.getCellById(cellId);
        cell.setFloor(4);
        manager.updateCell(cell);
        assertThat("section was not changed", cell.getFloor(), is(equalTo(4)));
        assertThat("capacity was changed", cell.getCapacity(), is(equalTo(2)));

        cell = manager.getCellById(cellId);
        cell.setCapacity(5);
        manager.updateCell(cell);

        assertThat("capacity was not changed", cell.getCapacity(), is(equalTo(5)));
        assertThat("section was changed", cell.getFloor(), is(equalTo(4)));

        assertDeepEquals(c2, manager.getCellById(c2.getId()));
    }


    @Test
    public void deleteCell() {
        Cell c1 = newCell(2, 2);
        Cell c2 = newCell(3, 3);
        manager.createCell(c1);
        manager.createCell(c2);

        assertNotNull(manager.getCellById(c1.getId()));
        assertNotNull(manager.getCellById(c2.getId()));

        manager.deleteCell(c1);

        assertNull(manager.getCellById(c1.getId()));
        assertNotNull(manager.getCellById(c2.getId()));
    }


    private static Cell newCell(int floor, int capacity) {
        Cell cell = new Cell();
        cell.setFloor(floor);
        cell.setCapacity(capacity);
        return cell;
    }

    private void assertDeepEquals(List<Cell> expectedList, List<Cell> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Cell expected = expectedList.get(i);
            Cell actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(Cell expected, Cell actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFloor(), actual.getFloor());
        assertEquals(expected.getCapacity(), actual.getCapacity());
    }

    private static Comparator<Cell> idComparator = new Comparator<Cell>() {
        public int compare(Cell o1, Cell o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };

}
