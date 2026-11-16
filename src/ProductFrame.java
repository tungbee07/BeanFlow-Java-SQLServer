import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ProductFrame extends JFrame {
    private ProductDAO dao = new ProductDAO();

    // Các ô nhập liệu
    private JTextField txtId = new JTextField(5);
    private JTextField txtName = new JTextField(18);
    private JTextField txtPrice = new JTextField(10);
    private JComboBox<CategoryItem> cboCategory = new JComboBox<>();

    // Thanh tìm kiếm và lọc
    private JTextField txtSearch = new JTextField(15);
    private JComboBox<CategoryItem> cboFilterCategory = new JComboBox<>();

    // Bảng dữ liệu
    private DefaultTableModel tableModel;
    private JTable tblProducts;

    public ProductFrame() {
        setTitle("BeanFlow - Quản lý Đồ uống");
        setSize(850, 550);
        setLocationRelativeTo(null); // Căn giữa màn hình
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initUI();
        loadCategories();
        loadTableData();
    }

    private void initUI() {
        // --- PHẦN TRÊN (NORTH): Khung nhập liệu + Tìm kiếm ---
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // Form nhập 2 hàng 4 cột
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin đồ uống"));
        txtId.setEditable(false); // Mã tự tăng, không cho sửa tay

        formPanel.add(new JLabel("Mã món:"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Tên đồ uống:"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Giá bán (VNĐ):"));
        formPanel.add(txtPrice);
        formPanel.add(new JLabel("Danh mục:"));
        formPanel.add(cboCategory);

        // Thanh tìm kiếm và lọc
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnSearch = new JButton("Tìm kiếm");
        searchPanel.add(new JLabel("Tìm tên món:"));
        searchPanel.add(txtSearch);
        searchPanel.add(new JLabel("Lọc danh mục:"));
        searchPanel.add(cboFilterCategory);
        searchPanel.add(btnSearch);

        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(searchPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // --- PHẦN GIỮA (CENTER): Bảng JTable ---
        String[] columns = {"Mã món", "Tên đồ uống", "Giá bán (VNĐ)", "Danh mục", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp trên ô bảng
            }
        };
        tblProducts = new JTable(tableModel);
        tblProducts.setRowHeight(25);
        add(new JScrollPane(tblProducts), BorderLayout.CENTER);

        // --- PHẦN DƯỚI (SOUTH): Các nút chức năng ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnAdd = new JButton("Thêm món");
        JButton btnUpdate = new JButton("Sửa món");
        JButton btnToggle = new JButton("Ngừng bán / Bán lại");
        JButton btnClear = new JButton("Làm mới");

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnToggle);
        btnPanel.add(btnClear);
        add(btnPanel, BorderLayout.SOUTH);

        // --- GẮN SỰ KIỆN CHO CÁC NÚT & BẢNG ---
        btnSearch.addActionListener(e -> loadTableData());
        cboFilterCategory.addActionListener(e -> loadTableData());

        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnToggle.addActionListener(e -> handleToggleStatus());
        btnClear.addActionListener(e -> clearForm());

        // Bấm vào 1 dòng trên bảng -> Đổ dữ liệu ngược lên ô nhập
        tblProducts.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblProducts.getSelectedRow();
                if (row >= 0) {
                    txtId.setText(tableModel.getValueAt(row, 0).toString());
                    txtName.setText(tableModel.getValueAt(row, 1).toString());
                    // Bỏ dấu phẩy ngăn cách hàng nghìn (29,000 -> 29000) để sửa số
                    String rawPrice = tableModel.getValueAt(row, 2).toString().replace(",", "").replace(".", "");
                    txtPrice.setText(rawPrice);

                    String catName = tableModel.getValueAt(row, 3).toString();
                    for (int i = 0; i < cboCategory.getItemCount(); i++) {
                        if (cboCategory.getItemAt(i).getName().equals(catName)) {
                            cboCategory.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void loadCategories() {
        cboCategory.removeAllItems();
        cboFilterCategory.removeAllItems();
        cboFilterCategory.addItem(new CategoryItem(0, "-- Tất cả danh mục --"));

        List<CategoryItem> list = dao.getAllCategories();
        for (CategoryItem item : list) {
            cboCategory.addItem(item);
            cboFilterCategory.addItem(item);
        }
    }

    private void loadTableData() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ trên bảng
        String keyword = txtSearch.getText().trim();
        CategoryItem selectedFilter = (CategoryItem) cboFilterCategory.getSelectedItem();
        int catId = (selectedFilter != null) ? selectedFilter.getId() : 0;

        List<Object[]> rows = dao.getProducts(keyword, catId);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
    }

    private void handleAdd() {
        String name = txtName.getText().trim();
        String priceStr = txtPrice.getText().trim();
        if (name.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tên và giá đồ uống!");
            return;
        }
        try {
            double price = Double.parseDouble(priceStr);
            if (price < 0) {
                JOptionPane.showMessageDialog(this, "Giá bán không được là số âm!");
                return;
            }
            CategoryItem cat = (CategoryItem) cboCategory.getSelectedItem();
            if (dao.insertProduct(name, price, cat.getId())) {
                JOptionPane.showMessageDialog(this, "Thêm đồ uống thành công!");
                loadTableData();
                clearForm();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá bán phải là số hợp lệ!");
        }
    }

    private void handleUpdate() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một món trong bảng trước khi sửa!");
            return;
        }
        String name = txtName.getText().trim();
        String priceStr = txtPrice.getText().trim();
        if (name.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên và giá không được để trống!");
            return;
        }
        try {
            int id = Integer.parseInt(txtId.getText());
            double price = Double.parseDouble(priceStr);
            if (price < 0) {
                JOptionPane.showMessageDialog(this, "Giá bán không được âm!");
                return;
            }
            CategoryItem cat = (CategoryItem) cboCategory.getSelectedItem();
            if (dao.updateProduct(id, name, price, cat.getId())) {
                JOptionPane.showMessageDialog(this, "Cập nhật đồ uống thành công!");
                loadTableData();
                clearForm();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá bán phải là số hợp lệ!");
        }
    }

    private void handleToggleStatus() {
        int row = tblProducts.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một món trong bảng để đổi trạng thái!");
            return;
        }
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        String currentStatus = tableModel.getValueAt(row, 4).toString();
        boolean newStatus = !currentStatus.equals("Đang bán");

        if (dao.toggleAvailability(id, newStatus)) {
            JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái kinh doanh!");
            loadTableData();
            clearForm();
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtPrice.setText("");
        tblProducts.clearSelection();
    }

    public static void main(String[] args) {
        // Bật giao diện theo phong cách Windows cho đẹp mắt
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new ProductFrame().setVisible(true));
    }
}