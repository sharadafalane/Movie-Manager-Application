package com.movie.movieManager.exception;

public class EmptyFileException extends RuntimeException{

    public EmptyFileException(String message){
        super(message);
    }
}
