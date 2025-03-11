/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package rrs;

import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import Database.DBConnection;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.text.SimpleDateFormat;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ADMIN
 */
public class Main extends javax.swing.JFrame {

    private DefaultTableModel cartModel = new DefaultTableModel(new Object[]{"Item", "Quantity", "Price"}, 0);
    
    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();
        HomePage.setVisible(true);
        FoodsPage.setVisible(false);
        ReservationsPage.setVisible(false);
        OrdersPage.setVisible(false);
        
        loadDashboard();
        loadTableBox();
        loadTables();
        loadOrders();
        loadReservations("All", "");
        restrictDateChooser();
        createRevenueChart("This year");
        createCategoryChart("This year");
    }
    
    private void restrictDateChooser() {
        dcDate.setMinSelectableDate(new java.util.Date());
        dcDate.getDateEditor().setEnabled(false);
    }

    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    // Dashboard
    
    private void loadDashboard() {
        try (Connection conn = DBConnection.Connect()) {

            String sql = "CALL GetTotalSales()";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double totalSales = rs.getDouble("total_sales");
                    lblTotalSales.setText("PHP " + formatNumber(totalSales));
                }
            }

            sql = "CALL GetNewSales()";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblTotalSalesW.setText("+" + String.format("%.2f", rs.getDouble("sales_this_week")));
                }
            }

            sql = "CALL GetSalesGrowth()";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblTotalSalesP.setText("+" + String.format("%.2f", rs.getDouble("sales_growth_percentage")) + "%");
                }
            }

            sql = "CALL GetAllCustomers()";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblCustomers.setText(Integer.toString(rs.getInt("total_customers")));
                }
            }

            sql = "CALL GetNewCustomers()";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblCustomersW.setText("+" + rs.getInt("customers_this_week") + " this week");
                }
            }

            sql = "CALL GetCustomerGrowth()";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblCustomersP.setText("+" + String.format("%.2f", rs.getDouble("customer_growth_percentage")) + "%");
                }
            }

            sql = "CALL GetTotalOrders()";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblTotalOrders.setText(Integer.toString(rs.getInt("total_orders")));
                }
            }

            sql = "CALL GetNewOrders()";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblTotalOrdersW.setText("+" + (rs.getInt("orders_this_week")) + " this week");
                }
            }

            sql = "CALL GetOrderGrowth()";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblTotalOrdersP.setText("+" + String.format("%.2f", rs.getDouble("order_growth_percentage")) + "%");
                }
            }

            sql = "CALL GetRefundedOrders()";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double refundedTotal = rs.getDouble("total_refunded");
                    lblRefunded.setText("PHP " + formatNumber(refundedTotal));
                }
            }

            sql = "CALL GetNewRefundedOrders()";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double refundedWeek = (rs.getDouble("refunded_this_week"));
                    lblRefundedW.setText("+" + formatNumber(refundedWeek));
                }
            }

            sql = "CALL GetRefundedOrderGrowth()";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblRefundedP.setText("+" + String.format("%.2f", rs.getDouble("refund_growth_percentage")) + "%");
                }
            }

        } catch (SQLException e) {
            showErrorMessage("Database Error: " + e.getMessage());
        }
    }
    
    private String formatNumber(double value) {
        if (value >= 10000) {
            return String.format("%.1fK", value / 1000); // Example: 10234 -> "10.2K"
        } else if (value >= 1000) {
            return String.format("%,d", (int) value); // Example: 1234 -> "1,234"
        } else {
            return String.format("%.2f", value); // Keep normal decimal format
        }
    }
    
    private void loadTableBox() {
        cbTable.removeAllItems();

        try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement("SELECT table_id FROM tables ORDER BY table_id ASC"); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int tableId = rs.getInt("table_id");
                cbTable.addItem("T" + tableId); // Format as "T" + table_id
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void loadTables() {
        DefaultTableModel model = (DefaultTableModel) tbTables.getModel();
        model.setRowCount(0);

        try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement("SELECT table_id, capacity FROM tables ORDER BY table_id ASC"); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("table_id"),
                    "T" + rs.getInt("table_id"),
                    rs.getInt("capacity"),
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void loadReservations(String statusFilter, String searchQuery) {
        DefaultTableModel model = (DefaultTableModel) tbReservations.getModel();
        model.setRowCount(0);

        String sql = "SELECT r.reservation_id, c.first_name, c.last_name, r.table_id, r.reservation_time, r.guests, r.status "
                + "FROM reservations r "
                + "INNER JOIN customers c ON r.customer_id = c.customer_id "
                + "WHERE (r.status LIKE ? OR ? = 'All') "
                + "AND (c.first_name LIKE ? OR c.last_name LIKE ?) "
                + "ORDER BY r.reservation_time ASC";
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");

        try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, statusFilter.equals("All") ? "%" : statusFilter);
            ps.setString(2, statusFilter);
            ps.setString(3, "%" + searchQuery + "%");
            ps.setString(4, "%" + searchQuery + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String formattedDate = dateFormat.format(rs.getTimestamp("reservation_time"));
                    
                    model.addRow(new Object[]{
                        rs.getInt("reservation_id"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        "T" + rs.getInt("table_id"),
                        formattedDate,
                        rs.getInt("guests"),
                        rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void loadOrders() {
        DefaultTableModel model = (DefaultTableModel) tbOrders.getModel();
        model.setRowCount(0);

        String sql = "SELECT o.order_id, CONCAT(c.first_name, ' ', c.last_name) AS customer_name, o.table_id, o.status "
                + "FROM orders o "
                + "INNER JOIN customers c ON o.customer_id = c.customer_id";
        try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("order_id"),
                    rs.getString("customer_name"),
                    rs.getString("table_id"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void loadSortedOrders(String status) {
        DefaultTableModel model = (DefaultTableModel) tbOrders.getModel();
        model.setRowCount(0);
        
        String sql = "SELECT o.order_id, CONCAT(c.first_name, ' ', c.last_name) AS customer_name, o.table_id, o.status "
                + "FROM orders o "
                + "INNER JOIN customers c ON o.customer_id = c.customer_id "
                + "WHERE status = ?";

        try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("order_id"),
                        rs.getString("customer_name"),
                        rs.getString("table_id"),
                        rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void loadOrderItems(int orderId) {
        DefaultTableModel model = (DefaultTableModel) tbItems.getModel();
        model.setRowCount(0);

        try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement(
                "SELECT m.item_name, oi.quantity, oi.price "
                        + "FROM order_items oi "
                        + "INNER JOIN menu m ON oi.menu_id = m.menu_id "
                        + "WHERE oi.order_id = ?")) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("item_name"),
                        rs.getInt("quantity"),
                        "PHP " + rs.getDouble("price")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void createRevenueChart(String period) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (Connection conn = DBConnection.Connect()) {
            String sql = "CALL GetRevenueStats(?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, period);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double totalSales = rs.getDouble("total_sales");
                        String dateLabel = rs.getString("date_label");

                        // Debugging: Print to console to check data
                        System.out.println("Date: " + dateLabel + ", Sales: " + totalSales);

                        dataset.addValue(totalSales, "Revenue", dateLabel);
                    }
                }
            }
        } catch (Exception e) {
            showErrorMessage("Database Error: " + e.getMessage());
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Revenue", "Period", "Sales (PHP)",
                dataset, PlotOrientation.VERTICAL, true, true, false
        );
        
        lineChart.setBackgroundPaint(new Color(240,240,240));
        lineChart.getPlot().setBackgroundPaint(Color.WHITE);

        // Force the Y-axis to start at 0 for better visibility
        CategoryPlot plot = lineChart.getCategoryPlot();
        plot.getRangeAxis().setAutoRange(true); // Ensures the axis scales properly

        // Improve line visibility
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesShapesVisible(0, true); // Show points
        renderer.setSeriesLinesVisible(0, true);  // Ensure line is drawn
        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(RevenuePanel.getSize());

        RevenuePanel.removeAll();
        RevenuePanel.setLayout(new BorderLayout());
        RevenuePanel.add(chartPanel, BorderLayout.CENTER);
        RevenuePanel.validate();
    }

    private void createCategoryChart(String period) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        try (Connection conn = DBConnection.Connect()) {
            String sql = "CALL GetSalesByCategory(?)"; // Replace with your actual stored procedure
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, period); // "This week", "This month", "This year"
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        dataset.setValue(rs.getString("category_name"), rs.getDouble("total_sales"));
                    }
                }
            }
        } catch (Exception e) {
            showErrorMessage("Database Error: " + e.getMessage());
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Sales", dataset, true, true, false
        );
        
        pieChart.setBackgroundPaint(Color.WHITE);
        pieChart.getPlot().setBackgroundPaint(Color.WHITE);

        PiePlot plot = (PiePlot) pieChart.getPlot();
        plot.setSimpleLabels(true); // Improve label visibility
        plot.setIgnoreZeroValues(true); // Don't show categories with 0 sales

        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(CategoryPanel.getSize()); // Make chart fill the panel

        CategoryPanel.removeAll();
        CategoryPanel.setLayout(new BorderLayout());
        CategoryPanel.add(chartPanel, BorderLayout.CENTER);
        CategoryPanel.validate();
    }
    
    // Reservations
    
    private void checkTableAvailability() {
        String selectedTable = cbTable.getSelectedItem().toString();
        int tableId = Integer.parseInt(selectedTable.replace("T", "")); // Extract table_id

        java.util.Date selectedDate = dcDate.getDate();
        String selectedTime = cbTime.getSelectedItem().toString();

        if (selectedDate == null || selectedTable.isEmpty() || selectedTime.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a table, date, and time.");
            return;
        }

        // Convert date to SQL format (YYYY-MM-DD)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(selectedDate);

        // Convert time from 12-hour format to 24-hour format
        String formattedTime = convertTo24HourFormat(selectedTime);

        try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM reservations WHERE table_id = ? AND DATE(reservation_time) = ? AND TIME(reservation_time) = ?")) {

            ps.setInt(1, tableId);
            ps.setString(2, formattedDate);
            ps.setString(3, formattedTime);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Table is already reserved at this time.");
                } else {
                    int choice = JOptionPane.showConfirmDialog(this, "Table is available. \nWould you like to order ahead of time or on arrival?", "Order Option", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        String[] options = {"Menu", "Bundles"};
                        int choices = JOptionPane.showOptionDialog(this,
                                "Would you like to order from the Menu or Bundles?",
                                "Select Order Type",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                null, options, options[0]);

                        if (choices == 0) {
                            showMenuRes(true, formattedDate, formattedTime);  // Don't reopen cart initially
                        } else if (choices == 1) {
                            showBundlesRes(true, formattedDate, formattedTime);
                        }
                    } else {
                        // Order on arrival
                        addReservation(tableId, formattedDate, formattedTime);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void placeOrderAhead(String reservationDate, String reservationTime) {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }

        // Calculate total price
        double totalPrice = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            totalPrice += Double.parseDouble(cartModel.getValueAt(i, 2).toString());
        }

        // Calculate down payment as 30% of total price
        double downPayment = totalPrice * 0.30;

        // Ask for customer details
        String firstName = JOptionPane.showInputDialog(this, "Enter Customer First Name:");
        String lastName = JOptionPane.showInputDialog(this, "Enter Customer Last Name:");
        String email = JOptionPane.showInputDialog(this, "Enter Customer Email:");
        String phone = JOptionPane.showInputDialog(this, "Enter Customer Phone Number:");

        if (firstName == null || lastName == null || email == null || phone == null
                || firstName.trim().isEmpty() || lastName.trim().isEmpty() || email.trim().isEmpty() || phone.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Order cancelled. All fields are required.");
            return;
        }

        // Ask for payment amount
        double paymentAmount = 0;
        while (true) {
            String paymentStr = JOptionPane.showInputDialog(this,
                    "Total Amount: PHP " + totalPrice + "\nDown Payment: PHP " + downPayment + "\nEnter Amount Paid:");

            if (paymentStr == null) {
                JOptionPane.showMessageDialog(this, "Order cancelled.");
                return;
            }

            try {
                paymentAmount = Double.parseDouble(paymentStr);
                if (paymentAmount < downPayment) {
                    JOptionPane.showMessageDialog(this,
                            "Insufficient amount! Please enter at least PHP " + downPayment);
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid number format.");
            }
        }

        double change = paymentAmount - downPayment;

        try (Connection conn = DBConnection.Connect()) {
            conn.setAutoCommit(false);

            int customerId = -1;

            // Check if the customer already exists
            try (PreparedStatement checkCustomer = conn.prepareStatement(
                    "SELECT customer_id FROM customers WHERE first_name = ? AND last_name = ? AND email = ? AND phone = ?")) {

                checkCustomer.setString(1, firstName);
                checkCustomer.setString(2, lastName);
                checkCustomer.setString(3, email);
                checkCustomer.setString(4, phone);

                try (ResultSet rs = checkCustomer.executeQuery()) {
                    if (rs.next()) {
                        customerId = rs.getInt("customer_id"); // Use existing customer ID
                    }
                }
            }

            // If the customer doesn't exist, insert a new one
            if (customerId == -1) {
                try (PreparedStatement insertCustomer = conn.prepareStatement(
                        "INSERT INTO customers (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS)) {

                    insertCustomer.setString(1, firstName);
                    insertCustomer.setString(2, lastName);
                    insertCustomer.setString(3, email);
                    insertCustomer.setString(4, phone);
                    insertCustomer.executeUpdate();

                    try (ResultSet rs = insertCustomer.getGeneratedKeys()) {
                        if (rs.next()) {
                            customerId = rs.getInt(1);
                        }
                    }
                }
            }

            if (customerId == -1) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Failed to add customer.");
                return;
            }

            // Create a reservation first
            int reservationId;
            try (PreparedStatement insertReservation = conn.prepareStatement(
                    "INSERT INTO reservations (customer_id, table_id, reservation_time, guests, down_payment, status) VALUES (?, ?, ?, ?, ?, 'Pending')",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {

                // Assuming you have the tableId from the selected table
                int tableId = Integer.parseInt(cbTable.getSelectedItem().toString().replace("T", ""));
                insertReservation.setInt(1, customerId);
                insertReservation.setInt(2, tableId);
                insertReservation.setString(3, reservationDate + " " + reservationTime);
                insertReservation.setInt(4, 1); // Assuming 1 guest for the reservation
                insertReservation.setDouble(5, downPayment);
                insertReservation.executeUpdate();

                try (ResultSet rs = insertReservation.getGeneratedKeys()) {
                    if (rs.next()) {
                        reservationId = rs.getInt(1);
                    } else {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "Failed to create reservation.");
                        return;
                    }
                }
            }

            // Insert the order with the reservation_id
            int orderId;
            try (PreparedStatement insertOrder = conn.prepareStatement(
                    "INSERT INTO orders (customer_id, table_id, total_price, payment, change_due, status, reservation_id) VALUES (?, ?, ?, ?, ?, 'Pending', ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {

                // Assuming you have the tableId from the selected table
                int tableId = Integer.parseInt(cbTable.getSelectedItem().toString().replace("T", ""));
                insertOrder.setInt(1, customerId);
                insertOrder.setInt(2, tableId);
                insertOrder.setDouble(3, totalPrice);
                insertOrder.setDouble(4, paymentAmount);
                insertOrder.setDouble(5, change);
                insertOrder.setInt(6, reservationId);
                insertOrder.executeUpdate();

                try (ResultSet rs = insertOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    } else {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "Failed to create order.");
                        return;
                    }
                }
            }

            // Insert order items
            try (PreparedStatement insertItem = conn.prepareStatement(
                    "INSERT INTO order_items (order_id, menu_id, quantity, price) VALUES (?, (SELECT menu_id FROM menu WHERE item_name = ?), ?, ?)")) {

                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    insertItem.setInt(1, orderId);
                    insertItem.setString(2, cartModel.getValueAt(i, 0).toString());
                    insertItem.setInt(3, Integer.parseInt(cartModel.getValueAt(i, 1).toString()));
                    insertItem.setDouble(4, Double.parseDouble(cartModel.getValueAt(i, 2).toString()));
                    insertItem.executeUpdate();
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this,
                    "Order placed successfully!\nTotal Price: PHP " + totalPrice
                    + "\nDown Payment: PHP " + downPayment + "\nChange: PHP " + change);

            cartModel.setRowCount(0);
            loadOrders();
            loadReservations("All", "");
            loadDashboard();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void showMenuRes(boolean reopenCart, String formattedDate, String formattedTime) {
        Menu menu = new Menu();

        selectMenuItems(menu);
        // Listen for when Menu is closed
        menu.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (reopenCart && cartModel.getRowCount() > 0) {
                    showCartDialogRes(formattedDate, formattedTime); // Reopen the cart only after Menu is closed
                }
            }
        });

        menu.setVisible(true);
    }

    private void showBundlesRes(boolean reopenCart, String formattedDate, String formattedTime) {
        Bundles bundles = new Bundles();

        selectBundles(bundles);
        // Listen for when Bundles is closed
        bundles.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (reopenCart && cartModel.getRowCount() > 0) {
                    showCartDialogRes(formattedDate, formattedTime); // Reopen the cart only after Bundles is closed
                }
            }
        });

        bundles.setVisible(true);
    }
    
    private void showCartDialogRes(String formattedDate, String formattedTime) {
        // Create a table to display cart items
        JTable cartTable = new JTable(cartModel);
        JScrollPane scrollPane = new JScrollPane(cartTable);

        // Buttons for actions
        JButton btnEdit = new JButton("Edit Quantity");
        JButton btnRemove = new JButton("Remove Item");
        JButton btnAddMore = new JButton("Add More");
        JButton btnConfirm = new JButton("Confirm Order");  // NEW BUTTON

        // Create a modal dialog
        JDialog cartDialog = new JDialog(this, "Cart Preview", true);
        cartDialog.setLayout(new BorderLayout());

        // Edit quantity action
        btnEdit.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(cartDialog, "Select an item to edit.");
                return;
            }

            int currentQuantity = (int) cartModel.getValueAt(row, 1);
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(currentQuantity, 1, 100, 1));

            int option = JOptionPane.showConfirmDialog(cartDialog, spinner, "Edit Quantity", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                cartModel.setValueAt(spinner.getValue(), row, 1);
                double pricePerItem = Double.parseDouble(cartModel.getValueAt(row, 2).toString()) / currentQuantity;
                cartModel.setValueAt(pricePerItem * (int) spinner.getValue(), row, 2);
            }
        });

        // Remove item action
        btnRemove.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(cartDialog, "Select an item to remove.");
                return;
            }
            cartModel.removeRow(row);
        });

        // Add more action
        btnAddMore.addActionListener(e -> {
            cartDialog.dispose(); // Close the cart

            String[] options = {"Menu", "Bundles"};
            int choice = JOptionPane.showOptionDialog(null,
                    "Would you like to add more from Menu or Bundles?",
                    "Add More Items",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);

            if (choice == 0) {
                showMenuRes(true, formattedDate, formattedTime);  // Pass a flag to reopen the cart later
            } else if (choice == 1) {
                showBundlesRes(true, formattedDate, formattedTime);
            }
        });

        // Confirm order action
        btnConfirm.addActionListener(e -> {
            cartDialog.dispose(); // Close the cart before placing the order
            placeOrderAhead(formattedDate, formattedTime);
        });

        // Panel to hold buttons
        JPanel panel = new JPanel();
        panel.add(btnEdit);
        panel.add(btnRemove);
        panel.add(btnAddMore);
        panel.add(btnConfirm);  // ADD CONFIRM BUTTON

        // Add components to dialog
        cartDialog.add(scrollPane, BorderLayout.CENTER);
        cartDialog.add(panel, BorderLayout.SOUTH);

        // Set dialog properties
        cartDialog.setSize(400, 300);
        cartDialog.setLocationRelativeTo(this);
        cartDialog.setVisible(true);
    }
    
    private void addReservation(int tableId, String formattedDate, String formattedTime) {
        String firstName = JOptionPane.showInputDialog(this, "Enter Customer First Name:");
        String lastName = JOptionPane.showInputDialog(this, "Enter Customer Last Name:");
        String email = JOptionPane.showInputDialog(this, "Enter Customer Email:");
        String phone = JOptionPane.showInputDialog(this, "Enter Customer Phone Number:");
        String guestsStr = JOptionPane.showInputDialog(this, "Enter Number of Guests:");

        if (firstName == null || lastName == null || email == null || phone == null || guestsStr == null
                || firstName.trim().isEmpty() || lastName.trim().isEmpty() || email.trim().isEmpty() || phone.trim().isEmpty() || guestsStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Reservation cancelled. All fields are required.");
            return;
        }

        int guests;
        try {
            guests = Integer.parseInt(guestsStr);
            if (guests <= 0) {
                JOptionPane.showMessageDialog(this, "Number of guests must be at least 1.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format for guests.");
            return;
        }

        // Check table capacity
        try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement("SELECT capacity FROM tables WHERE table_id = ?")) {

            ps.setInt(1, tableId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int capacity = rs.getInt("capacity");
                    if (guests > capacity) {
                        JOptionPane.showMessageDialog(this, "Number of guests exceeds table capacity (" + capacity + ").");
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid table selection.");
                    return;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
            return;
        }

        // Calculate default down payment based on guests (e.g., PHP 200 per guest)
        double downPaymentSuggested = guests * 200;
        String downPaymentStr = JOptionPane.showInputDialog(this, "Down Payment: PHP " + downPaymentSuggested + "\nEnter Down Payment Amount:");

        if (downPaymentStr == null || downPaymentStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Reservation cancelled. Down payment is required.");
            return;
        }

        double downPayment;
        try {
            downPayment = Double.parseDouble(downPaymentStr);
            if (downPayment < 0) {
                JOptionPane.showMessageDialog(this, "Down payment cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format for down payment.");
            return;
        }

        // Insert reservation
        try (Connection conn = DBConnection.Connect()) {
            conn.setAutoCommit(false); // Enable transaction

            int customerId = -1;

            // Check if the customer already exists
            try (PreparedStatement checkCustomer = conn.prepareStatement(
                    "SELECT customer_id FROM customers WHERE first_name = ? AND last_name = ? AND email = ? AND phone = ?")) {

                checkCustomer.setString(1, firstName);
                checkCustomer.setString(2, lastName);
                checkCustomer.setString(3, email);
                checkCustomer.setString(4, phone);

                try (ResultSet rs = checkCustomer.executeQuery()) {
                    if (rs.next()) {
                        customerId = rs.getInt("customer_id"); // Use existing customer ID
                    }
                }
            }

            // If the customer doesn't exist, insert a new one
            if (customerId == -1) {
                try (PreparedStatement insertCustomer = conn.prepareStatement(
                        "INSERT INTO customers (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS)) {

                    insertCustomer.setString(1, firstName);
                    insertCustomer.setString(2, lastName);
                    insertCustomer.setString(3, email);
                    insertCustomer.setString(4, phone);
                    insertCustomer.executeUpdate();

                    try (ResultSet rs = insertCustomer.getGeneratedKeys()) {
                        if (rs.next()) {
                            customerId = rs.getInt(1);
                        }
                    }
                }
            }

            if (customerId == -1) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Failed to add customer.");
                return;
            }

            // Insert reservation with user-defined down payment
            try (PreparedStatement insertReservation = conn.prepareStatement(
                    "INSERT INTO reservations (customer_id, table_id, reservation_time, guests, down_payment, status) VALUES (?, ?, ?, ?, ?, 'Pending')")) {

                insertReservation.setInt(1, customerId);
                insertReservation.setInt(2, tableId);
                insertReservation.setString(3, formattedDate + " " + formattedTime);
                insertReservation.setInt(4, guests);
                insertReservation.setDouble(5, downPayment);
                insertReservation.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Reservation successfully added with PHP " + downPayment + " down payment.");
            loadReservations("All", "");
            loadDashboard();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    private String convertTo24HourFormat(String time12Hour) {
        try {
            // Remove AM/PM and trim the string
            String timePart = time12Hour.replace("AM", "").replace("PM", "").trim();
            String[] timeSplit = timePart.split(":");
            int hour = Integer.parseInt(timeSplit[0]);
            String minutes = timeSplit[1];

            // Adjust hour based on AM/PM
            if (time12Hour.contains("PM") && hour != 12) {
                hour += 12; // Convert PM times (except 12 PM) to 24-hour format
            } else if (time12Hour.contains("AM") && hour == 12) {
                hour = 0; // Convert 12 AM to 00 hour
            }

            // Format as HH:mm:ss
            return String.format("%02d:%s:00", hour, minutes);
        } catch (Exception e) {
            return "00:00:00"; // Default if parsing fails
        }
    }

    private void addTable() {
        String capacityStr = JOptionPane.showInputDialog(this, "Enter table capacity:");

        if (capacityStr == null || capacityStr.trim().isEmpty()) {
            return;
        }

        try {
            int capacity = Integer.parseInt(capacityStr);

            try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement("INSERT INTO tables (capacity) VALUES (?)")) {

                ps.setInt(1, capacity);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Table added successfully.");
                loadTables();
                loadDashboard();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void editTable() {
        int row = tbTables.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a table first.");
            return;
        }

        int tableId = (int) tbTables.getValueAt(row, 0);
        String newCapacityStr = JOptionPane.showInputDialog(this, "Enter new capacity:");

        if (newCapacityStr == null || newCapacityStr.trim().isEmpty()) {
            return;
        }

        try {
            int newCapacity = Integer.parseInt(newCapacityStr);

            try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement("UPDATE tables SET capacity = ? WHERE table_id = ?")) {

                ps.setInt(1, newCapacity);
                ps.setInt(2, tableId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Table updated successfully.");
                loadTables();
                loadDashboard();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void deleteTable() {
        int row = tbTables.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a table first.");
            return;
        }

        int tableId = (int) tbTables.getValueAt(row, 0);

        try (Connection conn = DBConnection.Connect(); PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM reservations WHERE table_id = ? AND status IN ('Pending', 'Confirmed', 'Completed')"); PreparedStatement delete = conn.prepareStatement("DELETE FROM tables WHERE table_id = ?")) {

            check.setInt(1, tableId);
            ResultSet rs = check.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Cannot delete: Table has active reservations.");
                return;
            }

            delete.setInt(1, tableId);
            delete.executeUpdate();
            JOptionPane.showMessageDialog(this, "Table deleted successfully.");
            loadTables();
            loadDashboard();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void updateReservationStatus(String newStatus) {
        int row = tbReservations.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a reservation first.");
            return;
        }

        int resId = (int) tbReservations.getValueAt(row, 0);

        try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement("UPDATE reservations SET status = ? WHERE reservation_id = ?")) {

            ps.setString(1, newStatus);
            ps.setInt(2, resId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Reservation updated successfully.");
            loadReservations("All", "");
            loadDashboard();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    private void deleteReservation() {
        int row = tbReservations.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a reservation first.");
            return;
        }

        int resId = (int) tbReservations.getValueAt(row, 0);
        String status = tbReservations.getValueAt(row, 5).toString();

        if (!status.equals("Completed") && !status.equals("Cancelled")) {
            JOptionPane.showMessageDialog(this, "Only completed or cancelled reservations can be deleted.");
            return;
        }

        try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement("DELETE FROM reservations WHERE reservation_id = ?")) {

            ps.setInt(1, resId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Reservation deleted successfully.");
            loadReservations("All", "");
            loadDashboard();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    // Orders
    
    private void updateOrderStatus(String newStatus) {
        int row = tbOrders.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an order first.");
            return;
        }

        int orderId = (int) tbOrders.getValueAt(row, 0);

        try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement("UPDATE orders SET status = ? WHERE order_id = ?")) {

            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Order marked as " + newStatus + ".");
            loadOrders();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void showItemImage() {
        int row = tbItems.getSelectedRow();
        if (row == -1) {
            return;
        }

        String itemName = tbItems.getValueAt(row, 0).toString();

        try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement("SELECT image_path FROM menu WHERE item_name = ?")) {

            ps.setString(1, itemName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String imagePath = "src/assets/" + rs.getString("image_path") + ".png";
                    lblItemPic.setIcon(new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void selectMenuItems(Menu menu) {
        menu.tbMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = menu.tbMenu.getSelectedRow();
                if (row != -1) {
                    String itemName = menu.tbMenu.getValueAt(row, 0).toString();
                    double price = Double.parseDouble(menu.tbMenu.getValueAt(row, 1).toString().replace("PHP ", ""));

                    JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
                    int option = JOptionPane.showConfirmDialog(null, quantitySpinner,
                            "Select Quantity for " + itemName, JOptionPane.OK_CANCEL_OPTION);

                    if (option == JOptionPane.OK_OPTION) {
                        int quantity = (int) quantitySpinner.getValue();
                        cartModel.addRow(new Object[]{itemName, quantity, price * quantity});
                    }
                }
            }
        });
    }
    
    private void selectBundles(Bundles bundles) {
        bundles.tbBundles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = bundles.tbBundles.getSelectedRow();
                if (row != -1) {
                    String bundleName = bundles.tbBundles.getValueAt(row, 1).toString();
                    double price = Double.parseDouble(bundles.tbBundles.getValueAt(row, 2).toString().replace("PHP ", ""));

                    JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
                    int option = JOptionPane.showConfirmDialog(null, quantitySpinner,
                            "Select Quantity for " + bundleName, JOptionPane.OK_CANCEL_OPTION);

                    if (option == JOptionPane.OK_OPTION) {
                        int quantity = (int) quantitySpinner.getValue();
                        cartModel.addRow(new Object[]{bundleName, quantity, price * quantity});
                    }
                }
            }
        });
    }
    
    private void showMenu(boolean reopenCart) {
        Menu menu = new Menu();

        selectMenuItems(menu);
        // Listen for when Menu is closed
        menu.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (reopenCart && cartModel.getRowCount() > 0) {
                    showCartDialog(); // Reopen the cart only after Menu is closed
                }
            }
        });

        menu.setVisible(true);
    }

    private void showBundles(boolean reopenCart) {
        Bundles bundles = new Bundles();

        selectBundles(bundles);
        // Listen for when Bundles is closed
        bundles.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (reopenCart && cartModel.getRowCount() > 0) {
                    showCartDialog(); // Reopen the cart only after Bundles is closed
                }
            }
        });

        bundles.setVisible(true);
    }
    
    private int selectTable(Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT table_id FROM tables WHERE status = 'Available' ORDER BY table_id ASC"); ResultSet rs = ps.executeQuery()) {

            // Collect available tables into a list
            java.util.List<Integer> tableList = new java.util.ArrayList<>();
            while (rs.next()) {
                tableList.add(rs.getInt("table_id"));
            }

            if (tableList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No available tables.");
                return -1;
            }

            // Convert list to array for JComboBox
            Integer[] tableArray = tableList.toArray(new Integer[0]);
            JComboBox<Integer> tableComboBox = new JComboBox<>(tableArray);

            int option = JOptionPane.showConfirmDialog(this, tableComboBox,
                    "Select a Table", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                return (int) tableComboBox.getSelectedItem();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }

        return -1;
    }
    
    private int getTableIdForOrder(int orderId) {
        try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement("SELECT table_id FROM orders WHERE order_id = ?")) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("table_id");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
        return -1;
    }
    
    private void placeOrder() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }

        // Calculate total price
        double totalPrice = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            totalPrice += Double.parseDouble(cartModel.getValueAt(i, 2).toString());
        }

        // Ask for customer details
        String firstName = JOptionPane.showInputDialog(this, "Enter Customer First Name:");
        String lastName = JOptionPane.showInputDialog(this, "Enter Customer Last Name:");
        String email = JOptionPane.showInputDialog(this, "Enter Customer Email:");
        String phone = JOptionPane.showInputDialog(this, "Enter Customer Phone Number:");

        if (firstName == null || lastName == null || email == null || phone == null
                || firstName.trim().isEmpty() || lastName.trim().isEmpty() || email.trim().isEmpty() || phone.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Order cancelled. All fields are required.");
            return;
        }

        try (Connection conn = DBConnection.Connect()) {
            conn.setAutoCommit(false);

            int customerId = -1;

            // Check if the customer already exists
            try (PreparedStatement checkCustomer = conn.prepareStatement(
                    "SELECT customer_id FROM customers WHERE first_name = ? AND last_name = ? AND email = ? AND phone = ?")) {

                checkCustomer.setString(1, firstName);
                checkCustomer.setString(2, lastName);
                checkCustomer.setString(3, email);
                checkCustomer.setString(4, phone);

                try (ResultSet rs = checkCustomer.executeQuery()) {
                    if (rs.next()) {
                        customerId = rs.getInt("customer_id"); // Use existing customer ID
                    }
                }
            }

            // If the customer doesn't exist, insert a new one
            if (customerId == -1) {
                try (PreparedStatement insertCustomer = conn.prepareStatement(
                        "INSERT INTO customers (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS)) {

                    insertCustomer.setString(1, firstName);
                    insertCustomer.setString(2, lastName);
                    insertCustomer.setString(3, email);
                    insertCustomer.setString(4, phone);
                    insertCustomer.executeUpdate();

                    try (ResultSet rs = insertCustomer.getGeneratedKeys()) {
                        if (rs.next()) {
                            customerId = rs.getInt(1);
                        }
                    }
                }
            }

            if (customerId == -1) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Failed to add customer.");
                return;
            }

            // Ask the user to select a table
            int tableId = selectTable(conn);
            if (tableId == -1) {
                JOptionPane.showMessageDialog(this, "Order cancelled. No table selected.");
                return;
            }
            
            try (PreparedStatement checkTable = conn.prepareStatement("SELECT status FROM tables WHERE table_id = ?")) {
                checkTable.setInt(1, tableId);
                ResultSet rs = checkTable.executeQuery();
                if (rs.next()) {
                    if (rs.getString("status").equals("Occupied")) {
                        JOptionPane.showMessageDialog(this, "This table is currently occuppied.");
                    } else {
                        try (PreparedStatement updateTable = conn.prepareStatement(
                                "UPDATE tables SET status = 'Occupied' WHERE table_id = ?")) {
                            updateTable.setInt(1, tableId);
                            updateTable.executeUpdate();
                        }
                    }
                }
            }

            // ✅ Ask for payment amount
            double paymentAmount = 0;
            while (true) {
                String paymentStr = JOptionPane.showInputDialog(this,
                        "Total Amount: PHP " + totalPrice + "\nEnter Amount Paid:");

                if (paymentStr == null) {
                    JOptionPane.showMessageDialog(this, "Order cancelled.");
                    return;
                }

                try {
                    paymentAmount = Double.parseDouble(paymentStr);
                    if (paymentAmount < totalPrice) {
                        JOptionPane.showMessageDialog(this,
                                "Insufficient amount! Please enter at least PHP " + totalPrice);
                    } else {
                        break;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid number format.");
                }
            }

            double change = paymentAmount - totalPrice;

            // Insert the order with total price, payment, and change
            int orderId;
            try (PreparedStatement insertOrder = conn.prepareStatement(
                    "INSERT INTO orders (customer_id, table_id, total_price, payment, change_due, status) VALUES (?, ?, ?, ?, ?, 'Pending')",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {

                insertOrder.setInt(1, customerId);
                insertOrder.setInt(2, tableId);
                insertOrder.setDouble(3, totalPrice);  // Insert total price
                insertOrder.setDouble(4, paymentAmount);  // Insert payment amount
                insertOrder.setDouble(5, change);  // Insert change amount
                insertOrder.executeUpdate();

                try (ResultSet rs = insertOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    } else {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "Failed to create order.");
                        return;
                    }
                }
            }

            // Insert order items
            try (PreparedStatement insertItem = conn.prepareStatement(
                    "INSERT INTO order_items (order_id, menu_id, quantity, price) VALUES (?, (SELECT menu_id FROM menu WHERE item_name = ?), ?, ?)")) {

                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    insertItem.setInt(1, orderId);
                    insertItem.setString(2, cartModel.getValueAt(i, 0).toString());
                    insertItem.setInt(3, Integer.parseInt(cartModel.getValueAt(i, 1).toString()));
                    insertItem.setDouble(4, Double.parseDouble(cartModel.getValueAt(i, 2).toString()));
                    insertItem.executeUpdate();
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this,
                    "Order placed successfully!\nTotal Price: PHP " + totalPrice
                    + "\nPayment: PHP " + paymentAmount + "\nChange: PHP " + change);

            cartModel.setRowCount(0);
            loadOrders();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void showCartDialog() {
        // Create a table to display cart items
        JTable cartTable = new JTable(cartModel);
        JScrollPane scrollPane = new JScrollPane(cartTable);

        // Buttons for actions
        JButton btnEdit = new JButton("Edit Quantity");
        JButton btnRemove = new JButton("Remove Item");
        JButton btnAddMore = new JButton("Add More");
        JButton btnConfirm = new JButton("Confirm Order");  // NEW BUTTON

        // Create a modal dialog
        JDialog cartDialog = new JDialog(this, "Cart Preview", true);
        cartDialog.setLayout(new BorderLayout());

        // Edit quantity action
        btnEdit.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(cartDialog, "Select an item to edit.");
                return;
            }

            int currentQuantity = (int) cartModel.getValueAt(row, 1);
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(currentQuantity, 1, 100, 1));

            int option = JOptionPane.showConfirmDialog(cartDialog, spinner, "Edit Quantity", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                cartModel.setValueAt(spinner.getValue(), row, 1);
                double pricePerItem = Double.parseDouble(cartModel.getValueAt(row, 2).toString()) / currentQuantity;
                cartModel.setValueAt(pricePerItem * (int) spinner.getValue(), row, 2);
            }
        });

        // Remove item action
        btnRemove.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(cartDialog, "Select an item to remove.");
                return;
            }
            cartModel.removeRow(row);
        });

        // Add more action
        btnAddMore.addActionListener(e -> {
            cartDialog.dispose(); // Close the cart

            String[] options = {"Menu", "Bundles"};
            int choice = JOptionPane.showOptionDialog(null,
                    "Would you like to add more from Menu or Bundles?",
                    "Add More Items",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);

            if (choice == 0) {
                showMenu(true);  // Pass a flag to reopen the cart later
            } else if (choice == 1) {
                showBundles(true);
            }
        });

        // Confirm order action
        btnConfirm.addActionListener(e -> {
            cartDialog.dispose(); // Close the cart before placing the order
            placeOrder();
        });

        // Panel to hold buttons
        JPanel panel = new JPanel();
        panel.add(btnEdit);
        panel.add(btnRemove);
        panel.add(btnAddMore);
        panel.add(btnConfirm);  // ADD CONFIRM BUTTON

        // Add components to dialog
        cartDialog.add(scrollPane, BorderLayout.CENTER);
        cartDialog.add(panel, BorderLayout.SOUTH);

        // Set dialog properties
        cartDialog.setSize(400, 300);
        cartDialog.setLocationRelativeTo(this);
        cartDialog.setVisible(true);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblFoods = new javax.swing.JLabel();
        lblReservations = new javax.swing.JLabel();
        lblHome = new javax.swing.JLabel();
        lblOrders = new javax.swing.JLabel();
        lblLogout = new javax.swing.JLabel();
        contentPanel = new javax.swing.JLayeredPane();
        OrdersPage = new javax.swing.JPanel();
        OrdersPanel = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tbOrders = new javax.swing.JTable();
        jLabel36 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tbItems = new javax.swing.JTable();
        btnAddOrder = new javax.swing.JButton();
        btnMarkAsServed = new javax.swing.JButton();
        btnCancelOrder = new javax.swing.JButton();
        cbOrderSort = new javax.swing.JComboBox<>();
        jPanel11 = new javax.swing.JPanel();
        lblItemPic = new javax.swing.JLabel();
        lblItemName = new javax.swing.JLabel();
        lblItemPrice = new javax.swing.JLabel();
        btnMarkAsCompleted = new javax.swing.JButton();
        btnRefund = new javax.swing.JButton();
        btnMarkAsPreparing = new javax.swing.JButton();
        FoodsPage = new javax.swing.JPanel();
        contentPane = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        buffetPanel = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        btnViewBundles = new javax.swing.JButton();
        lblBuffet = new javax.swing.JLabel();
        alaCartePanel = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        btnViewMenu = new javax.swing.JButton();
        lblBuffet1 = new javax.swing.JLabel();
        HomePage = new javax.swing.JScrollPane();
        Home = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        btnBook = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        DashPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        lblTotalSales = new javax.swing.JLabel();
        lblTotalSalesP = new javax.swing.JLabel();
        lblTotalSalesW = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        lblCustomers = new javax.swing.JLabel();
        lblCustomersP = new javax.swing.JLabel();
        lblCustomersW = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        lblTotalOrders = new javax.swing.JLabel();
        lblTotalOrdersP = new javax.swing.JLabel();
        lblTotalOrdersW = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        lblRefunded = new javax.swing.JLabel();
        lblRefundedP = new javax.swing.JLabel();
        lblRefundedW = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        RevenuePanel = new javax.swing.JPanel();
        cbSortRev = new javax.swing.JComboBox<>();
        jLabel26 = new javax.swing.JLabel();
        CategoryPanel = new javax.swing.JPanel();
        cbSalesCat = new javax.swing.JComboBox<>();
        ReservationsPage = new javax.swing.JScrollPane();
        Reservations = new javax.swing.JPanel();
        contents = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        cbTable = new javax.swing.JComboBox<>();
        dcDate = new com.toedter.calendar.JDateChooser();
        cbTime = new javax.swing.JComboBox<>();
        btnSearchTable = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel10 = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbTables = new javax.swing.JTable();
        jLabel34 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbReservations = new javax.swing.JTable();
        btnEditTable = new javax.swing.JButton();
        btnDeleteTable = new javax.swing.JButton();
        btnAddTable = new javax.swing.JButton();
        btnDelRes = new javax.swing.JButton();
        btnConfirmed = new javax.swing.JButton();
        txtSearchRes = new javax.swing.JTextField();
        lblSearchRes = new javax.swing.JLabel();
        btnCancelled = new javax.swing.JButton();
        cbResStatus = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        mainPanel.setBackground(new java.awt.Color(69, 79, 99));
        mainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblLogo.setFont(new java.awt.Font("Radley", 1, 36)); // NOI18N
        lblLogo.setForeground(new java.awt.Color(255, 255, 255));
        lblLogo.setText("Lutò");
        mainPanel.add(lblLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, -1, -1));

        lblFoods.setFont(new java.awt.Font("Poppins", 0, 18)); // NOI18N
        lblFoods.setForeground(new java.awt.Color(255, 255, 255));
        lblFoods.setText("foods");
        lblFoods.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblFoods.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFoodsMouseClicked(evt);
            }
        });
        mainPanel.add(lblFoods, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 20, -1, -1));

        lblReservations.setFont(new java.awt.Font("Poppins", 0, 18)); // NOI18N
        lblReservations.setForeground(new java.awt.Color(255, 255, 255));
        lblReservations.setText("reservations");
        lblReservations.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblReservations.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblReservationsMouseClicked(evt);
            }
        });
        mainPanel.add(lblReservations, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 20, -1, -1));

        lblHome.setFont(new java.awt.Font("Poppins", 0, 18)); // NOI18N
        lblHome.setForeground(new java.awt.Color(255, 255, 255));
        lblHome.setText("home");
        lblHome.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblHome.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblHomeMouseClicked(evt);
            }
        });
        mainPanel.add(lblHome, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 20, -1, -1));

        lblOrders.setFont(new java.awt.Font("Poppins", 0, 18)); // NOI18N
        lblOrders.setForeground(new java.awt.Color(255, 255, 255));
        lblOrders.setText("orders");
        lblOrders.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblOrders.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblOrdersMouseClicked(evt);
            }
        });
        mainPanel.add(lblOrders, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 20, -1, -1));

        lblLogout.setFont(new java.awt.Font("Poppins", 0, 18)); // NOI18N
        lblLogout.setForeground(new java.awt.Color(255, 255, 255));
        lblLogout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/log-out-regular-24-white.png"))); // NOI18N
        lblLogout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblLogoutMouseClicked(evt);
            }
        });
        mainPanel.add(lblLogout, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 10, -1, 50));

        OrdersPanel.setBackground(new java.awt.Color(250, 250, 250));

        jLabel31.setFont(new java.awt.Font("Crimson Pro", 1, 24)); // NOI18N
        jLabel31.setText("Items");

        tbOrders.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        tbOrders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Customer", "Table", "Status"
            }
        ));
        tbOrders.setRowHeight(30);
        tbOrders.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbOrdersMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tbOrders);
        if (tbOrders.getColumnModel().getColumnCount() > 0) {
            tbOrders.getColumnModel().getColumn(0).setMinWidth(40);
            tbOrders.getColumnModel().getColumn(0).setMaxWidth(40);
            tbOrders.getColumnModel().getColumn(2).setMinWidth(50);
            tbOrders.getColumnModel().getColumn(2).setMaxWidth(50);
        }

        jLabel36.setFont(new java.awt.Font("Crimson Pro", 1, 36)); // NOI18N
        jLabel36.setText("Orders Overview");

        tbItems.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        tbItems.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Item", "Qty", "Price"
            }
        ));
        tbItems.setRowHeight(30);
        tbItems.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbItemsMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tbItems);
        if (tbItems.getColumnModel().getColumnCount() > 0) {
            tbItems.getColumnModel().getColumn(1).setMinWidth(40);
            tbItems.getColumnModel().getColumn(1).setMaxWidth(40);
        }

        btnAddOrder.setBackground(new java.awt.Color(153, 255, 153));
        btnAddOrder.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        btnAddOrder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/plus-regular-24.png"))); // NOI18N
        btnAddOrder.setText(" Add Order");
        btnAddOrder.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAddOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddOrderActionPerformed(evt);
            }
        });

        btnMarkAsServed.setBackground(new java.awt.Color(204, 204, 204));
        btnMarkAsServed.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        btnMarkAsServed.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/edit-regular-24.png"))); // NOI18N
        btnMarkAsServed.setText("  Served");
        btnMarkAsServed.setToolTipText("");
        btnMarkAsServed.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMarkAsServed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMarkAsServedActionPerformed(evt);
            }
        });

        btnCancelOrder.setBackground(new java.awt.Color(255, 153, 153));
        btnCancelOrder.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        btnCancelOrder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/block-regular-24.png"))); // NOI18N
        btnCancelOrder.setText(" Cancel Order");
        btnCancelOrder.setToolTipText("");
        btnCancelOrder.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelOrderActionPerformed(evt);
            }
        });

        cbOrderSort.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        cbOrderSort.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pending", "Preparing", "Served", "Cancelled" }));
        cbOrderSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbOrderSortActionPerformed(evt);
            }
        });

        jPanel11.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblItemPic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/image.png"))); // NOI18N

        lblItemName.setFont(new java.awt.Font("Crimson Pro", 1, 18)); // NOI18N
        lblItemName.setText("Name");

        lblItemPrice.setFont(new java.awt.Font("Radley", 0, 14)); // NOI18N
        lblItemPrice.setText("Price");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblItemPic, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblItemPrice)
                    .addComponent(lblItemName, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(lblItemName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblItemPrice)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblItemPic, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnMarkAsCompleted.setBackground(new java.awt.Color(204, 204, 204));
        btnMarkAsCompleted.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        btnMarkAsCompleted.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/edit-regular-24.png"))); // NOI18N
        btnMarkAsCompleted.setText(" Completed");
        btnMarkAsCompleted.setToolTipText("");
        btnMarkAsCompleted.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMarkAsCompleted.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMarkAsCompletedActionPerformed(evt);
            }
        });

        btnRefund.setBackground(new java.awt.Color(255, 153, 153));
        btnRefund.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        btnRefund.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/money-withdraw-regular-24.png"))); // NOI18N
        btnRefund.setText("  Refund");
        btnRefund.setToolTipText("");
        btnRefund.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRefund.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefundActionPerformed(evt);
            }
        });

        btnMarkAsPreparing.setBackground(new java.awt.Color(204, 204, 204));
        btnMarkAsPreparing.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        btnMarkAsPreparing.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/edit-regular-24.png"))); // NOI18N
        btnMarkAsPreparing.setText("Prepare");
        btnMarkAsPreparing.setToolTipText("");
        btnMarkAsPreparing.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMarkAsPreparing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMarkAsPreparingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout OrdersPanelLayout = new javax.swing.GroupLayout(OrdersPanel);
        OrdersPanel.setLayout(OrdersPanelLayout);
        OrdersPanelLayout.setHorizontalGroup(
            OrdersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OrdersPanelLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(OrdersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36))
                .addGap(18, 18, 18)
                .addGroup(OrdersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel31)
                    .addGroup(OrdersPanelLayout.createSequentialGroup()
                        .addComponent(btnAddOrder)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMarkAsCompleted)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMarkAsServed)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMarkAsPreparing, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(OrdersPanelLayout.createSequentialGroup()
                        .addGroup(OrdersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(cbOrderSort, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCancelOrder, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                            .addComponent(btnRefund, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 470, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        OrdersPanelLayout.setVerticalGroup(
            OrdersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OrdersPanelLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(OrdersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel31)
                    .addComponent(jLabel36))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(OrdersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(OrdersPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(OrdersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnMarkAsCompleted, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnMarkAsServed, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnMarkAsPreparing, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(OrdersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, OrdersPanelLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(cbOrderSort, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRefund, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCancelOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout OrdersPageLayout = new javax.swing.GroupLayout(OrdersPage);
        OrdersPage.setLayout(OrdersPageLayout);
        OrdersPageLayout.setHorizontalGroup(
            OrdersPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(OrdersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        OrdersPageLayout.setVerticalGroup(
            OrdersPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(OrdersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        contentPane.setBackground(new java.awt.Color(250, 250, 250));
        contentPane.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel10.setFont(new java.awt.Font("Radley", 1, 48)); // NOI18N
        jLabel10.setText("Savor the flavors of tradition");
        contentPane.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 20, -1, -1));

        jLabel14.setFont(new java.awt.Font("Poppins", 0, 18)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("A fusion of tradition and contemporary flavors.");
        contentPane.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 80, -1, -1));
        contentPane.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 280, 790, 10));

        buffetPanel.setBackground(new java.awt.Color(250, 250, 250));
        buffetPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(150, 150, 150), 1, true));

        jLabel18.setFont(new java.awt.Font("Ramaraja", 0, 48)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("LUTO");

        jLabel22.setFont(new java.awt.Font("Ramaraja", 0, 48)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("COMBOS");

        btnViewBundles.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        btnViewBundles.setText("View Bundles");
        btnViewBundles.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnViewBundles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewBundlesActionPerformed(evt);
            }
        });

        lblBuffet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/Buffet.png"))); // NOI18N

        javax.swing.GroupLayout buffetPanelLayout = new javax.swing.GroupLayout(buffetPanel);
        buffetPanel.setLayout(buffetPanelLayout);
        buffetPanelLayout.setHorizontalGroup(
            buffetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buffetPanelLayout.createSequentialGroup()
                .addGroup(buffetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(buffetPanelLayout.createSequentialGroup()
                        .addGap(69, 69, 69)
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(buffetPanelLayout.createSequentialGroup()
                        .addGap(99, 99, 99)
                        .addComponent(btnViewBundles))
                    .addGroup(buffetPanelLayout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(lblBuffet, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(buffetPanelLayout.createSequentialGroup()
                        .addGap(106, 106, 106)
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        buffetPanelLayout.setVerticalGroup(
            buffetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buffetPanelLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(buffetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(buffetPanelLayout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(jLabel22))
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblBuffet, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnViewBundles))
        );

        contentPane.add(buffetPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 140, 320, 360));

        alaCartePanel.setBackground(new java.awt.Color(250, 250, 250));
        alaCartePanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(150, 150, 150), 1, true));

        jLabel27.setFont(new java.awt.Font("Ramaraja", 0, 48)); // NOI18N
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel27.setText("A LA");

        jLabel28.setFont(new java.awt.Font("Ramaraja", 0, 48)); // NOI18N
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel28.setText("CARTE");

        btnViewMenu.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        btnViewMenu.setText("View Menu");
        btnViewMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnViewMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewMenuActionPerformed(evt);
            }
        });

        lblBuffet1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/Ala Carte.png"))); // NOI18N

        javax.swing.GroupLayout alaCartePanelLayout = new javax.swing.GroupLayout(alaCartePanel);
        alaCartePanel.setLayout(alaCartePanelLayout);
        alaCartePanelLayout.setHorizontalGroup(
            alaCartePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alaCartePanelLayout.createSequentialGroup()
                .addGroup(alaCartePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(alaCartePanelLayout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addGroup(alaCartePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(alaCartePanelLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(alaCartePanelLayout.createSequentialGroup()
                        .addGap(99, 99, 99)
                        .addComponent(btnViewMenu))
                    .addGroup(alaCartePanelLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(lblBuffet1, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        alaCartePanelLayout.setVerticalGroup(
            alaCartePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alaCartePanelLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(alaCartePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel27)
                    .addGroup(alaCartePanelLayout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(jLabel28)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblBuffet1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnViewMenu))
        );

        contentPane.add(alaCartePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 140, 310, 359));

        javax.swing.GroupLayout FoodsPageLayout = new javax.swing.GroupLayout(FoodsPage);
        FoodsPage.setLayout(FoodsPageLayout);
        FoodsPageLayout.setHorizontalGroup(
            FoodsPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contentPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        FoodsPageLayout.setVerticalGroup(
            FoodsPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contentPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        HomePage.setViewportView(null);

        Home.setBackground(new java.awt.Color(69, 79, 99));
        Home.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Ramaraja", 0, 84)); // NOI18N
        jLabel1.setText("FILIPINO");
        Home.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 100, -1, -1));

        jLabel2.setFont(new java.awt.Font("Radley", 0, 36)); // NOI18N
        jLabel2.setText("Soul of the");
        Home.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 70, -1, -1));

        jLabel4.setFont(new java.awt.Font("Arial", 2, 18)); // NOI18N
        jLabel4.setText("Authentic Filipino dishes, bundled with");
        Home.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 240, -1, -1));

        jLabel5.setFont(new java.awt.Font("Arial", 2, 18)); // NOI18N
        jLabel5.setText("passion and tradition, served ");
        Home.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 270, 310, -1));

        jLabel7.setFont(new java.awt.Font("Arial", 2, 18)); // NOI18N
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/philippines.png"))); // NOI18N
        jLabel7.setText("with a modern touch! ");
        jLabel7.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        Home.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 300, 225, -1));

        btnBook.setBackground(new java.awt.Color(153, 28, 64));
        btnBook.setFont(new java.awt.Font("Poppins", 1, 18)); // NOI18N
        btnBook.setForeground(new java.awt.Color(255, 255, 255));
        btnBook.setText("Book now");
        btnBook.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBookActionPerformed(evt);
            }
        });
        Home.add(btnBook, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 370, 180, 50));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/main-bg.png"))); // NOI18N
        Home.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 979, 524));

        DashPanel.setBackground(new java.awt.Color(255, 255, 255));
        DashPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Crimson Pro", 1, 36)); // NOI18N
        jLabel3.setText("Welcome back, Linux Adona");
        DashPanel.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(38, 26, -1, -1));

        jLabel8.setFont(new java.awt.Font("Crimson Pro", 0, 24)); // NOI18N
        jLabel8.setText("Explore your Dashboard");
        DashPanel.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(38, 73, -1, -1));

        jPanel1.setBackground(new java.awt.Color(204, 255, 204));

        jLabel9.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        jLabel9.setText("Total Sales");

        lblTotalSales.setFont(new java.awt.Font("Poppins", 1, 18)); // NOI18N
        lblTotalSales.setText("0");

        lblTotalSalesP.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        lblTotalSalesP.setText("+0%");

        lblTotalSalesW.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        lblTotalSalesW.setText("+0 this week");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblTotalSales, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(95, 95, 95))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lblTotalSalesP, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTotalSalesW, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(19, 19, 19))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalSales, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalSalesP)
                    .addComponent(lblTotalSalesW))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        DashPanel.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(38, 136, -1, -1));

        jPanel2.setBackground(new java.awt.Color(255, 255, 204));

        jLabel13.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        jLabel13.setText("Customers");

        lblCustomers.setFont(new java.awt.Font("Poppins", 1, 48)); // NOI18N
        lblCustomers.setText("0");

        lblCustomersP.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        lblCustomersP.setText("+0%");

        lblCustomersW.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        lblCustomersW.setText("+0 this week");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(94, 94, 94))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lblCustomersP, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCustomersW, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblCustomers, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(19, 19, 19))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCustomers, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCustomersP)
                    .addComponent(lblCustomersW))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        DashPanel.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(265, 136, -1, -1));

        jPanel3.setBackground(new java.awt.Color(229, 229, 255));

        jLabel17.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        jLabel17.setText("Total Orders");

        lblTotalOrders.setFont(new java.awt.Font("Poppins", 1, 48)); // NOI18N
        lblTotalOrders.setText("0");

        lblTotalOrdersP.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        lblTotalOrdersP.setText("+0%");

        lblTotalOrdersW.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        lblTotalOrdersW.setText("+0 this week");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(85, 85, 85))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lblTotalOrdersP, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTotalOrdersW, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblTotalOrders, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(19, 19, 19))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalOrdersP)
                    .addComponent(lblTotalOrdersW))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        DashPanel.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(492, 136, -1, -1));

        jPanel4.setBackground(new java.awt.Color(255, 204, 204));

        jLabel21.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        jLabel21.setText("Refunded");

        lblRefunded.setFont(new java.awt.Font("Poppins", 1, 18)); // NOI18N
        lblRefunded.setText("0");

        lblRefundedP.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        lblRefundedP.setText("+0%");

        lblRefundedW.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        lblRefundedW.setText("+0 this week");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(105, 105, 105))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lblRefundedP, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblRefundedW, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblRefunded, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(19, 19, 19))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblRefunded, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRefundedP)
                    .addComponent(lblRefundedW))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        DashPanel.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(719, 136, -1, -1));

        jLabel25.setFont(new java.awt.Font("Crimson Pro", 1, 24)); // NOI18N
        jLabel25.setText("Revenue Stats");
        DashPanel.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(38, 321, -1, -1));

        RevenuePanel.setBackground(new java.awt.Color(240, 240, 240));

        javax.swing.GroupLayout RevenuePanelLayout = new javax.swing.GroupLayout(RevenuePanel);
        RevenuePanel.setLayout(RevenuePanelLayout);
        RevenuePanelLayout.setHorizontalGroup(
            RevenuePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 550, Short.MAX_VALUE)
        );
        RevenuePanelLayout.setVerticalGroup(
            RevenuePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 290, Short.MAX_VALUE)
        );

        DashPanel.add(RevenuePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(38, 375, -1, -1));

        cbSortRev.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        cbSortRev.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "This year", "This month", "This week" }));
        cbSortRev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSortRevActionPerformed(evt);
            }
        });
        DashPanel.add(cbSortRev, new org.netbeans.lib.awtextra.AbsoluteConstraints(489, 322, -1, 35));

        jLabel26.setFont(new java.awt.Font("Crimson Pro", 1, 24)); // NOI18N
        jLabel26.setText("Sales by Category");
        DashPanel.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(618, 321, -1, -1));

        CategoryPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout CategoryPanelLayout = new javax.swing.GroupLayout(CategoryPanel);
        CategoryPanel.setLayout(CategoryPanelLayout);
        CategoryPanelLayout.setHorizontalGroup(
            CategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        CategoryPanelLayout.setVerticalGroup(
            CategoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        DashPanel.add(CategoryPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(618, 375, 316, 290));

        cbSalesCat.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        cbSalesCat.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "This year", "This month", "This week" }));
        cbSalesCat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSalesCatActionPerformed(evt);
            }
        });
        DashPanel.add(cbSalesCat, new org.netbeans.lib.awtextra.AbsoluteConstraints(835, 322, -1, 35));

        Home.add(DashPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 529, 979, 691));

        HomePage.setViewportView(Home);

        Reservations.setBackground(new java.awt.Color(69, 79, 99));

        contents.setBackground(new java.awt.Color(250, 250, 250));
        contents.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel7.setBackground(new java.awt.Color(250, 250, 250));
        jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(150, 150, 150)));

        jPanel9.setBackground(new java.awt.Color(240, 240, 240));

        jLabel29.setFont(new java.awt.Font("Poppins", 1, 18)); // NOI18N
        jLabel29.setText("VIEW AVAILABILITY");

        cbTable.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        cbTable.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Table Number" }));

        dcDate.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N

        cbTime.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        cbTime.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM", "1:00 PM", "1:30 PM", "2:00 PM", "2:30 PM", "3:00 PM", "4:00 PM", "4:30 PM", "5:00 PM", "5:30 PM", "6:00 PM", "6:30 PM", "7:00 PM" }));

        btnSearchTable.setBackground(new java.awt.Color(40, 54, 102));
        btnSearchTable.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        btnSearchTable.setForeground(new java.awt.Color(255, 255, 255));
        btnSearchTable.setText("SEARCH");
        btnSearchTable.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSearchTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchTableActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap(27, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel29)
                        .addGap(102, 102, 102))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addComponent(dcDate, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(cbTime, 0, 102, Short.MAX_VALUE))
                            .addComponent(cbTable, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnSearchTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(29, 29, 29))))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel29)
                .addGap(18, 18, 18)
                .addComponent(cbTable, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(dcDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbTime, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(btnSearchTable, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        contents.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 140, 390, 260));

        jPanel8.setBackground(new java.awt.Color(250, 250, 250));
        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(150, 150, 150)));

        jLabel30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/Reservations.png"))); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        contents.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 120, -1, -1));

        jLabel32.setFont(new java.awt.Font("Crimson Pro", 1, 36)); // NOI18N
        jLabel32.setText("Reserve a Table");
        contents.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 30, -1, -1));

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        contents.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, 950, 10));

        jPanel10.setBackground(new java.awt.Color(69, 79, 99));

        jLabel33.setFont(new java.awt.Font("Crimson Pro", 1, 24)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(255, 255, 255));
        jLabel33.setText("Tables");

        tbTables.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        tbTables.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Table", "Capacity"
            }
        ));
        tbTables.setRowHeight(30);
        tbTables.getTableHeader().setResizingAllowed(false);
        tbTables.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tbTables);
        if (tbTables.getColumnModel().getColumnCount() > 0) {
            tbTables.getColumnModel().getColumn(0).setMinWidth(50);
            tbTables.getColumnModel().getColumn(0).setMaxWidth(50);
            tbTables.getColumnModel().getColumn(2).setMinWidth(60);
            tbTables.getColumnModel().getColumn(2).setMaxWidth(60);
        }

        jLabel34.setFont(new java.awt.Font("Crimson Pro", 1, 24)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(255, 255, 255));
        jLabel34.setText("Reservations");

        tbReservations.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        tbReservations.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Customer", "Table", "Date", "Guests", "Status"
            }
        ));
        tbReservations.setRowHeight(30);
        tbReservations.getTableHeader().setResizingAllowed(false);
        tbReservations.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(tbReservations);
        if (tbReservations.getColumnModel().getColumnCount() > 0) {
            tbReservations.getColumnModel().getColumn(0).setMinWidth(40);
            tbReservations.getColumnModel().getColumn(0).setMaxWidth(40);
            tbReservations.getColumnModel().getColumn(2).setMinWidth(60);
            tbReservations.getColumnModel().getColumn(2).setMaxWidth(60);
            tbReservations.getColumnModel().getColumn(4).setMinWidth(60);
            tbReservations.getColumnModel().getColumn(4).setMaxWidth(60);
            tbReservations.getColumnModel().getColumn(5).setMinWidth(100);
            tbReservations.getColumnModel().getColumn(5).setMaxWidth(100);
        }

        btnEditTable.setBackground(new java.awt.Color(153, 153, 255));
        btnEditTable.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        btnEditTable.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/edit-alt-regular-24.png"))); // NOI18N
        btnEditTable.setText(" Edit");
        btnEditTable.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEditTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditTableActionPerformed(evt);
            }
        });

        btnDeleteTable.setBackground(new java.awt.Color(255, 153, 153));
        btnDeleteTable.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        btnDeleteTable.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/trash-regular-24.png"))); // NOI18N
        btnDeleteTable.setText(" Delete");
        btnDeleteTable.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDeleteTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteTableActionPerformed(evt);
            }
        });

        btnAddTable.setBackground(new java.awt.Color(153, 255, 153));
        btnAddTable.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        btnAddTable.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/plus-regular-24.png"))); // NOI18N
        btnAddTable.setText(" Add");
        btnAddTable.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAddTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTableActionPerformed(evt);
            }
        });

        btnDelRes.setBackground(new java.awt.Color(255, 153, 153));
        btnDelRes.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        btnDelRes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/trash-regular-24.png"))); // NOI18N
        btnDelRes.setText(" Delete");
        btnDelRes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDelRes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelResActionPerformed(evt);
            }
        });

        btnConfirmed.setBackground(new java.awt.Color(153, 153, 255));
        btnConfirmed.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        btnConfirmed.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/edit-alt-regular-24.png"))); // NOI18N
        btnConfirmed.setText("Confirmed");
        btnConfirmed.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnConfirmed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfirmedActionPerformed(evt);
            }
        });

        txtSearchRes.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N

        lblSearchRes.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSearchRes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/search-regular-24.png"))); // NOI18N
        lblSearchRes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblSearchRes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSearchResMouseClicked(evt);
            }
        });

        btnCancelled.setBackground(new java.awt.Color(255, 153, 153));
        btnCancelled.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        btnCancelled.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/block-regular-24.png"))); // NOI18N
        btnCancelled.setText("Cancel");
        btnCancelled.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelledActionPerformed(evt);
            }
        });

        cbResStatus.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        cbResStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Pending", "Confirmed", "Completed", "Cancelled", " " }));
        cbResStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbResStatusActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(btnAddTable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEditTable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDeleteTable)
                        .addGap(48, 48, 48)
                        .addComponent(txtSearchRes)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSearchRes, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnConfirmed)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelled)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelRes))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel33)
                        .addGap(250, 250, 250)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addComponent(jLabel34)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cbResStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel34)
                            .addComponent(cbResStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel33)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnEditTable, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnDeleteTable, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnAddTable, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtSearchRes, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnConfirmed, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnDelRes, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnCancelled, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblSearchRes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout ReservationsLayout = new javax.swing.GroupLayout(Reservations);
        Reservations.setLayout(ReservationsLayout);
        ReservationsLayout.setHorizontalGroup(
            ReservationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(contents, javax.swing.GroupLayout.DEFAULT_SIZE, 979, Short.MAX_VALUE)
        );
        ReservationsLayout.setVerticalGroup(
            ReservationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ReservationsLayout.createSequentialGroup()
                .addComponent(contents, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        ReservationsPage.setViewportView(Reservations);

        contentPanel.setLayer(OrdersPage, javax.swing.JLayeredPane.DEFAULT_LAYER);
        contentPanel.setLayer(FoodsPage, javax.swing.JLayeredPane.DEFAULT_LAYER);
        contentPanel.setLayer(HomePage, javax.swing.JLayeredPane.DEFAULT_LAYER);
        contentPanel.setLayer(ReservationsPage, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout contentPanelLayout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(contentPanelLayout);
        contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(HomePage, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
            .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(FoodsPage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(ReservationsPage, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE))
            .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(OrdersPage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(HomePage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
            .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(FoodsPage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(ReservationsPage, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE))
            .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(OrdersPage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        mainPanel.add(contentPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 1000, 530));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void lblLogoutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblLogoutMouseClicked
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to Log Out?", "Log Out", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            Login login = Login.getInstance();
            login.setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_lblLogoutMouseClicked

    private void lblFoodsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblFoodsMouseClicked
        FoodsPage.setVisible(true);
        HomePage.setVisible(false);
        ReservationsPage.setVisible(false);
        OrdersPage.setVisible(false);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }//GEN-LAST:event_lblFoodsMouseClicked

    private void lblHomeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblHomeMouseClicked
        HomePage.setVisible(true);
        FoodsPage.setVisible(false);
        ReservationsPage.setVisible(false);
        OrdersPage.setVisible(false);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }//GEN-LAST:event_lblHomeMouseClicked

    private void lblReservationsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblReservationsMouseClicked
        ReservationsPage.setVisible(true);
        HomePage.setVisible(false);
        FoodsPage.setVisible(false);
        OrdersPage.setVisible(false);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }//GEN-LAST:event_lblReservationsMouseClicked

    private void lblOrdersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblOrdersMouseClicked
        OrdersPage.setVisible(true);
        ReservationsPage.setVisible(false);
        HomePage.setVisible(false);
        FoodsPage.setVisible(false);

        contentPanel.revalidate();
        contentPanel.repaint();
    }//GEN-LAST:event_lblOrdersMouseClicked

    private void btnBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookActionPerformed
        ReservationsPage.setVisible(true);
        HomePage.setVisible(false);
        FoodsPage.setVisible(false);
        OrdersPage.setVisible(false);

        contentPanel.revalidate();
        contentPanel.repaint();
    }//GEN-LAST:event_btnBookActionPerformed

    private void cbSortRevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSortRevActionPerformed
        createRevenueChart(cbSortRev.getSelectedItem().toString());
    }//GEN-LAST:event_cbSortRevActionPerformed

    private void cbSalesCatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSalesCatActionPerformed
        createCategoryChart(cbSalesCat.getSelectedItem().toString());
    }//GEN-LAST:event_cbSalesCatActionPerformed

    private void btnViewMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewMenuActionPerformed
        Menu menu = new Menu();
        menu.setVisible(true);
    }//GEN-LAST:event_btnViewMenuActionPerformed

    private void btnViewBundlesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewBundlesActionPerformed
        Bundles bundle = new Bundles();
        bundle.setVisible(true);
    }//GEN-LAST:event_btnViewBundlesActionPerformed

    private void btnSearchTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchTableActionPerformed
        checkTableAvailability();
    }//GEN-LAST:event_btnSearchTableActionPerformed

    private void btnAddTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTableActionPerformed
        addTable();
    }//GEN-LAST:event_btnAddTableActionPerformed

    private void btnEditTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditTableActionPerformed
        editTable();
    }//GEN-LAST:event_btnEditTableActionPerformed

    private void btnDeleteTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteTableActionPerformed
        deleteTable();
    }//GEN-LAST:event_btnDeleteTableActionPerformed

    private void btnConfirmedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmedActionPerformed
        updateReservationStatus("Confirmed");
    }//GEN-LAST:event_btnConfirmedActionPerformed

    private void btnCancelledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelledActionPerformed
        updateReservationStatus("Cancelled");
    }//GEN-LAST:event_btnCancelledActionPerformed

    private void btnDelResActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelResActionPerformed
        deleteReservation();
    }//GEN-LAST:event_btnDelResActionPerformed

    private void lblSearchResMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSearchResMouseClicked
        loadReservations("All", txtSearchRes.getText());
    }//GEN-LAST:event_lblSearchResMouseClicked

    private void cbResStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbResStatusActionPerformed
        loadReservations(cbResStatus.getSelectedItem().toString(), "");
    }//GEN-LAST:event_cbResStatusActionPerformed

    private void btnMarkAsPreparingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkAsPreparingActionPerformed
        updateOrderStatus("Preparing");
    }//GEN-LAST:event_btnMarkAsPreparingActionPerformed

    private void tbOrdersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbOrdersMouseClicked
        int row = tbOrders.getSelectedRow();
        if (row != -1) {
            int orderId = (int) tbOrders.getValueAt(row, 0);
            loadOrderItems(orderId);
        }
    }//GEN-LAST:event_tbOrdersMouseClicked

    private void btnAddOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddOrderActionPerformed
        String[] options = {"Menu", "Bundles"};
        int choice = JOptionPane.showOptionDialog(this,
                "Would you like to order from the Menu or Bundles?",
                "Select Order Type",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) {
            showMenu(true);  // Don't reopen cart initially
        } else if (choice == 1) {
            showBundles(true);
        }
    }//GEN-LAST:event_btnAddOrderActionPerformed

    private void btnMarkAsCompletedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkAsCompletedActionPerformed
        int row = tbOrders.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an order first.");
            return;
        }

        int orderId = (int) tbOrders.getValueAt(row, 0);
        int tableId = getTableIdForOrder(orderId);

        try (Connection conn = DBConnection.Connect(); PreparedStatement ps = conn.prepareStatement(
                "UPDATE orders SET status = 'Completed' WHERE order_id = ?")) {

            ps.setInt(1, orderId);
            ps.executeUpdate();

            // Mark the table as "Available"
            try (PreparedStatement updateTable = conn.prepareStatement(
                    "UPDATE tables SET status = 'Available' WHERE table_id = ?")) {
                updateTable.setInt(1, tableId);
                updateTable.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Order marked as Completed.");
            loadOrders();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnMarkAsCompletedActionPerformed

    private void btnMarkAsServedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkAsServedActionPerformed
        updateOrderStatus("Served");
    }//GEN-LAST:event_btnMarkAsServedActionPerformed

    private void btnCancelOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelOrderActionPerformed
        updateOrderStatus("Cancelled");
    }//GEN-LAST:event_btnCancelOrderActionPerformed

    private void btnRefundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefundActionPerformed
        int row = tbOrders.getSelectedRow();
        if (row != -1) {
            String status = tbOrders.getValueAt(row, 3).toString();
            if (!status.equals("Cancelled")) {
                JOptionPane.showMessageDialog(this, "Only Cancelled orders can be refunded.");
            } else {
                updateOrderStatus("Refunded");
            }
        }
    }//GEN-LAST:event_btnRefundActionPerformed

    private void cbOrderSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbOrderSortActionPerformed
        String selectedStatus = cbOrderSort.getSelectedItem().toString();
        loadSortedOrders(selectedStatus);
    }//GEN-LAST:event_cbOrderSortActionPerformed

    private void tbItemsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbItemsMouseClicked
        int row = tbItems.getSelectedRow();
        if (row != -1) {
            lblItemName.setText(tbItems.getValueAt(row, 0).toString());
            lblItemPrice.setText(tbItems.getValueAt(row, 2).toString());
            showItemImage();
        }
    }//GEN-LAST:event_tbItemsMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CategoryPanel;
    private javax.swing.JPanel DashPanel;
    private javax.swing.JPanel FoodsPage;
    private javax.swing.JPanel Home;
    private javax.swing.JScrollPane HomePage;
    private javax.swing.JPanel OrdersPage;
    private javax.swing.JPanel OrdersPanel;
    private javax.swing.JPanel Reservations;
    private javax.swing.JScrollPane ReservationsPage;
    private javax.swing.JPanel RevenuePanel;
    private javax.swing.JPanel alaCartePanel;
    private javax.swing.JButton btnAddOrder;
    private javax.swing.JButton btnAddTable;
    private javax.swing.JButton btnBook;
    private javax.swing.JButton btnCancelOrder;
    private javax.swing.JButton btnCancelled;
    private javax.swing.JButton btnConfirmed;
    private javax.swing.JButton btnDelRes;
    private javax.swing.JButton btnDeleteTable;
    private javax.swing.JButton btnEditTable;
    private javax.swing.JButton btnMarkAsCompleted;
    private javax.swing.JButton btnMarkAsPreparing;
    private javax.swing.JButton btnMarkAsServed;
    private javax.swing.JButton btnRefund;
    private javax.swing.JButton btnSearchTable;
    private javax.swing.JButton btnViewBundles;
    private javax.swing.JButton btnViewMenu;
    private javax.swing.JPanel buffetPanel;
    private javax.swing.JComboBox<String> cbOrderSort;
    private javax.swing.JComboBox<String> cbResStatus;
    private javax.swing.JComboBox<String> cbSalesCat;
    private javax.swing.JComboBox<String> cbSortRev;
    private javax.swing.JComboBox<String> cbTable;
    private javax.swing.JComboBox<String> cbTime;
    private javax.swing.JPanel contentPane;
    private javax.swing.JLayeredPane contentPanel;
    private javax.swing.JPanel contents;
    private com.toedter.calendar.JDateChooser dcDate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblBuffet;
    private javax.swing.JLabel lblBuffet1;
    private javax.swing.JLabel lblCustomers;
    private javax.swing.JLabel lblCustomersP;
    private javax.swing.JLabel lblCustomersW;
    private javax.swing.JLabel lblFoods;
    private javax.swing.JLabel lblHome;
    private javax.swing.JLabel lblItemName;
    private javax.swing.JLabel lblItemPic;
    private javax.swing.JLabel lblItemPrice;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblLogout;
    private javax.swing.JLabel lblOrders;
    private javax.swing.JLabel lblRefunded;
    private javax.swing.JLabel lblRefundedP;
    private javax.swing.JLabel lblRefundedW;
    private javax.swing.JLabel lblReservations;
    private javax.swing.JLabel lblSearchRes;
    private javax.swing.JLabel lblTotalOrders;
    private javax.swing.JLabel lblTotalOrdersP;
    private javax.swing.JLabel lblTotalOrdersW;
    private javax.swing.JLabel lblTotalSales;
    private javax.swing.JLabel lblTotalSalesP;
    private javax.swing.JLabel lblTotalSalesW;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTable tbItems;
    private javax.swing.JTable tbOrders;
    private javax.swing.JTable tbReservations;
    private javax.swing.JTable tbTables;
    private javax.swing.JTextField txtSearchRes;
    // End of variables declaration//GEN-END:variables
}
