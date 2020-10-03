package assignment2.model;

import java.time.LocalDate;
import java.time.Period;

public class Person {
    private int id;
    private int age;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;

    public Person(){
        id = 0;
        age = 0;
        firstName = "first name";
        lastName = "last name";
        dateOfBirth = LocalDate.of(2000, 1, 1);
    }

    public Person(int id, String firstName, String lastName, String dateOfBirth){
        this.id = id;
        LocalDate DOB = LocalDate.parse(dateOfBirth);
        Period period = Period.between(DOB, LocalDate.now());
        this.age = period.getYears();
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = DOB;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth.toString();
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = LocalDate.parse(dateOfBirth);
        Period period = Period.between(this.dateOfBirth, LocalDate.now());
        this.age = period.getYears();
    }
}
