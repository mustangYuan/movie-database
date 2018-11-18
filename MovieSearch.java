/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 *
 * @author: Ben Yuan
 */
import java.sql.*;
import javax.swing.JCheckBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;

public class MovieSearch extends javax.swing.JFrame {

    /**
     * Creates new form MovieSearch
     */
    private JCheckBox[] genres = new JCheckBox[50];
    private JCheckBox[] countries = new JCheckBox[100];
    private JCheckBox[] locations = new JCheckBox[200];
    private int num_genres = 0;
    private int num_countries = 0;
    private int num_locations = 0;
    private int genreSelected = 0;
    private int countrySelected = 0;
    private String countryQuery = null;
    private String locationQuery = null;
    private String tagQuery = null;
    private String tagQueryFixed = "select distinct t.tagtext from movie_tags mt, tags t, movies m where mt.tagid=t.tagid and mt.mid=m.mid";
    private String finalQuery = null;
    private String finalQueryFixed = "select mid, title, year, all_critic_rating, all_critic_num_reviews, top_critic_rating, top_critic_num_reviews, audience_rating, audience_num_ratings from movies where";
    private boolean isAnd = true;

    public MovieSearch() {
        initComponents();
        jTextAreaQuery.setLineWrap(true);
        jTextAreaQuery.setWrapStyleWord(true);
        jTextAreaTag.setLineWrap(true);
        jTextAreaTag.setWrapStyleWord(true);
        jTextAreaResult.setLineWrap(true);
        jTextAreaResult.setWrapStyleWord(true);
        jPanelGenre.setLayout(new BoxLayout(jPanelGenre, BoxLayout.Y_AXIS));
        jPanelCountry.setLayout(new BoxLayout(jPanelCountry, BoxLayout.Y_AXIS));
        jPanelLocation.setLayout(new BoxLayout(jPanelLocation, BoxLayout.Y_AXIS));
        jPanelCountry.updateUI();
        jPanelCountry.repaint();
        jPanelLocation.updateUI();
        jPanelLocation.repaint();
        display();
    }

