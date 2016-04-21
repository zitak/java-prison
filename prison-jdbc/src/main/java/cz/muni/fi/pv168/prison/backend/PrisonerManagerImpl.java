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
 * This class implements {@link PrisonerManager}.
 */
public class PrisonerManagerImpl implements PrisonerManager {

    private static final Logger logger = Logger.getLogger(
            PrisonerManagerImpl.class.getName());

    private DataSource dataSource;
    private final Clock clock;

    public PrisonerManagerImpl(Clock clock) {
        this.clock = clock;
    }
    //public PrisonerManagerImpl() { }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void createPrisoner(Prisoner prisoner) throws ServiceFailureException {
        checkDataSource();
        validate(prisoner);
        if (prisoner.getId() != null) {
            throw new IllegalArgumentException("prisoner id is already set");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);
                st = conn.prepareStatement(
                    "INSERT INTO Prisoner (name, surname, born) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, prisoner.getName());
            st.setString(2, prisoner.getSurname());
            st.setDate(3, toSqlDate(prisoner.getBorn()));

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, prisoner, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            prisoner.setId(id);
            conn.commit();

        } catch (SQLException ex) {
            String msg = "Error when inserting prisoner into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    /*private Long getKey(ResultSet keyRS, Prisoner prisoner) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert prisoner" + prisoner
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert prisoner " + prisoner
                        + " - no key found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retrieving failed when trying to insert prisoner " + prisoner
                    + " - no key found");
        }
    }*/

    @Override
    public Prisoner getPrisonerById(Long id) throws ServiceFailureException {
        checkDataSource();

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, name, surname, born FROM Prisoner WHERE id = ?");
            st.setLong(1, id);
            return executeQueryForSinglePrisoner(st);
        } catch (SQLException ex) {
            String msg = "Error when getting prisoner with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Prisoner> getPrisonerBySurname(String surname) throws ServiceFailureException {
        checkDataSource();
        if (surname == null) {
            throw new IllegalArgumentException("surname is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, name, surname, born FROM Prisoner WHERE surname = ?");
            st.setString(1,surname);
            return executeQueryForMultiplePrisoners(st);
        }catch (SQLException ex) {
            String msg = "Error when getting prisoner with surname = " + surname + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }finally {
            DBUtils.closeQuietly(conn, st);
        }

    }

    /*private Prisoner resultSetToPrisoner(ResultSet rs) throws SQLException {
        Prisoner prisoner = new Prisoner();
        prisoner.setId(rs.getLong("id"));
        prisoner.setName(rs.getString("name"));
        prisoner.setSurname(rs.getString("surname"));
        prisoner.setBorn(rs.getDate("date").toLocalDate());
        return prisoner;
    }*/

    /*public List<Prisoner> getPrisonerBySurname(String surname) throws ServiceFailureException {
        throw new ServiceFailureException("Not supported yet.");
    }*/

    @Override
    public List<Prisoner> findAllPrisoners() throws ServiceFailureException {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT id,name,surname,born FROM prisoner");
            return executeQueryForMultiplePrisoners(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all prisoners from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void updatePrisoner(Prisoner prisoner) throws ServiceFailureException {
        checkDataSource();
        validate(prisoner);
        if (prisoner.getId() == null) {
            throw new IllegalEntityException("prisoner id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                "UPDATE Prisoner SET name = ?, surname = ?, born = ? WHERE id =?");
            st.setString(1, prisoner.getName());
            st.setString(2, prisoner.getSurname());
            st.setDate(3, toSqlDate(prisoner.getBorn()));
            st.setLong(4, prisoner.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, prisoner, false);
            conn.commit();
        } catch (SQLException ex) {
                String msg = "Error when updating prisoner in the db";
                logger.log(Level.SEVERE, msg, ex);
                throw new ServiceFailureException(msg, ex);
        } finally {
                DBUtils.doRollbackQuietly(conn);
                DBUtils.closeQuietly(conn, st);
            }
        }

    @Override
    public void deletePrisoner(Prisoner prisoner) throws ServiceFailureException {
        checkDataSource();
        if (prisoner == null) {
            throw new IllegalArgumentException("prisoner is null");
        }
        if (prisoner.getId() == null) {
            throw new IllegalEntityException("prisoner id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                "DELETE FROM prisoner WHERE id = ?");
            st.setLong(1, prisoner.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, prisoner, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting prisoner from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    static Prisoner executeQueryForSinglePrisoner(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Prisoner result = rowToPrisoner(rs);
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more prisoners with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }

    static List<Prisoner> executeQueryForMultiplePrisoners(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Prisoner> result = new ArrayList<Prisoner>();
        while (rs.next()) {
            result.add(rowToPrisoner(rs));
        }
        return result;
    }

    static private Prisoner rowToPrisoner(ResultSet rs) throws SQLException {
        Prisoner result = new Prisoner();
        result.setId(rs.getLong("id"));
        result.setName(rs.getString("name"));
        result.setSurname(rs.getString("surname"));
        result.setBorn(toLocalDate(rs.getDate("born")));

        return result;
    }

    private void validate(Prisoner prisoner) {
        if (prisoner == null) {
            throw new IllegalArgumentException("grave is null");
        }
        if (prisoner.getName() == null) {
            throw new ValidationException("name is null");
        }
        if (prisoner.getSurname() == null) {
            throw new ValidationException("surname is null");
        }
        LocalDate today = LocalDate.now(clock);
        if (prisoner.getBorn() != null && prisoner.getBorn().isAfter(today)) {
            throw new ValidationException("born is in future");
        }
    }

    private static Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

}
