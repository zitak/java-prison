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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link SentenceManagerImpl}.
 */
public class SentenceManagerImplTest {

    private SentenceManagerImpl manager;
    private PrisonerManagerImpl prisonerManager;
    private CellManagerImpl cellManager;
    private DataSource dataSource;



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
        DBUtils.executeSqlScript(dataSource,SentenceManager.class.getResource("createTables.sql"));
        manager = new SentenceManagerImpl(prepareClockMock(NOW));
        manager.setDataSource(dataSource);
        prisonerManager = new PrisonerManagerImpl(Clock.fixed(NOW.toInstant(), NOW.getZone()));
        prisonerManager.setDataSource(dataSource);
        cellManager = new CellManagerImpl();
        cellManager.setDataSource(dataSource);
        prepareTestData();
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(dataSource, SentenceManager.class.getResource("dropTables.sql"));
    }

    private LocalDate ld2000() {
        return LocalDate.of(2000, 1, 1);
    }
    private LocalDate ld2015() {
        return LocalDate.of(2015, 1, 1);
    }
    private LocalDate ld2030() {
        return LocalDate.of(2030, 1, 1);
    }

    private PrisonerBuilder pb() {
        return new PrisonerBuilder()
                .name("Adam")
                .surname("Zdechly")
                .born(LocalDate.of(1994, 5, 26));
    }

    private CellBuilder cb() {
        return new CellBuilder()
                .id(null)
                .floor(1)
                .capacity(1);
    }

    private SentenceBuilder sb() {
        return new SentenceBuilder();
    }

    private Sentence createCorrectSentence() {

        Prisoner prisoner = new Prisoner("Libor", "Liška", LocalDate.of(1990, 12,1));
        prisonerManager.createPrisoner(prisoner);
        Cell cell = new Cell(4,3);
        cellManager.createCell(cell);
        LocalDate startDay = LocalDate.of(1994,8,1);
        LocalDate endDay = LocalDate.of(2020,3,16);
        String punishment = "murderer";

        Sentence sentence = new Sentence();
        SentenceBuilder sb = new SentenceBuilder();
        sentence = sb
                .prisonerId(prisoner.getId())
                .cellId(cell.getId())
                .startDay(startDay)
                .endDay(endDay)
                .punishment(punishment).build();
        return sentence;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSentenceWithNull() throws Exception {
        manager.createSentence(null);
    }

    @Test
    public void testCreateSentenceWithCorrectArgument() throws ServiceFailureException {
        Sentence sentence;
        Prisoner prisoner = pb().build();
        Cell cell = cb().build();
        cellManager.createCell(cell);
        prisonerManager.createPrisoner(prisoner);
        sentence = sb().prisonerId(prisoner.getId())
                .cellId(cell.getId())
                .startDay(ld2000())
                .endDay(ld2015())
                .punishment("p").build();
        manager.createSentence(sentence);
        assertThat(manager.findAllSentences()).containsOnly(sentence);
    }

    @Test(expected = ValidationException.class)
    public void testCreateSentenceWithNullPrisoner() throws ServiceFailureException {
        Sentence sentence = this.createCorrectSentence();
        sentence.setPrisonerId(null);
        manager.createSentence(sentence);
    }

    @Test(expected = ValidationException.class)
    public void testCreateSentenceWithNullCell() throws ServiceFailureException {
        Sentence sentence = createCorrectSentence();
        sentence.setCellId(null);
        manager.createSentence(sentence);
    }

    @Test(expected = ValidationException.class)
    public void testCreateSentenceWithWrongDates() throws ServiceFailureException {
        Sentence sentence = createCorrectSentence();
        sentence.setStartDay(LocalDate.of(2000,1,1));
        sentence.setEndDay(LocalDate.of(1900,1,1));
        manager.createSentence(sentence);
    }
    /*
    @Test(expected = ValidationException.class)
    public void testCreateSentenceTooManyPrisonersInCell() throws ServiceFailureException {
        Prisoner prisoner = new Prisoner("Václav", "Novotný", LocalDate.of(1978, 1,1));
        prisonerManager.createPrisoner(prisoner);
        Cell cell = new Cell(1,1);
        cellManager.createCell(cell);

        Sentence sentence = sb().prisonerId(prisoner.getId())
                .cellId(cell.getId())
                .startDay(LocalDate.of(2000,1,1))
                .endDay(LocalDate.of(2015,1,1))
                .punishment("ucitel").build();
        manager.createSentence(sentence);

        Prisoner prisoner1 = new Prisoner("Otto", "Kuczman", LocalDate.of(1975, 1,1));
        prisonerManager.createPrisoner(prisoner1);
        Sentence sentence1 = sb().prisonerId(prisoner1.getId())
                .cellId(cell.getId())
                .startDay(LocalDate.of(2000,1,1))
                .endDay(LocalDate.of(2015,1,1))
                .punishment("nevim").build();
        manager.createSentence(sentence1);
    */

    /*
    @Test
    public void testCreateSentenceTwiceSameSentence() throws Exception {
        SentenceManager sm = new SentenceManagerImpl(prepareClockMock(NOW));

        Sentence sentence = createCorrectSentence();
        sm.createSentence(sentence);
        sm.createSentence(sentence);

        List<Sentence> list = sm.findAllSentences();

        assertTrue(list.size() == 1);

    }*/

    @Test()
    public void testFindAllSentences() throws Exception {
        //SentenceManager sm = new SentenceManagerImpl(prepareClockMock(NOW));

        Prisoner prisoner1 = new Prisoner("Harry", "Potter", LocalDate.of(1990, 1,1));
        Prisoner prisoner2 = new Prisoner("Ron", "Weasley", LocalDate.of(1990, 1,1));
        Prisoner prisoner3 = new Prisoner("Hermione", "Granger", LocalDate.of(1990,1,1));
        prisonerManager.createPrisoner(prisoner1);
        prisonerManager.createPrisoner(prisoner2);
        prisonerManager.createPrisoner(prisoner3);
        Cell cell1 = new Cell(1,2);
        Cell cell2 = new Cell(2,2);
        cellManager.createCell(cell1);
        cellManager.createCell(cell2);

        Sentence sentence1 = sb().prisonerId(prisoner1.getId())
                .cellId(cell1.getId())
                .startDay(LocalDate.of(2000,1,1))
                .endDay(LocalDate.of(2015,1,1))
                .punishment("tried to kill lord Woldemort").build();

        Sentence sentence2 = sb().prisonerId(prisoner2.getId())
                .cellId(cell1.getId())
                .startDay(LocalDate.of(2000,1,1))
                .endDay(LocalDate.of(2015,1,1))
                .punishment("support harry potter").build();

        Sentence sentence3 = sb().prisonerId(prisoner3.getId())
                .cellId(cell2.getId())
                .startDay(LocalDate.of(2000,1,1))
                .endDay(LocalDate.of(2015,1,1))
                .punishment("muggle-born").build();


        manager.createSentence(sentence1);
        manager.createSentence(sentence2);
        manager.createSentence(sentence3);

        List<Sentence> list = manager.findAllSentences();

        assertTrue(list.contains(sentence1));
        assertTrue(list.contains(sentence2));
        assertTrue(list.contains(sentence3));


    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateSentenceWithNullOldSentence() throws Exception {
        manager.updateSentence(null, createCorrectSentence());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateSentenceWithNullNewSentence() throws Exception {
        manager.updateSentence(createCorrectSentence(), null);
    }

    @Test(expected = IllegalEntityException.class)
    public void testUpdateSentenceWithNonExistentOldSentence() throws Exception {
        //SentenceManager sm = new SentenceManagerImpl(prepareClockMock(NOW));
        Sentence oldSentence = createCorrectSentence();

        Sentence newSentence = createCorrectSentence();
        newSentence.setPunishment("jejda");

        manager.updateSentence(oldSentence,newSentence);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateSentenceWithNullNewSentencePrisoner() throws Exception {
        //SentenceManager sm = new SentenceManagerImpl(prepareClockMock(NOW));
        Sentence sentence = createCorrectSentence();
        Sentence sentence1 = createCorrectSentence();
        sentence1.setPrisonerId(null);

        manager.createSentence(sentence);
        manager.updateSentence(sentence,sentence1);
    }
    /*
    @Test(expected = ValidationException.class)
    public void testUpdateSentenceWithNullNewCellId() throws Exception {
        //SentenceManager sm = new SentenceManagerImpl(prepareClockMock(NOW));
        Sentence sentence = createCorrectSentence();
        Sentence sentence1 = createCorrectSentence();
        sentence1.setCellId(null);

        manager.createSentence(sentence);
        manager.updateSentence(sentence, sentence1);
    }
    */

    @Test
    public void testDeleteSentenceWithCorrectArgument() throws Exception {
        //SentenceManager sm = new SentenceManagerImpl(prepareClockMock(NOW));

        Sentence sentence = createCorrectSentence();

        manager.createSentence(sentence);
        manager.deleteSentence(sentence);
        List<Sentence> list = manager.findAllSentences();

        assertFalse(list.contains(sentence));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteSentenceWithNullArgument() throws Exception {
        manager.deleteSentence(null);
    }

    /*
    @Test
    public void testDeleteSentenceNonExistentSentence() throws Exception {
        //SentenceManager sm = new SentenceManagerImpl(prepareClockMock(NOW));

        Sentence sentence = createCorrectSentence();
        Sentence sentence1 = createCorrectSentence();
        Prisoner prisoner = new Prisoner("Martin", "Teplíček", LocalDate.of(1994, 6,24));
        prisonerManager.createPrisoner(prisoner);
        sentence1.setPrisonerId(prisoner.getId());

        manager.createSentence(sentence);
        manager.deleteSentence(sentence1);

        List<Sentence> list = manager.findAllSentences();

        assertTrue(list.contains(sentence));

    }*/

    private Cell c1, c2, c3, cellWithNullId, cellNotInDB;
    private Prisoner p1, p2, p3, p4, p5, prisonerWithNullId, prisonerNotInDB;

    private void prepareTestData() {
        c1 = new CellBuilder().floor(1).capacity(1).build();
        c2 = new CellBuilder().floor(2).capacity(2).build();
        c3 = new CellBuilder().floor(3).capacity(3).build();

        p1 = new PrisonerBuilder().name("Adam").surname("Antonovic").born(LocalDate.of(2001, 1, 1)).build();
        p2 = new PrisonerBuilder().name("Boris").surname("Brucivy").born(LocalDate.of(2002, 2, 2)).build();
        p3 = new PrisonerBuilder().name("Cyril").surname("Cakavy").born(LocalDate.of(2003, 3, 3)).build();
        p4 = new PrisonerBuilder().name("Dominik").surname("Drum").born(LocalDate.of(2004, 4, 4)).build();
        p5 = new PrisonerBuilder().name("Evzen").surname("Elf").born(LocalDate.of(2005, 5, 5)).build();

        prisonerManager.createPrisoner(p1);
        prisonerManager.createPrisoner(p2);
        prisonerManager.createPrisoner(p3);
        prisonerManager.createPrisoner(p4);
        prisonerManager.createPrisoner(p5);

        cellManager.createCell(c1);
        cellManager.createCell(c2);
        cellManager.createCell(c3);

        cellWithNullId = new CellBuilder().id(null).build();
        cellNotInDB = new CellBuilder().id(c3.getId() + 100).build();
        assertThat(cellManager.getCellById(cellNotInDB.getId())).isNull();

        prisonerWithNullId = new PrisonerBuilder().name("PrisN").surname("Null").born(LocalDate.of(2000, 10, 10)).id(null).build();
        prisonerNotInDB = new PrisonerBuilder().name("PrisDB").surname("DB").born(LocalDate.of(2000, 10, 10)).id(p5.getId() + 100).build();
        assertThat(prisonerManager.getPrisonerById(prisonerNotInDB.getId())).isNull();
    }

    /*
    @Test
    public void findCellWithPrisoner() {

        assertThat(manager.findCellWithPrisoner(p1)).isNull();
        assertThat(manager.findCellWithPrisoner(p2)).isNull();
        assertThat(manager.findCellWithPrisoner(p3)).isNull();
        assertThat(manager.findCellWithPrisoner(p4)).isNull();
        assertThat(manager.findCellWithPrisoner(p5)).isNull();

        manager.putPrisonerIntoCell(p1, c3);

        assertThat(manager.findCellWithPrisoner(p1))
                .isEqualToComparingFieldByField(c3);
        assertThat(manager.findCellWithPrisoner(p2)).isNull();
        assertThat(manager.findCellWithPrisoner(p3)).isNull();
        assertThat(manager.findCellWithPrisoner(p4)).isNull();
        assertThat(manager.findCellWithPrisoner(p5)).isNull();
    }*/

    @Test(expected = IllegalArgumentException.class)
    public void testfindCellWithNullPrisoner() {
        manager.findCellWithPrisoner(null);
    }

    @Test(expected = ValidationException.class)
    public void findCellWithPrisonerHavingNullId() {
        manager.findCellWithPrisoner(prisonerWithNullId);
    }

    /*
    @Test
    public void findPrisonersInCell() {

        assertThat(manager.findPrisonersInCell(c1)).isEmpty();
        assertThat(manager.findPrisonersInCell(c2)).isEmpty();
        assertThat(manager.findPrisonersInCell(c3)).isEmpty();

        manager.putPrisonerIntoCell(p2, c3);
        manager.putPrisonerIntoCell(p3, c2);
        manager.putPrisonerIntoCell(p4, c3);
        manager.putPrisonerIntoCell(p5, c2);

        assertThat(manager.findPrisonersInCell(c1))
                .isEmpty();
        assertThat(manager.findPrisonersInCell(c2))
                .usingFieldByFieldElementComparator()
                .containsOnly(p3,p5);
        assertThat(manager.findPrisonersInCell(c3))
                .usingFieldByFieldElementComparator()
                .containsOnly(p2,p4);
    }*/

    @Test(expected = IllegalArgumentException.class)
    public void findPrisonersInNullCell() {
        manager.findPrisonersInCell(null);
    }

    @Test(expected = IllegalEntityException.class)
    public void findPrisonersInNullCellHavingNullId() {
        manager.findPrisonersInCell(cellWithNullId);
    }

    /*
    @Test
    public void findEmptyCells() {

        assertThat(manager.findEmptyCells())
                .usingFieldByFieldElementComparator()
                .containsOnly(c1,c2,c3);

        manager.putPrisonerIntoCell(p1, c3);
        manager.putPrisonerIntoCell(p3, c3);
        manager.putPrisonerIntoCell(p5, c1);

        assertThat(manager.findEmptyCells())
                .usingFieldByFieldElementComparator()
                .containsOnly(c2);
    }*/

    /*
    @Test
    public void findCellsWithSomeFreeSpace() {

        assertThat(manager.findCellsWithSomeFreeSpace())
                .usingFieldByFieldElementComparator()
                .containsOnly(c1,c2,c3);

        manager.putPrisonerIntoCell(p1, c3);
        manager.putPrisonerIntoCell(p3, c3);
        manager.putPrisonerIntoCell(p5, c1);

        assertThat(manager.findCellsWithSomeFreeSpace())
                .usingFieldByFieldElementComparator()
                .containsOnly(c2,c3);
    }


    @Test
    public void putPrisonerIntoCell() {

        assertThat(manager.findCellWithPrisoner(p1)).isNull();
        assertThat(manager.findCellWithPrisoner(p2)).isNull();
        assertThat(manager.findCellWithPrisoner(p3)).isNull();
        assertThat(manager.findCellWithPrisoner(p4)).isNull();
        assertThat(manager.findCellWithPrisoner(p5)).isNull();

        manager.putPrisonerIntoCell(p1, c3);
        manager.putPrisonerIntoCell(p5, c1);
        manager.putPrisonerIntoCell(p3, c3);

        assertThat(manager.findPrisonersInCell(c1))
                .usingFieldByFieldElementComparator()
                .containsOnly(p5);
        assertThat(manager.findPrisonersInCell(c2))
                .isEmpty();
        assertThat(manager.findPrisonersInCell(c3))
                .usingFieldByFieldElementComparator()
                .containsOnly(p1,p3);

        assertThat(manager.findCellWithPrisoner(p1))
                .isEqualToComparingFieldByField(c3);
        assertThat(manager.findCellWithPrisoner(p2))
                .isNull();
        assertThat(manager.findCellWithPrisoner(p3))
                .isEqualToComparingFieldByField(c3);
        assertThat(manager.findCellWithPrisoner(p4))
                .isNull();
        assertThat(manager.findCellWithPrisoner(p5))
                .isEqualToComparingFieldByField(c1);
    }*/

    /*
    @Test
    public void putPrisonerIntoCellMultipleTime() {

        manager.putPrisonerIntoCell(p1, c3);
        manager.putPrisonerIntoCell(p5, c1);
        manager.putPrisonerIntoCell(p3, c3);

        assertThatThrownBy(() -> manager.putPrisonerIntoCell(p1, c3))
                .isInstanceOf(IllegalEntityException.class);

        assertThat(manager.findPrisonersInCell(c1))
                .usingFieldByFieldElementComparator()
                .containsOnly(p5);
        assertThat(manager.findPrisonersInCell(c2))
                .isEmpty();
        assertThat(manager.findPrisonersInCell(c3))
                .usingFieldByFieldElementComparator()
                .containsOnly(p1,p3);
    }*/

    /*
    @Test
    public void putPrisonerIntoMultipleCells() {

        manager.putPrisonerIntoCell(p1, c3);
        manager.putPrisonerIntoCell(p5, c1);
        manager.putPrisonerIntoCell(p3, c3);

        assertThatThrownBy(() -> manager.putPrisonerIntoCell(p1, c2))
                .isInstanceOf(IllegalEntityException.class);

        assertThat(manager.findPrisonersInCell(c1))
                .usingFieldByFieldElementComparator()
                .containsOnly(p5);
        assertThat(manager.findPrisonersInCell(c2))
                .isEmpty();
        assertThat(manager.findPrisonersInCell(c3))
                .usingFieldByFieldElementComparator()
                .containsOnly(p1,p3);
    }*/


    /*
    @Test(expected = IllegalArgumentException.class)
    public void putNullPrisonerIntoCell() {
        manager.putPrisonerIntoCell(null, c2);
    }

    @Test(expected = IllegalEntityException.class)
    public void putPrisonerWithNullIdIntoCell() {
        manager.putPrisonerIntoCell(prisonerWithNullId, c2);
    }

    @Test(expected = IllegalEntityException.class)
    public void putPrisonerNotInDBIntoCell() {
        manager.putPrisonerIntoCell(prisonerNotInDB, c2);
    }*/

    /*
    @Test(expected = IllegalArgumentException.class)
    public void putPrisonerIntoNullCell() {
        manager.putPrisonerIntoCell(p2, null);
    }*/

    /*
    @Test(expected = IllegalEntityException.class)
    public void putPrisonerIntoCellWithNullId() {
        manager.putPrisonerIntoCell(p2, cellWithNullId);
    }*/

    /*
    @Test(expected = IllegalEntityException.class)
    public void putPrisonerIntoCellNotInDB() {
        manager.putPrisonerIntoCell(p2, cellNotInDB);
    }*/



    /*
    @Test
    public void removePrisonerFromCell() {

        manager.putPrisonerIntoCell(p1, c3);
        manager.putPrisonerIntoCell(p3, c3);
        manager.putPrisonerIntoCell(p4, c3);
        manager.putPrisonerIntoCell(p5, c1);

        assertThat(manager.findCellWithPrisoner(p1))
                .isEqualToComparingFieldByField(c3);
        assertThat(manager.findCellWithPrisoner(p2))
                .isNull();
        assertThat(manager.findCellWithPrisoner(p3))
                .isEqualToComparingFieldByField(c3);
        assertThat(manager.findCellWithPrisoner(p4))
                .isEqualToComparingFieldByField(c3);
        assertThat(manager.findCellWithPrisoner(p5))
                .isEqualToComparingFieldByField(c1);

        manager.removePrisonerFromCell(p3, c3);

        assertThat(manager.findPrisonersInCell(c1))
                .usingFieldByFieldElementComparator()
                .containsOnly(p5);
        assertThat(manager.findPrisonersInCell(c2))
                .isEmpty();
        assertThat(manager.findPrisonersInCell(c3))
                .usingFieldByFieldElementComparator()
                .containsOnly(p1,p4);


        assertThat(manager.findCellWithPrisoner(p1))
                .isEqualToComparingFieldByField(c3);
        assertThat(manager.findCellWithPrisoner(p2))
                .isNull();
        assertThat(manager.findCellWithPrisoner(p3))
                .isNull();
        assertThat(manager.findCellWithPrisoner(p4))
                .isEqualToComparingFieldByField(c3);
        assertThat(manager.findCellWithPrisoner(p5))
                .isEqualToComparingFieldByField(c1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeNullPrisonerFromCell() {
        manager.removePrisonerFromCell(null, c2);
    }

    @Test(expected = IllegalEntityException.class)
    public void removePrisonerWithNullIdFromCell() {
        manager.removePrisonerFromCell(prisonerWithNullId, c2);
    }

    @Test(expected = IllegalEntityException.class)
    public void removePrisonerNotInDBFromCell() {
        manager.removePrisonerFromCell(prisonerNotInDB, c2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removePrisonerFromNullCell() {
        manager.removePrisonerFromCell(p2, null);
    }

    @Test(expected = IllegalEntityException.class)
    public void removePrisonerFromCellWithNullId() {
        manager.removePrisonerFromCell(p2, cellWithNullId);
    }

    @Test(expected = IllegalEntityException.class)
    public void removePrisonerFromCellNotInDB() {
        manager.removePrisonerFromCell(p2, cellNotInDB);
    }

    @FunctionalInterface
    private static interface Operation<T> {
        void callOn(T subjectOfOperation);
    }



    private void testExpectedServiceFailureException(Operation<SentenceManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.callOn(manager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void findPrisonersInCellWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((manager) -> manager.findPrisonersInCell(c1));
    }

    @Test
    public void findEmptyCellsWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((manager) -> manager.findEmptyCells());
    }

    @Test
    public void findGraveWithBodyWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((manager) -> manager.findCellWithPrisoner(p1));
    }

    @Test
    public void findGravesWithSomeFreeSpaceWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((manager) -> manager.findCellsWithSomeFreeSpace());
    }

    @Test
    public void putBodyIntoGraveWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((manager) -> manager.putPrisonerIntoCell(p1, c1));
    }

    @Test
    public void removeBodyIntoGraveWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((manager) -> manager.removePrisonerFromCell(p1, c1));
    }
    */
}