package com.example.inventory.service;

import com.example.inventory.model.Item;
import com.example.inventory.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    // Save new item
    public Item addItem(Item item) {
    return itemRepository.save(item);
}


    // Get all items
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    // Find item by name
    public Optional<Item> findByName(String name) {
        return itemRepository.findByName(name);
    }

    // Find item by category
    public List<Item> findByCategory(String category) {
        return itemRepository.findByCategory(category);
    }

    // Delete item
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }
}
