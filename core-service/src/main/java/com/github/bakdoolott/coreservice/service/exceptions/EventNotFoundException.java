package com.github.bakdoolott.coreservice.service.exceptions;

public class EventNotFoundException extends RuntimeException{
    public EventNotFoundException(Long id){
        super("Event Not Found: " + id);
    }
}
