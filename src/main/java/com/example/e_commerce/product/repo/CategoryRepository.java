package com.example.e_commerce.product.repo;

import com.example.e_commerce.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    // Used during update: check if name is taken by a DIFFERENT category
    boolean existsByNameAndIdNot(String name, Long id);
}
