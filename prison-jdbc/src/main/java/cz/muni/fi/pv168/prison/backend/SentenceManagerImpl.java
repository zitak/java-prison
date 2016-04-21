package cz.muni.fi.pv168.prison.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.common.ValidationException;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements sentence manager.
 */
public class SentenceManagerImpl implements SentenceManager {

    private static final Logger logger = Logger.getLogger(
            CellManagerImpl.class.getName());
    private DataSource dataSource;
    private Clock clock;

    public SentenceManagerImpl(Clock clock) {
        this.clock = clock;
    }

    public SentenceManagerImpl(DataSource ds) {this.dataSource = ds; }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }



    @Override
    public void createSentence(Sentence sentence) throws ServiceFailureException {
        checkDataSource();
        validate(sentence);
        checkCell(sentence.getCellId());
        Connection connection = null;
        PreparedStatement st = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "INSERT INTO sentence (prisonerId, cellId, startDay, endDay, punishment) VALUES (?,?,?,?,?)");
            st.setLong(1, sentence.getPrisonerId());
            st.setLong(2, sentence.getCellId());
            st.setDate(3, toSqlDate(sentence.getStartDay()));
            st.setDate(4, toSqlDate(sentence.getEndDay()));
            st.setString(5, sentence.getPunishment());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, sentence, true);
            connection.commit();

        }catch (SQLException ex) {
            String msg = "Error when inserting sentence into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection,st);
        }


    }

    @Override
    public List<Sentence> findAllSentences() throws ServiceFailureException {
        checkDataSource();
        Connection connection = null;
        PreparedStatement st = null;

        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT prisonerId, cellId, startDay, endDay, punishment FROM sentence");
            return executeQueryFromMultipleSentence(st);

        }catch (SQLException ex) {
            String msg = "Error when getting sentences from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }finally {
            DBUtils.closeQuietly(connection, st);
        }

    }

    static List<Sentence> executeQueryFromMultipleSentence(PreparedStatement st) throws SQLException{
        ResultSet rs = st.executeQuery();
        List<Sentence> list = new ArrayList<>();

        while (rs.next()) {
            list.add(rowToSentence(rs));
        }
        return list;
    }

    static Sentence executeQueryForSingleSentence(PreparedStatement st) throws SQLException, ServiceFailureException{
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Sentence sentence = rowToSentence(rs);
            if (rs.next()) {
                throw new ServiceFailureException("Error, more sentences with same id found");
            }
            return sentence;
        }else{
            return null;
        }

    }

    static private Sentence rowToSentence(ResultSet rs) throws SQLException {
        Sentence sentence = new Sentence();
        sentence.setPrisonerId(rs.getLong("prisonerId"));
        sentence.setCellId(rs.getLong("cellId"));
        sentence.setStartDay(toLocalDate(rs.getDate("startDay")));
        sentence.setEndDay(toLocalDate(rs.getDate("endDay")));
        sentence.setPunishment(rs.getString("punishment"));
        return sentence;
    }

    @Override
    public void updateSentence(Sentence oldSentence, Sentence newSentence) throws ServiceFailureException {
        checkDataSource();
        validate(oldSentence);
        validate(newSentence);

        Connection connection = null;
        PreparedStatement st = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "UPDATE sentence SET prisonerId = ?, cellId = ?, startDay = ?, endDay = ?, punishment = ? " +
                            "WHERE prisonerId = ? AND cellId = ? AND startDay = ? AND endDay = ?");
            st.setLong(1, newSentence.getPrisonerId());
            st.setLong(2, newSentence.getCellId());
            st.setDate(3, toSqlDate(newSentence.getStartDay()));
            st.setDate(4, toSqlDate(newSentence.getEndDay()));
            st.setString(5, newSentence.getPunishment());

            st.setLong(6, oldSentence.getPrisonerId());
            st.setLong(7, oldSentence.getCellId());
            st.setDate(8, toSqlDate(oldSentence.getStartDay()));
            st.setDate(9, toSqlDate(oldSentence.getEndDay()));

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, newSentence, false);
            connection.commit();

        }catch(SQLException ex) {
            String msg = "Error when updating sentence in db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection,st);
        }
    }


    @Override
    public void deleteSentence(Sentence sentence) throws ServiceFailureException {
        checkDataSource();
        validate(sentence);

        Connection connection = null;
        PreparedStatement st = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "DELETE FROM sentence WHERE prisonerId = ? AND cellId = ? AND startDay = ? AND endDay = ?");
            st.setLong(1, sentence.getPrisonerId());
            st.setLong(2, sentence.getCellId());
            st.setDate(3, toSqlDate(sentence.getStartDay()));
            st.setDate(4, toSqlDate(sentence.getEndDay()));

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, sentence, false);
            connection.commit();

        }catch (SQLException ex) {
            String msg = "Error when deleting sentence from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public List<Sentence> findSentencesForPrisoner(Prisoner prisoner) throws ServiceFailureException {
        checkDataSource();
        if (prisoner == null) {
            throw new IllegalArgumentException("Error, prisoner is null");
        }
        if (prisoner.getId() == null) {
            throw new ValidationException("Error, prisoners id is null");
        }

        Connection connection = null;
        PreparedStatement st = null;

        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT prisonerId, cellId, startDay, endDay, punishment FROM sentence " +
                            "WHERE prisonerId = ?");
            st.setLong(1, prisoner.getId());
            return executeQueryFromMultipleSentence(st);


        }catch (SQLException ex) {
            String msg = "Error when getting sentences with prisoner = " + prisoner + " from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }finally {
            //DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection,st);
        }
    }

    @Override
    public List<Sentence> findSentencesForCell(Cell cell) throws ServiceFailureException {
        checkDataSource();
        if (cell == null) {
            throw new IllegalArgumentException("Error, cell is null");
        }
        if (cell.getId() == null) {
            throw new ValidationException("Error, cells id is null");
        }
        Connection connection = null;
        PreparedStatement st = null;

        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT prisonerId, cellId, startDay, endDay, punishment FROM sentence " +
                            "WHERE cellId = ?");
            st.setLong(1, cell.getId());

            return executeQueryFromMultipleSentence(st);

        }catch (SQLException ex) {
            String msg = "Error when getting sentences with cell = " + cell + " from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }finally {
            //DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection,st);
        }

    }


    @Override
    public int findFreeCapacity(Cell cell) throws ServiceFailureException {
        if (cell == null) {
            throw new IllegalArgumentException("Error, cell is null");
        }

        List<Sentence> list;
        list = findSentencesForCell(cell);
        return cell.getCapacity() - getCurrentSentenceFromList(list).size();
    }

    public Cell findCellWithPrisoner(Prisoner prisoner) {
        if (prisoner == null) {
            throw new IllegalArgumentException("Error, prisoner is null");
        }

        List<Sentence> list = findSentencesForPrisoner(prisoner);
        if (list.size() == 0) {
            return null;
        }
        Sentence sentence =  list.get(0);
        CellManagerImpl cm = new CellManagerImpl();
        cm.setDataSource(dataSource);
        Cell cell = cm.getCellById(sentence.getCellId());
        return cell;

    }



    public void putPrisonerIntoCell(Prisoner prisoner, Cell cell) {
    }

    /*public List<Prisoner> findPrisonersInCell(Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Error, cell is null");
        }
        List<Sentence> list = findSentencesForCell(cell);
        List<Prisoner> retList = new ArrayList<>();
        PrisonerManagerImpl pm = new PrisonerManagerImpl();
        pm.setDataSource(dataSource);

        for (Sentence s : list) {
            retList.add(pm.getPrisonerById(s.getPrisonerId()));
        }
        return retList;
    }*/

    public List<Prisoner> findPrisonersInCell(Cell cell) throws ServiceFailureException, IllegalEntityException {
        checkDataSource();
        if (cell == null) {
            throw new IllegalArgumentException("cell is null");
        }
        if (cell.getId() == null) {
            throw new IllegalEntityException("cell id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT prisonerid " +
                            "FROM sentence JOIN Cell ON Cell.id = sentence.cellId " +
                            "WHERE Cell.id = ?");
            st.setLong(1, cell.getId());
            return PrisonerManagerImpl.executeQueryForMultiplePrisoners(st);
        } catch (SQLException ex) {
            String msg = "Error when trying to find prisoners in cell " + cell;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    public List<Cell> findEmptyCells() {
        List<Sentence> list = findAllSentences();
        List<Sentence> currentSentences = getCurrentSentenceFromList(list);
        CellManagerImpl cm = new CellManagerImpl();
        cm.setDataSource(dataSource);
        List<Cell> allCells = cm.findAllCells();
        Cell cellToDelete;
        for (Sentence s :currentSentences) {
            cellToDelete = cm.getCellById(s.getCellId());
            allCells.remove(cellToDelete);
        }
        return allCells;

    }

    /*
    public List<Cell> findCellsWithSomeFreeSpace() {
        return null;
    }
    */

    private void validate(Sentence sentence) {
        if (sentence == null) {
            throw new IllegalArgumentException("Error, sentence is null");
        }
        if (sentence.getPrisonerId() == null) {
            throw new ValidationException("Error, prisonerId in sentence is null");
        }
        if (sentence.getCellId() == null) {
            throw new ValidationException("Error, cellId in sentence is null");
        }

        //checkCell(sentence.getCellId());
        //checkPrisoner(sentence.getPrisonerId());

        if (sentence.getStartDay() == null) {
            throw new ValidationException("Error, startDay in sentence is null");
        }
        if (sentence.getEndDay() == null) {
            throw new ValidationException("Error, endDay in sentence is null");
        }
        if (sentence.getStartDay().isAfter(sentence.getEndDay())) {
            throw new ValidationException("Error, startDay  is before endDay in sentence ");
        }
    }

    private void checkCell(Long cellId) {
        CellManagerImpl cm = new CellManagerImpl();
        cm.setDataSource(dataSource);
        Cell cell = cm.getCellById(cellId);
        if (cell == null) {
            throw new ValidationException("Error, cell with this id is not in database");
        }

        List<Sentence> list = findSentencesForCell(cell);

        if (getCurrentSentenceFromList(list).size() >= cell.getCapacity()) {
            throw new ValidationException("Error, cell capacity is to low");
        }
    }
    /*private void checkPrisoner(Long prisonerId) {
        PrisonerManagerImpl pm = new PrisonerManagerImpl();
        pm.setDataSource(dataSource);
        Prisoner prisoner = pm.getPrisonerById(prisonerId);
        if (prisoner == null) {
            throw new ValidationException("Error prisoner with this id is not in database");
        }
    }*/

    public List<Sentence> getCurrentSentenceFromList(List<Sentence> list) {
        List<Sentence> retList = new ArrayList<>();

        for (Sentence s : list) {
            if (s.getEndDay().isAfter(LocalDate.now(clock))) {
                retList.add(s);
            }
        }
        return retList;
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

}
