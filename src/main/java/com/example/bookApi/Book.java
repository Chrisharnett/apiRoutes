package com.example.bookApi;

import java.util.List;

/**
 * @author saxDev
 * studentnumber 20188141
 **/
public class Book {
    private String ISBN;
    private String title;
    private int edition;
    private String copyright;
    private List<Author> authorList;

    /**
     * Constructor of a Book.
     * @param ISBN ISBN
     * @param title
     * @param edition
     * @param copyright
     */
    public Book(String ISBN, String title, int edition, String copyright) {
        this.ISBN = ISBN;
        this.title = title;
        this.edition = edition;
        this.copyright = copyright;
    }

    /**
     * get ISBN
     * @return ISBN
     */
    public String getISBN() {
        return ISBN;
    }


    /**
     * get Title
     * @return title
     */
    public String getTitle() {
        return title;
    }


    /**
     * get Edition
     * @return edition
     */
    public int getEdition() {
        return edition;
    }


    /**
     * get Copyright
     * @return copyright
     */
    public String getCopyright() {
        return copyright;
    }


    /**
     * get authorList
     * @return authorList
     */
    public List<Author> getAuthorList() {
        return authorList;
    }

    /**
     * set authorList
     * @param authorList
     */
    public void setAuthorList(List<Author> authorList) {
        this.authorList = authorList;
    }
}