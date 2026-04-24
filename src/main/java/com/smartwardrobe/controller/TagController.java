package com.smartwardrobe.controller;

import com.smartwardrobe.dto.Result;
import com.smartwardrobe.dto.TagDTO;
import com.smartwardrobe.service.TagService;
import com.smartwardrobe.vo.TagVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 按类型查询标签列表
     * type: style | season | occasion | color
     */
    @GetMapping
    public Result<List<TagVO>> getTags(@RequestParam String type) {
        return Result.success(tagService.getTagsByType(type));
    }

    /**
     * 新增标签
     */
    @PostMapping
    public Result<TagVO> createTag(@Valid @RequestBody TagDTO dto) {
        return Result.success(tagService.createTag(dto));
    }

    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    public Result<TagVO> updateTag(@PathVariable Long id, @Valid @RequestBody TagDTO dto) {
        return Result.success(tagService.updateTag(id, dto));
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
