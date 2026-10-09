package com.app.service;

import com.app.dto.ItemRequest;
import com.app.dto.ItemResponse;
import com.app.exception.ResourceNotFoundException;
import com.app.model.Item;
import com.app.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getAll(String search) {
        List<Item> items = (search == null || search.isBlank())
                ? itemRepository.findAllByOrderByCreatedAtDesc()
                : itemRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(search.trim());
        return items.stream().map(ItemResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ItemResponse getById(Long id) {
        return ItemResponse.from(findOrThrow(id));
    }

    public ItemResponse create(ItemRequest request) {
        Item item = new Item();
        apply(item, request);
        return ItemResponse.from(itemRepository.save(item));
    }

    public ItemResponse update(Long id, ItemRequest request) {
        Item item = findOrThrow(id);
        apply(item, request);
        return ItemResponse.from(itemRepository.save(item));
    }

    public void delete(Long id) {
        itemRepository.delete(findOrThrow(id));
    }

    private Item findOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
    }

    private void apply(Item item, ItemRequest request) {
        item.setName(request.name().trim());
        item.setDescription(request.description() == null ? null : request.description().trim());
    }
}
