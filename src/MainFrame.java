import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("BeanFlow Coffee Shop Management System");
        setSize(520, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Tiêu đề quán cà phê ở trên
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(new Color(78, 52, 46)); // Màu nâu cà phê
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel lblTitle = new JLabel("PHẦN MỀM QUẢN LÝ QUÁN CÀ PHÊ BEANFLOW", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Hệ thống bán hàng, quản lý thực đơn và thống kê doanh thu", SwingConstants.CENTER);
        lblSub.setFont(new Font("Arial", Font.ITALIC, 12));
        lblSub.setForeground(new Color(239, 235, 233));

        headerPanel.add(lblTitle);
        headerPanel.add(lblSub);
        add(headerPanel, BorderLayout.NORTH);

        // 4 nút mở 4 Form chức năng ở giữa
        JPanel menuPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JButton btnPos = new JButton("1. Bán hàng tại quầy (POS)");
        JButton btnProduct = new JButton("2. Quản lý Đồ uống");
        JButton btnEmployee = new JButton("3. Quản lý Nhân viên");
        JButton btnReport = new JButton("4. Đơn hàng & Doanh thu");

        Font btnFont = new Font("Arial", Font.BOLD, 14);
        btnPos.setFont(btnFont);
        btnProduct.setFont(btnFont);
        btnEmployee.setFont(btnFont);
        btnReport.setFont(btnFont);

        menuPanel.add(btnPos);
        menuPanel.add(btnProduct);
        menuPanel.add(btnEmployee);
        menuPanel.add(btnReport);
        add(menuPanel, BorderLayout.CENTER);

        // Chú thích chân trang
        JLabel lblFooter = new JLabel("Đồ án môn Lập trình Java - SQL Server (BeanFlowDB)", SwingConstants.CENTER);
        lblFooter.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(lblFooter, BorderLayout.SOUTH);

        // Sự kiện mở từng Form
        btnPos.addActionListener(e -> new PosFrame().setVisible(true));
        btnProduct.addActionListener(e -> new ProductFrame().setVisible(true));
        btnEmployee.addActionListener(e -> new EmployeeFrame().setVisible(true));
        btnReport.addActionListener(e -> new OrderReportFrame().setVisible(true));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}