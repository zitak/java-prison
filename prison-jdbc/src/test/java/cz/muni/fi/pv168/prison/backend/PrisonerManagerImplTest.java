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

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PrisonerManagerImpl} class.
 */
public class PrisonerManagerImplTest {

    private PrisonerManagerImpl manager;
    private DataSource dataSource;

    /*private final static LocalDate NOW
            = LocalDate.of(2016, 4, 1);*/
    private final static ZonedDateTime NOW
            = LocalDateTime.of(2016, 4, 1, 12, 00).atZone(ZoneId.of("UTC"));

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("memory:prisonmgr-test");
        dataSource.setCreateDatabase("create");
        return dataSource;
    }

    private static Clock prepareClockMock(ZonedDateTime now) {
        return Clock.fixed(now.toInstant(), now.getZone());
    }

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        //System.out.print(CellManager.class.getResource("createTables.sql"));
        DBUtils.executeSqlScript(dataSource,CellManager.class.getResource("createTables.sql"));
        manager = new PrisonerManagerImpl(prepareClockMock(NOW));
        manager.setDataSource(dataSource);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(dataSource, CellManager.class.getResource("dropTables.sql"));
    }

    private PrisonerBuilder sampleAdamBodyBuilder() {
        return new PrisonerBuilder()
                .name("Adam")
                .surname("Zdechly")
                .born(LocalDate.of(1994, 5, 26));
    }

    private PrisonerBuilder sampleBorisBodyBuilder() {
        return new PrisonerBuilder()
                .name("Boris")
                .surname("Ceckovy")
                .born(LocalDate.of(1892, 1, 1));
    }

    @Test
    public void createPrisoner() {
        Prisoner prisoner = sampleAdamBodyBuilder().build();
        manager.createPrisoner(prisoner);

        Long prisonerId = prisoner.getId();
        assertThat(prisonerId).isNotNull();

        assertThat(manager.getPrisonerById(prisonerId))
            .isNotSameAs(prisoner)
            .isEqualToComparingFieldByField(prisoner);
    }

    @Test
    public void findAllPrisoners() {
        assertThat(manager.findAllPrisoners().isEmpty());

        Prisoner adam = sampleAdamBodyBuilder().build();
        Prisoner boris = sampleBorisBodyBuilder().build();

        manager.createPrisoner(adam);
        manager.createPrisoner(boris);

        assertThat(manager.findAllPrisoners())
            .usingFieldByFieldElementComparator()
            .containsOnly(adam, boris);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNullPrisoner() {
        manager.createPrisoner(null);
    }

    @Test
    public void createPrisonerWithExistingId() {
        Prisoner prisoner = sampleAdamBodyBuilder()
                .id(1L)
                .build();
        expectedException.expect(IllegalArgumentException.class);
        manager.createPrisoner(prisoner);
    }

    @Test
    public void createPrisonerWithNullName() {
        Prisoner prisoner = sampleAdamBodyBuilder()
                .name(null)
                .build();
        assertThatThrownBy(() -> manager.createPrisoner(prisoner))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createPrisonerWithNullSurname() {
        Prisoner prisoner = sampleAdamBodyBuilder()
                .surname(null)
                .build();
        assertThatThrownBy(() -> manager.createPrisoner(prisoner))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createPrisonerWithNullBorn() {
        Prisoner prisoner = sampleAdamBodyBuilder()
                .born(null)
                .build();
        manager.createPrisoner(prisoner);
        assertThat(manager.getPrisonerById(prisoner.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(prisoner);
    }

    @Test
    public void createBodyWithBornTomorrow() {
        LocalDate tomorrow = NOW.toLocalDate().plusDays(1);
        Prisoner prisoner = sampleAdamBodyBuilder()
                .born(tomorrow)
                .build();
        assertThatThrownBy(() -> manager.createPrisoner(prisoner))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createBodyWithBornToday() {
        LocalDate today = NOW.toLocalDate();
        Prisoner prisoner = sampleAdamBodyBuilder()
                .born(today)
                .build();
        manager.createPrisoner(prisoner);

        assertThat(manager.getPrisonerById(prisoner.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(prisoner);
    }

    @Test
    public void updatePrisonerName() {
        Prisoner prisonerForUpdate = sampleAdamBodyBuilder().build();
        Prisoner anotherPrisoner = sampleBorisBodyBuilder().build();
        manager.createPrisoner(prisonerForUpdate);
        manager.createPrisoner(anotherPrisoner);

        prisonerForUpdate.setName("New");

        manager.updatePrisoner(prisonerForUpdate);

        assertThat(manager.getPrisonerById(prisonerForUpdate.getId()))
                .isEqualToComparingFieldByField(prisonerForUpdate);
        assertThat(manager.getPrisonerById(anotherPrisoner.getId()))
                .isEqualToComparingFieldByField(anotherPrisoner);
    }

    @FunctionalInterface
    private static interface Operation<T> {
        void callOn(T subjectOfOperation);
    }

    private void testUpdatePrisoner(Operation<Prisoner> updateOperation) {
        Prisoner prisonerForUpdate = sampleAdamBodyBuilder().build();
        Prisoner anotherPrisoner = sampleBorisBodyBuilder().build();
        manager.createPrisoner(prisonerForUpdate);
        manager.createPrisoner(anotherPrisoner);

        updateOperation.callOn(prisonerForUpdate);

        manager.updatePrisoner(prisonerForUpdate);
        assertThat(manager.getPrisonerById(prisonerForUpdate.getId()))
                .isEqualToComparingFieldByField(prisonerForUpdate);
        assertThat(manager.getPrisonerById(anotherPrisoner.getId()))
                .isEqualToComparingFieldByField(anotherPrisoner);
    }

    @Test
    public void updateSurname() {
        testUpdatePrisoner((prisoner) -> prisoner.setSurname("Hadavy"));
    }

    @Test
    public void updatePrisonerBorn() {
        testUpdatePrisoner((prisoner) -> prisoner.setBorn(LocalDate.of(1999,9,9)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateNullPrisoner() {
        manager.updatePrisoner(null);
    }

    @Test
    public void updatePrisonerWithNullId() {
        Prisoner prisoner = sampleAdamBodyBuilder().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        manager.updatePrisoner(prisoner);
    }

    @Test
    public void updateNonExistingPrisoner() {
        Prisoner prisoner = sampleAdamBodyBuilder().build();
        manager.createPrisoner(prisoner);
        prisoner.setId(2L);

        expectedException.expect(IllegalEntityException.class);
        manager.updatePrisoner(prisoner);
    }

    @Test
    public void updateBodyWithNullName() {
        Prisoner prisoner = sampleAdamBodyBuilder().build();
        manager.createPrisoner(prisoner);
        prisoner.setName(null);

        expectedException.expect(ValidationException.class);
        manager.updatePrisoner(prisoner);
    }

    @Test
    public void updatePrisonerWithNullSurname() {
        Prisoner prisoner = sampleAdamBodyBuilder().build();
        manager.createPrisoner(prisoner);
        prisoner.setSurname(null);

        expectedException.expect(ValidationException.class);
        manager.updatePrisoner(prisoner);
    }

    @Test
    public  void deletePrisoner() {
        Prisoner adam = sampleAdamBodyBuilder().build();
        Prisoner boris = sampleBorisBodyBuilder().build();

        manager.createPrisoner(adam);
        manager.createPrisoner(boris);

        assertThat(manager.getPrisonerById(adam.getId())).isNotNull();
        assertThat(manager.getPrisonerById(boris.getId())).isNotNull();

        manager.deletePrisoner(adam);

        assertThat(manager.getPrisonerById(adam.getId())).isNull();
        assertThat(manager.getPrisonerById(boris.getId())).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullPrisoner() {
        manager.deletePrisoner(null);
    }

    @Test
    public void deletePrisonerWithNullId() {
        Prisoner prisoner = sampleAdamBodyBuilder().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        manager.deletePrisoner(prisoner);
    }

    @Test
    public void deleteNonExistingPrisoner() {
        Prisoner prisoner = sampleAdamBodyBuilder().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        manager.deletePrisoner(prisoner);
    }

    @Test
    public void createPrisonerWithSqlExceptionThrown() throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);

        Prisoner prisoner = sampleAdamBodyBuilder().build();

        assertThatThrownBy(() -> manager.createPrisoner(prisoner))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    private void testExpectedServiceFailureException(Operation<PrisonerManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.callOn(manager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void updatePrisonerWithSqlExceptionThrown() throws SQLException {
        Prisoner prisoner = sampleAdamBodyBuilder().build();
        manager.createPrisoner(prisoner);
        testExpectedServiceFailureException((prisonerManager) -> prisonerManager.updatePrisoner(prisoner));
    }

    @Test
    public void getPrisonerWithSqlExceptionThrown() throws SQLException {
        Prisoner prisoner = sampleAdamBodyBuilder().build();
        manager.createPrisoner(prisoner);
        testExpectedServiceFailureException((prisonerManager) -> prisonerManager.getPrisonerById(prisoner.getId()));
    }

    @Test
    public void deletePrisonerWithSqlExceptionThrown() throws SQLException {
        Prisoner prisoner = sampleAdamBodyBuilder().build();
        manager.createPrisoner(prisoner);
        testExpectedServiceFailureException((prisonerManager) -> prisonerManager.deletePrisoner(prisoner));
    }

    @Test
    public void findAllBodiesWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((prisonerManager) -> prisonerManager.findAllPrisoners());
    }

    @Test
    public void testGetPrisonerBySurname() {
        Prisoner prisoner = sampleAdamBodyBuilder().build();
        manager.createPrisoner(prisoner);

        List<Prisoner> prisonerList = manager.getPrisonerBySurname(prisoner.getSurname());
        assertThat(manager.getPrisonerBySurname(prisoner.getSurname())).containsOnly(prisoner);
    }

}
