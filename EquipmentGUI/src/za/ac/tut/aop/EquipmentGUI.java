package za.ac.tut.aop;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * EquipmentGUI
 * 
 * Full Swing CRUD GUI for APP.TBL_EQUIPMENT with explicit manual database connection handling.
 */
public class EquipmentGUI extends JFrame {

    // Database credentials configured for Apache Derby (DailyDB/LABDB)
    private static final String DB_URL = "jdbc:derby://localhost:1527/DailyDB";
    private static final String DB_USER = "app";
    private static final String DB_PASSWORD = "123";

    private Connection conn;

    // Top Connection Bar Controls
    private JButton btnConnect;
    private JLabel lblConnectionStatus;

    // Input Form Controls
    private JTextField tfId;
    private JTextField tfName;
    private JTextField tfDepartment;
    private JTextField tfPurchaseDate;
    private JTextField tfCondition;
    private JCheckBox ckbInUse;
    private JTextField tfPrice;

    // CRUD Action Buttons
    private JButton btnInsert;
    private JButton btnViewAll;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    // Status Label
    private JLabel lblStatus;

    // JTable for display
    private JTable tblEquipment;
    private DefaultTableModel tableModel;

    public EquipmentGUI() {
        setTitle("Equipment CRUD Practice - Derby Database");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= NORTH: Connection Bar & Input Form =================
        JPanel northContainer = new JPanel(new BorderLayout());

        // Connection Panel
        JPanel connectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnConnect = new JButton("Connect to Database");
        lblConnectionStatus = new JLabel("Status: Disconnected (Click 'Connect' to start)");
        btnConnect.addActionListener(new BtnConnectListener());

        connectPanel.add(btnConnect);
        connectPanel.add(lblConnectionStatus);
        northContainer.add(connectPanel, BorderLayout.NORTH);

        // Input Form Panel
        JPanel formPanel = new JPanel(new GridLayout(4, 4, 8, 8));

        JLabel lblId = new JLabel("Equipment ID:");
        tfId = new JTextField();
        tfId.setEditable(false);

        JLabel lblName = new JLabel("Name:");
        tfName = new JTextField();

        JLabel lblDepartment = new JLabel("Department:");
        tfDepartment = new JTextField();

        JLabel lblPurchaseDate = new JLabel("Purchase Date (yyyy-MM-dd):");
        tfPurchaseDate = new JTextField();

        JLabel lblCondition = new JLabel("Condition:");
        tfCondition = new JTextField();

        JLabel lblInUse = new JLabel("In Use:");
        ckbInUse = new JCheckBox();

        JLabel lblPrice = new JLabel("Price:");
        tfPrice = new JTextField();

        formPanel.add(lblId);
        formPanel.add(tfId);
        formPanel.add(lblName);
        formPanel.add(tfName);

        formPanel.add(lblDepartment);
        formPanel.add(tfDepartment);
        formPanel.add(lblPurchaseDate);
        formPanel.add(tfPurchaseDate);

        formPanel.add(lblCondition);
        formPanel.add(tfCondition);
        formPanel.add(lblInUse);
        formPanel.add(ckbInUse);

        formPanel.add(lblPrice);
        formPanel.add(tfPrice);
        formPanel.add(new JLabel(""));
        formPanel.add(new JLabel(""));

        northContainer.add(formPanel, BorderLayout.SOUTH);
        add(northContainer, BorderLayout.NORTH);

        // ================= CENTER: JTable Setup =================
        String[] columnNames = {
            "EQUIPMENT_ID", "NAME", "DEPARTMENT", "PURCHASE_DATE",
            "CONDITION", "IN_USE", "PRICE"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblEquipment = new JTable(tableModel);

        tblEquipment.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = tblEquipment.getSelectedRow();
                if (selectedRow != -1) {
                    tfId.setText(tableModel.getValueAt(selectedRow, 0).toString());
                    tfName.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    tfDepartment.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    tfPurchaseDate.setText(tableModel.getValueAt(selectedRow, 3).toString());
                    tfCondition.setText(tableModel.getValueAt(selectedRow, 4).toString());
                    ckbInUse.setSelected((Boolean) tableModel.getValueAt(selectedRow, 5));
                    tfPrice.setText(tableModel.getValueAt(selectedRow, 6).toString());
                    lblStatus.setText("Status: Row selected (ID: " + tfId.getText() + ")");
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(tblEquipment);
        add(tableScrollPane, BorderLayout.CENTER);

        // ================= SOUTH: CRUD Buttons & System Status =================
        JPanel southPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());

        btnInsert = new JButton("Insert");
        btnViewAll = new JButton("View All");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear Fields");

        btnInsert.addActionListener(new BtnInsertListener());
        btnViewAll.addActionListener(new BtnViewAllListener());
        btnUpdate.addActionListener(new BtnUpdateListener());
        btnDelete.addActionListener(new BtnDeleteListener());
        btnClear.addActionListener(new BtnClearListener());

        buttonPanel.add(btnInsert);
        buttonPanel.add(btnViewAll);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        southPanel.add(buttonPanel, BorderLayout.NORTH);

        lblStatus = new JLabel("Status: Awaiting database connection...");
        southPanel.add(lblStatus, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);

        // Lock form components until database connects
        setFormEnabled(false);
    }

    private void setFormEnabled(boolean enabled) {
        tfName.setEnabled(enabled);
        tfDepartment.setEnabled(enabled);
        tfPurchaseDate.setEnabled(enabled);
        tfCondition.setEnabled(enabled);
        ckbInUse.setEnabled(enabled);
        tfPrice.setEnabled(enabled);

        btnInsert.setEnabled(enabled);
        btnViewAll.setEnabled(enabled);
        btnUpdate.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        btnClear.setEnabled(enabled);
        tblEquipment.setEnabled(enabled);
    }

    private void loadEquipmentData() {
        if (conn == null) {
            return;
        }
        String sql = "SELECT EQUIPMENT_ID, NAME, DEPARTMENT, PURCHASE_DATE, \"CONDITION\", IN_USE, PRICE "
                + "FROM TBL_EQUIPMENT ORDER BY 2 DESC";

        try (Statement state = conn.createStatement();
             ResultSet rs = state.executeQuery(sql)) {

            tableModel.setRowCount(0);
            while (rs.next()) {
                int id = rs.getInt("EQUIPMENT_ID");
                String name = rs.getString("NAME");
                String dept = rs.getString("DEPARTMENT");
                java.sql.Date sqlDate = rs.getDate("PURCHASE_DATE");
                String cond = rs.getString(5);
                boolean inUse = rs.getBoolean(6);
                double price = rs.getDouble(7);
                Object[] row = {id, name, dept, sqlDate, cond, inUse, price};
                tableModel.addRow(row);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    private void clearFormFields() {
        tfId.setText("");
        tfName.setText("");
        tfDepartment.setText("");
        tfPurchaseDate.setText("");
        tfCondition.setText("");
        ckbInUse.setSelected(false);
        tfPrice.setText("");
        tblEquipment.clearSelection();
    }

    // =========================================================
    //  Action Listeners
    // =========================================================

    private class BtnConnectListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                lblConnectionStatus.setText("Status: Connected TO THE DATABASE");
                btnConnect.setEnabled(false);
                setFormEnabled(true);

                loadEquipmentData();

            } catch (SQLException ex) {
                lblConnectionStatus.setText("Status: Not Connected");
                lblStatus.setText("Status: " + ex.getMessage());
                JOptionPane.showMessageDialog(null, "Failed to connect database!! \n" + ex.getMessage());
            }
        }
    }

    private class BtnViewAllListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            clearFormFields();
            loadEquipmentData();
        }
    }

    private class BtnInsertListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (tfName.getText().trim().isEmpty() || tfDepartment.getText().trim().isEmpty()
                    || tfPurchaseDate.getText().trim().isEmpty() || tfPrice.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(EquipmentGUI.this, "Please fill in Name, Department, Date, and Price.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Trying to get the initial values of the placeholders.
            String n = tfName.getText().trim();
            String d = tfDepartment.getText().trim();
            String condition = tfCondition.getText().trim();
            boolean isUsed = ckbInUse.isSelected();

            java.sql.Date fdte;
            double p;
            try {
                // for the date, we have to get the local date first then convert it to sql date.
                LocalDate dte = LocalDate.parse(tfPurchaseDate.getText().trim());
                fdte = Date.valueOf(dte);
                p = Double.parseDouble(tfPrice.getText().trim());
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(EquipmentGUI.this,
                        "Please enter the date as yyyy-MM-dd.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(EquipmentGUI.this,
                        "Please enter a valid numeric price.", "Invalid Price", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Now we need the SQL statement
            String slq = "INSERT INTO TBL_EQUIPMENT (NAME, DEPARTMENT, PURCHASE_DATE, CONDITION, IN_USE, PRICE) "
                    + "VALUES(?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(slq)) {
                ps.setString(1, n);
                ps.setString(2, d);
                ps.setDate(3, fdte);
                ps.setString(4, condition);
                ps.setBoolean(5, isUsed);
                ps.setDouble(6, p);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 1) {
                    JOptionPane.showMessageDialog(null, "Record ADDED");
                    btnViewAll.doClick();
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        }
    }

    private class BtnUpdateListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String idText = tfId.getText().trim();
            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(EquipmentGUI.this, "Please select an item from the table to update.",
                        "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id;
            String n = tfName.getText().trim();
            String d = tfDepartment.getText().trim();
            String condition = tfCondition.getText().trim();
            boolean isUsed = ckbInUse.isSelected();
            java.sql.Date fdte;
            double p;

            try {
                id = Integer.parseInt(idText);
                // for the date, we have to get the local date first then convert it to sql date.
                LocalDate dte = LocalDate.parse(tfPurchaseDate.getText().trim());
                fdte = Date.valueOf(dte);
                p = Double.parseDouble(tfPrice.getText().trim());
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(EquipmentGUI.this,
                        "Please enter the date as yyyy-MM-dd.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(EquipmentGUI.this,
                        "Please enter a valid numeric price.", "Invalid Price", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String dml = "UPDATE TBL_EQUIPMENT SET NAME = ?, DEPARTMENT = ?, PURCHASE_DATE = ?, \"CONDITION\" = ?, IN_USE = ?, PRICE = ? "
                    + "WHERE EQUIPMENT_ID = ?";

            try (PreparedStatement ps = conn.prepareStatement(dml)) {
                ps.setString(1, n);
                ps.setString(2, d);
                ps.setDate(3, fdte);
                ps.setString(4, condition);
                ps.setBoolean(5, isUsed);
                ps.setDouble(6, p);
                // Since we're updating values, we need the current id of the value we want to update.
                ps.setInt(7, id);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 1) {
                    JOptionPane.showMessageDialog(null, "Row Updated");
                    btnViewAll.doClick();
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        }
    }

    private class BtnDeleteListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String idText = tfId.getText().trim();
            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(EquipmentGUI.this, "Please select an item from the table to delete.",
                        "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(EquipmentGUI.this,
                        "Selected row has an invalid ID.", "Invalid Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(null, "Delete this record?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            String dml = "DELETE FROM TBL_EQUIPMENT WHERE EQUIPMENT_ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(dml)) {
                ps.setInt(1, id);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 1) {
                    JOptionPane.showMessageDialog(null, "Row deleted successfully.");
                    btnViewAll.doClick();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        }
    }

    private class BtnClearListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            clearFormFields();
            lblStatus.setText("Status: Form cleared.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                EquipmentGUI gui = new EquipmentGUI();
                gui.setVisible(true);
            }
        });
    }
}