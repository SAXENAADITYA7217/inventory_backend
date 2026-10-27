import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.school.inventary.dto.ProductRequestDto;
import java.util.List;

public class TestJackson {
    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = "[{\"categoryId\":\"CAT_123\",\"productName\":\"Pen\",\"uom\":\"PCS\",\"minQuantity\":5,\"rackDetail\":\"A1\",\"imageIndex\":null}]";
            List<ProductRequestDto> list = mapper.readValue(json, new TypeReference<List<ProductRequestDto>>() {});
            System.out.println("DESERIALIZED SUCCESS: " + list.size());
            System.out.println("Product Name: " + list.get(0).getProductName());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
