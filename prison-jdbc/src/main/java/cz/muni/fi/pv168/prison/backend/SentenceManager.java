package cz.muni.fi.pv168.prison.backend;

import cz.muni.fi.pv168.common.ServiceFailureException;

import java.util.List;

/**
 * Interface for cell manager.
 */
public interface SentenceManager {

    /**
     * Stores new sentence into database.
     * @param sentence sentence to be created.
     * @throws IllegalArgumentException when sentence is null, or has null prisoner or cell, or has wrong dates,
     *          or if Cell is full.
     * @throws ServiceFailureException when db operation fails.
     */
    void createSentence(Sentence sentence) throws ServiceFailureException;


    /**
     * Returns list of all sentences in the database.
     * @return list of all sentences in database.
     * @throws ServiceFailureException when db operation fails.
     */
    List<Sentence> findAllSentences() throws ServiceFailureException;

    /**
     * Updates sentence in database.
     * @param oldSentence old sentence that should be updated
     * @param newSentence updated sentence to be stored into database.
     * @throws IllegalArgumentException when one of sentences is null, or sentence has null prisoner or cell.
     * @throws ServiceFailureException when db operation fails.
     */
    void updateSentence(Sentence oldSentence, Sentence newSentence) throws ServiceFailureException;

    /**
     * Deleted sentence from database.
     * @param sentence sentence to be deleted from db.
     * @throws IllegalArgumentException when sentence is null, or sentence has null prisoner or cell.
     * @throws ServiceFailureException when db operation fails.
     */
    void deleteSentence(Sentence sentence) throws ServiceFailureException;

    /**
     * Returns list of all sentences for given prisoner.
     * @param prisoner prisoner whose sentences we want to find.
     * @return list of all sentences for prisoner.
     * @throws IllegalArgumentException when prisoner is null.
     * @throws ServiceFailureException when db operation fails.
     */
    List<Sentence> findSentencesForPrisoner(Prisoner prisoner) throws ServiceFailureException;

    /**
     * Returns list of current sentences for given cell.
     * @param cell cell where we want to find sentences.
     * @return list of current sentences in cell.
     * @throws IllegalArgumentException when cell is null.
     * @throws ServiceFailureException when db operation fails.
     */
    List<Sentence> findSentencesForCell(Cell cell) throws ServiceFailureException;

    /**
     * Returns free capacity of given cell.
     * @param cell cell we want to find free capacity.
     * @return number of free capacity.
     * @throws IllegalArgumentException when cell is null.
     * @throws ServiceFailureException when db operation fails.
     */
    int findFreeCapacity(Cell cell) throws ServiceFailureException;
}
