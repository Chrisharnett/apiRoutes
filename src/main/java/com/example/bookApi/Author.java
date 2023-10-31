package com.example.bookApi;
import java.util.List;

/**
 * @author saxDev
 * studentnumber 20188141
 **/
public class Author {
    private int id;
    private String firstName;
    private String lastName;
    private List<Book> bookList;

    public Author(int id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<Book> getBookList() {
        return bookList;
    }

    public void setBookList(List<Book> bookList) {
        this.bookList = bookList;
    }

    public String printAuthorName(){
        return this.lastName + ", " + this.firstName;
    }
}