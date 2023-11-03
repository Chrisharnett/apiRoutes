package com.example.bookApi;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

@Path("/library")
public class LibraryController {

    @Path("/")
    @GET
    public Response Library() {
        String message  = "Welcome to my Library API.\n" +
                "All paths start with /api/library\n" +
                "/books\t List all books\n" +
                "/authors\t List all authors\n" +
                "Find author or book by adding the correct id\n" +
                "/addbook or /addauthor to add to database.\n" +
                "delbook/{id} or delauthor/{id} to delete";
        return Response.ok(message).build();
    }

    @Path("/books")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response books() {

        LinkedList<Book> bookList = new LinkedList<>();
        LinkedList<Author> authorList = new LinkedList<>();

        try (Connection conn = DatabaseConnection.getBookssDatabaseConnection();){
            // get all the books
            Statement statement = conn.createStatement();
            String sqlQuery = "SELECT * from titles";
            ResultSet resultSet = statement.executeQuery(sqlQuery);

            while (resultSet.next()) {
                bookList.add(new Book(resultSet.getString(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getString(4)));
            }

            // Get all the authors
            ResultSet authorsResultSet = statement.executeQuery("SELECT * FROM authors");
            while (authorsResultSet.next()) {
                Author author = new Author(authorsResultSet.getInt("authorid"), authorsResultSet.getString("firstName"),
                        authorsResultSet.getString("lastName"));
                authorList.add(author);
            }
            String createAuthorList = "SELECT a.authorID, a.firstName, a.lastName " +
                    "FROM authors a JOIN authorISBN i ON(a.authorID = i.authorID)" +
                    "JOIN titles t using(isbn)" +
                    "WHERE i.isbn = ?";

            // Loop through books. Get matching authors and add them to the book.authorList
            for (Book book : bookList) {
                PreparedStatement pstmt = conn.prepareStatement(createAuthorList);
                pstmt.setString(1, book.getISBN());
                ResultSet results = pstmt.executeQuery();
                List<Author> bookAuthors = new LinkedList<>();
                while (results.next()) {
                    for (Author author : authorList) {
                        if (author.getId() == results.getInt("authorID")) {
                            bookAuthors.add(author);
                        }
                    }
                }
                book.setAuthorList(bookAuthors);
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }

        GenericEntity<List<Book>> entityList = new GenericEntity<List<Book>>(bookList) {};
        return Response.ok(entityList).build();
    }

    @Path("/authors")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response authors() {

        LinkedList<Book> bookList = new LinkedList<>();
        LinkedList<Author> authorList = new LinkedList<>();

        try (Connection conn = DatabaseConnection.getBookssDatabaseConnection()){
            Statement statement = conn.createStatement();
            // Get all the authors
            ResultSet authorsResultSet = statement.executeQuery("SELECT * FROM authors");
            while (authorsResultSet.next()) {
                Author author = new Author(authorsResultSet.getInt("authorID"), authorsResultSet.getString("firstName"),
                        authorsResultSet.getString("lastName"));
                authorList.add(author);
            }
            String createTitleList = "SELECT t.title, t.isbn, t.editionNumber, t.copyright  " +
                    "FROM authors a JOIN authorISBN i ON(a.authorID = i.authorID)" +
                    "JOIN titles t using(isbn)" +
                    "WHERE i.authorId = ?";

            // Loop through authors, get matching books and add them to the author.setBooklist
            for (Author author : authorList) {
                PreparedStatement pstmt = conn.prepareStatement(createTitleList);
                pstmt.setInt(1, author.getId());

                // Get book titles
                ResultSet titleResultSet = pstmt.executeQuery();
                List<Book> titlesByAuthor = new LinkedList<>();
                while (titleResultSet.next()) {
                    Book b = new Book(titleResultSet.getString("isbn"),
                            titleResultSet.getString("title"),
                            titleResultSet.getInt("editionNumber"),
                            titleResultSet.getString("copyright"));
                            titlesByAuthor.add(b);
                }
                author.setBookList(titlesByAuthor);
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }

        GenericEntity<List<Author>> entityList = new GenericEntity<List<Author>>(authorList) {};
        return Response.ok(entityList).build();
    }
    @Path("/books/{isbn}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response bookById(@PathParam("isbn") String isbn) {

        Book book = new Book();
        LinkedList<Author> authorList = new LinkedList<>();

        try (Connection conn = DatabaseConnection.getBookssDatabaseConnection();){
            // get all the books
            String sqlQuery = "SELECT * from titles WHERE isbn = ?";
            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            stmt.setString(1, isbn);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                book = new Book(resultSet.getString(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getString(4));
            }

            // Get the books' authors
            String SQL =  "SELECT a.authorID, a.firstName, a.lastName " +
                    "FROM authors a JOIN authorISBN i ON(a.authorID = i.authorID)" +
                    "JOIN titles t using(isbn)" +
                    "WHERE i.isbn = ?";;
            PreparedStatement bookAuthorsStmt = conn.prepareStatement(SQL);
            bookAuthorsStmt.setString(1, isbn);
            ResultSet authorsResultSet = bookAuthorsStmt.executeQuery();

            while (authorsResultSet.next()) {
                Author author = new Author(authorsResultSet.getInt("authorid"), authorsResultSet.getString("firstName"),
                        authorsResultSet.getString("lastName"));
                authorList.add(author);
            }
            book.setAuthorList(authorList);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return Response.ok(book).build();
    }

    @Path("/authors/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authorsById(@PathParam("id") int id) {

        Author author = new Author();
        LinkedList<Book> bookList = new LinkedList<>();

        try (Connection conn = DatabaseConnection.getBookssDatabaseConnection();){
            // get the author
            String sqlQuery = "SELECT * from authors WHERE authorID = ?";
            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                author = new Author(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3));
            }

            // Get the authors' books
            String authorsBooksSQL =  "SELECT t.title, t.isbn, t.editionNumber, t.copyright  " +
                    "FROM authors a JOIN authorISBN i ON(a.authorID = i.authorID)" +
                    "JOIN titles t using(isbn)" +
                    "WHERE i.authorId = ?";;
            PreparedStatement authorsBooksStmt = conn.prepareStatement(authorsBooksSQL);
            authorsBooksStmt.setInt(1, id);
            ResultSet authorsBooksResultSet = authorsBooksStmt.executeQuery();

            while (authorsBooksResultSet.next()) {
                Book b = new Book(authorsBooksResultSet.getString("isbn"),
                        authorsBooksResultSet.getString("title"),
                        authorsBooksResultSet.getInt("editionNumber"),
                        authorsBooksResultSet.getString("copyright"));
                bookList.add(b);
            }
            author.setBookList(bookList);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return Response.ok(author).build();
    }

    @Path("/books/addbook")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addBook(Book book) {
        try (Connection conn = DatabaseConnection.getBookssDatabaseConnection()) {

            String SQL = "INSERT INTO titles ( isbn, title, editionNumber, copyright ) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setString(1, book.getISBN());
            stmt.setString(2, book.getTitle());
            stmt.setInt(3, book.getEdition());
            stmt.setString(4, book.getCopyright());
            stmt.executeUpdate();

        } catch (SQLException e){
            e.printStackTrace();
            return Response.status(400, "Unable to perform insert" + e.getMessage()).build();
        }
        return Response.ok(book).build();
    }

    @Path("/authors/addauthor")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addAuthor(Author author) {
        try (Connection conn = DatabaseConnection.getBookssDatabaseConnection()) {

            String SQL = "INSERT INTO authors ( firstName, lastName ) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setString(1, author.getFirstName());
            stmt.setString(2,author.getLastName());
            stmt.executeUpdate();

//            Get the new authors ID from the database for the response message.
            SQL = "SELECT authorID FROM authors WHERE firstName = ? AND lastName = ?";
            stmt = conn.prepareStatement(SQL);
            stmt.setString(1, author.getFirstName());
            stmt.setString(2,author.getLastName());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()){
                author.setId(rs.getInt(1));
            }

        } catch (SQLException e){
            e.printStackTrace();
            return Response.status(400, "Unable to perform insert" + e.getMessage()).build();
        }
        return Response.ok(author).build();
    }

    @Path("/associateauthor")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response associateAuthor(AuthorISBN authorISBN) {
        try (Connection conn = DatabaseConnection.getBookssDatabaseConnection()) {

            String SQL = "INSERT INTO authorISBN ( authorID, isbn ) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setInt(1, authorISBN.getAuthorID());
            stmt.setString(2, authorISBN.getIsbn());
            stmt.executeUpdate();

        } catch (SQLException e){
            e.printStackTrace();
            return Response.status(400, "Unable to perform insert" + e.getMessage()).build();
        }
        return Response.ok(authorISBN).build();
    }

    @Path("/modbook")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modBook(Book book) {
        try (Connection conn = DatabaseConnection.getBookssDatabaseConnection()) {

            String SQL = "UPDATE titles " +
                    "SET title = ?, editionNumber = ?, copyright = ?" +
                    "WHERE isbn = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setString(1, book.getTitle());
            stmt.setInt(2, book.getEdition());
            stmt.setString(3, book.getCopyright());
            stmt.setString(4, book.getISBN());
            stmt.executeUpdate();

        } catch (SQLException e){
            e.printStackTrace();
            return Response.status(400, "Unable to perform insert" + e.getMessage()).build();
        }
        return Response.ok(book).build();
    }

    @Path("/modauthor")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modAuthor(Author author) {
        try (Connection conn = DatabaseConnection.getBookssDatabaseConnection()) {

            String SQL = "UPDATE authors " +
                    "SET firstName = ?, lastName = ?" +
                    "WHERE authorID = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setString(1, author.getFirstName());
            stmt.setString(2, author.getLastName());
            stmt.setInt(3, author.getId());
            stmt.executeUpdate();

        } catch (SQLException e){
            e.printStackTrace();
            return Response.status(400, "Unable to perform insert" + e.getMessage()).build();
        }
        return Response.ok(author).build();
    }

    @DELETE
    @Path("/delbook/{isbn}")
    public Response deleteBook(@PathParam("isbn") String isbn) {
        String message;
        try (Connection conn = DatabaseConnection.getBookssDatabaseConnection()) {

            String SQL = "DELETE FROM titles WHERE isbn = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setString(1, isbn);
            stmt.executeUpdate();

            message = "Book removed.";
        } catch (SQLException e){
            e.printStackTrace();
            return Response.status(400, "Unable to perform insert" + e.getMessage()).build();
        }
        return Response.ok(message).build();
    }
    @DELETE
    @Path("/delauthor/{id}")
    public Response deleteAuthor(@PathParam("id") int id) {
        String message;
        try (Connection conn = DatabaseConnection.getBookssDatabaseConnection()) {
            String SQL = "DELETE FROM authors WHERE authorID = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setInt(1, id);
            stmt.executeUpdate();

            message = "Author removed.";
        } catch (SQLException e){
            e.printStackTrace();
            return Response.status(400, "Unable to perform insert" + e.getMessage()).build();
        }
        return Response.ok(message).build();
    }

}
