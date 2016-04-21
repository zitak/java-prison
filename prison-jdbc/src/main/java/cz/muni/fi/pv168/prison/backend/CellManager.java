package cz.muni.fi.pv168.prison.backend;

import cz.muni.fi.pv168.common.ServiceFailureException;

import java.util.List;

/**
 * Interface for cell manager.
 */
public interface CellManager {

    /**
     * Stores new cell into database. Id for the new cell is automatically generated and stored into id attribute.
     * @param cell cell to be created.
     * @throws IllegalArgumentException when cell is null, or cell has already assigned id.
     * @throws ServiceFailureException when db operation fails.
     */
    void createCell(Cell cell) throws ServiceFailureException;

    /**
     * Returns cell with given id.
     * @param id primary key of requested cell.
     * @return cell with given id or null if such cell does not exist.
     * @throws IllegalArgumentException when given id is null.
     * @throws ServiceFailureException when db operation fails.
     */
    Cell getCellById(Long id) throws ServiceFailureException;

    /**
     * Returns list of all cells in the database.
     * @return list of all cells in database.
     * @throws ServiceFailureException when db operation fails.
     */
    List<Cell> findAllCells() throws ServiceFailureException;

    /**
     * Updates cell in database.
     * @param cell updated cell to be stored into database.
     * @throws IllegalArgumentException when cell is null, or cell has null id.
     * @throws ServiceFailureException when db operation fails.
     */
    void updateCell(Cell cell) throws ServiceFailureException;

    /**
     * Deleted cell from database.
     * @param cell cell to be deleted from db.
     * @throws IllegalArgumentException when cell is null, or cell has null id.
     * @throws ServiceFailureException when db operation fails.
     */
    void deleteCell(Cell cell) throws ServiceFailureException;

}
