package com.hubianluanma.scheduling.openapi.xf.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hubianluanma.scheduling.openapi.deepseek.IChatService;
import com.hubianluanma.scheduling.openapi.xf.IXfOCRService;
import com.hubianluanma.scheduling.util.HttpUtil;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * @author huhailong
 * @version 1.0
 * @description: 讯飞OCR服务实现类
 * @date 2025/7/31 10:14
 */
@Service
public class XfOCRServiceImpl implements IXfOCRService {

    @Value("${xf.appId}")
    private String appId;
    @Value("${xf.key}")
    private String key;
    @Value("${xf.secret}")
    private String secret;

    @Autowired
    IChatService chatService;

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder()
            .connectTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .build();

    @Override
    public String ocrByImage(String imagePath) {
        String markdownContent = callAPI(imagePath);
//        String markdownContent = """
//                # 九区护士排班表
//
//                |姓名  日期 |姓名  日期 |姓名  日期 |能级 |8月4日 |8月5日 |8月6日 |8月7日 |8月8日 |8月9日 |8月10日 |**出勤** |**存休** |
//                |--|--|--|--|--|--|--|--|--|--|--|--|--|
//                |姓名  日期 |姓名  日期 |姓名  日期 |能级 |星期一 |星期二 |星期三 |星期四 |星期五 |星期六 |星期日 |**出勤** |**存休** |
//                |1 |2957 |张彩玉★ |**M2** |休 |$\\bigstar$ **责8-8** |$\\bigstar$ 责8-8 |两头 |休 |$\\bigstar$ 应急 |$\\bigstar$ 责8-8 |5.5 |0.5 |
//                |2 |3262 |李妍 |**H1** |休 |休# |**责8-8** |夜8-8 |休 |休# |**责8-8** |4.5 |-0.5 |
//                |3 |1424 |张巧平 $\\bigstar$ |**H3** |$\\bigstar$ **壹8-8** |休 |休 |$\\bigstar$ 应急 |$\\bigstar$ **责8-8** |两头 |两头 |5 |0 |
//                |4 |3630 |王茜\\* |**M1** |**责8-8** |两头 |休 |休 $*$ |**责8-8** |夜8-8 |休 |5.5 |0.5 |
//                |5 |2988 |马听★ |M2 |两头 |休 |$\\bigstar$ 应急 |$\\bigstar$ 责8-8 |夜8-8 |休 |$\\bigstar$ 应急 |4 |-1 |
//                |6 |3427 |刘文婧 |**NO** |夜8-8 |休 |休 $*$ |**责8-8** |两头 |休 |休# |4 |-1 |
//                |7 |851 |田朵云 |M2 |分离室 |分离室 |分离室 |分离室 |分离室 |休 |休 |5 |0 |
//                |8 |1879 |赵晓春★ |M3 |主8-5 |夜8-8 |两头 |休 |$\\bigstar$ 应急 |$\\bigstar$ **责8-8** |主8-5 |6 |1 |
//                |9 |3394 |**郝好** |**H1** |主8-5 |主8-5 |主8-5 |主8-5 |主8-5 |休 |休 |5 |0 |
//                |10 |117 |任云云★ |**H4** |$\\bigstar$ 应急 |主8-5 |主8-5 |主8-5 |主8-5 |主8-5 |休 |5 |0 |
//                |11 |1214 |辛丽莉 |**H3** |七区 |七区 |七区 |七区 |七区 |七区 |七区 |||
//                |12 |2705 |计龙儿 |**H2** |休# |责8-8 |夜8-8 |休 |休 $*$ |**责8-8** |夜8-8 |6 |1 |
//                |13 |2266 |韩明月 |NS |8-5 |8-5 |8-5 |8-5 |8-5 |休 |**体** |5 |0 |
//                |||三线护士长 |||||||||||
//                |||值班护士长 ||王然 |王春熹 |王翠翠 |孙燕 |韩燕丽 |**杨欣欣** |牛重哲 |||
//                ||备注：  1、★为组长 ▲为转入\\*为新入职 △为实习  2、“体#”为科室备班，科室工作需要时到岗支援工作。  3、“ $\\bigstar$ ”为院内应急班次，应急状态时紧急支援，应急备班人员到岗后返回科室工作。  4、 $\\bigstar$ 应急”为全院突发事件应急备班人员，应急状态时接到通知后30-40分钟内到达指定位置，接院内应急班次 |备注：  1、★为组长 ▲为转入\\*为新入职 △为实习  2、“体#”为科室备班，科室工作需要时到岗支援工作。  3、“ $\\bigstar$ ”为院内应急班次，应急状态时紧急支援，应急备班人员到岗后返回科室工作。  4、 $\\bigstar$ 应急”为全院突发事件应急备班人员，应急状态时接到通知后30-40分钟内到达指定位置，接院内应急班次 |备注：  1、★为组长 ▲为转入\\*为新入职 △为实习  2、“体#”为科室备班，科室工作需要时到岗支援工作。  3、“ $\\bigstar$ ”为院内应急班次，应急状态时紧急支援，应急备班人员到岗后返回科室工作。  4、 $\\bigstar$ 应急”为全院突发事件应急备班人员，应急状态时接到通知后30-40分钟内到达指定位置，接院内应急班次 |备注：  1、★为组长 ▲为转入\\*为新入职 △为实习  2、“体#”为科室备班，科室工作需要时到岗支援工作。  3、“ $\\bigstar$ ”为院内应急班次，应急状态时紧急支援，应急备班人员到岗后返回科室工作。  4、 $\\bigstar$ 应急”为全院突发事件应急备班人员，应急状态时接到通知后30-40分钟内到达指定位置，接院内应急班次 |备注：  1、★为组长 ▲为转入\\*为新入职 △为实习  2、“体#”为科室备班，科室工作需要时到岗支援工作。  3、“ $\\bigstar$ ”为院内应急班次，应急状态时紧急支援，应急备班人员到岗后返回科室工作。  4、 $\\bigstar$ 应急”为全院突发事件应急备班人员，应急状态时接到通知后30-40分钟内到达指定位置，接院内应急班次 |备注：  1、★为组长 ▲为转入\\*为新入职 △为实习  2、“体#”为科室备班，科室工作需要时到岗支援工作。  3、“ $\\bigstar$ ”为院内应急班次，应急状态时紧急支援，应急备班人员到岗后返回科室工作。  4、 $\\bigstar$ 应急”为全院突发事件应急备班人员，应急状态时接到通知后30-40分钟内到达指定位置，接院内应急班次 |备注：  1、★为组长 ▲为转入\\*为新入职 △为实习  2、“体#”为科室备班，科室工作需要时到岗支援工作。  3、“ $\\bigstar$ ”为院内应急班次，应急状态时紧急支援，应急备班人员到岗后返回科室工作。  4、 $\\bigstar$ 应急”为全院突发事件应急备班人员，应急状态时接到通知后30-40分钟内到达指定位置，接院内应急班次 |备注：  1、★为组长 ▲为转入\\*为新入职 △为实习  2、“体#”为科室备班，科室工作需要时到岗支援工作。  3、“ $\\bigstar$ ”为院内应急班次，应急状态时紧急支援，应急备班人员到岗后返回科室工作。  4、 $\\bigstar$ 应急”为全院突发事件应急备班人员，应急状态时接到通知后30-40分钟内到达指定位置，接院内应急班次 |备注：  1、★为组长 ▲为转入\\*为新入职 △为实习  2、“体#”为科室备班，科室工作需要时到岗支援工作。  3、“ $\\bigstar$ ”为院内应急班次，应急状态时紧急支援，应急备班人员到岗后返回科室工作。  4、 $\\bigstar$ 应急”为全院突发事件应急备班人员，应急状态时接到通知后30-40分钟内到达指定位置，接院内应急班次 |备注：  1、★为组长 ▲为转入\\*为新入职 △为实习  2、“体#”为科室备班，科室工作需要时到岗支援工作。  3、“ $\\bigstar$ ”为院内应急班次，应急状态时紧急支援，应急备班人员到岗后返回科室工作。  4、 $\\bigstar$ 应急”为全院突发事件应急备班人员，应急状态时接到通知后30-40分钟内到达指定位置，接院内应急班次 |备注：  1、★为组长 ▲为转入\\*为新入职 △为实习  2、“体#”为科室备班，科室工作需要时到岗支援工作。  3、“ $\\bigstar$ ”为院内应急班次，应急状态时紧急支援，应急备班人员到岗后返回科室工作。  4、 $\\bigstar$ 应急”为全院突发事件应急备班人员，应急状态时接到通知后30-40分钟内到达指定位置，接院内应急班次 |备注：  1、★为组长 ▲为转入\\*为新入职 △为实习  2、“体#”为科室备班，科室工作需要时到岗支援工作。  3、“ $\\bigstar$ ”为院内应急班次，应急状态时紧急支援，应急备班人员到岗后返回科室工作。  4、 $\\bigstar$ 应急”为全院突发事件应急备班人员，应急状态时接到通知后30-40分钟内到达指定位置，接院内应急班次 |
//                |注：1、每天夜班检查体温单、新入院评估单的书写；2、检查PDA的巡视及扫码率 |注：1、每天夜班检查体温单、新入院评估单的书写；2、检查PDA的巡视及扫码率 |注：1、每天夜班检查体温单、新入院评估单的书写；2、检查PDA的巡视及扫码率 |注：1、每天夜班检查体温单、新入院评估单的书写；2、检查PDA的巡视及扫码率 |注：1、每天夜班检查体温单、新入院评估单的书写；2、检查PDA的巡视及扫码率 |注：1、每天夜班检查体温单、新入院评估单的书写；2、检查PDA的巡视及扫码率 |注：1、每天夜班检查体温单、新入院评估单的书写；2、检查PDA的巡视及扫码率 |注：1、每天夜班检查体温单、新入院评估单的书写；2、检查PDA的巡视及扫码率 |注：1、每天夜班检查体温单、新入院评估单的书写；2、检查PDA的巡视及扫码率 |注：1、每天夜班检查体温单、新入院评估单的书写；2、检查PDA的巡视及扫码率 |注：1、每天夜班检查体温单、新入院评估单的书写；2、检查PDA的巡视及扫码率 |注：1、每天夜班检查体温单、新入院评估单的书写；2、检查PDA的巡视及扫码率 |注：1、每天夜班检查体温单、新入院评估单的书写；2、检查PDA的巡视及扫码率 |
//                """;
        String jsonRule = """
                {
                     "nurses": [
                         {
                             "id": 1,
                             "name": "张彩玉★",
                             "level": "M2",
                             "schedule": {
                                 "8月4日": "休",
                                 "8月5日": "责8-8",
                                 "8月6日": "责8-8",
                                 "8月7日": "两头",
                                 "8月8日": "休",
                                 "8月9日": "应急",
                                 "8月10日": "责8-8"
                             },
                             "attendance": 5.5,
                             "rest": 0.5
                         }
                     ],
                     "headNurses": {
                         "三线护士长": "",
                         "值班护士长": {
                             "8月4日": "王然",
                             "8月5日": "王春熹",
                             "8月6日": "王翠翠",
                             "8月7日": "孙燕",
                             "8月8日": "韩燕丽",
                             "8月9日": "杨欣欣",
                             "8月10日": "牛重哲"
                         }
                     },
                     "notes": [
                         "1、★为组长 ▲为转入*为新入职 △为实习",
                         "2、“体#”为科室备班，科室工作需要时到岗支援工作。",
                         "3、“★”为院内应急班次，应急状态时紧急支援，应急备班人员到岗后返回科室工作。",
                         "4、“★应急”为全院突发事件应急备班人员，应急状态时接到通知后30-40分钟内到达指定位置，接院内应急班次",
                         "1、每天夜班检查体温单、新入院评估单的书写；2、检查PDA的巡视及扫码率"
                     ]
                 }
                """;
        String jsonStr = chatService.convertMarkdownToJson(jsonRule, markdownContent);
        String responseContent = chatService.getResponseContent(jsonStr);
        return responseContent;
    }

