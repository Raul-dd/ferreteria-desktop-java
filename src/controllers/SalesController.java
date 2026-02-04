package controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import models.Customers;
import models.CustomersDao;
import models.Products;
import models.ProductsDao;
import models.Sales;
import models.SalesDao;
import static models.EmployeesDao.id_user;
import static models.EmployeesDao.rol_user;
import views.PrintSale;
import views.SystemView;

public class SalesController implements ActionListener, MouseListener, KeyListener {

    private Sales sale;
    private SalesDao saleDao;
    private SystemView views;

    Products product = new Products();
    ProductsDao productDao = new ProductsDao();

    Customers customer = new Customers();
    CustomersDao customerDao = new CustomersDao();

    private int item = 0;
    String rol = rol_user;

    DefaultTableModel model = new DefaultTableModel();
    DefaultTableModel temp = new DefaultTableModel();

    public SalesController(Sales sale, SalesDao saleDao, SystemView views) {
        this.sale = sale;
        this.saleDao = saleDao;
        this.views = views;

        // Botones
        this.views.btn_confirm_sale.addActionListener(this);
        this.views.btn_new_sale.addActionListener(this);
        this.views.btn_remove_sale.addActionListener(this);
        this.views.btn_add_product_sale.addActionListener(this);

        // Labels de menú (ajusta nombres si son distintos)
        this.views.jLabelSales.addMouseListener(this);
        this.views.jLabelReports.addMouseListener(this);

        // Key listeners
        this.views.txt_sale_product_code.addKeyListener(this);
        this.views.txt_sale_customer_id.addKeyListener(this);
        this.views.txt_sale_quantity.addKeyListener(this);

        // Listar ventas (si aplica)
        listAllSales();
    }

