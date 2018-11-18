# movie-database
This is a small project to demonstrate the communication with database using JDBC in java.
I use oracle db11g as my database in this project.

"hetrec2011-movielens-2k-v2.zip" contains all the data file needed in this project.

"createdb.sql" and "dropdb.sql" are used to create and drop talbes in the database.

"populate.java" is used to insert all needed data into database tables. It uses command line parameters to get input data files.

"MovieSearch.java" is a java GUI used to query movies. User can find movies by specifying conditions such as movie genre, movie country, filming location, critics rating, number of critics reviews, released year range and movie tag weight. The combobox "and/or" is for different values chosen in genre area/country area/location area. User must choose a genre to make the program work, and other conditions are optional. The final query will be displayed and by clicking "execute query" botton, the movies satisfying all the conditions will be displayed in result area.

In order to execute the two java file, you need to use oracle database driver "ojdbc6.jar". Build dependency in your IDE or use "-classpath" to specify this file if you execute through command line.