    private void display() {
        Connection conn = null;
        String sql = "select distinct genre from movie_genres order by genre";
        Statement stmt = null;
        ResultSet rs = null;
        num_genres = 0;

        //execute query and display result
        try {
            conn = openConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs != null) {
                while (rs.next()) {
                    genres[num_genres] = new JCheckBox();
                    genres[num_genres].setText(rs.getString(1));
                    jPanelGenre.add(genres[num_genres]);
                    genres[num_genres].addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            showCountry();
                        }
                    });
                    num_genres++;
                }
            }

        } catch (SQLException e) {
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the database driver");
        } finally {
            closeConnection(conn);
        }

    }

    private void showCountry() {
        Connection conn = null;
        String sql = "select distinct country from movie_countries where mid in (select mid from movie_genres where genre in";
        String forAnd = " group by mid having count(*)=";
        String order = " order by country";
        ResultSet rs = null;
        Statement stmt = null;
        tagQuery = null;
        finalQuery = null;
        countryQuery = null;
        num_countries = 0;
        genreSelected = 0;
        jPanelCountry.removeAll();
        jPanelCountry.updateUI();
        jPanelCountry.repaint();
        jPanelLocation.removeAll();
        jPanelLocation.updateUI();
        jPanelLocation.repaint();

        //check selected genres
        String genreCondition = "";
        for (int j = 0; j < num_genres; j++) {
            if (genres[j].isSelected()) {
                genreCondition += "'" + genres[j].getText() + "',";
                genreSelected++;
            }
        }

        //if no genre selected
        if (genreSelected == 0) {
            tagQuery = null;
            finalQuery = null;
            queryTags();
            return;
        }

        //build query string
        genreCondition = "(" + genreCondition.substring(0, genreCondition.length() - 1) + ")";
        sql += genreCondition;
        forAnd += Integer.toString(genreSelected);

        if (isAnd) {
            sql += forAnd + ")";
        } else {
            sql += ")";
        }

        sql += order;   //add order by clause
        countryQuery = sql; //remember the query string in this step

        setQuery(sql);
        queryTags();

        //execute query and display result
        try {
            conn = openConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            if (rs != null) {
                while (rs.next()) {
                    countries[num_countries] = new JCheckBox();
                    countries[num_countries].setText(rs.getString(1));
                    jPanelCountry.add(countries[num_countries]);
                    countries[num_countries].addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            showLocation();
                        }
                    });
                    num_countries++;
                }

            }
            jPanelCountry.updateUI();
            jPanelCountry.repaint();

        } catch (SQLException e) {
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the database driver");
        } finally {
            closeConnection(conn);
        }

    }

    private void showLocation() {
        Connection conn = null;
        String sql = "select distinct country from movie_locations where mid in ";
        String forAnd = " group by mid having count(*)=";
        String order = " order by country";
        ResultSet rs = null;
        Statement stmt = null;
        locationQuery = null;
        num_locations = 0;
        countrySelected = 0;
        jPanelLocation.removeAll();
        jPanelLocation.updateUI();
        jPanelLocation.repaint();

        //check selected country
        String countryCondition = "";
        for (int j = 0; j < num_countries; j++) {
            if (countries[j].isSelected()) {
                countryCondition += "'" + countries[j].getText() + "',";
                countrySelected++;
            }
        }

        //if no genre selected
        if (countrySelected == 0) {
            locationQuery = countryQuery;
            setQuery(countryQuery);
            queryTags();
            return;
        }

        //build query string
        int start = countryQuery.indexOf('(');
        int end = countryQuery.lastIndexOf(')');
        sql += countryQuery.substring(start, end);  //get conditions from previous query for country
        sql += " intersect select mid from movie_countries where country in";
        countryCondition = "(" + countryCondition.substring(0, countryCondition.length() - 1) + ")";
        sql += countryCondition;

        forAnd += Integer.toString(countrySelected);

        if (isAnd) {
            sql += forAnd + ")";
        } else {
            sql += ")";
        }

        sql += order;   //add order by clause
        locationQuery = sql; //remember the query string in this step

        setQuery(sql);
        queryTags();

        //execute query and display result
        try {
            conn = openConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            if (rs != null) {
                while (rs.next()) {
                    locations[num_locations] = new JCheckBox();
                    locations[num_locations].setText(rs.getString(1));
                    jPanelLocation.add(locations[num_locations]);
                    locations[num_locations].addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            showTag();
                        }
                    });
                    num_locations++;
                }

            }
            jPanelLocation.updateUI();
            jPanelLocation.repaint();

        } catch (SQLException e) {
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the database driver");
        } finally {
            closeConnection(conn);
        }

    }

    //set tagQuery and finalQuery according to genre selected, country selected and location selected
    private void setQuery(String sql) {
        int start = sql.indexOf('(');
        int end = sql.lastIndexOf(')');
        String condition = sql.substring(start, end + 1);

        if (tagQuery == null) {
            tagQuery = tagQueryFixed + " and m.mid in" + condition;
            finalQuery = finalQueryFixed + " mid in" + condition;
        } else {
            tagQuery = tagQuery.substring(0, tagQuery.indexOf('(')) + condition;
            finalQuery = finalQuery.substring(0, finalQuery.indexOf('(')) + condition;
        }

    }

    //get compare operator of combo box
    private String getCmp(int state) {
        String cmp = null;
        switch (state) {
            case 0:
                cmp = "=";
                break;
            case 1:
                cmp = ">";
                break;
            case 2:
                cmp = ">=";
                break;
            case 3:
                cmp = "<";
                break;
            case 4:
                cmp = "<=";
                break;
        }
        return cmp;
    }

    private void showTag() {
        String forAnd = " group by mid having count(*)=";

        setQuery(locationQuery);

        //check selected location
        int locationSelected = 0;
        String locationCondition = "";
        for (int j = 0; j < num_locations; j++) {
            if (locations[j].isSelected()) {
                locationCondition += "'" + locations[j].getText() + "',";
                locationSelected++;
            }
        }

        //add location condition for tagQuery and finalQuery
        if (locationSelected != 0) {
            tagQuery = tagQuery.substring(0, tagQuery.lastIndexOf(')'));
            tagQuery += " intersect select mid from movie_locations where country in";
            locationCondition = "(" + locationCondition.substring(0, locationCondition.length() - 1) + ")";
            tagQuery += locationCondition;

            finalQuery = finalQuery.substring(0, finalQuery.lastIndexOf(')'));
            if (finalQuery.indexOf("any(") != -1) {
                finalQuery = finalQuery.substring(0, finalQuery.lastIndexOf(')'));
            }
            finalQuery += " intersect select mid from movie_locations where country in";
            finalQuery += locationCondition;

            forAnd += Integer.toString(locationSelected);

            if (isAnd) {
                tagQuery += forAnd + ")";
                finalQuery += forAnd + ")";
            } else {
                tagQuery += ")";
                finalQuery += ")";
            }

        }

        queryTags();
    }

    private void queryTags() {
        Connection conn = null;
        ResultSet rs = null;
        Statement stmt = null;
        jTextAreaTag.setText(null);
        jTextAreaQuery.setText(null);

        if (tagQuery == null) {
            return;
        }

        //get query without additional conditions first
        tagQuery = tagQuery.substring(0, tagQuery.lastIndexOf(')') + 1);
        finalQuery = finalQuery.substring(0, finalQuery.lastIndexOf(')'));
        if (finalQuery.indexOf("any(") != -1) {
            finalQuery = finalQuery.substring(0, finalQuery.lastIndexOf(')') + 1);
        } else {
            finalQuery += ")";
        }

        //add additional conditions
        if (jTextFieldRatingValue.getText() != null) {
            try {
                Float.parseFloat(jTextFieldRatingValue.getText());
                tagQuery += " and m.all_critic_rating" + getCmp(jComboBoxRatingCmp.getSelectedIndex()) + jTextFieldRatingValue.getText();
                finalQuery += " and all_critic_rating" + getCmp(jComboBoxRatingCmp.getSelectedIndex()) + jTextFieldRatingValue.getText();
            } catch (Exception e) {
            }
        }

        if (jTextFieldReview.getText() != null) {
            try {
                Integer.parseInt(jTextFieldReview.getText());
                tagQuery += " and m.all_critic_num_reviews" + getCmp(jComboBoxReviewCmp.getSelectedIndex()) + jTextFieldReview.getText();
                finalQuery += " and all_critic_num_reviews" + getCmp(jComboBoxReviewCmp.getSelectedIndex()) + jTextFieldReview.getText();
            } catch (Exception e) {
            }
        }

        if (jTextFieldYearFrom.getText() != null) {
            try {
                Integer.parseInt(jTextFieldYearFrom.getText());
                tagQuery += " and m.year>=" + jTextFieldYearFrom.getText();
                finalQuery += " and year>=" + jTextFieldYearFrom.getText();
            } catch (Exception e) {
            }
        }

        if (jTextFieldYearTo.getText() != null) {
            try {
                Integer.parseInt(jTextFieldYearTo.getText());
                tagQuery += " and m.year<=" + jTextFieldYearTo.getText();
                finalQuery += " and year<=" + jTextFieldYearTo.getText();
            } catch (Exception e) {
            }
        }

        tagQuery += " order by t.tagtext";
        finalQuery += " order by year desc, title";

        updateFinalQuery();

        //execute query and display result
        try {
            conn = openConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(tagQuery);

            if (rs != null) {
                if (rs.next()) {
                    jTextAreaTag.setText(rs.getString(1));
                }
                while (rs.next()) {
                    jTextAreaTag.append(", " + rs.getString(1));
                }

                jTextAreaTag.setCaretPosition(0);
            }

        } catch (SQLException e) {
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the database driver");
        } finally {
            closeConnection(conn);
        }

    }

    //add tag weight condition to finalQuery
    private void updateFinalQuery() {
        String twCondition = "any(select mt.tagweight from movie_tags mt where mid=mt.mid) ";
        jTextAreaQuery.setText(null);

        if (finalQuery == null) {
            return;
        }

        //remove previous condition if any
        if (finalQuery.indexOf("any(") != -1) {
            finalQuery = finalQuery.substring(0, finalQuery.lastIndexOf("and"));
        } else {
            finalQuery = finalQuery.substring(0, finalQuery.lastIndexOf("order"));
        }

        if (jTextFieldWeightValue.getText() != null) {
            try {
                Integer.parseInt(jTextFieldWeightValue.getText());
                finalQuery += "and " + jTextFieldWeightValue.getText() + getCmp(jComboBoxWeightCmp.getSelectedIndex()) + twCondition;
            } catch (Exception e) {
            }
        }

        finalQuery += "order by year desc, title";
        jTextAreaQuery.setText(finalQuery);
    }

    private void showResult() {

        Connection conn = null;
        ResultSet rs = null;
        ResultSet rsCountry = null;
        ResultSet rsLocation = null;
        ResultSet rsGenre = null;
        Statement stmt = null;
        PreparedStatement psCountry = null;
        PreparedStatement psLocation = null;
        PreparedStatement psGenre = null;

        jTextAreaResult.setText("");

        if (finalQuery == null) {
            jTextAreaResult.setText("need more searching criteria");
            return;
        }

        int mid = 0;    //movie id

        //execute query and display result
        try {
            conn = openConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(finalQuery);
            psCountry = conn.prepareStatement("select country from movie_countries where mid=? order by country");
            psGenre = conn.prepareStatement("select genre from movie_genres where mid=? order by genre");
            psLocation = conn.prepareStatement("select country from movie_locations where mid=? order by country");

            if (rs != null && rs.next()) {
                do {
                    mid = rs.getInt(1);

                    jTextAreaResult.append("Title: " + rs.getString(2) + "\n");
                    jTextAreaResult.append("Year: " + Integer.toString(rs.getInt(3)) + "\n");

                    //query country and display
                    jTextAreaResult.append("Country: ");
                    psCountry.setInt(1, mid);
                    rsCountry = psCountry.executeQuery();
                    String country = "";
                    if (rsCountry != null) {
                        while (rsCountry.next()) {
                            country += rsCountry.getString(1) + ", ";
                        }
                    }
                    if (!country.equals("")) {
                        country = country.substring(0, country.length() - 2);
                    }
                    jTextAreaResult.append(country + "\n");

                    //query genre and display
                    jTextAreaResult.append("Genre: ");
                    psGenre.setInt(1, mid);
                    rsGenre = psGenre.executeQuery();
                    String genre = "";
                    if (rsGenre != null) {
                        while (rsGenre.next()) {
                            genre += rsGenre.getString(1) + ", ";
                        }
                    }
                    if (!genre.equals("")) {
                        genre = genre.substring(0, genre.length() - 2);
                    }
                    jTextAreaResult.append(genre + "\n");

                    //query filming location and display
                    jTextAreaResult.append("Filming Country: ");
                    psLocation.setInt(1, mid);
                    rsLocation = psLocation.executeQuery();
                    String location = "";
                    if (rsLocation != null) {
                        while (rsLocation.next()) {
                            location += rsLocation.getString(1) + ", ";
                        }
                    }
                    if (!location.equals("")) {
                        location = location.substring(0, location.length() - 2);
                    }
                    jTextAreaResult.append(location + "\n");

                    jTextAreaResult.append("Average of Rotten Tomato All Critics Rating: ");
                    try {
                        jTextAreaResult.append(Float.toString(rs.getFloat(4)) + "\n");
                    } catch (Exception e) {
                        jTextAreaResult.append("\n");
                    }

                    jTextAreaResult.append("Rotten Tomato All Critics Number of Reviews: ");
                    try {
                        jTextAreaResult.append(Integer.toString(rs.getInt(5)) + "\n");
                    } catch (Exception e) {
                        jTextAreaResult.append("\n");
                    }

                    jTextAreaResult.append("Average of Rotten Tomato Top Critics Rating: ");
                    try {
                        jTextAreaResult.append(Float.toString(rs.getFloat(6)) + "\n");
                    } catch (Exception e) {
                        jTextAreaResult.append("\n");
                    }

                    jTextAreaResult.append("Rotten Tomato Top Critics Number of Reviews: ");
                    try {
                        jTextAreaResult.append(Integer.toString(rs.getInt(7)) + "\n");
                    } catch (Exception e) {
                        jTextAreaResult.append("\n");
                    }

                    jTextAreaResult.append("Average of Rotten Tomato Audience Rating: ");
                    try {
                        jTextAreaResult.append(Float.toString(rs.getFloat(8)) + "\n");
                    } catch (Exception e) {
                        jTextAreaResult.append("\n");
                    }

                    jTextAreaResult.append("Rotten Tomato Audience Number of Rating: ");
                    try {
                        jTextAreaResult.append(Integer.toString(rs.getInt(9)) + "\n");
                    } catch (Exception e) {
                        jTextAreaResult.append("\n");
                    }

                    jTextAreaResult.append("\n");
                } while (rs.next());
                jTextAreaResult.setCaretPosition(0);

            } else {
                jTextAreaResult.setText("No movie satisfies the searching criteria.");
            }

        } catch (SQLException e) {
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the database driver");
        } finally {
            closeConnection(conn);
        }

    }

    private Connection openConnection() throws SQLException, ClassNotFoundException {
        Class.forName("oracle.jdbc.driver.OracleDriver");   //load driver
        String url = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";   //Oracle url
        String username = "scott";  //username
        String password = "tiger";  //password
        return DriverManager.getConnection(url, username, password); //get connection
    }

    private void closeConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println("Cannot close connection: " + e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelHeader = new javax.swing.JLabel();
        jScrollPaneGenre = new javax.swing.JScrollPane();
        jPanelGenre = new javax.swing.JPanel();
        jLabelGenre = new javax.swing.JLabel();
        jLabelCountry = new javax.swing.JLabel();
        jLabelLocation = new javax.swing.JLabel();
        jScrollCountry = new javax.swing.JScrollPane();
        jPanelCountry = new javax.swing.JPanel();
        jScrollLocation = new javax.swing.JScrollPane();
        jPanelLocation = new javax.swing.JPanel();
        jLabelRating = new javax.swing.JLabel();
        jLabelTag = new javax.swing.JLabel();
        jLabelYear = new javax.swing.JLabel();
        jScrollPaneResult = new javax.swing.JScrollPane();
        jTextAreaResult = new javax.swing.JTextArea();
        jScrollPaneQuery = new javax.swing.JScrollPane();
        jTextAreaQuery = new javax.swing.JTextArea();
        jComboBoxIsAnd = new javax.swing.JComboBox<>();
        jLabelIsAnd = new javax.swing.JLabel();
        jPanelRating = new javax.swing.JPanel();
        jComboBoxRatingCmp = new javax.swing.JComboBox<>();
        jLabelRatingCmp = new javax.swing.JLabel();
        jTextFieldRatingValue = new javax.swing.JTextField();
        jComboBoxReviewCmp = new javax.swing.JComboBox<>();
        jLabelReview = new javax.swing.JLabel();
        jTextFieldReview = new javax.swing.JTextField();
        jLabelReviewValue = new javax.swing.JLabel();
        jLabelRatingValue = new javax.swing.JLabel();
        jButtonExecute = new javax.swing.JButton();
        jScrollPaneTag = new javax.swing.JScrollPane();
        jTextAreaTag = new javax.swing.JTextArea();
        jComboBoxWeightCmp = new javax.swing.JComboBox<>();
        jTextFieldWeightValue = new javax.swing.JTextField();
        jLabelTagWeight = new javax.swing.JLabel();
        jLabelWeightValue = new javax.swing.JLabel();
        jLabelResult = new javax.swing.JLabel();
        jLabelYearFrom = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldYearFrom = new javax.swing.JTextField();
        jTextFieldYearTo = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabelHeader.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabelHeader.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelHeader.setText("Movie");
        jLabelHeader.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanelGenre.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        javax.swing.GroupLayout jPanelGenreLayout = new javax.swing.GroupLayout(jPanelGenre);
        jPanelGenre.setLayout(jPanelGenreLayout);
        jPanelGenreLayout.setHorizontalGroup(
            jPanelGenreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );
        jPanelGenreLayout.setVerticalGroup(
            jPanelGenreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 431, Short.MAX_VALUE)
        );

        jScrollPaneGenre.setViewportView(jPanelGenre);

        jLabelGenre.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabelGenre.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelGenre.setText("Genres");
        jLabelGenre.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabelCountry.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabelCountry.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelCountry.setText("Country");
        jLabelCountry.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabelLocation.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabelLocation.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelLocation.setText("Filming Location");
        jLabelLocation.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanelCountryLayout = new javax.swing.GroupLayout(jPanelCountry);
        jPanelCountry.setLayout(jPanelCountryLayout);
        jPanelCountryLayout.setHorizontalGroup(
            jPanelCountryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 153, Short.MAX_VALUE)
        );
        jPanelCountryLayout.setVerticalGroup(
            jPanelCountryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 664, Short.MAX_VALUE)
        );

        jScrollCountry.setViewportView(jPanelCountry);

        javax.swing.GroupLayout jPanelLocationLayout = new javax.swing.GroupLayout(jPanelLocation);
        jPanelLocation.setLayout(jPanelLocationLayout);
        jPanelLocationLayout.setHorizontalGroup(
            jPanelLocationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 153, Short.MAX_VALUE)
        );
        jPanelLocationLayout.setVerticalGroup(
            jPanelLocationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 664, Short.MAX_VALUE)
        );

        jScrollLocation.setViewportView(jPanelLocation);

        jLabelRating.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabelRating.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelRating.setText("Critics' Rating");
        jLabelRating.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabelTag.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabelTag.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTag.setText("Movie Tag Values");
        jLabelTag.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabelYear.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabelYear.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelYear.setText("Movie Year");
        jLabelYear.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jTextAreaResult.setColumns(20);
        jTextAreaResult.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jTextAreaResult.setRows(5);
        jScrollPaneResult.setViewportView(jTextAreaResult);

        jScrollPaneQuery.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jTextAreaQuery.setColumns(20);
        jTextAreaQuery.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jTextAreaQuery.setRows(5);
        jScrollPaneQuery.setViewportView(jTextAreaQuery);

        jComboBoxIsAnd.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jComboBoxIsAnd.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AND", "OR" }));
        jComboBoxIsAnd.setToolTipText("");
        jComboBoxIsAnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxIsAndActionPerformed(evt);
            }
        });

        jLabelIsAnd.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        jLabelIsAnd.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelIsAnd.setText("Search Between Attributes' Values:");

        jPanelRating.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jComboBoxRatingCmp.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jComboBoxRatingCmp.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "=", ">", ">=", "<", "<=" }));
        jComboBoxRatingCmp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxRatingCmpActionPerformed(evt);
            }
        });

        jLabelRatingCmp.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabelRatingCmp.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelRatingCmp.setText("Rating:");

        jTextFieldRatingValue.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jTextFieldRatingValue.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldRatingValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldRatingValueActionPerformed(evt);
            }
        });

        jComboBoxReviewCmp.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jComboBoxReviewCmp.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "=", ">", ">=", "<", "<=" }));
        jComboBoxReviewCmp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxReviewCmpActionPerformed(evt);
            }
        });

        jLabelReview.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabelReview.setText("Reviews:");

        jTextFieldReview.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jTextFieldReview.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldReview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldReviewActionPerformed(evt);
            }
        });

        jLabelReviewValue.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabelReviewValue.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelReviewValue.setText("Value:");

        jLabelRatingValue.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabelRatingValue.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelRatingValue.setText("Value:");

        javax.swing.GroupLayout jPanelRatingLayout = new javax.swing.GroupLayout(jPanelRating);
        jPanelRating.setLayout(jPanelRatingLayout);
        jPanelRatingLayout.setHorizontalGroup(
            jPanelRatingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRatingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelRatingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelRatingLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanelRatingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelRatingValue, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabelRatingCmp, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelRatingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextFieldRatingValue)
                            .addComponent(jComboBoxRatingCmp, 0, 86, Short.MAX_VALUE)))
                    .addGroup(jPanelRatingLayout.createSequentialGroup()
                        .addGroup(jPanelRatingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabelReview)
                            .addComponent(jLabelReviewValue))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelRatingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldReview, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jComboBoxReviewCmp, 0, 84, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanelRatingLayout.setVerticalGroup(
            jPanelRatingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRatingLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanelRatingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxRatingCmp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelRatingCmp))
                .addGap(18, 18, 18)
                .addGroup(jPanelRatingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldRatingValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelRatingValue))
                .addGap(52, 52, 52)
                .addGroup(jPanelRatingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxReviewCmp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelReview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanelRatingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldReview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelReviewValue))
                .addContainerGap(39, Short.MAX_VALUE))
        );

        jButtonExecute.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jButtonExecute.setText("Execute Query");
        jButtonExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExecuteActionPerformed(evt);
            }
        });

        jScrollPaneTag.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jTextAreaTag.setColumns(20);
        jTextAreaTag.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jTextAreaTag.setRows(5);
        jScrollPaneTag.setViewportView(jTextAreaTag);

        jComboBoxWeightCmp.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jComboBoxWeightCmp.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "=", "<", "<=", ">", ">=" }));
        jComboBoxWeightCmp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxWeightCmpActionPerformed(evt);
            }
        });

        jTextFieldWeightValue.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jTextFieldWeightValue.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldWeightValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldWeightValueActionPerformed(evt);
            }
        });

        jLabelTagWeight.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabelTagWeight.setText("Tag Weight:");

        jLabelWeightValue.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabelWeightValue.setText("Value:");

        jLabelResult.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabelResult.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelResult.setText("Result");
        jLabelResult.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabelYearFrom.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabelYearFrom.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelYearFrom.setText("From:");

        jLabel2.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel2.setText("To:");

        jTextFieldYearFrom.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jTextFieldYearFrom.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldYearFrom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldYearFromActionPerformed(evt);
            }
        });

        jTextFieldYearTo.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jTextFieldYearTo.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldYearTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldYearToActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jScrollPaneQuery, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(jLabelGenre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(jScrollPaneGenre, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(jScrollCountry, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                                                    .addComponent(jLabelCountry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                            .addComponent(jLabelIsAnd, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jComboBoxIsAnd, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jScrollLocation, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                                            .addComponent(jLabelLocation, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(188, 188, 188)
                                .addComponent(jButtonExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPaneResult, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabelRating, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jPanelRating, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabelYear, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelTag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jScrollPaneTag)))
                            .addComponent(jLabelResult, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabelYearFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextFieldYearFrom))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextFieldYearTo)))
                                .addGap(31, 31, 31)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabelTagWeight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabelWeightValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jComboBoxWeightCmp, 0, 127, Short.MAX_VALUE)
                                    .addComponent(jTextFieldWeightValue))
                                .addGap(49, 49, 49)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelHeader)
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelGenre, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelCountry, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelRating, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTag, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPaneGenre, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                        .addComponent(jScrollCountry, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(jScrollLocation, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelRating, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelYear, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, 38)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jTextFieldYearTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPaneTag, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBoxWeightCmp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelTagWeight)
                            .addComponent(jTextFieldYearFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelYearFrom))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelWeightValue)
                            .addComponent(jTextFieldWeightValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabelIsAnd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBoxIsAnd)
                        .addComponent(jLabelResult, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPaneQuery, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPaneResult))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxIsAndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxIsAndActionPerformed
        // TODO add your handling code here:
        if (evt.getSource() == jComboBoxIsAnd) {
            int index = jComboBoxIsAnd.getSelectedIndex();
            if (index == 0) {
                isAnd = true;
            } else {
                isAnd = false;
            }

            //if just one selected, and/or will have the same result, so no need to change
            if (genreSelected == 1 && countrySelected == 1) {
                showTag();
            } else if (genreSelected == 1) {
                showLocation();
            } else {
                showCountry();
            }

        }
    }//GEN-LAST:event_jComboBoxIsAndActionPerformed

    private void jTextFieldRatingValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldRatingValueActionPerformed
        // TODO add your handling code here:
        queryTags();
    }//GEN-LAST:event_jTextFieldRatingValueActionPerformed

    private void jComboBoxRatingCmpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxRatingCmpActionPerformed
        // TODO add your handling code here:
        queryTags();
    }//GEN-LAST:event_jComboBoxRatingCmpActionPerformed

    private void jComboBoxReviewCmpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxReviewCmpActionPerformed
        // TODO add your handling code here:
        queryTags();
    }//GEN-LAST:event_jComboBoxReviewCmpActionPerformed

    private void jTextFieldReviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldReviewActionPerformed
        // TODO add your handling code here:
        queryTags();
    }//GEN-LAST:event_jTextFieldReviewActionPerformed

    private void jButtonExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExecuteActionPerformed
        // TODO add your handling code here:
        showResult();
    }//GEN-LAST:event_jButtonExecuteActionPerformed

    private void jTextFieldYearFromActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldYearFromActionPerformed
        // TODO add your handling code here:
        queryTags();
    }//GEN-LAST:event_jTextFieldYearFromActionPerformed

    private void jTextFieldYearToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldYearToActionPerformed
        // TODO add your handling code here:
        queryTags();
    }//GEN-LAST:event_jTextFieldYearToActionPerformed

    private void jComboBoxWeightCmpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxWeightCmpActionPerformed
        // TODO add your handling code here:
        updateFinalQuery();
    }//GEN-LAST:event_jComboBoxWeightCmpActionPerformed

    private void jTextFieldWeightValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldWeightValueActionPerformed
        // TODO add your handling code here:
        updateFinalQuery();
    }//GEN-LAST:event_jTextFieldWeightValueActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MovieSearch().setVisible(true);
            }
        });

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExecute;
    private javax.swing.JComboBox<String> jComboBoxIsAnd;
    private javax.swing.JComboBox<String> jComboBoxRatingCmp;
    private javax.swing.JComboBox<String> jComboBoxReviewCmp;
    private javax.swing.JComboBox<String> jComboBoxWeightCmp;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelCountry;
    private javax.swing.JLabel jLabelGenre;
    private javax.swing.JLabel jLabelHeader;
    private javax.swing.JLabel jLabelIsAnd;
    private javax.swing.JLabel jLabelLocation;
    private javax.swing.JLabel jLabelRating;
    private javax.swing.JLabel jLabelRatingCmp;
    private javax.swing.JLabel jLabelRatingValue;
    private javax.swing.JLabel jLabelResult;
    private javax.swing.JLabel jLabelReview;
    private javax.swing.JLabel jLabelReviewValue;
    private javax.swing.JLabel jLabelTag;
    private javax.swing.JLabel jLabelTagWeight;
    private javax.swing.JLabel jLabelWeightValue;
    private javax.swing.JLabel jLabelYear;
    private javax.swing.JLabel jLabelYearFrom;
    private javax.swing.JPanel jPanelCountry;
    private javax.swing.JPanel jPanelGenre;
    private javax.swing.JPanel jPanelLocation;
    private javax.swing.JPanel jPanelRating;
    private javax.swing.JScrollPane jScrollCountry;
    private javax.swing.JScrollPane jScrollLocation;
    private javax.swing.JScrollPane jScrollPaneGenre;
    private javax.swing.JScrollPane jScrollPaneQuery;
    private javax.swing.JScrollPane jScrollPaneResult;
    private javax.swing.JScrollPane jScrollPaneTag;
    private javax.swing.JTextArea jTextAreaQuery;
    private javax.swing.JTextArea jTextAreaResult;
    private javax.swing.JTextArea jTextAreaTag;
    private javax.swing.JTextField jTextFieldRatingValue;
    private javax.swing.JTextField jTextFieldReview;
    private javax.swing.JTextField jTextFieldWeightValue;
    private javax.swing.JTextField jTextFieldYearFrom;
    private javax.swing.JTextField jTextFieldYearTo;
    // End of variables declaration//GEN-END:variables
}
