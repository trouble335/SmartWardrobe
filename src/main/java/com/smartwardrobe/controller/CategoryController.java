package com.smartwardrobe.controller;

import com.smartwardrobe.dto.Result;
import com.smartwardrobe.service.CategoryService;
import com.smartwardrobe.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryVO>> getCategoryTree() {
        List<CategoryVO> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }
}
