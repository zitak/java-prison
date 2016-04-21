package cz.muni.fi.pv168.prison.backend;

/**
 * This exception is thrown when delete or update operation is performed
 * with entity which does not exist in the database.
 */
public class EntityNotFoundException extends RuntimeException{

    /**
     * Constructs an instance of <code>EntityNotFoundException</code> with the specified detail message/
     * @param msg the detail message
     */
    public EntityNotFoundException(String msg) {
        super(msg);
    }
}
