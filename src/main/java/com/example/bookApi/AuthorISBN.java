package com.example.bookApi;

/**
 * @author saxDev
 * studentnumber 20188141
 **/
public class AuthorISBN {
    public int authorID;
    public String isbn;

    public AuthorISBN(int authorID, String ISBN) {
        this.authorID = this.authorID;
        this.isbn = ISBN;
    }

    public AuthorISBN() {
    }

    public int getAuthorID() {
        return authorID;
    }

    public void setAuthorID(int authorID) {
        this.authorID = authorID;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
