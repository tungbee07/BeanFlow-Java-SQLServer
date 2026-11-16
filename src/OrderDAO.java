import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // 1. Lấy danh sách nhân viên ĐANG LÀM VIỆC (IsActive = 1)
    public List<ComboItem> getActiveEmployees() {
        List<ComboItem> list = new ArrayList<>();
        String sql = "SELECT EmployeeID, FullName, Role FROM Employees WHERE IsActive = 1 ORDER BY EmployeeID";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String label = rs.getNString("FullName") + " (" + rs.getNString("Role") + ")";
                list.add(new ComboItem(rs.getInt("EmployeeID"), label));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Lấy thực đơn các món ĐANG BÁN (IsAvailable = 1)
    public List<Object[]> getAvailableProducts(String keyword) {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT p.ProductID, p.ProductName, p.Price, c.CategoryName "
                   + "FROM Products p "
                   + "INNER JOIN Categories c ON p.CategoryID = c.CategoryID "
                   + "WHERE p.IsAvailable = 1 AND p.ProductName LIKE ? "
                   + "ORDER BY p.ProductID";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getInt("ProductID"),
                        rs.getNString("ProductName"),
                        String.format("%,.0f", rs.getDouble("Price")),
                        rs.getNString("CategoryName")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    // 3. Lưu đơn hàng & chi tiết đơn hàng bằng TRANSACTION (Trả về mã đơn vừa tạo, lỗi trả về -1)
    // Mỗi phần tử trong cartItems gồm: [0]=ProductID (int), [1]=Quantity (int), [2]=UnitPrice (double)
    public int createOrder(int employeeId, double totalAmount, String note, List<Object[]> cartItems) {
        String sqlOrder = "INSERT INTO Orders (EmployeeID, TotalAmount, Status, Note) VALUES (?, ?, N'Hoàn tất', ?)";
        String sqlDetail = "INSERT INTO OrderDetails (OrderID, ProductID, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnect.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction (chưa lưu thật ngay)

            // Bước 1: Lưu vào bảng Orders và lấy OrderID tự tăng vừa sinh ra
            int newOrderId = -1;
            try (PreparedStatement psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, employeeId);
                psOrder.setDouble(2, totalAmount);
                psOrder.setNString(3, note);
                psOrder.executeUpdate();

                try (ResultSet rsKey = psOrder.getGeneratedKeys()) {
                    if (rsKey.next()) {
                        newOrderId = rsKey.getInt(1);
                    }
                }
            }

            if (newOrderId == -1) {
                throw new SQLException("Không lấy được mã đơn hàng mới!");
            }

            // Bước 2: Lưu từng món trong giỏ hàng vào bảng OrderDetails (kèm UnitPrice tại thời điểm bán)
            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                for (Object[] item : cartItems) {
                    psDetail.setInt(1, newOrderId);
                    psDetail.setInt(2, (int) item[0]);      // ProductID
                    psDetail.setInt(3, (int) item[1]);      // Quantity
                    psDetail.setDouble(4, (double) item[2]); // UnitPrice
                    psDetail.executeUpdate();
                }
            }

            conn.commit(); // Cả 2 bảng đều thành công -> Chốt lưu vĩnh viễn!
            return newOrderId;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Có lỗi -> Hủy bỏ toàn bộ đơn hàng dở dang
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}