package vn.tt.practice.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.tt.practice.productservice.entity.ProductSpec;

@Repository
public interface ProductSpecRepository extends JpaRepository<ProductSpec, Long> {
}