    @Override
    public void corByImageFile(MultipartFile imageFile) {

    }

    /**
     * 调用讯飞OCR API
     * 该方法将使用HttpUtil类发送请求到讯飞OCR服务
     *
     * @return
     */
    private String callAPI(String imagePath) {
        URL url;
        try {
            String OCR_API_URL = "https://cbm01.cn-huabei-1.xf-yun.com/v1/private/se75ocrbm";
            url = new URL(OCR_API_URL);
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = format.format(new Date());
            String host = url.getHost();
            StringBuilder builder = new StringBuilder("host: ").append(host).append("\n").
                    append("date: ").append(date).append("\n").
                    append("POST").append(" ").append(url.getPath()).append(" HTTP/1.1");
            Charset charset = StandardCharsets.UTF_8;
            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(secret.getBytes(charset), "hmacsha256");
            mac.init(spec);
            byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
            String sha = Base64.getEncoder().encodeToString(hexDigits);
            String authorization = String.format("hmac username=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", key, "hmac-sha256", "host date request-line", sha);
            String authBase = Base64.getEncoder().encodeToString(authorization.getBytes(charset));
            String authUrl = String.format("%s?authorization=%s&host=%s&date=%s", OCR_API_URL, URLEncoder.encode(authBase), URLEncoder.encode(host), URLEncoder.encode(date));
            // 使用HttpUtil发送POST请求
            // 构建请求体
            // 读取并编码图片
            FileData fileData = readImageFile(imagePath);
            String requestJson = buildRequest(appId, 12345L, fileData.base64, fileData.fileType);
            okhttp3.MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, requestJson);
            Request request = new Request.Builder()
                    .url(authUrl)
                    .post(body)
                    .build();
            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    parseResponse(response.body().string());
                    throw new IOException("Unexpected code " + response);
                }
                //System.err.println(response);
                return parseResponse(response.body().string());
            }
        } catch (Exception e) {
            throw new RuntimeException("assemble requestUrl error:" + e.getMessage());
        }
    }

    // 解析响应
    private static String parseResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode respObj = mapper.readTree(response);
            JsonNode header = respObj.get("header");

            if (header.get("code").asInt() != 0) {
                System.out.println(header.toString());
                throw new RuntimeException("API Error: code is " + header.get("code").asText() + ", message:" + header.get("message").asText());
            }

            String encodedText = respObj.get("payload")
                    .get("result")
                    .get("text").asText();
            String resultText = new String(Base64.getDecoder().decode(encodedText));
            JsonNode resultObj = mapper.readTree(resultText);
            ArrayNode document = resultObj.withArrayProperty("document");
            if (!document.isEmpty()) {
                return document.get(0).get("value").asText();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("JSON parse error: " + e.getMessage());
        }
    }

    private static String buildRequest(String appId, Long uid, String imageBase64, String fileType) {
        return "{\n" +
                "    \"header\": {\n" +
                "        \"app_id\": \"" + appId + "\",\n" +
                "        \"uid\": \"" + uid + "\",\n" +
                "        \"did\": \"iocr\",\n" +
                "        \"net_type\": \"wifi\",\n" +
                "        \"net_isp\": \"CMCC\",\n" +
                "        \"status\": 0,\n" +
                "        \"request_id\": null,\n" +
                "        \"res_id\": \"\"\n" +
                "    },\n" +
                "    \"parameter\": {\n" +
                "        \"ocr\": {\n" +
                "            \"result_option\": \"normal\",\n" +
                "            \"result_format\": \"json,markdown\",\n" +
                "            \"output_type\": \"one_shot\",\n" +
                "            \"exif_option\": \"1\",\n" +
                "            \"json_element_option\": \"\",\n" +
                "            \"markdown_element_option\": \"watermark=1,page_header=1,page_footer=1,page_number=1,graph=1\",\n" +
                "            \"sed_element_option\": \"watermark=0,page_header=0,page_footer=0,page_number=0,graph=0\",\n" +
                "            \"alpha_option\": \"0\",\n" +
                "            \"rotation_min_angle\": 5,\n" +
                "            \"result\": {\n" +
                "                \"encoding\": \"utf8\",\n" +
                "                \"compress\": \"raw\",\n" +
                "                \"format\": \"plain\"\n" +
                "            }\n" +
                "        }\n" +
                "    },\n" +
                "    \"payload\": {\n" +
                "        \"image\": {\n" +
                "            \"encoding\": \"" + fileType + "\",\n" +
                "            \"image\": \"" + imageBase64 + "\",\n" +
                "            \"status\": 0,\n" +
                "            \"seq\": 0\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    // 读取图片文件
    private static FileData readImageFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        String base64 = Base64.getEncoder().encodeToString(bytes);
        String fileType = path.substring(path.lastIndexOf(".") + 1);
        return new FileData(base64, fileType);
    }

    private static class FileData {
        String base64;
        String fileType;

        FileData(String base64, String fileType) {
            this.base64 = base64;
            this.fileType = fileType;
        }
    }
}
