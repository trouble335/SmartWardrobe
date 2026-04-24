package com.smartwardrobe.controller;

import com.smartwardrobe.dto.*;
import com.smartwardrobe.service.ItemService;
import com.smartwardrobe.vo.ItemVO;
import com.smartwardrobe.vo.WearRecordVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public Result<PageResult<ItemVO>> getItems(HttpServletRequest request, ItemQueryDTO query) {
        Long userId = (Long) request.getAttribute("userId");
        PageResult<ItemVO> result = itemService.getItems(userId, query);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ItemVO> getItemDetail(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        ItemVO item = itemService.getItemDetail(userId, id);
        return Result.success(item);
    }

    @PostMapping
    public Result<Long> createItem(HttpServletRequest request, @Valid @RequestBody ItemCreateDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        Long id = itemService.createItem(userId, dto);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Void> updateItem(HttpServletRequest request, @PathVariable Long id,
                                   @Valid @RequestBody ItemCreateDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        itemService.updateItem(userId, id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteItem(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        itemService.deleteItem(userId, id);
        return Result.success();
    }

    @PostMapping("/{id}/wear")
    public Result<Void> recordWear(HttpServletRequest request, @PathVariable Long id,
                                   @Valid @RequestBody WearRecordDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        itemService.recordWear(userId, id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(HttpServletRequest request, @PathVariable Long id,
                                     @RequestParam Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        itemService.updateStatus(userId, id, status);
        return Result.success();
    }

    @GetMapping("/{id}/wear-records")
    public Result<List<WearRecordVO>> getWearRecords(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        List<WearRecordVO> records = itemService.getWearRecords(userId, id);
        return Result.success(records);
    }
}
