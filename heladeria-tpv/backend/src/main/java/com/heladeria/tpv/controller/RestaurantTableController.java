package com.heladeria.tpv.controller;

import com.heladeria.tpv.dto.AddToTableRequest;
import com.heladeria.tpv.dto.RestaurantTableRequest;
import com.heladeria.tpv.dto.UpdateTablePositionRequest;
import com.heladeria.tpv.model.RestaurantTable;
import com.heladeria.tpv.service.RestaurantTableService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class RestaurantTableController {

    private final RestaurantTableService tableService;

    public RestaurantTableController(RestaurantTableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public List<RestaurantTable> findAll() {
        return tableService.findAll();
    }

    @GetMapping("/{id}")
    public RestaurantTable findById(@PathVariable Long id) {
        return tableService.findById(id);
    }

    @PostMapping
    public RestaurantTable create(@Valid @RequestBody RestaurantTableRequest request) {
        return tableService.create(request);
    }

    @PutMapping("/{id}")
    public RestaurantTable rename(@PathVariable Long id, @Valid @RequestBody RestaurantTableRequest request) {
        return tableService.rename(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        tableService.delete(id);
    }

    @PatchMapping("/{id}/position")
    public RestaurantTable updatePosition(@PathVariable Long id, @RequestBody UpdateTablePositionRequest request) {
        return tableService.updatePosition(id, request.getPositionX(), request.getPositionY());
    }

    @PostMapping("/{id}/items")
    public RestaurantTable addProduct(@PathVariable Long id, @Valid @RequestBody AddToTableRequest request) {
        return tableService.addProduct(id, request.getProductId(), request.getQuantity());
    }
}
