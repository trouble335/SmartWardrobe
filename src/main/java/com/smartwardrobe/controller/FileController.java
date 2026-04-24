package com.smartwardrobe.controller;

import com.smartwardrobe.dto.Result;
import com.smartwardrobe.service.FileService;
import com.smartwardrobe.service.RemoveBgService;
import com.smartwardrobe.vo.FileUploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final RemoveBgService removeBgService;

    @PostMapping("/upload")
    public Result<FileUploadVO> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam("type") String type) {
        FileUploadVO result = fileService.upload(file, type);
        return Result.success(result);
    }

    @PostMapping("/remove-background")
    public Result<FileUploadVO> removeBackground(@RequestParam("imageUrl") String imageUrl) {
        FileUploadVO result = removeBgService.removeBackground(imageUrl);
        return Result.success(result);
    }
}
