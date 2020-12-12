package backend.database;

import client.model.Person;

public class UpdateException extends RuntimeException {
    public Person person;
    public UpdateException(Person person) {
        this.person = person;
    }
}
