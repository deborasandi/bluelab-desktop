package productcolor;


public class ProductColor {

    private int id;
    private String name;

    public ProductColor() {
        // TODO Auto-generated constructor stub
    }

    public ProductColor(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
    

}
