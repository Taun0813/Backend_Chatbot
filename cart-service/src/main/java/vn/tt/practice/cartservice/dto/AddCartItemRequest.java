package vn.tt.practice.cartservice.dto;


import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.UUID;

@Getter
@Setter
public class AddCartItemRequest {
    @NotNull
    private UUID productId;

//    @Min(1)
    private int quantity;
}


