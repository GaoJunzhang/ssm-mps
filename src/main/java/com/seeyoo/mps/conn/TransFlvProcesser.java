package com.seeyoo.mps.conn;

import com.seeyoo.mps.dao.MediaRepository;
import com.seeyoo.mps.integration.redis.RedisObjectManager;
import com.seeyoo.mps.model.Media;
import com.seeyoo.mps.tool.FFmpegUtil;
import com.seeyoo.mps.tool.OSSClientUtil;
import com.seeyoo.mps.vo.VideoInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by user on 2019/9/27.
 */
@Component
@Slf4j
public class TransFlvProcesser implements Runnable {

    @Autowired
    RedisObjectManager redisManager;

    @Autowired
    private MediaRepository mediaRepository;

    private ArrayBlockingQueue<Media> queue = new ArrayBlockingQueue<>(3000);

    private List<Media> mediaList = new ArrayList<>();

    public TransFlvProcesser() {
        new Thread(this).start();
    }

    public void produce(Media media) {
        if (!queue.offer(media)) {
            System.out.println("TansFlvProcesser length = " + queue.size());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                transFlvProc(queue.take());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void transFlvProc(Media media) {
        mediaList.add(media);
        redisManager.setObject("transFiles", mediaList);
        if (queue.size() == 0 || mediaList.size() >= 100) {
            for (Media md : mediaList) {
                File file = new File(md.getPath());
                String flvName = FFmpegUtil.convertToFlv(file);
                File file1 = new File(flvName);
                if (!file1.exists()){
                    log.info("视频转换失败== {}",md.getName());
                    continue;
                }
                VideoInfo videoInfo = FFmpegUtil.getVideoDuration(file1);
                log.info("转换后视频宽{},高{},时长{}", videoInfo.getWidth(), videoInfo.getHeight(), videoInfo.getDuration());
                log.info("转换后视频名称{}",file1.getName());
                InputStream ins = null;
                try {
                    ins = new FileInputStream(file1);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                String path = OSSClientUtil.uploadFile2OSS(ins, "video/" + file1.getName());
                log.info("文件oss地址 {}", path);
                mediaRepository.updateMediaPathAndDuration(md.getId(), path, videoInfo.getDuration(), "", videoInfo.getWidth(), videoInfo.getHeight());
//                file1.delete();
            }
            mediaList.clear();
        }
    }
}
