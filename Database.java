import java.sql.*;

public class Database {

    private Connection connection;
    private Statement statement;

    public Database() {
        String url = "jdbc:mysql://localhost:3306/tablesdb?useTimezone=true&serverTimezone=UTC";
        String user = "telegram";
        String password = "telegram";
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
            System.out.println("Database initialised!");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public String displayAllLibraries() {
        String result = "";
        int numFree = 0;
        String currentLibrary = "central";
        try {
            ResultSet rs = statement
                    .executeQuery("select * from tablesdb.overall where taken = 0");
            while (rs.next()) {
                String library = rs.getString("venue");
                if (!library.equals(currentLibrary)) {
                    result += "No. of free seats at " + currentLibrary + " library: " +
                            numFree + "\n";
                    currentLibrary = library;
                    numFree = 0;
                }
                numFree++;
            }
            result += "No. of free seats at " + currentLibrary + " library: " +
                    numFree;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    public boolean takeSeat(String seatId) {
        PreparedStatement preparedStmt = null;
        boolean success = false;
        int taken = 0;

        String query = "select * from tablesdb.overall where id = ?";
        try {
            preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, seatId);
            ResultSet rs = preparedStmt.executeQuery();
            while (rs.next()) {
                taken = rs.getInt("taken");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        if (taken == 0) {
            String update = "update tablesdb.overall set taken = 1 where id = ?";
            try {
                preparedStmt = connection.prepareStatement(update);
                preparedStmt.setString(1, seatId);
                preparedStmt.executeUpdate();
                success = true;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return success;
    }

    public void leaveSeat(String seatId) {
        String update = "update tablesdb.overall set taken = 0 where id = ?";
        try {
            PreparedStatement preparedStmt = connection.prepareStatement(update);
            preparedStmt.setString(1, seatId);
            preparedStmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public int queryFreeSeats(String queryLibrary, int floor) {
        String query = "select * from tablesdb.overall where venue = ? and floor = ? and taken = 0";
        int sum = 0;
        try {
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, queryLibrary);
            preparedStmt.setInt(2, floor);
            ResultSet rs = preparedStmt.executeQuery();
            while (rs.next()) {
                sum++;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return sum;
    }

    public String getFreeSeatsByTable(String queryLibrary, int floor) {
        String query = "select * from tablesdb.overall where venue = ? and floor = ? and taken = 0";
        String acc = "";
        int prevTableId = 0;

        try {
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, queryLibrary);
            preparedStmt.setInt(2, floor);
            ResultSet rs = preparedStmt.executeQuery();

            while (rs.next()) {
                int currentTableId = rs.getInt("table_id");
                if (currentTableId != prevTableId)  {
                    prevTableId = currentTableId;
                    acc += "\nTable " + currentTableId + ": ";
                    acc += rs.getString("id");
                } else {
                    acc += ", " + rs.getString("id");
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return acc;
    }

    public Statement getStatement() {
        return this.statement;
    }
}
