package assignment2.model;

public class PersonParameters {
    private static Person personParam;

    public static Person getPersonParam() {
        return personParam;
    }

    public static void setPersonParam(Person personParam) {
        PersonParameters.personParam = personParam;
    }
}
