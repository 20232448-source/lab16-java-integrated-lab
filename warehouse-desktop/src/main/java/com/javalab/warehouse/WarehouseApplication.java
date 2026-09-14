package com.javalab.warehouse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class WarehouseApplication extends JFrame {
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Khach hang", "Tong tien", "Trang thai", "Version"}, 0);
    private final JTable orders = new JTable(model);
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"ALL", "PENDING", "PROCESSING", "READY", "SHIPPING", "COMPLETED", "CANCELLED"});
    private final JLabel detail = new JLabel("Chon mot don hang de xem chi tiet");

    public WarehouseApplication() {
        super("Warehouse Desktop - Quan ly don hang");
        setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(980, 560); setLocationRelativeTo(null);
        JButton refresh = new JButton("Tai don"); JButton process = new JButton("Tiep nhan");
        JButton ready = new JButton("Dong goi READY"); JButton shipping = new JButton("Ban giao SHIPPING"); JButton completed = new JButton("Hoan tat");
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Loc:")); toolbar.add(statusFilter); toolbar.add(refresh); toolbar.add(process); toolbar.add(ready); toolbar.add(shipping); toolbar.add(completed);
        detail.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        add(toolbar, BorderLayout.NORTH); add(new JScrollPane(orders), BorderLayout.CENTER); add(detail, BorderLayout.SOUTH);
        refresh.addActionListener(e -> loadOrders()); statusFilter.addActionListener(e -> loadOrders());
        process.addActionListener(e -> changeStatus("PROCESSING")); ready.addActionListener(e -> changeStatus("READY")); shipping.addActionListener(e -> changeStatus("SHIPPING")); completed.addActionListener(e -> changeStatus("COMPLETED"));
        orders.getSelectionModel().addListSelectionListener(e -> showDetails()); loadOrders();
    }

    private Connection open() throws SQLException { return DriverManager.getConnection("jdbc:mysql://localhost:3306/java_integrated_lab?serverTimezone=UTC", "root", ""); }

    private void loadOrders() {
        model.setRowCount(0); String selected = (String) statusFilter.getSelectedItem(); String filter = "ALL".equals(selected) ? "" : " WHERE o.status=?";
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement("SELECT o.id,u.full_name,o.total_amount,o.status,o.version FROM orders o JOIN users u ON u.id=o.customer_id" + filter + " ORDER BY o.created_at")) {
            if (!filter.isEmpty()) statement.setString(1, selected);
            try (ResultSet result = statement.executeQuery()) { while (result.next()) model.addRow(new Object[]{result.getLong(1), result.getString(2), result.getBigDecimal(3), result.getString(4), result.getInt(5)}); }
        } catch (SQLException exception) { showError(exception); }
    }

    private void showDetails() {
        int row = orders.getSelectedRow(); if (row < 0) return; long orderId = ((Number) model.getValueAt(row, 0)).longValue();
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement("SELECT p.name,oi.quantity FROM order_items oi JOIN products p ON p.id=oi.product_id WHERE oi.order_id=?")) {
            statement.setLong(1, orderId); StringBuilder text = new StringBuilder("Don #").append(orderId).append(": ");
            try (ResultSet result = statement.executeQuery()) { while (result.next()) text.append(result.getString(1)).append(" x").append(result.getInt(2)).append("; "); } detail.setText(text.toString());
        } catch (SQLException exception) { showError(exception); }
    }

    private void changeStatus(String target) {
        int row = orders.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(this, "Hay chon don hang."); return; }
        long orderId = ((Number) model.getValueAt(row, 0)).longValue(); int version = ((Number) model.getValueAt(row, 4)).intValue(); String oldStatus = String.valueOf(model.getValueAt(row, 3));
        if (!validTransition(oldStatus, target)) { JOptionPane.showMessageDialog(this, "Khong the chuyen " + oldStatus + " sang " + target); return; }
        try (Connection connection = open()) {
            connection.setAutoCommit(false);
            try (PreparedStatement update = connection.prepareStatement("UPDATE orders SET status=?,version=version+1 WHERE id=? AND version=?")) {
                update.setString(1, target); update.setLong(2, orderId); update.setInt(3, version); if (update.executeUpdate() == 0) throw new SQLException("Don hang da bi cap nhat boi nguoi khac");
            }
            try (PreparedStatement history = connection.prepareStatement("INSERT INTO order_status_history(order_id,old_status,new_status,note) VALUES(?,?,?,?)")) {
                history.setLong(1, orderId); history.setString(2, oldStatus); history.setString(3, target); history.setString(4, "Warehouse desktop"); history.executeUpdate();
            }
            connection.commit(); loadOrders();
        } catch (SQLException exception) { showError(exception); }
    }

    private boolean validTransition(String from, String to) { return (from.equals("PENDING") && (to.equals("PROCESSING") || to.equals("CANCELLED"))) || (from.equals("PROCESSING") && to.equals("READY")) || (from.equals("READY") && to.equals("SHIPPING")) || (from.equals("SHIPPING") && to.equals("COMPLETED")); }
    private void showError(Exception exception) { JOptionPane.showMessageDialog(this, exception.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE); }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new WarehouseApplication().setVisible(true)); }
}
