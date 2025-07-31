package com.hubianluanma.scheduling.controller;

import com.hubianluanma.scheduling.openapi.xf.IXfOCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author huhailong
 * @version 1.0
 * @description: TODO
 * @date 2025/7/31 10:56
 */
@RestController
@RequestMapping("/api/ocr")
public class OCRController {

    @Autowired
    IXfOCRService xfOCRService;

    @RequestMapping("/ocrByImage")
    public String ocrByImage(String imagePath) {
        return xfOCRService.ocrByImage(imagePath);
    }
}
