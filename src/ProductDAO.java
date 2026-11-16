import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // 1. Lấy toàn bộ danh mục để nạp vào JComboBox
    public List<CategoryItem> getAllCategories() {
        List<CategoryItem> list = new ArrayList<>();
        String sql = "SELECT CategoryID, CategoryName FROM Categories ORDER BY CategoryID";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new CategoryItem(rs.getInt("CategoryID"), rs.getString("CategoryName")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Lấy danh sách đồ uống (có hỗ trợ tìm theo tên và lọc theo mã danh mục)
    public List<Object[]> getProducts(String keyword, int categoryId) {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT p.ProductID, p.ProductName, p.Price, c.CategoryName, p.IsAvailable "
                   + "FROM Products p "
                   + "INNER JOIN Categories c ON p.CategoryID = c.CategoryID "
                   + "WHERE p.ProductName LIKE ? ";
        if (categoryId > 0) {
            sql += "AND p.CategoryID = ? ";
        }
        sql += "ORDER BY p.ProductID";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, "%" + keyword + "%");
            if (categoryId > 0) {
                ps.setInt(2, categoryId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("ProductID");
                    String name = rs.getNString("ProductName");
                    double price = rs.getDouble("Price");
                    String catName = rs.getNString("CategoryName");
                    boolean isAvailable = rs.getBoolean("IsAvailable");
                    String statusText = isAvailable ? "Đang bán" : "Ngừng bán";

                    rows.add(new Object[]{id, name, String.format("%,.0f", price), catName, statusText});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    // 3. Thêm đồ uống mới
    public boolean insertProduct(String name, double price, int categoryId) {
        String sql = "INSERT INTO Products (ProductName, Price, CategoryID, IsAvailable) VALUES (?, ?, ?, 1)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, categoryId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Cập nhật thông tin đồ uống
    public boolean updateProduct(int id, String name, double price, int categoryId) {
        String sql = "UPDATE Products SET ProductName = ?, Price = ?, CategoryID = ? WHERE ProductID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, categoryId);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. Đổi trạng thái Đang bán <-> Ngừng bán (Tránh xóa mất dữ liệu hóa đơn cũ)
    public boolean toggleAvailability(int id, boolean newStatus) {
        String sql = "UPDATE Products SET IsAvailable = ? WHERE ProductID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, newStatus);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
