import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class EmployeeFrame extends JFrame {
    private EmployeeDAO dao = new EmployeeDAO();

    private JTextField txtId = new JTextField(5);
    private JTextField txtName = new JTextField(18);
    private JTextField txtPhone = new JTextField(12);
    private JComboBox<String> cboRole = new JComboBox<>(new String[]{"Thu ngân", "Pha chế", "Quản lý"});
    private JTextField txtSearch = new JTextField(18);

    private DefaultTableModel tableModel;
    private JTable tblEmployees;

    public EmployeeFrame() {
        setTitle("BeanFlow - Quản lý Nhân viên");
        setSize(800, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initUI();
        loadTableData();
    }

    private void initUI() {
        // --- PHẦN TRÊN: Nhập liệu + Tìm kiếm ---
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin nhân viên"));
        txtId.setEditable(false);

        formPanel.add(new JLabel("Mã NV:"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Họ và tên:"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Số điện thoại:"));
        formPanel.add(txtPhone);
        formPanel.add(new JLabel("Chức vụ:"));
        formPanel.add(cboRole);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnSearch = new JButton("Tìm kiếm");
        searchPanel.add(new JLabel("Tìm theo tên / SĐT:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(searchPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // --- PHẦN GIỮA: Bảng JTable ---
        String[] columns = {"Mã NV", "Họ và tên", "Số điện thoại", "Chức vụ", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblEmployees = new JTable(tableModel);
        tblEmployees.setRowHeight(25);
        add(new JScrollPane(tblEmployees), BorderLayout.CENTER);

        // --- PHẦN DƯỚI: Nút bấm ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnAdd = new JButton("Thêm nhân viên");
        JButton btnUpdate = new JButton("Sửa thông tin");
        JButton btnToggle = new JButton("Đổi trạng thái làm việc");
        JButton btnClear = new JButton("Làm mới");

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnToggle);
        btnPanel.add(btnClear);
        add(btnPanel, BorderLayout.SOUTH);

        // --- SỰ KIỆN ---
        btnSearch.addActionListener(e -> loadTableData());
        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnToggle.addActionListener(e -> handleToggle());
        btnClear.addActionListener(e -> clearForm());

        tblEmployees.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblEmployees.getSelectedRow();
                if (row >= 0) {
                    txtId.setText(tableModel.getValueAt(row, 0).toString());
                    txtName.setText(tableModel.getValueAt(row, 1).toString());
                    txtPhone.setText(tableModel.getValueAt(row, 2).toString());
                    cboRole.setSelectedItem(tableModel.getValueAt(row, 3).toString());
                }
            }
        });
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        String keyword = txtSearch.getText().trim();
        List<Object[]> rows = dao.getEmployees(keyword);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
    }

    private void handleAdd() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String role = cboRole.getSelectedItem().toString();

        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên và số điện thoại không được để trống!");
            return;
        }
        String err = dao.insertEmployee(name, phone, role);
        if (err.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
            loadTableData();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, err, "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một nhân viên trong bảng trước khi sửa!");
            return;
        }
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String role = cboRole.getSelectedItem().toString();
        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên và số điện thoại không được để trống!");
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        String err = dao.updateEmployee(id, name, phone, role);
        if (err.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!");
            loadTableData();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, err, "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleToggle() {
        int row = tblEmployees.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một nhân viên trong bảng!");
            return;
        }
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        String currentStatus = tableModel.getValueAt(row, 4).toString();
        boolean newStatus = !currentStatus.equals("Đang làm việc");

        if (dao.toggleActive(id, newStatus)) {
            JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái nhân viên!");
            loadTableData();
            clearForm();
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtPhone.setText("");
        cboRole.setSelectedIndex(0);
        tblEmployees.clearSelection();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new EmployeeFrame().setVisible(true));
    }
}