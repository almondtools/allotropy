package net.amygdalum.allotropy;

/**
 * thrown in cases that should not be
 */
public class UnexpectedException extends RuntimeException {

    public UnexpectedException(Throwable cause) {
        super(cause);
    }

}
