package cz.muni.fi.pv168.prison.backend;

import cz.muni.fi.pv168.common.ServiceFailureException;

import java.util.List;

/**
 * Interface for prisoner manager.
 */
public interface PrisonerManager {

    /**
     * Stores new prisoner into database. Id for the new prisoner is automatically generated and stored into id attribute.
     * @param prisoner prisoner to be created.
     * @throws IllegalArgumentException when prisoner is null, or prisoner has already assigned id.
     * @throws ServiceFailureException when db operation fails.
     */
    void createPrisoner(Prisoner prisoner) throws ServiceFailureException;

    /**
     * Returns prisoner with given id.
     * @param id primary key of requested prisoner.
     * @return prisoner with given id or null if such prisoner does not exist.
     * @throws IllegalArgumentException when given id is null.
     * @throws ServiceFailureException when db operation fails.
     */
    Prisoner getPrisonerById(Long id) throws ServiceFailureException;

    /**
     * Returns list of prisoners with given surname in the database.
     * @param surname key of requested prisoner.
     * @return list of prisoners with given surname.
     * @throws IllegalArgumentException when given surname is null.
     * @throws ServiceFailureException when db operation fails.
     */
    List<Prisoner> getPrisonerBySurname(String surname) throws ServiceFailureException;

    /**
     * Returns list of all prisoners in the database.
     * @return list of all prisoners in database.
     * @throws ServiceFailureException when db operation fails.
     */
    List<Prisoner> findAllPrisoners() throws ServiceFailureException;

    /**
     * Updates prisoner in database.
     * @param prisoner updated prisoner to be stored into database.
     * @throws IllegalArgumentException when prisoner is null, or prisoner has null id.
     * @throws ServiceFailureException when db operation fails.
     */
    void updatePrisoner(Prisoner prisoner) throws ServiceFailureException;

    /**
     * Deleted prisoner from database.
     * @param prisoner prisoner to be deleted from db.
     * @throws IllegalArgumentException when prisoner is null, or prisoner has null id.
     * @throws ServiceFailureException when db operation fails.
     */
    void deletePrisoner(Prisoner prisoner) throws ServiceFailureException;

}
