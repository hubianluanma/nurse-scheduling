package com.hubianluanma.scheduling.openapi.xf;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author huhailong
 * @version 1.0
 * @description: 讯飞OCR服务接口
 * 该接口定义了与讯飞OCR服务交互的方法，具体实现由相关
 * @date 2025/7/31 10:13
 */
public interface IXfOCRService {

    String ocrByImage(String imagePath);

    void corByImageFile(MultipartFile imageFile);
}
