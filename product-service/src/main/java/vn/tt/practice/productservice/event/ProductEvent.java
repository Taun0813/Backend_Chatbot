package vn.tt.practice.productservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEvent implements Serializable {
    private String eventType; // CREATED, UPDATED, DELETED
    private Long productId;
    private String productName;
    private Long categoryId;
}
