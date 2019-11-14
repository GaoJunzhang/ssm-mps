package com.seeyoo.mps.tool;

import com.seeyoo.mps.vo.VideoInfo;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by user on 2019/9/24.
 */
public class FFmpegUtil {

    public static ByteArrayOutputStream getVideoImage(File file, int widthdist, int heightdist,int limit) throws FrameGrabber.Exception {
        FFmpegFrameGrabber ff = FFmpegFrameGrabber.createDefault(file);
        BufferedImage bufferedImage = null;
        ff.start();
        int ffLength = ff.getLengthInFrames();
        org.bytedeco.javacv.Frame f;
        int i = 0;
        while (i < ffLength) {
            f = ff.grabImage();
            if (i == limit) {
                bufferedImage = getThumbnail(f,widthdist,heightdist);
                break;
            }
            i++;
        }
        ff.stop();
        ByteArrayOutputStream compressedOs = new ByteArrayOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(compressedOs);
        try {
            encoder.encode(bufferedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compressedOs;
    }

    private static BufferedImage getThumbnail(org.bytedeco.javacv.Frame f, int widthdist, int heightdist) {
        String imagemat = "jpg";
        if (null == f) {
            return null;
        }
        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage bi = converter.getBufferedImage(f);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Image src = null;
        try {
            ImageIO.write(bi, imagemat, os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            src = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedImage tag = new BufferedImage((int) widthdist, (int) heightdist, BufferedImage.TYPE_INT_RGB);
        tag.getGraphics().drawImage(src.getScaledInstance(widthdist, heightdist, Image.SCALE_SMOOTH), 0, 0, null);
        return tag;
    }

    public static VideoInfo getVideoDuration(File video) {
        VideoInfo videoInfo = new VideoInfo();
        long duration = 0L;
        FFmpegFrameGrabber ff = new FFmpegFrameGrabber(video);
        try {
            ff.start();
            duration = ff.getLengthInTime() / (1000 * 1000);
            videoInfo.setDuration((int)duration);
            videoInfo.setWidth(ff.getImageWidth());
            videoInfo.setHeight(ff.getImageHeight());
            ff.stop();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        return videoInfo;
    }

    public static String convertToFlv(File file) {

        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(file);

        String fileName = null;

        org.bytedeco.javacv.Frame captured_frame = null;

        FFmpegFrameRecorder recorder = null;

        try {
            frameGrabber.start();
            String str = file.getAbsolutePath();
            fileName = str.substring(0,str.lastIndexOf(".")) + ".flv";
//            recorder = new FFmpegFrameRecorder(fileName, frameGrabber.getImageWidth(), frameGrabber.getImageHeight());
            recorder = new FFmpegFrameRecorder(fileName, frameGrabber.getImageWidth(), frameGrabber.getImageHeight(), frameGrabber.getAudioChannels());
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
            recorder.setFormat("flv");

            recorder.setFrameRate(frameGrabber.getFrameRate());
            recorder.setVideoBitrate(frameGrabber.getVideoBitrate());
            recorder.setAudioBitrate(frameGrabber.getAudioBitrate());
//            recorder.setSampleFormat(frameGrabber.getSampleFormat()); //
            recorder.setSampleRate(frameGrabber.getSampleRate());

            recorder.setAudioChannels(frameGrabber.getAudioChannels());
            recorder.start();
            while ((captured_frame = frameGrabber.grabFrame()) != null) {
                try {
//                    recorder.setTimestamp(frameGrabber.getTimestamp());
                    recorder.record(captured_frame);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            recorder.stop();
            recorder.release();
            frameGrabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //file.delete();
        return fileName;
    }
/*
    public static void main(String[] args) {
        String base_path = "D:\\video\\";
//        String file_name = "a.mp4";
        String file_name = "CASIO.mpg";
        File file = new File(base_path + file_name);

        System.out.println(convertToFlv(file));
    }
*/

}
