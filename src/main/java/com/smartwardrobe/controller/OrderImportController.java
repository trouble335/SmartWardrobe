package com.smartwardrobe.controller;

import com.smartwardrobe.dto.ItemCreateDTO;
import com.smartwardrobe.dto.Result;
import com.smartwardrobe.service.FileService;
import com.smartwardrobe.service.OrderImportService;
import com.smartwardrobe.vo.OrderImportVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderImportController {

    private final OrderImportService orderImportService;
    private final FileService fileService;

    @PostMapping("/import")
    public Result<OrderImportVO> importOrder(HttpServletRequest request,
                                             @RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "platform", required = false) String platform) {
        Long userId = (Long) request.getAttribute("userId");

        // 上传图片
        var uploadResult = fileService.upload(file, "orders");

        // 导入订单
        OrderImportVO result = orderImportService.importOrder(userId, uploadResult.getUrl(), platform);

        return Result.success(result);
    }

    @PostMapping("/import/{id}/confirm")
    public Result<Long> confirmImport(HttpServletRequest request,
                                      @PathVariable Long id,
                                      @Valid @RequestBody ItemCreateDTO itemDTO) {
        Long userId = (Long) request.getAttribute("userId");
        Long itemId = orderImportService.confirmImport(userId, id, itemDTO);
        return Result.success(itemId);
    }
}
