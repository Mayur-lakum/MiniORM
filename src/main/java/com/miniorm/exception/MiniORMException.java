package com.miniorm.exception;

public class MiniORMException extends RuntimeException
{
    public MiniORMException(String message)
    {
        super(message);
    }

    public MiniORMException(String message, Throwable cause)
    {
        super(message, cause);
    }
}