package com.seeyoo.mps.tool;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.seeyoo.mps.service.SystemSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Random;

/**
 * Created by user on 2019/10/21.
 */
@Slf4j
public class OSSClientUtil {
    private static String endpoint;
    private static String accessKeyId;
    private static String accessKeySecret;
    private static String bucketName;
    private static String accessUrl;
    private static String ossDir;

    public static String getAccessUrl() {
        return accessUrl;
    }

    public static void setAccessUrl(String accessUrl) {
        OSSClientUtil.accessUrl = accessUrl;
    }

    public static String getOssDir() {
        return ossDir;
    }

    public static void setOssDir(String ossDir) {
        OSSClientUtil.ossDir = ossDir;
    }

    static {
        SystemSettingService systemSettingService = SpringUtil.getBean(SystemSettingService.class);
        String ossStr = systemSettingService.getSetting("aliyun_oss", "{}");
        JSONObject ossObj = JSONUtil.parseObj(ossStr);
        endpoint = ossObj.get("endpoint") + "";
        accessKeyId = ossObj.get("key") + "";
        accessKeySecret = ossObj.get("secret") + "";
        bucketName = ossObj.get("bucket") + "";
        accessUrl = ossObj.get("url") + "";
        ossDir = ossObj.get("file") + "";
    }

    public static OSSClient getOssClient() {
        return new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }

