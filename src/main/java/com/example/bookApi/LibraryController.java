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

//    @POST
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response addBook(Book book) {
//        try (Connection conn = DatabaseConnection.getBookssDatabaseConnection()) {
//            PreparedStatement authorCheckStatement = conn.prepareStatement("SELECT * from authors " +
//                    "WHERE ? = firstName AND " +
//                    "? = lastName");
//            authorCheckStatement.setString(1, authors.authorFirstName);
//            authorCheckStatement.setString(2, book.authorLastName);
//            ResultSet authorResults = authorCheckStatement.executeQuery();
//            Author author = null;
//
//
//            String SQL = "INSERT INTO books ( name, speeds) VALUES (?, ?)";
//            PreparedStatement stmt = conn.prepareStatement(SQL);
//            stmt.setString(1, bike.name);
//            stmt.setInt(2, bike.speeds);
//            stmt.executeUpdate();
//
//            PreparedStatement authorCheckStatement = conn.prepareStatement("SELECT * from authors " +
//                    "WHERE ? = firstName AND " +
//                    "? = lastName");
//            authorCheckStatement.setString(1, authorFirstName);
//            authorCheckStatement.setString(2, authorLastName);
//            ResultSet authorResults = authorCheckStatement.executeQuery();
//            Author author = null;
//        } catch (SQLException e){
//            e.printStackTrace();
//            return Response.status(400, "Unable to perform insert" + e.getMessage()).build();
//        }
//        return Response.ok(bike).build();
//    }
//
//    @DELETE
//    @Path("/{bikeId}")
//    public Response deleteBicycle(@PathParam("bikeId") int bikeId) {
//        try (Connection conn = DatabaseConnection.getBicyclesDatabaseConnection()) {
//            String SQL = "DELETE FROM books WHERE id = ?";
//            PreparedStatement stmt = conn.prepareStatement(SQL);
//            stmt.setInt(1, bikeId);
//            stmt.executeUpdate();
//        } catch (SQLException e){
//            e.printStackTrace();
//            return Response.status(400, "Unable to perform insert" + e.getMessage()).build();
//        }
//        return Response.status(200, Integer.toString(bikeId)).build();
//    }

}
