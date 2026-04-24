package com.smartwardrobe.controller;

import com.smartwardrobe.dto.*;
import com.smartwardrobe.service.RecommendService;
import com.smartwardrobe.vo.OutfitRecommendationVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping("/outfits")
    public Result<OutfitRecommendationVO> getRecommendations(HttpServletRequest request,
                                                             RecommendRequest recommendRequest) {
        Long userId = (Long) request.getAttribute("userId");
        OutfitRecommendationVO result = recommendService.recommend(
                userId,
                recommendRequest.getLocation(),
                recommendRequest.getOccasion(),
                recommendRequest.getStyle(),
                recommendRequest.getItemId(),
                recommendRequest.getCount()
        );
        return Result.success(result);
    }

    @PostMapping("/outfits/{id}/feedback")
    public Result<Void> submitFeedback(HttpServletRequest request,
                                       @PathVariable String id,
                                       @Valid @RequestBody FeedbackDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        recommendService.recordFeedback(userId, Long.parseLong(id.replace("rec_", "")), dto.getType(), dto.getReason());
        return Result.success();
    }

    @PostMapping("/outfits/{id}/wear")
    public Result<Void> recordWear(HttpServletRequest request,
                                   @PathVariable String id,
                                   @RequestBody WearRecordDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        recommendService.recordWear(userId, Long.parseLong(id.replace("rec_", "")), dto.getWearDate(), dto.getNote());
        return Result.success();
    }
}
