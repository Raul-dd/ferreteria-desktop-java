package models;

public class SalesDetail {
    private String sale_date;
    private String product_name;
    private int sale_quantity;
    private double sale_price;
    private double sale_subtotal;
    private String customer_name;
    private String employee_name;

    public SalesDetail() {}

    public String getSale_date() { return sale_date; }
    public void setSale_date(String sale_date) { this.sale_date = sale_date; }

    public String getProduct_name() { return product_name; }
    public void setProduct_name(String product_name) { this.product_name = product_name; }

    public int getSale_quantity() { return sale_quantity; }
    public void setSale_quantity(int sale_quantity) { this.sale_quantity = sale_quantity; }

    public double getSale_price() { return sale_price; }
    public void setSale_price(double sale_price) { this.sale_price = sale_price; }

    public double getSale_subtotal() { return sale_subtotal; }
    public void setSale_subtotal(double sale_subtotal) { this.sale_subtotal = sale_subtotal; }

    public String getCustomer_name() { return customer_name; }
    public void setCustomer_name(String customer_name) { this.customer_name = customer_name; }

    public String getEmployee_name() { return employee_name; }
    public void setEmployee_name(String employee_name) { this.employee_name = employee_name; }
}
