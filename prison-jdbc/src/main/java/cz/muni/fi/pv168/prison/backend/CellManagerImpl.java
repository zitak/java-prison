package cz.muni.fi.pv168.prison.backend;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.common.ValidationException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements cell manager.
 */
public class CellManagerImpl implements CellManager {

    private static final Logger logger = Logger.getLogger(
            CellManagerImpl.class.getName());

    private DataSource dataSource;

    public CellManagerImpl() { }


    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void createCell(Cell cell) {
        checkDataSource();
        validate(cell);
        if (cell.getId() != null) {
            throw new IllegalEntityException("cell ID is already set");
        }

        Connection conn = null;
        PreparedStatement st = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO cell (floor, capacity) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setInt(1,cell.getFloor());
            st.setInt(2,cell.getCapacity());
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, cell, true);
            Long id = DBUtils.getId(st.getGeneratedKeys());
            cell.setId(id);
            conn.commit();

        }catch (SQLException ex) {
            String msg = "Error when inserting cell into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }

    }


    @Override
    public Cell getCellById(Long id) {
        checkDataSource();
        if (id == null) {
            throw new IllegalArgumentException("ID is null");
        }
        Connection conn = null;
        PreparedStatement st = null;

        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, floor, capacity FROM Cell WHERE id = ?");
            st.setLong(1,id);
            return executeQueryForSingleCell(st);

        }catch (SQLException ex) {
            String msg = "Error when getting cell with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }finally {
            DBUtils.closeQuietly(conn, st);
        }

    }




    @Override
    public List<Cell> findAllCells() {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;

        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, floor, capacity FROM Cell");
            return executeQueryForMultipleCells(st);

        }catch (SQLException ex) {
            String msg = "Error when getting all cells from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }finally {
            DBUtils.closeQuietly(conn, st);
        }

    }



    @Override
    public void updateCell(Cell cell) throws ServiceFailureException {
        checkDataSource();
        validate(cell);
        if (cell.getId() == null) {
            throw new ValidationException("cell id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "UPDATE Cell SET floor = ?, capacity = ? WHERE id = ?");
            st.setInt(1,cell.getFloor());
            st.setInt(2,cell.getCapacity());
            st.setLong(3,cell.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, cell, false);
            conn.commit();

        }catch (SQLException ex) {
            String msg = "Error when updating cell in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }

    }

    @Override
    public void deleteCell(Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("cell is null");
        }
        if (cell.getId() == null) {
            throw new ValidationException("cell id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM Cell WHERE id = ?");
            st.setLong(1,cell.getId());
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, cell, false);
            conn.commit();
        }catch (SQLException ex) {
            String msg = "Error when deleting cell from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }

    }

    static Cell executeQueryForSingleCell(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Cell result = resultSetToCell(rs);
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more cells with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }

    static List<Cell> executeQueryForMultipleCells(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Cell> result = new ArrayList<>();
        while (rs.next()) {
            result.add(resultSetToCell(rs));
        }
        return result;
    }

    private static Cell resultSetToCell(ResultSet rs) throws SQLException {
        Cell cell = new Cell();
        cell.setId(rs.getLong("id"));
        cell.setCapacity(rs.getInt("capacity"));
        cell.setFloor(rs.getInt("floor"));
        return cell;
    }

    private static void validate(Cell cell) {
        if (cell == null) throw new IllegalArgumentException("cell is null");
        if (cell.getCapacity() < 1) throw new ValidationException("cell capacity is lower than 1");
    }

}
