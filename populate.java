package com.insert;

import java.sql.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class populate {
    public static void main(String[] args) {
        //read file path from commend line
        if (args.length == 0) {
            System.out.println("Please provide one or more filename.");
            System.exit(0);
        }
        populate create = new populate();
        for (int i = 0; i < args.length; i++) create.publishData(args[i]);
    }

    public populate() {
    }

    private void publishData(String filepath) {
        //get the file name and call corresponding insert method
        int found = filepath.lastIndexOf('\\');
        String filename;
        if (found != -1)
            filename = filepath.substring(found + 1);
        else
            filename = filepath;

        if (filename.equals("movies.dat")) insertMovies(filepath);
        if (filename.equals("movie_genres.dat")) insertMovieGenres(filepath);
        if (filename.equals("movie_countries.dat")) insertMovieCountries(filepath);
        if (filename.equals("movie_locations.dat")) insertMovieLocations(filepath);
        if (filename.equals("movie_tags.dat")) insertMovieTags(filepath);
        if (filename.equals("tags.dat")) insertTags(filepath);

    }

    private void insertMovies(String filepath) {
        File file = new File(filepath);
        BufferedReader reader = null;
        Connection conn = null;

        int[] index = {0, 1, 5, 7, 8, 12, 13, 17, 18};  //index of useful columns in data file
        String sql = "insert into movies values(?,?,?,?,?,?,?,?,?)";

        try {
            conn = openConnection();

            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();    //skip the first line which is column names

            //delete all data in the table, don't need to do this if we want to append data
            Statement statement = conn.createStatement();
            statement.executeUpdate("delete from movies");
            statement.close();

            PreparedStatement ps = conn.prepareStatement(sql);

            while ((line = reader.readLine()) != null) {
                String[] content = line.split("\t");
                int i = 0;

                //fill data into prepared statement and execute update
                ps.setInt(1, Integer.parseInt(content[index[i++]]));    //movie id
                ps.setString(2, content[index[i++]]);   //title
                ps.setInt(3, Integer.parseInt(content[index[i++]]));    //release year

                //Rotten Tomato all critics rating
                try {
                    ps.setFloat(4, Float.parseFloat(content[index[i++]]));
                } catch (Exception e) {
                    ps.setNull(4, Types.NUMERIC);
                }

                //Rotten Tomato all critics number of reviews
                try {
                    ps.setInt(5, Integer.parseInt(content[index[i++]]));
                } catch (Exception e) {
                    ps.setNull(5, Types.INTEGER);
                }

                //Rotten Tomato top critics rating
                try {
                    ps.setFloat(6, Float.parseFloat(content[index[i++]]));
                } catch (Exception e) {
                    ps.setNull(6, Types.NUMERIC);
                }

                //Rotten Tomato top critics number of reviews
                try {
                    ps.setInt(7, Integer.parseInt(content[index[i++]]));
                } catch (Exception e) {
                    ps.setNull(7, Types.INTEGER);
                }

                //Rotten Tomato audience rating
                try {
                    ps.setFloat(8, Float.parseFloat(content[index[i++]]));
                } catch (Exception e) {
                    ps.setNull(8, Types.NUMERIC);
                }

                //Rotten Tomato audience number of ratings
                try {
                    ps.setInt(9, Integer.parseInt(content[index[i++]]));
                } catch (Exception e) {
                    ps.setNull(9, Types.INTEGER);
                }

                ps.executeUpdate();
            }

            ps.close();
            reader.close();

        } catch (SQLException e) {
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the database driver");
        } catch (Exception e) {
            System.err.println("Errors occurs when opening or closing file: " + e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    private void insertMovieGenres(String filepath) {
        File file = new File(filepath);
        BufferedReader reader = null;
        Connection conn = null;

        int[] index = {0, 1};   //index of useful columns in data file
        String sql = "insert into movie_genres values(?,?)";

        try {
            conn = openConnection();

            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();    //skip the first line which is column names

            //delete all data in the table, don't need to do this if we want to append data
            Statement statement = conn.createStatement();
            statement.executeUpdate("delete from movie_genres");
            statement.close();

            PreparedStatement ps = conn.prepareStatement(sql);

            while ((line = reader.readLine()) != null) {
                String[] content = line.split("\t");
                int i = 0;
                if (content.length < index.length) continue;

                //fill data into prepared statement and execute update
                ps.setInt(1, Integer.parseInt(content[index[i++]]));    //movie id
                ps.setString(2, content[index[i++]]);   //genre

                ps.executeUpdate();
            }

            ps.close();
            reader.close();

        } catch (SQLException e) {
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the database driver");
        } catch (Exception e) {
            System.err.println("Errors occurs when opening or closing file: " + e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    private void insertMovieCountries(String filepath) {
        File file = new File(filepath);
        BufferedReader reader = null;
        Connection conn = null;

        int[] index = {0, 1};   //index of useful columns in data file
        String sql = "insert into movie_countries values(?,?)";

        try {
            conn = openConnection();

            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();    //skip the first line which is column names

            //delete all data in the table, don't need to do this if we want to append data
            Statement statement = conn.createStatement();
            statement.executeUpdate("delete from movie_countries");
            statement.close();

            PreparedStatement ps = conn.prepareStatement(sql);

            while ((line = reader.readLine()) != null) {
                String[] content = line.split("\t");
                int i = 0;
                if (content.length < index.length) continue;

                //fill data into prepared statement and execute update
                ps.setInt(1, Integer.parseInt(content[index[i++]]));    //movie id
                ps.setString(2, content[index[i++]]);   //country

                ps.executeUpdate();
            }

            ps.close();
            reader.close();

        } catch (SQLException e) {
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the database driver");
        } catch (Exception e) {
            System.err.println("Errors occurs when opening or closing file: " + e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    private void insertMovieLocations(String filepath) {
        File file = new File(filepath);
        BufferedReader reader = null;
        Connection conn = null;

        int[] index = {0, 1};   //index of useful columns in data file
        String sql = "insert into movie_locations values(?,?)";

        try {
            conn = openConnection();

            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();    //skip the first line which is column names

            //delete all data in the table, don't need to do this if we want to append data
            Statement statement = conn.createStatement();
            statement.executeUpdate("delete from movie_locations");
            statement.close();

            PreparedStatement ps = conn.prepareStatement(sql);

            while ((line = reader.readLine()) != null) {
                String[] content = line.split("\t");
                int i = 0;
                if (content.length < index.length) continue;

                //fill data into prepared statement and execute update
                ps.setInt(1, Integer.parseInt(content[index[i++]]));    //movie id
                ps.setString(2, content[index[i++]]);   //filming location

                //we only insert country, so there may be duplicates, let DB check primary key constrains
                try {
                    ps.executeUpdate();
                } catch (SQLException e) {
                }
            }

            ps.close();
            reader.close();

        } catch (SQLException e) {
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the database driver");
        } catch (Exception e) {
            System.err.println("Errors occurs when opening or closing file: " + e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    private void insertTags(String filepath) {
        File file = new File(filepath);
        BufferedReader reader = null;
        Connection conn = null;

        int[] index = {0, 1};   //index of useful columns in data file
        String sql = "insert into tags values(?,?)";

        try {
            conn = openConnection();

            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();    //skip the first line which is column names

            //delete all data in the table, don't need to do this if we want to append data
            Statement statement = conn.createStatement();
            statement.executeUpdate("delete from tags");
            statement.close();

            PreparedStatement ps = conn.prepareStatement(sql);

            while ((line = reader.readLine()) != null) {
                String[] content = line.split("\t");
                int i = 0;
                if (content.length < index.length) continue;

                //fill data into prepared statement and execute update
                ps.setInt(1, Integer.parseInt(content[index[i++]]));    //tag id
                ps.setString(2, content[index[i++]]);   //tag name

                ps.executeUpdate();
            }

            ps.close();
            reader.close();

        } catch (SQLException e) {
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the database driver");
        } catch (Exception e) {
            System.err.println("Errors occurs when opening or closing file: " + e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    private void insertMovieTags(String filepath) {
        File file = new File(filepath);
        BufferedReader reader = null;
        Connection conn = null;

        int[] index = {0, 1, 2};    //index of useful columns in data file
        String sql = "insert into movie_tags values(?,?,?)";

        try {
            conn = openConnection();

            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();    //skip the first line which is column names

            //delete all data in the table, don't need to do this if we want to append data
            Statement statement = conn.createStatement();
            statement.executeUpdate("delete from movie_tags");
            statement.close();

            PreparedStatement ps = conn.prepareStatement(sql);

            while ((line = reader.readLine()) != null) {
                String[] content = line.split("\t");
                int i = 0;
                if (content.length < index.length) continue;

                //fill data into prepared statement and execute update
                ps.setInt(1, Integer.parseInt(content[index[i++]]));    //movie id
                ps.setInt(2, Integer.parseInt(content[index[i++]]));    //tag id
                ps.setInt(3, Integer.parseInt(content[index[i++]]));    //tag weight

                ps.executeUpdate();
            }

            ps.close();
            reader.close();

        } catch (SQLException e) {
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the database driver");
        } catch (Exception e) {
            System.err.println("Errors occurs when opening or closing file: " + e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    private Connection openConnection() throws SQLException, ClassNotFoundException {
        Class.forName("oracle.jdbc.driver.OracleDriver");   //load driver
        String url = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";   // Oracle url
        String username = "scott";  // username
        String password = "tiger";  // password
        return DriverManager.getConnection(url, username, password); // get connection
    }

    private void closeConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println("Cannot close connection: " + e.getMessage());
        }
    }

}