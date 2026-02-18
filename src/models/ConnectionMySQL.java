package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionMySQL {
    
    private String database_name = "ferreteria_db"; // antes pharmacy_database
    private String user = "root";
    private String password = "TU_PASSWORD";
    private String url = "jdbc:mysql://localhost:3306/" + database_name + "?useSSL=false&serverTimezone=UTC";
    Connection conn = null;
    
    public Connection getConnection(){
        try{
            //Objtener valor del driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            //Obtener la conexion
            conn = DriverManager.getConnection(url, user, password);
        }catch(ClassNotFoundException e){
            System.err.println("Ha ocurrido un ClassNotFoundException " + e.getMessage());
        }catch(SQLException e){
            System.err.println("Ha ocurrido un SQLException " + e.getMessage());
        }
        return conn;
    }
}
