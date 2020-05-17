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

    public int getNumFree(String library) {
        ResultSet rs = null;
        int freeSeats = 0;
        try {
            rs = statement.executeQuery("select * from tablesdb." + library);
            while (rs.next()) {
                freeSeats += rs.getInt("freeseats");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return freeSeats;
    }

    public void takeTable(String string) {
        PreparedStatement preparedStmt = null;
        int oldValue = 0;
        String query = "select * from tablesdb.science where id = ?";
        try {
            preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, string);
            ResultSet rs = preparedStmt.executeQuery();
            while (rs.next()) {
                oldValue = rs.getInt("freeseats");
            }
            System.out.println(oldValue);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        int newValue = oldValue - 1;
        String update = "update tablesdb.science set freeseats = ? where id = ?";

        try {
            preparedStmt = connection.prepareStatement(update);
            preparedStmt.setInt(1, newValue);
            preparedStmt.setString(2, string);
            preparedStmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void leaveTable(String string) {
        PreparedStatement preparedStmt = null;
        int oldValue = 0;
        String query = "select * from tablesdb.science where id = ?";
        try {
            preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, string);
            ResultSet rs = preparedStmt.executeQuery();
            while (rs.next()) {
                oldValue = rs.getInt("freeseats");
            }
            System.out.println(oldValue);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        int newValue = oldValue + 1;
        String update = "update tablesdb.science set freeseats = ? where id = ?";

        try {
            preparedStmt = connection.prepareStatement(update);
            preparedStmt.setInt(1, newValue);
            preparedStmt.setString(2, string);
            preparedStmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public Statement getStatement() {
        return this.statement;
    }
}
