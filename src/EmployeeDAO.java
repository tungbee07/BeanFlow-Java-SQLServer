import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    // 1. Lấy danh sách nhân viên (tìm theo tên hoặc số điện thoại)
    public List<Object[]> getEmployees(String keyword) {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT EmployeeID, FullName, Phone, Role, IsActive "
                   + "FROM Employees "
                   + "WHERE FullName LIKE ? OR Phone LIKE ? "
                   + "ORDER BY EmployeeID";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("EmployeeID");
                    String name = rs.getNString("FullName");
                    String phone = rs.getString("Phone");
                    String role = rs.getNString("Role");
                    boolean isActive = rs.getBoolean("IsActive");
                    String statusText = isActive ? "Đang làm việc" : "Đã nghỉ";

                    rows.add(new Object[]{id, name, phone, role, statusText});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    // 2. Thêm nhân viên mới (trả về chuỗi rỗng "" nếu thành công, hoặc thông báo lỗi)
    public String insertEmployee(String fullName, String phone, String role) {
        String sql = "INSERT INTO Employees (FullName, Phone, Role, IsActive) VALUES (?, ?, ?, 1)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, fullName);
            ps.setString(2, phone);
            ps.setNString(3, role);
            ps.executeUpdate();
            return "";
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                return "Số điện thoại '" + phone + "' đã tồn tại trong hệ thống!";
            }
            return "Lỗi SQL: " + e.getMessage();
        }
    }

    // 3. Cập nhật thông tin nhân viên
    public String updateEmployee(int id, String fullName, String phone, String role) {
        String sql = "UPDATE Employees SET FullName = ?, Phone = ?, Role = ? WHERE EmployeeID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, fullName);
            ps.setString(2, phone);
            ps.setNString(3, role);
            ps.setInt(4, id);
            ps.executeUpdate();
            return "";
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                return "Số điện thoại '" + phone + "' đã thuộc về nhân viên khác!";
            }
            return "Lỗi SQL: " + e.getMessage();
        }
    }

    // 4. Đổi trạng thái Đang làm việc <-> Đã nghỉ
    public boolean toggleActive(int id, boolean newStatus) {
        String sql = "UPDATE Employees SET IsActive = ? WHERE EmployeeID = ?";
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