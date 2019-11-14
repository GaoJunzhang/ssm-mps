package com.seeyoo.mps.controller;

import cn.hutool.core.io.FileUtil;
import com.seeyoo.mps.conn.TransFlvProcesser;
import com.seeyoo.mps.model.Media;
import com.seeyoo.mps.model.Mgroup;
import com.seeyoo.mps.model.User;
import com.seeyoo.mps.service.MediaService;
import com.seeyoo.mps.tool.FFmpegUtil;
import com.seeyoo.mps.tool.MD5Util;
import com.seeyoo.mps.tool.OSSClientUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.Result;
import com.seeyoo.mps.vo.VideoInfo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

/**
 * Created by user on 2019/9/19.
 */
@Slf4j
@RestController
@Api(description = "上传文件接口")
@RequestMapping("/oss")
@Transactional
public class OssController {

    @Value("${file.dir}")
    private String fileDir;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private TransFlvProcesser transFlvProcesser;

    @RequiresPermissions("media:upload")
    @RequestMapping(value = "/uploadOss", method = RequestMethod.POST)
    public Result<String> uploadOss(HttpSession session, @RequestParam(name = "file") MultipartFile file,
                                    @RequestParam(name = "mGroupId", required = true) long mGroupId,
                                    @RequestParam(name = "type", required = true) short type,
                                    @RequestParam(name = "size", required = true) int size,
                                    @RequestParam(name = "name", required = true) String name) {
        log.info("{},{},{},{}", type, size, file.getOriginalFilename(), mGroupId);
        String result = "";
        Media media = new Media();
        try {
            String prefix = name.substring(name.lastIndexOf("."));
            name = name.substring(0, name.lastIndexOf("."));
            String md5 = MD5Util.getMd5ByIns(file.getInputStream());
            List<Media> mediaList = mediaService.findAllByMd5(md5);
            long userId = (long) session.getAttribute("userId");
            User user = new User();
            user.setId(userId);
            media.setUser(user);
            Mgroup mgroup = new Mgroup();
            mgroup.setId(mGroupId);
            media.setMgroup(mgroup);
            media.setType(type);
            media.setSize(size);
            media.setName(name);
            media.setIsDelete((short) 0);
            media.setCreateTime(new Timestamp(System.currentTimeMillis()));
            media.setMd5(md5);
            if (mediaList.size() > 0) {
                media.setLocalPath(mediaList.get(0).getLocalPath());
                media.setPath(mediaList.get(0).getPath());
                media.setWidth(mediaList.get(0).getWidth());
                media.setHeight(mediaList.get(0).getHeight());
                media.setDuration(mediaList.get(0).getDuration());
                mediaService.save(media);
            } else {
                String localPath = OSSClientUtil.getOssDir();
                localPath = localPath.substring(0, localPath.length() - 1);
                String tmpFileName = name + "_" + md5;
                String newFileName = tmpFileName + prefix;
                String fileDirName = "other";
                if (type == 0) {
                    fileDirName = "img";
                }
                if (type == 1) {
                    fileDirName = "video";
                }
                if (type == 2) {
                    fileDirName = "audio";
                }
                if (type == 3) {
                    fileDirName = "zip";
                }
                File f = FileUtil.touch(fileDir + File.separator + localPath + File.separator + fileDirName + File.separator + newFileName);
                FileUtil.writeFromStream(file.getInputStream(), f);
                if (type != 1) {
                    result = OSSClientUtil.uploadFileToOSS(file, fileDirName + "/" + tmpFileName);
                    media.setPath(result);
                    if (type == 0) {
                        try {
                            BufferedImage sourceImg = ImageIO.read(new URL(OSSClientUtil.getAccessUrl() + "/" + media.getPath()).openStream());
                            media.setWidth(sourceImg.getWidth());
                            media.setHeight(sourceImg.getHeight());
                        } catch (IOException e) {
                            return new ResultUtil<String>().setErrorMsg("上传文件【" + name + "】异常");
                        }
                    }
                } else {
                    if (".flv".equals(prefix.toLowerCase())) {
                        result = OSSClientUtil.uploadFileToOSS(file, fileDirName + "/" + tmpFileName);
                        ByteArrayOutputStream byteArrayOutputStream = FFmpegUtil.getVideoImage(f, 800, 600, 5);
                        OSSClientUtil.uploadByByteToOSS(byteArrayOutputStream.toByteArray(), fileDirName + "/" + tmpFileName + ".jpg");
                        VideoInfo videoInfo = FFmpegUtil.getVideoDuration(f);
                        media.setDuration(videoInfo.getDuration());
                        media.setWidth(videoInfo.getWidth());
                        media.setHeight(videoInfo.getHeight());
                        media.setPath(result);
                    } else {
                        ByteArrayOutputStream byteArrayOutputStream = FFmpegUtil.getVideoImage(f, 800, 600, 5);
                        OSSClientUtil.uploadByByteToOSS(byteArrayOutputStream.toByteArray(), fileDirName + "/" + tmpFileName + ".jpg");
                        media.setPath(OSSClientUtil.getOssDir() + fileDirName + "/" + tmpFileName + ".flv");
                        media.setRemark("transitioning");
                    }
                }
                //2.保存数据库
                media.setLocalPath(localPath + File.separator + fileDirName + File.separator + newFileName);
                mediaService.save(media);
                if (type == 1 && !".flv".equals(prefix.toLowerCase())) {
                    Media newMedia = new Media();
                    newMedia.setId(media.getId());
                    newMedia.setName(f.getName());
                    newMedia.setPath(fileDir + File.separator + localPath + File.separator + fileDirName + File.separator + f.getName());
                    newMedia.setUser(media.getUser());
                    transFlvProcesser.produce(newMedia);
                }
            }
        } catch (IOException e) {
            return new ResultUtil<String>().setErrorMsg("上传文件失败，请稍后再试");
        }
        return new ResultUtil<String>().setSuccessMsg("上传成功");
    }

    @RequiresPermissions("media:del")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public void delete(@RequestParam(name = "filePath") String filePath) {
        OSSClientUtil.deleteFile(filePath);
    }

    @RequestMapping(value = "/uploadAvatar", method = RequestMethod.POST)
    public String uploadAvatar(@RequestParam(name = "file") MultipartFile file) {
        Random random = new Random();
        String fileName = random.nextInt(10000) + System.currentTimeMillis() + "";
        String result = "";
        try {
            result = OSSClientUtil.uploadFileToOSS(file, "sysimg/avatar/" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return OSSClientUtil.getAccessUrl() + "/" + result;
    }
}
