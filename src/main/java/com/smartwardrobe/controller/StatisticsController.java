package com.smartwardrobe.controller;

import com.smartwardrobe.dto.PageResult;
import com.smartwardrobe.dto.Result;
import com.smartwardrobe.service.StatisticsService;
import com.smartwardrobe.vo.WardrobeReportVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/report")
    public Result<WardrobeReportVO> getReport(HttpServletRequest request,
                                              @RequestParam(required = false) String month) {
        Long userId = (Long) request.getAttribute("userId");
        WardrobeReportVO report = statisticsService.getReport(userId, month);
        return Result.success(report);
    }

    @GetMapping("/sleeping-items")
    public Result<List<WardrobeReportVO.SleepingItem>> getSleepingItems(HttpServletRequest request,
                                                                        @RequestParam(defaultValue = "90") int days,
                                                                        @RequestParam(defaultValue = "1") int page,
                                                                        @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) request.getAttribute("userId");
        List<WardrobeReportVO.SleepingItem> items = statisticsService.getSleepingItemsList(userId, days, page, size);
        return Result.success(items);
    }
}
