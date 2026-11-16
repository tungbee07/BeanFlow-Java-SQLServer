import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PosFrame extends JFrame {
    private OrderDAO dao = new OrderDAO();

    // Bên trái: Thực đơn đang bán
    private JTextField txtSearch = new JTextField(14);
    private JTextField txtQty = new JTextField("1", 4);
    private DefaultTableModel menuModel;
    private JTable tblMenu;

    // Bên phải: Giỏ hàng & Thanh toán
    private JComboBox<ComboItem> cboEmployee = new JComboBox<>();
    private JTextField txtNote = new JTextField(20);
    private JLabel lblTotal = new JLabel("Tổng tiền: 0 VNĐ");
    private DefaultTableModel cartModel;
    private JTable tblCart;

    public PosFrame() {
        setTitle("BeanFlow - Bán hàng tại quầy (POS)");
        setSize(1050, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2, 10, 10)); // Chia đôi màn hình trái - phải

        initUI();
        loadEmployees();
        loadMenuData();
    }

    private void initUI() {
        // ================= PANEL TRÁI: THỰC ĐƠN =================
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("1. Chọn đồ uống đang bán"));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnSearch = new JButton("Tìm món");
        searchPanel.add(new JLabel("Tên món:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        String[] menuCols = {"Mã", "Tên đồ uống", "Đơn giá (VNĐ)", "Danh mục"};
        menuModel = new DefaultTableModel(menuCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblMenu = new JTable(menuModel);
        tblMenu.setRowHeight(25);
        leftPanel.add(new JScrollPane(tblMenu), BorderLayout.CENTER);

        JPanel addCartPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAddToCart = new JButton("Thêm vào giỏ hàng >>");
        addCartPanel.add(new JLabel("Số lượng:"));
        addCartPanel.add(txtQty);
        addCartPanel.add(btnAddToCart);
        leftPanel.add(addCartPanel, BorderLayout.SOUTH);

        // ================= PANEL PHẢI: GIỎ HÀNG =================
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("2. Giỏ hàng & Thanh toán"));

        JPanel empPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        empPanel.add(new JLabel("Nhân viên lập đơn:"));
        empPanel.add(cboEmployee);
        rightPanel.add(empPanel, BorderLayout.NORTH);

        String[] cartCols = {"Mã", "Tên món", "Đơn giá", "SL", "Thành tiền"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCart = new JTable(cartModel);
        tblCart.setRowHeight(25);
        rightPanel.add(new JScrollPane(tblCart), BorderLayout.CENTER);

        // Khu vực chỉnh giỏ + ghi chú + tổng tiền + nút thanh toán
        JPanel bottomRight = new JPanel(new GridLayout(3, 1, 5, 5));

        JPanel cartActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnUpdateQty = new JButton("Đổi số lượng");
        JButton btnRemoveItem = new JButton("Xóa món");
        JButton btnClearCart = new JButton("Làm mới giỏ");
        cartActionPanel.add(btnUpdateQty);
        cartActionPanel.add(btnRemoveItem);
        cartActionPanel.add(btnClearCart);

        JPanel notePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        notePanel.add(new JLabel("Ghi chú đơn:"));
        notePanel.add(txtNote);

        JPanel payPanel = new JPanel(new BorderLayout());
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setForeground(new Color(180, 40, 20));
        JButton btnCheckout = new JButton("Xác nhận thanh toán & Lưu đơn");
        btnCheckout.setFont(new Font("Arial", Font.BOLD, 13));
        payPanel.add(lblTotal, BorderLayout.WEST);
        payPanel.add(btnCheckout, BorderLayout.EAST);

        bottomRight.add(cartActionPanel);
        bottomRight.add(notePanel);
        bottomRight.add(payPanel);
        rightPanel.add(bottomRight, BorderLayout.SOUTH);

        add(leftPanel);
        add(rightPanel);

        // ================= SỰ KIỆN =================
        btnSearch.addActionListener(e -> loadMenuData());
        btnAddToCart.addActionListener(e -> handleAddToCart());
        btnUpdateQty.addActionListener(e -> handleUpdateQty());
        btnRemoveItem.addActionListener(e -> handleRemoveItem());
        btnClearCart.addActionListener(e -> clearCart());
        btnCheckout.addActionListener(e -> handleCheckout());
    }

    private void loadEmployees() {
        cboEmployee.removeAllItems();
        for (ComboItem item : dao.getActiveEmployees()) {
            cboEmployee.addItem(item);
        }
    }

    private void loadMenuData() {
        menuModel.setRowCount(0);
        for (Object[] row : dao.getAvailableProducts(txtSearch.getText().trim())) {
            menuModel.addRow(row);
        }
    }

    private double parseMoney(String text) {
        return Double.parseDouble(text.replace(",", "").replace(".", "").trim());
    }

    private void handleAddToCart() {
        int row = tblMenu.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một món ở thực đơn bên trái!");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(txtQty.getText().trim());
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Số lượng phải lớn hơn 0!");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên hợp lệ!");
            return;
        }

        int productId = Integer.parseInt(menuModel.getValueAt(row, 0).toString());
        String productName = menuModel.getValueAt(row, 1).toString();
        double price = parseMoney(menuModel.getValueAt(row, 2).toString());

        // Nếu món đã có trong giỏ thì cộng dồn số lượng (tránh trùng dòng)
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int existingId = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
            if (existingId == productId) {
                int oldQty = Integer.parseInt(cartModel.getValueAt(i, 3).toString());
                int newQty = oldQty + qty;
                cartModel.setValueAt(newQty, i, 3);
                cartModel.setValueAt(String.format("%,.0f", price * newQty), i, 4);
                updateGrandTotal();
                txtQty.setText("1");
                return;
            }
        }

        // Nếu chưa có trong giỏ thì thêm dòng mới
        double lineTotal = price * qty;
        cartModel.addRow(new Object[]{
            productId, productName, String.format("%,.0f", price), qty, String.format("%,.0f", lineTotal)
        });
        updateGrandTotal();
        txtQty.setText("1");
    }

    private void handleUpdateQty() {
        int row = tblCart.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn 1 món trong giỏ hàng để đổi số lượng!");
            return;
        }
        String input = JOptionPane.showInputDialog(this, "Nhập số lượng mới:", cartModel.getValueAt(row, 3));
        if (input == null) return;
        try {
            int newQty = Integer.parseInt(input.trim());
            if (newQty <= 0) {
                JOptionPane.showMessageDialog(this, "Số lượng phải lớn hơn 0 (hoặc bấm Xóa món)!");
                return;
            }
            double price = parseMoney(cartModel.getValueAt(row, 2).toString());
            cartModel.setValueAt(newQty, row, 3);
            cartModel.setValueAt(String.format("%,.0f", price * newQty), row, 4);
            updateGrandTotal();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ!");
        }
    }

    private void handleRemoveItem() {
        int row = tblCart.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn 1 món trong giỏ hàng để xóa!");
            return;
        }
        cartModel.removeRow(row);
        updateGrandTotal();
    }

    private void clearCart() {
        cartModel.setRowCount(0);
        txtNote.setText("");
        updateGrandTotal();
    }

    private double updateGrandTotal() {
        double total = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            total += parseMoney(cartModel.getValueAt(i, 4).toString());
        }
        lblTotal.setText("Tổng tiền: " + String.format("%,.0f", total) + " VNĐ");
        return total;
    }

    private void handleCheckout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống, vui lòng chọn đồ uống!");
            return;
        }
        ComboItem emp = (ComboItem) cboEmployee.getSelectedItem();
        if (emp == null) {
            JOptionPane.showMessageDialog(this, "Chưa có nhân viên đang làm việc để lập đơn!");
            return;
        }

        double totalAmount = updateGrandTotal();
        String note = txtNote.getText().trim();

        List<Object[]> items = new ArrayList<>();
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int productId = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
            int qty = Integer.parseInt(cartModel.getValueAt(i, 3).toString());
            double unitPrice = parseMoney(cartModel.getValueAt(i, 2).toString());
            items.add(new Object[]{productId, qty, unitPrice});
        }

        int newOrderId = dao.createOrder(emp.getId(), totalAmount, note, items);
        if (newOrderId > 0) {
            JOptionPane.showMessageDialog(this,
                "Thanh toán thành công!\nMã hóa đơn: #" + newOrderId + "\nTổng tiền: " + String.format("%,.0f", totalAmount) + " VNĐ");
            clearCart();
        } else {
            JOptionPane.showMessageDialog(this, "Lưu đơn hàng thất bại (đã rollback)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new PosFrame().setVisible(true));
    }
}