    // KeyListener
    @Override
    public void keyPressed(KeyEvent e) {

        // Buscar producto por código (ENTER)
        if (e.getSource() == views.txt_sale_product_code) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                String codeTxt = views.txt_sale_product_code.getText().trim();

                if (codeTxt.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Ingrese el código del producto a vender");
                    return;
                }

                if (!codeTxt.matches("\\d+")) {
                    JOptionPane.showMessageDialog(null, "El código del producto debe ser numérico");
                    views.txt_sale_product_code.requestFocus();
                    return;
                }

                int code = Integer.parseInt(codeTxt);
                product = productDao.searchCode(code);

                if (product.getName() != null) {
                    views.txt_sale_product_name.setText(product.getName());
                    views.txt_sale_product_id.setText(String.valueOf(product.getId()));
                    views.txt_sale_stock.setText(String.valueOf(product.getProduct_quantity()));
                    views.txt_sale_price.setText(String.format("%.2f", product.getUnit_price()));
                    views.txt_sale_quantity.requestFocus();
                } else {
                    JOptionPane.showMessageDialog(null, "No existe ningún producto con ese código");
                    cleanFieldsSales();
                    views.txt_sale_product_code.requestFocus();
                }
            }
        }

        // Buscar cliente por cédula (ENTER)
        else if (e.getSource() == views.txt_sale_customer_id) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                String customerIdTxt = views.txt_sale_customer_id.getText().trim();

                if (customerIdTxt.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Ingrese la cédula del cliente");
                    return;
                }

                if (!customerIdTxt.matches("\\d+")) {
                    JOptionPane.showMessageDialog(null, "La cédula debe ser numérica");
                    views.txt_sale_customer_id.requestFocus();
                    return;
                }

                int customer_id = Integer.parseInt(customerIdTxt);
                customer = customerDao.searchCustomer(customer_id);

                if (customer.getFull_name() != null) {
                    views.txt_sale_customer_name.setText(customer.getFull_name());
                    views.txt_sale_product_code.requestFocus();
                } else {
                    views.txt_sale_customer_id.setText("");
                    views.txt_sale_customer_name.setText("");
                    JOptionPane.showMessageDialog(null, "El cliente no existe");
                    views.txt_sale_customer_id.requestFocus();
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        // Calcular subtotal al escribir cantidad
        if (e.getSource() == views.txt_sale_quantity) {

            String qtyTxt = views.txt_sale_quantity.getText().trim();
            String priceTxt = views.txt_sale_price.getText().trim();
            String stockTxt = views.txt_sale_stock.getText().trim();

            // Si aún no hay datos, limpiar subtotal y salir
            if (qtyTxt.isEmpty() || priceTxt.isEmpty() || stockTxt.isEmpty()) {
                views.txt_sale_subtotal.setText("");
                return;
            }

            // cantidad numérica
            if (!qtyTxt.matches("\\d+")) {
                views.txt_sale_subtotal.setText("");
                return;
            }

            int quantity = Integer.parseInt(qtyTxt);

            int stock;
            try {
                stock = Integer.parseInt(stockTxt);
            } catch (NumberFormatException ex) {
                views.txt_sale_subtotal.setText("");
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceTxt);
            } catch (NumberFormatException ex) {
                views.txt_sale_subtotal.setText("");
                return;
            }

            if (quantity <= 0) {
                views.txt_sale_subtotal.setText("");
                return;
            }

            if (quantity > stock) {
                JOptionPane.showMessageDialog(null, "Cantidad más alta de la que existe en stock");
                views.txt_sale_quantity.setText("");
                views.txt_sale_subtotal.setText("");
                views.txt_sale_quantity.requestFocus();
                return;
            }

            double subtotal = quantity * price;
            views.txt_sale_subtotal.setText(String.format("%.2f", subtotal));
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // No se usa
    }

    // ActionListener
    @Override
    public void actionPerformed(ActionEvent e) {

        // Confirmar venta
        if (e.getSource() == views.btn_confirm_sale) {
            insertSale();
        }

        // Nueva venta
        else if (e.getSource() == views.btn_new_sale) {
            cleanAllFieldsSales();
            cleanTableTemp();
            views.txt_sale_customer_id.requestFocus();
        }

        // Remover item de venta
        else if (e.getSource() == views.btn_remove_sale) {
            if (views.sales_table.getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(null, "Seleccione una fila para remover");
                return;
            }

            temp = (DefaultTableModel) views.sales_table.getModel();
            temp.removeRow(views.sales_table.getSelectedRow());
            views.sales_table.setModel(temp);

            calculateSales();
            views.txt_sale_product_code.requestFocus();
        }

        // Agregar producto a tabla
        else if (e.getSource() == views.btn_add_product_sale) {
            addProductToSaleTable();
        }
    }
    // MouseListener
    @Override
    public void mouseClicked(MouseEvent e) {

        if (e.getSource() == views.jLabelSales) {
            views.jTabbedPane10.setSelectedIndex(2); 
        }

        else if (e.getSource() == views.jLabelReports) {

            if (rol.equals("Administrador")) {
                views.jTabbedPane10.setSelectedIndex(7);
                cleanTable();
                listAllSales();
            } else {
                views.jTabbedPane10.setEnabledAt(7, false);
                views.jLabelReports.setEnabled(false);
                JOptionPane.showMessageDialog(null, "No tiene privilegios de administrador para acceder a esta vista");
            }
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // Lógica de ventas
    private void addProductToSaleTable() {

        String qtyTxt = views.txt_sale_quantity.getText().trim();
        String productName = views.txt_sale_product_name.getText().trim();
        String priceTxt = views.txt_sale_price.getText().trim();
        String productIdTxt = views.txt_sale_product_id.getText().trim();
        String stockTxt = views.txt_sale_stock.getText().trim();
        String fullName = views.txt_sale_customer_name.getText().trim();

        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Primero busque el cliente (cédula)");
            views.txt_sale_customer_id.requestFocus();
            return;
        }

        if (productName.isEmpty() || productIdTxt.isEmpty() || priceTxt.isEmpty() || stockTxt.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Primero busque el producto por código");
            views.txt_sale_product_code.requestFocus();
            return;
        }

        if (qtyTxt.isEmpty() || !qtyTxt.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "Ingrese una cantidad válida");
            views.txt_sale_quantity.requestFocus();
            return;
        }

        int amount = Integer.parseInt(qtyTxt);

        int stock = Integer.parseInt(stockTxt);
        if (amount <= 0) {
            JOptionPane.showMessageDialog(null, "La cantidad debe ser mayor a 0");
            return;
        }
        if (stock < amount) {
            JOptionPane.showMessageDialog(null, "Stock no disponible");
            return;
        }

        double price = Double.parseDouble(priceTxt);
        int product_id = Integer.parseInt(productIdTxt);

        // Evitar duplicados (comparando por ID del producto)
        temp = (DefaultTableModel) views.sales_table.getModel();
        for (int i = 0; i < views.sales_table.getRowCount(); i++) {
            int existingId = Integer.parseInt(views.sales_table.getValueAt(i, 0).toString());
            if (existingId == product_id) {
                JOptionPane.showMessageDialog(null, "El producto ya está registrado en la tabla de ventas");
                return;
            }
        }

        item++;

        double subtotal = amount * price;

        // Columnas esperadas: 0 idProducto, 1 nombreProducto, 2 cantidad, 3 precio, 4 subtotal, 5 cliente
        Object[] obj = new Object[6];
        obj[0] = product_id;
        obj[1] = productName;
        obj[2] = amount;
        obj[3] = String.format("%.2f", price);
        obj[4] = String.format("%.2f", subtotal);
        obj[5] = fullName;

        temp.addRow(obj);
        views.sales_table.setModel(temp);

        calculateSales();
        cleanFieldsSales();
        views.txt_sale_product_code.requestFocus();
    }

    private void insertSale() {

        // Validaciones principales
        if (views.sales_table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "No hay productos en la venta");
            return;
        }

        String customerIdTxt = views.txt_sale_customer_id.getText().trim();
        String totalTxt = views.txt_sale_total_to_pay.getText().trim();

        if (customerIdTxt.isEmpty() || !customerIdTxt.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "Ingrese la cédula del cliente correctamente");
            views.txt_sale_customer_id.requestFocus();
            return;
        }

        if (totalTxt.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se puede generar venta sin total");
            return;
        }

        int customer_id = Integer.parseInt(customerIdTxt);
        int employee_id = id_user;

        double total;
        try {
            total = Double.parseDouble(totalTxt);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Total inválido");
            return;
        }

        if (total <= 0) {
            JOptionPane.showMessageDialog(null, "Total inválido");
            return;
        }

        // Insertar venta
        if (saleDao.registerSaleQuery(customer_id, employee_id, total)) {

            int sale_id = saleDao.saleId();

            Products p = new Products();
            ProductsDao pDao = new ProductsDao();

            for (int i = 0; i < views.sales_table.getRowCount(); i++) {

                int product_id = Integer.parseInt(views.sales_table.getValueAt(i, 0).toString());
                int sale_quantity = Integer.parseInt(views.sales_table.getValueAt(i, 2).toString());
                double sale_price = Double.parseDouble(views.sales_table.getValueAt(i, 3).toString());
                double sale_subtotal = sale_quantity * sale_price;

                saleDao.registerSaleDetailQuery(product_id, sale_id, sale_quantity, sale_price, sale_subtotal);

                // actualizar stock
                p = pDao.searchId(product_id);
                int amount = p.getProduct_quantity() - sale_quantity;
                pDao.updateStockQuery(amount, product_id);
            }

            JOptionPane.showMessageDialog(null, "Venta generada");
            PrintSale printSale = new PrintSale(sale_id);
            printSale.setVisible(true);
            cleanTableTemp();
            cleanAllFieldsSales();
            views.txt_sale_customer_id.requestFocus();
        }
    }

    //Listar todas las ventas (reportes)
    public void listAllSales() {
        if (rol.equals("Administrador")) {
            List<Sales> list = saleDao.listAllSalesQuery();
            model = (DefaultTableModel) views.table_all_sales.getModel();

            Object[] row = new Object[5];
            for (int i = 0; i < list.size(); i++) {
                row[0] = list.get(i).getId();
                row[1] = list.get(i).getCustomer_name();
                row[2] = list.get(i).getEmployee_name();
                row[3] = list.get(i).getTotal_to_pay();
                row[4] = list.get(i).getSale_date();
                model.addRow(row);
            }
            views.table_all_sales.setModel(model);
        }
    }

    //Calcular total a pagar tabla de ventas
    private void calculateSales() {
        double total = 0.00;
        int numRow = views.sales_table.getRowCount();

        for (int i = 0; i < numRow; i++) {
            total = total + Double.parseDouble(String.valueOf(views.sales_table.getValueAt(i, 4)));
        }
        views.txt_sale_total_to_pay.setText(String.format("%.2f", total));
    }
    
    // Limpieza de tablas y campos
    public void cleanTable() {
        model = (DefaultTableModel) views.table_all_sales.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            model.removeRow(i);
            i = i - 1;
        }
    }

    public void cleanTableTemp() {
        temp = (DefaultTableModel) views.sales_table.getModel();
        for (int i = 0; i < temp.getRowCount(); i++) {
            temp.removeRow(i);
            i = i - 1;
        }
        views.sales_table.setModel(temp);
        calculateSales();
    }

    public void cleanFieldsSales() {
        views.txt_sale_product_code.setText("");
        views.txt_sale_product_name.setText("");
        views.txt_sale_quantity.setText("");
        views.txt_sale_product_id.setText("");
        views.txt_sale_price.setText("");
        views.txt_sale_subtotal.setText("");
        views.txt_sale_stock.setText("");
    }

    public void cleanAllFieldsSales() {
        views.txt_sale_product_code.setText("");
        views.txt_sale_product_name.setText("");
        views.txt_sale_quantity.setText("");
        views.txt_sale_product_id.setText("");
        views.txt_sale_price.setText("");
        views.txt_sale_subtotal.setText("");
        views.txt_sale_customer_id.setText("");
        views.txt_sale_customer_name.setText("");
        views.txt_sale_total_to_pay.setText("");
        views.txt_sale_stock.setText("");
    }
}
