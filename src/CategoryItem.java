public class CategoryItem {
    private int id;
    private String name;

    public CategoryItem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // JComboBox sẽ tự động gọi hàm toString() để hiển thị tên danh mục lên màn hình
    @Override
    public String toString() {
        return name;
    }
}