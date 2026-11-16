import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderReportDAO {

    // 1. Lấy danh sách đơn hàng (nếu fromDate/toDate rỗng thì lấy tất cả)
    public List<Object[]> getOrders(String fromDate, String toDate) {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT o.OrderID, o.OrderDate, e.FullName, o.TotalAmount, o.Status, ISNULL(o.Note, N'') AS Note "
                   + "FROM Orders o "
                   + "INNER JOIN Employees e ON o.EmployeeID = e.EmployeeID ";
        boolean hasFilter = !fromDate.isEmpty() && !toDate.isEmpty();
        if (hasFilter) {
            sql += "WHERE CAST(o.OrderDate AS DATE) BETWEEN ? AND ? ";
        }
        sql += "ORDER BY o.OrderID DESC";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasFilter) {
                ps.setString(1, fromDate);
                ps.setString(2, toDate);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getInt("OrderID"),
                        sdf.format(rs.getTimestamp("OrderDate")),
                        rs.getNString("FullName"),
                        String.format("%,.0f", rs.getDouble("TotalAmount")),
                        rs.getNString("Status"),
                        rs.getNString("Note")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    // 2. Lấy chi tiết các món của 1 đơn hàng
    public List<Object[]> getOrderDetails(int orderId) {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT p.ProductName, od.Quantity, od.UnitPrice, (od.Quantity * od.UnitPrice) AS SubTotal "
                   + "FROM OrderDetails od "
                   + "INNER JOIN Products p ON od.ProductID = p.ProductID "
                   + "WHERE od.OrderID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getNString("ProductName"),
                        rs.getInt("Quantity"),
                        String.format("%,.0f", rs.getDouble("UnitPrice")),
                        String.format("%,.0f", rs.getDouble("SubTotal"))
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    // 3. Hủy đơn hàng (không xóa lịch sử, chỉ đổi trạng thái sang Đã hủy)
    public boolean cancelOrder(int orderId) {
        String sql = "UPDATE Orders SET Status = N'Đã hủy' WHERE OrderID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Thống kê tổng số đơn hoàn tất & tổng doanh thu (chỉ tính Status = N'Hoàn tất')
    public double[] getRevenueSummary(String fromDate, String toDate) {
        double[] result = {0, 0}; // [0]=Số đơn, [1]=Tổng doanh thu
        String sql = "SELECT COUNT(OrderID) AS TotalOrders, ISNULL(SUM(TotalAmount), 0) AS TotalRevenue "
                   + "FROM Orders WHERE Status = N'Hoàn tất' ";
        boolean hasFilter = !fromDate.isEmpty() && !toDate.isEmpty();
        if (hasFilter) {
            sql += "AND CAST(OrderDate AS DATE) BETWEEN ? AND ?";
        }
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasFilter) {
                ps.setString(1, fromDate);
                ps.setString(2, toDate);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result[0] = rs.getInt("TotalOrders");
                    result[1] = rs.getDouble("TotalRevenue");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // 5. Thống kê đồ uống bán chạy nhất (chỉ tính đơn Hoàn tất)
    public List<Object[]> getTopProducts(String fromDate, String toDate) {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT p.ProductName, SUM(od.Quantity) AS TotalQty, SUM(od.Quantity * od.UnitPrice) AS Revenue "
                   + "FROM OrderDetails od "
                   + "INNER JOIN Orders o ON od.OrderID = o.OrderID "
                   + "INNER JOIN Products p ON od.ProductID = p.ProductID "
                   + "WHERE o.Status = N'Hoàn tất' ";
        boolean hasFilter = !fromDate.isEmpty() && !toDate.isEmpty();
        if (hasFilter) {
            sql += "AND CAST(o.OrderDate AS DATE) BETWEEN ? AND ? ";
        }
        sql += "GROUP BY p.ProductName ORDER BY TotalQty DESC";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasFilter) {
                ps.setString(1, fromDate);
                ps.setString(2, toDate);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getNString("ProductName"),
                        rs.getInt("TotalQty"),
                        String.format("%,.0f", rs.getDouble("Revenue"))
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }
}