    public static String uploadByByteToOSS(byte[] bcyte, String fileName) {
        String resultStr = null;
        Long fileSize = (long) bcyte.length;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileSize);
        metadata.setCacheControl("no-cache");
        metadata.setHeader("Pragma", "no-cache");
        metadata.setContentEncoding("utf-8");
        metadata.setContentType(getContentType(fileName));
        String filePath = ossDir + fileName;
        metadata.setContentDisposition("filename/filesize=" + fileName + "/" + fileSize + "Byte.");
        getOssClient().putObject(bucketName, filePath, new ByteArrayInputStream(bcyte),
                metadata);
        StringBuilder sb = new StringBuilder(endpoint + "/" + filePath);
        sb.insert(7, bucketName + ".");
        resultStr = sb.toString();
        return resultStr;
    }

    /**
     * 上传图片至OSS
     *
     * @return String 返回的唯一MD5数字签名
     */
    public static String uploadObject2OSS(InputStream is, String fileName) {
        String resultStr = null;
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(is.available());
            metadata.setCacheControl("no-cache");
            metadata.setHeader("Pragma", "no-cache");
            metadata.setContentEncoding("utf-8");
            metadata.setContentType(getContentType(fileName));
            String filePath = ossDir + fileName;
            metadata.setContentDisposition("filename/filesize=" + (filePath));
            PutObjectResult putResult = getOssClient().putObject(bucketName, filePath, is, metadata);
            resultStr = putResult.getETag();
            StringBuilder sb = new StringBuilder(endpoint + "/" + filePath);
            sb.insert(7, bucketName + ".");
            resultStr = sb.toString();
        } catch (Exception e) {
            log.error("上传阿里云OSS服务器异常." + e.getMessage(), e);
        }
        return resultStr;
    }

    /**
     * 上传到OSS服务器 如果同名文件会覆盖服务器上的
     *
     * @param instream 文件流
     * @param fileName 文件名称 包括后缀名
     * @return 出错返回"" ,唯一MD5数字签名
     */
    public static String uploadFile2OSS(InputStream instream, String fileName) {
        String ret = "";
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(instream.available());
            objectMetadata.setCacheControl("no-cache");
            objectMetadata.setHeader("Pragma", "no-cache");
            objectMetadata.setContentType(getcontentType(fileName.substring(fileName.lastIndexOf("."))));
            objectMetadata.setContentDisposition("inline;filename=" + fileName);
            String filePath = ossDir + fileName;
            getOssClient().putObject(bucketName, filePath, instream, objectMetadata);
            ret = filePath;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (instream != null) {
                    instream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static String uploadFile2OSS(File file, String fileName) {
        String ret = "";
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.length());
            objectMetadata.setCacheControl("no-cache");
            objectMetadata.setHeader("Pragma", "no-cache");
            objectMetadata.setContentType(getcontentType(fileName.substring(fileName.lastIndexOf("."))));
            objectMetadata.setContentDisposition("inline;filename=" + fileName);
            String filePath = ossDir + fileName;
            getOssClient().putObject(bucketName, filePath, file, objectMetadata);
            ret = filePath;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
        }
        return ret;
    }

    /**
     * @param file
     * @return
     * @throws IOException
     * @Title: uploadFileToOSS
     * @Description: 以文件的形式上传文件到OSS
     * @return: String
     */
    public static String uploadFileToOSS(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        Random random = new Random();
        String fileName = random.nextInt(10000) + System.currentTimeMillis() + substring;
        InputStream inputStream = null;
        inputStream = file.getInputStream();
        return uploadFile2OSS(inputStream, fileName);
    }

    public static String uploadFileToOSS(MultipartFile file, String fileName) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        fileName += substring;
        InputStream inputStream = null;
        inputStream = file.getInputStream();
        return uploadFile2OSS(inputStream, fileName);
    }

    /**
     * Description: 判断OSS服务文件上传时文件的contentType
     *
     * @param FilenameExtension 文件后缀
     * @return String
     */
    public static String getcontentType(String FilenameExtension) {
        if (FilenameExtension.equalsIgnoreCase(".bmp")) {
            return "image/bmp";
        }
        if (FilenameExtension.equalsIgnoreCase(".gif")) {
            return "image/gif";
        }
        if (FilenameExtension.equalsIgnoreCase(".jpeg") || FilenameExtension.equalsIgnoreCase(".jpg")
                || FilenameExtension.equalsIgnoreCase(".png")) {
            return "image/jpeg";
        }
        if (FilenameExtension.equalsIgnoreCase(".html")) {
            return "text/html";
        }
        if (FilenameExtension.equalsIgnoreCase(".txt")) {
            return "text/plain";
        }
        if (FilenameExtension.equalsIgnoreCase(".vsd")) {
            return "application/vnd.visio";
        }
        if (FilenameExtension.equalsIgnoreCase(".pptx") || FilenameExtension.equalsIgnoreCase(".ppt")) {
            return "application/vnd.ms-powerpoint";
        }
        if (FilenameExtension.equalsIgnoreCase(".docx") || FilenameExtension.equalsIgnoreCase(".doc")) {
            return "application/msword";
        }
        if (FilenameExtension.equalsIgnoreCase(".xml")) {
            return "text/xml";
        }
        if (FilenameExtension.equalsIgnoreCase(".pdf")) {
            return "application/pdf";
        }
        if (FilenameExtension.equalsIgnoreCase(".xls")) {
            return "application/vnd.ms-excel";
        }
        if (FilenameExtension.equalsIgnoreCase(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        return "image/jpeg";
    }

    /**
     * 获得图片路径
     *
     * @param fileUrl
     * @return
     */
    public static String getImgUrl(String fileUrl) {
        System.out.println(fileUrl);
        if (!StringUtils.isEmpty(fileUrl)) {
            String[] split = fileUrl.split("/");
            return getUrl(ossDir + split[split.length - 1]);
        }
        return null;
    }

    /**
     * 获得url链接
     *
     * @param key
     * @return
     */
    public static String getUrl(String key) {
        // 设置URL过期时间为10年 3600l* 1000*24*365*10
        Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
        // 生成URL
        URL url = getOssClient().generatePresignedUrl(bucketName, key, expiration);
        if (url != null) {
            return url.toString();
        }
        return null;
    }

    /**
     * 通过文件名判断并获取OSS服务文件上传时文件的contentType
     *
     * @param fileName 文件名
     * @return 文件的contentType
     */
    private static String getContentType(String fileName) {
        log.info("getContentType:" + fileName);
        // 文件的后缀名
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        if (".bmp".equalsIgnoreCase(fileExtension)) {
            return "image/bmp";
        }
        if (".gif".equalsIgnoreCase(fileExtension)) {
            return "image/gif";
        }
        if (".jpeg".equalsIgnoreCase(fileExtension) || ".jpg".equalsIgnoreCase(fileExtension)
                || ".png".equalsIgnoreCase(fileExtension)) {
            return "image/jpeg";
        }
        if (".html".equalsIgnoreCase(fileExtension)) {
            return "text/html";
        }
        if (".txt".equalsIgnoreCase(fileExtension)) {
            return "text/plain";
        }
        if (".vsd".equalsIgnoreCase(fileExtension)) {
            return "application/vnd.visio";
        }
        if (".ppt".equalsIgnoreCase(fileExtension) || "pptx".equalsIgnoreCase(fileExtension)) {
            return "application/vnd.ms-powerpoint";
        }
        if (".doc".equalsIgnoreCase(fileExtension) || "docx".equalsIgnoreCase(fileExtension)) {
            return "application/msword";
        }
        if (".xml".equalsIgnoreCase(fileExtension)) {
            return "text/xml";
        }
        if (".pdf".equalsIgnoreCase(fileExtension)) {
            return "application/pdf";
        }
        // 默认返回类型
        return "image/jpeg";
    }

    /**
     * @param fileName
     * @return
     * @Title: getInputStreamByFileUrl
     * @Description: 根据文件路径获取InputStream流
     * @return: InputStream
     */
    public static InputStream getInputStreamByFileUrl(String fileName) {
        OSSObject ossObject = getOssClient().getObject(bucketName, ossDir + fileName);
        return ossObject.getObjectContent();
    }

    /**
     * 根据key删除OSS服务器上的文件 @Title: deleteFile @Description: @param @param
     * ossConfigure @param 配置文件实体 @param filePath 设定文件 @return void 返回类型 @throws
     */
    public static void deleteFile(String filePath) {
        InputStream ins = null;
        try {
            ins = new URL(accessUrl + "/" + filePath).openStream();
            getOssClient().deleteObject(bucketName, filePath);
        } catch (IOException e) {
            System.out.println("文件不存在");
        }
    }
}