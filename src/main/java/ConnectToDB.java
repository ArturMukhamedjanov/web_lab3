import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ApplicationScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@ManagedBean
@ApplicationScoped
public class ConnectToDB {
    private final Properties properties = new Properties();

    private DataSource dataSource;
   /* private InputStream fis = ConnectToDB.class.getResourceAsStream("/config.properties");
    private String dbUrl;
    private String user;
    private String pass;*/

    private Connection connection;

    @PostConstruct
    public void init() throws NamingException, IOException {
        initConnection();
    }

    private void initConnection() throws NamingException, IOException {
        Context ctx = new InitialContext();
        dataSource = (DataSource) ctx.lookup("java:jboss/Lab3");
        /*properties.load(fis);
        dbUrl = properties.getProperty("DB.URL");
        user = properties.getProperty("DB.USER");
        pass = properties.getProperty("DB.PASSWORD");*/
        try {
            connection = dataSource.getConnection();
            //connection = DriverManager.getConnection(dbUrl, user, pass);
            //connection = DatabaseCommunicator.getConnection();
            connection.createStatement().execute(
                    "create table if not exists results (" +
                            "x float , y float, r float, res text, owner text)"
            );
        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't create connection", e);
        }
    }

    public void addPointToTable(Point point){
        try {
            if (connection == null)
                initConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO results VALUES (?, ?, ?, ?, ?)"
            );
            preparedStatement.setDouble(1,point.getX());
            preparedStatement.setDouble(2,point.getY());
            preparedStatement.setFloat(3,point.getR());
            preparedStatement.setString(4,point.getRes());
            preparedStatement.setString(5,point.getOwner());
            preparedStatement.execute();
        } catch (NamingException | SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public List<Point> getPoints(String session_id){
        List<Point> pointsList = new ArrayList<>();
        try {
            if (connection == null)
                initConnection();
            ResultSet rs = connection.createStatement().executeQuery("select * from results");
            while (rs.next()) {
                Point point = new Point();
                point.setX(rs.getFloat("x"));
                point.setY(rs.getFloat("y"));
                point.setR(rs.getFloat("r"));
                point.setRes(rs.getString("res"));
                if (rs.getString("owner").equals(session_id)) {
                    pointsList.add(0,point);
                }
            }
        }catch (SQLException | NamingException | IOException throwable) {
            throwable.printStackTrace();
        }
        return pointsList;
    }

    @PreDestroy
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }*/
}