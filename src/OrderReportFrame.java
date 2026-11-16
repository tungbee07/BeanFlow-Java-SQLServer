import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class OrderReportFrame extends JFrame {
    private OrderReportDAO dao = new OrderReportDAO();

    private JTextField txtFromDate = new JTextField(10);
    private JTextField txtToDate = new JTextField(10);
    private JLabel lblSummary = new JLabel("Đơn hoàn tất: 0 | Tổng doanh thu: 0 VNĐ");

    private DefaultTableModel orderModel, detailModel, topModel;
    private JTable tblOrders, tblDetails, tblTopProducts;

    public OrderReportFrame() {
        setTitle("BeanFlow - Lịch sử Đơn hàng & Thống kê Doanh thu");
        setSize(1050, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initUI();
        loadAllData("", "");
    }

    private void initUI() {
        // --- PHẦN TRÊN: Lọc theo ngày & Thanh tổng doanh thu ---
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 5, 10));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton btnFilter = new JButton("Lọc thống kê");
        JButton btnAll = new JButton("Xem tất cả");
        JButton btnCancelOrder = new JButton("Hủy đơn đã chọn");

        filterPanel.add(new JLabel("Từ ngày (yyyy-MM-dd):"));
        filterPanel.add(txtFromDate);
        filterPanel.add(new JLabel("Đến ngày:"));
        filterPanel.add(txtToDate);
        filterPanel.add(btnFilter);
        filterPanel.add(btnAll);
        filterPanel.add(btnCancelOrder);

        lblSummary.setFont(new Font("Arial", Font.BOLD, 16));
        lblSummary.setForeground(new Color(0, 110, 60));
        lblSummary.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        topPanel.add(filterPanel, BorderLayout.NORTH);
        topPanel.add(lblSummary, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // --- PHẦN GIỮA: Chia 2 bên (Trái: Đơn hàng + Chi tiết | Phải: Món bán chạy) ---
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Cột trái: Bảng Orders ở trên, bảng OrderDetails ở dưới
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 8, 8));

        orderModel = new DefaultTableModel(new String[]{"Mã đơn", "Thời gian", "Nhân viên", "Tổng tiền", "Trạng thái", "Ghi chú"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrders = new JTable(orderModel);
        tblOrders.setRowHeight(24);
        JScrollPane spOrders = new JScrollPane(tblOrders);
        spOrders.setBorder(BorderFactory.createTitledBorder("Danh sách đơn hàng (Bấm vào 1 dòng để xem món)"));

        detailModel = new DefaultTableModel(new String[]{"Tên đồ uống", "Số lượng", "Đơn giá lúc bán", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDetails = new JTable(detailModel);
        tblDetails.setRowHeight(24);
        JScrollPane spDetails = new JScrollPane(tblDetails);
        spDetails.setBorder(BorderFactory.createTitledBorder("Chi tiết các món trong đơn hàng"));

        leftPanel.add(spOrders);
        leftPanel.add(spDetails);

        // Cột phải: Thống kê đồ uống bán chạy
        topModel = new DefaultTableModel(new String[]{"Tên đồ uống", "Tổng ly đã bán", "Doanh thu (VNĐ)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblTopProducts = new JTable(topModel);
        tblTopProducts.setRowHeight(24);
        JScrollPane spTop = new JScrollPane(tblTopProducts);
        spTop.setBorder(BorderFactory.createTitledBorder("Thống kê đồ uống bán chạy (Đơn hoàn tất)"));

        centerPanel.add(leftPanel);
        centerPanel.add(spTop);
        add(centerPanel, BorderLayout.CENTER);

        // --- SỰ KIỆN ---
        btnFilter.addActionListener(e -> loadAllData(txtFromDate.getText().trim(), txtToDate.getText().trim()));
        btnAll.addActionListener(e -> {
            txtFromDate.setText("");
            txtToDate.setText("");
            loadAllData("", "");
        });

        tblOrders.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblOrders.getSelectedRow();
                if (row >= 0) {
                    int orderId = Integer.parseInt(orderModel.getValueAt(row, 0).toString());
                    loadOrderDetails(orderId);
                }
            }
        });

        btnCancelOrder.addActionListener(e -> handleCancelOrder());
    }

    private void loadAllData(String fromDate, String toDate) {
        orderModel.setRowCount(0);
        detailModel.setRowCount(0);
        for (Object[] row : dao.getOrders(fromDate, toDate)) {
            orderModel.addRow(row);
        }

        double[] summary = dao.getRevenueSummary(fromDate, toDate);
        lblSummary.setText("Số đơn hoàn tất: " + (int) summary[0] + "   |   Tổng doanh thu: " + String.format("%,.0f", summary[1]) + " VNĐ");

        topModel.setRowCount(0);
        for (Object[] row : dao.getTopProducts(fromDate, toDate)) {
            topModel.addRow(row);
        }
    }

    private void loadOrderDetails(int orderId) {
        detailModel.setRowCount(0);
        for (Object[] row : dao.getOrderDetails(orderId)) {
            detailModel.addRow(row);
        }
    }

    private void handleCancelOrder() {
        int row = tblOrders.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một đơn hàng để hủy!");
            return;
        }
        int orderId = Integer.parseInt(orderModel.getValueAt(row, 0).toString());
        String status = orderModel.getValueAt(row, 4).toString();
        if (status.equals("Đã hủy")) {
            JOptionPane.showMessageDialog(this, "Đơn hàng này đã bị hủy trước đó!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn hủy đơn #" + orderId + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && dao.cancelOrder(orderId)) {
            JOptionPane.showMessageDialog(this, "Đã hủy đơn #" + orderId + "!");
            loadAllData(txtFromDate.getText().trim(), txtToDate.getText().trim());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new OrderReportFrame().setVisible(true));
    }
}