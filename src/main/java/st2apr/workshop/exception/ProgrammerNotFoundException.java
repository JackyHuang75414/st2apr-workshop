package st2apr.workshop.exception;

public class ProgrammerNotFoundException extends RuntimeException {

    public ProgrammerNotFoundException(Long id) {
        super("Programmer " + id + " not found");
    }
}
