package com.app.repository;

import com.app.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);

    List<Item> findAllByOrderByCreatedAtDesc();
}
