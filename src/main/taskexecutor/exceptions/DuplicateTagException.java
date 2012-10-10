package main.taskexecutor.exceptions;
public class DuplicateTagException extends Exception
{
    private static final long serialVersionUID = -1918100318215304085L;
    public DuplicateTagException(String message)
    {
	super(message);
    }
}