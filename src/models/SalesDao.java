package models;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class SalesDao {

    //Instanciar la conexion
    ConnectionMySQL cn = new ConnectionMySQL();
    Connection conn;
    PreparedStatement pst;
    ResultSet rs;

    //Registrar una venta
    public boolean registerSaleQuery(int customer_id, int employee_id, double total) {
        String query = "INSERT INTO sales (customer_id, employee_id, total, sale_date) VALUES (?,?,?,?)";
        Timestamp datatime = new Timestamp(new Date().getTime());

        try {
            conn = cn.getConnection();
            pst = conn.prepareStatement(query);
            pst.setInt(1, customer_id);
            pst.setInt(2, employee_id);
            pst.setDouble(3, total);
            pst.setTimestamp(4, datatime);
            pst.execute();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return false;
        }
    }

    //Registrar detalles de la venta
    public boolean registerSaleDetailQuery(int product_id, double sale_id, int sale_quantity, double sale_price, double sale_subtotal) {
        String query = "INSERT INTO sale_details (product_id, sale_id, sale_quantity, sale_price, sale_subtotal) VALUES (?,?,?,?,?)";
        Timestamp datetime = new Timestamp(new Date().getTime());

        try {
            conn = cn.getConnection();
            pst = conn.prepareStatement(query);
            pst.setInt(1, product_id);
            pst.setDouble(2, sale_id);
            pst.setInt(3, sale_quantity);
            pst.setDouble(4, sale_price);
            pst.setDouble(5, sale_subtotal);
            pst.execute();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.toString());
            return false;
        }
    }

    //Obtener id maximo de la venta y relacionar la tabla sales y sale_details
    public int saleId() {
        int id = 0;
        String query = "SELECT MAX(id) AS id FROM sales";

        try {
            conn = cn.getConnection();
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return id;
    }

    //Listar ventas realizadas
    public List listAllSalesQuery() {
        List<Sales> list_sales = new ArrayList();
        String query
                = "SELECT s.id AS invoice, c.full_name AS customer, e.full_name AS employee, s.total, s.sale_date "
                + "FROM sales s "
                + "INNER JOIN customers c ON s.customer_id = c.id "
                + "INNER JOIN employees e ON s.employee_id = e.id "
                + "ORDER BY s.id ASC";
        try {
            conn = cn.getConnection();
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            //Recorrer
            while (rs.next()) {
                Sales sale = new Sales();
                sale.setId(rs.getInt("invoice"));
                sale.setCustomer_name(rs.getString("customer"));
                sale.setEmployee_name(rs.getString("employee"));
                sale.setTotal_to_pay(rs.getDouble("total"));
                sale.setSale_date(rs.getString("sale_date"));

                list_sales.add(sale);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.toString());
        }
        return list_sales;
    }

    //Listar todas las ventas (reportes)
    public List<SalesDetail> listSalesDetailQuery(int id) {
        List<SalesDetail> list_sales = new ArrayList<>();

        String query
                = "SELECT sa.sale_date, sd.sale_price, sd.sale_quantity, sd.sale_subtotal, "
                + "       pro.name AS product_name, "
                + "       em.full_name AS employee_name, "
                + "       cu.full_name AS customer_name "
                + "FROM sales sa "
                + "INNER JOIN sale_details sd ON sa.id = sd.sale_id "
                + "INNER JOIN products pro ON sd.product_id = pro.id "
                + "INNER JOIN customers cu ON sa.customer_id = cu.id "
                + "INNER JOIN employees em ON sa.employee_id = em.id "
                + "WHERE sa.id = ?";

        try {
            conn = cn.getConnection();
            pst = conn.prepareStatement(query);
            pst.setInt(1, id);
            rs = pst.executeQuery();

            while (rs.next()) {
                SalesDetail detail = new SalesDetail();
                detail.setProduct_name(rs.getString("product_name"));
                detail.setSale_quantity(rs.getInt("sale_quantity"));
                detail.setSale_price(rs.getDouble("sale_price"));
                detail.setSale_subtotal(rs.getDouble("sale_subtotal"));
                detail.setCustomer_name(rs.getString("customer_name"));
                detail.setEmployee_name(rs.getString("employee_name"));
                detail.setSale_date(rs.getString("sale_date"));
                list_sales.add(detail);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        return list_sales;
    }

}
