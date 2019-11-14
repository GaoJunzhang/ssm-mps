package com.seeyoo.mps.controller;

import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.integration.redis.RedisObjectManager;
import com.seeyoo.mps.model.Media;
import com.seeyoo.mps.model.Mgroup;
import com.seeyoo.mps.model.User;
import com.seeyoo.mps.service.MediaService;
import com.seeyoo.mps.tool.MD5Util;
import com.seeyoo.mps.tool.OSSClientUtil;
import com.seeyoo.mps.tool.PageUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.PageVo;
import com.seeyoo.mps.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author GaoJunZhang
 */
@Slf4j
@RestController
@Api(tags = "媒体管理接口")
@RequestMapping("/media")
@Transactional
public class MediaController {

    @Autowired
    private MediaService mediaService;

    @Autowired
    private RedisObjectManager redisObjectManager;

    @Value("${file.dir}")
    private String fileDir;

    @RequiresPermissions("mediaList")
    @RequestMapping(value = "/getMediaData", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Object getMediaData(HttpSession session, Long mgroupId, String name, Short audit, String types, @ModelAttribute PageVo pageVo) {
        long userId = (long) session.getAttribute("userId");
        String[] typeArry = null;
        if (types != null) {
            typeArry = types.split(",");
        }
        if (StrUtil.isEmpty(pageVo.getSortField())) {
            pageVo.setSortField("createTime");
            pageVo.setSortOrder("desc");
        }
        return new ResultUtil().setData(mediaService.pageMediaData(mgroupId, name, audit, typeArry, userId, PageUtil.initPage(pageVo)));
    }

    @RequiresPermissions("media:del")
    @RequestMapping(value = "/delMedia/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "批量删除媒体")
    public Result<String> delMedia(@PathVariable String[] ids) {
        for (String id : ids) {
            Media media = mediaService.get(Long.parseLong(id));
            media.setIsDelete((short) 1);
            mediaService.update(media);
            /* mediaService.delete(Long.parseLong(id));
           ossClientUtil.deleteFile(media.getPath());
            String jpgFilename = media.getPath().substring(0, media.getPath().lastIndexOf(".")) + ".jpg";
            ossClientUtil.deleteFile(jpgFilename);*/
        }
        return new ResultUtil<String>().setSuccessMsg("删除成功");
    }

    @RequestMapping(value = "/getTransStatus", method = RequestMethod.GET)
    @ApiOperation(value = "获取视频转化上传信息")
    public Object getTransStatus() {
        List<Media> medias = (List<Media>) redisObjectManager.getObject("transFiles");
        if (medias == null) {
            return new ResultUtil<List<Media>>().setData(new ArrayList<>());
        }
        return new ResultUtil<List<Media>>().setData(medias);
    }

    @RequiresPermissions("media:upload")
    @RequestMapping(value = "/saveMedia", method = RequestMethod.POST)
    @ApiOperation(value = "保存媒体")
    public Result<String> saveMedia(@RequestParam(name = "name", required = true) String name,
                                    @RequestParam(name = "id", required = true) Long id, String remark,
                                    @RequestParam(name = "mGroupId", required = true) long mGroupId) {
        Media media = mediaService.get(id);
        if (media == null) {
            return new ResultUtil<String>().setErrorMsg("媒体数据异常");
        }
        media.setName(name);
        media.setRemark(remark);
        Mgroup mgroup = new Mgroup();
        mgroup.setId(mGroupId);
        media.setMgroup(mgroup);
        mediaService.save(media);
        return new ResultUtil<String>().setSuccessMsg("保存成功");
    }

    @RequiresPermissions("media:addSubtitle")
    @GetMapping(value = "/getSutitleInfoById")
    @ApiOperation(value = "读取字幕文件，获取字幕信息")
    public Result<Map> getSutitleInfoById(@RequestParam(name = "id", required = true) long id) {
        Map<String, String> map = new HashMap<>();
        Media media = mediaService.get(id);
        if (media == null) {
            return new ResultUtil<Map>().setErrorMsg("媒体不存在");
        }
        File file = new File(fileDir + File.separator + media.getLocalPath());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            if (file.exists()) {
                Document document = builder.parse(file);
                NodeList sList = document.getElementsByTagName("Body");
                map = node(sList);
            } else {
                Document document = builder.parse(OSSClientUtil.getAccessUrl() + "/" + media.getPath());
                NodeList sList = document.getElementsByTagName("Body");
                map = node(sList);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        map.put("fileName", media.getName());
        map.put("mGroupId", media.getMgroup().getId() + "");
        map.put("mGroupName", media.getMgroup().getName());
        return new ResultUtil<Map>().setData(map);
    }

    @RequiresPermissions("media:addSubtitle")
    @RequestMapping(value = "/saveSubtitle", method = RequestMethod.POST)
    @ApiOperation(value = "保存字幕")
    public Result<String> saveSubtitle(HttpSession session, Long id,
                                       @RequestParam(name = "fileName", required = true) String fileName,
                                       @RequestParam(name = "FontName", required = true) String FontName,
                                       @RequestParam(name = "BackColor", required = true) String BackColor,
                                       @RequestParam(name = "FontColor", required = true) String FontColor,
                                       @RequestParam(name = "FontSize", required = true) String FontSize,
                                       @RequestParam(name = "subtitleHtml", required = true) String subtitleHtml,
                                       @RequestParam(name = "mGroupId", required = true) long mGroupId,
                                       @RequestParam(name = "subtitle", required = true) String subtitle) {


        Media media = null;
        String subxml = createTextXml(fileName, BackColor, FontColor, FontSize, FontName, subtitle, subtitleHtml);
        int size = Integer.parseInt(getTextSize(subxml));//文件大小
        System.out.println(fileDir + File.separator + subxml);
        String md5 = MD5Util.getLocalMd5(fileDir + File.separator + subxml);
        List<Media> mediaList = mediaService.findAllByMd5(md5);
        if (id == null) {
            media = new Media();
            media.setSize(size);
            media.setMd5(md5);
            media.setType((short) 4);
            media.setCreateTime(new Timestamp(System.currentTimeMillis()));
            media.setIsDelete((short) 0);
        } else {
            media = mediaService.get(id);
        }
        if (mediaList.size() > 0) {
            File subTmpFile = new File(fileDir + File.separator + subxml);
            subTmpFile.delete();
            media.setSize(mediaList.get(0).getSize());
            media.setLocalPath(mediaList.get(0).getLocalPath());
            media.setPath(mediaList.get(0).getPath());
            media.setMd5(md5);
        } else {
            String localPath = OSSClientUtil.getOssDir();
            localPath = localPath.substring(0, localPath.length() - 1);
            String tmpFileName = fileName + "_" + md5 + ".txt";
            File oldFile = new File(fileDir + File.separator + subxml);
            File file = new File(fileDir + File.separator + localPath + File.separator + "text" + File.separator + tmpFileName);
            oldFile.renameTo(file);
            InputStream ins = null;
            try {
                ins = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String result = OSSClientUtil.uploadFile2OSS(ins, "text/" + tmpFileName);
            media.setSize(size);
            media.setPath(result);
            media.setLocalPath(subxml);
        }
        media.setName(fileName);
        Mgroup mgroup = new Mgroup();
        mgroup.setId(mGroupId);
        media.setMgroup(mgroup);
        long userId = (long) session.getAttribute("userId");
        User user = new User();
        user.setId(userId);
        media.setUser(user);
        mediaService.save(media);
        return new ResultUtil<String>().setSuccessMsg("保存成功");
    }

    @RequiresPermissions("media:audit")
    @RequestMapping(value = "/auditMedia", method = RequestMethod.POST)
    @ApiOperation(value = "媒体审核")
    public Result<String> auditMedia(@RequestParam(name = "ids") String[] ids, @RequestParam(name = "audit", required = true) short audit) {
        try {
            mediaService.updateAuditByIds(audit, ids);
            return new ResultUtil<String>().setSuccessMsg("审核成功");
        } catch (Exception e) {
            return new ResultUtil<String>().setErrorMsg("审核失败");
        }
    }

    public String getTextSize(String path) {
        File f = new File(fileDir + File.separator + path);
        if (f.exists() && f.isFile()) {
            return Long.toString(f.length());
        }
        return null;
    }

    public String createTextXml(String name, String backColor, String fontColor, String fontSize, String fontFamily, String textContent,
                                String htmlText) {
        String textFileName = name + "_" + UUID.randomUUID().toString().replace("-", "") + ".txt";
        String localPath = OSSClientUtil.getOssDir();
        localPath = localPath.substring(0, localPath.length() - 1);
        String sysDir = localPath + File.separator + "text";
        File ftpTaskPath = new File(fileDir + File.separator + sysDir);
        if (!ftpTaskPath.exists()) {
            ftpTaskPath.mkdirs();
        }
        String path = fileDir + File.separator + sysDir + File.separator + textFileName;
        log.info("字幕文件生成路径:{}=>", path);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement("Text");
            document.appendChild(root);
            Element Body = document.createElement("Body");
            root.appendChild(Body);
            Element Align = document.createElement("Align");
            Align.appendChild(document.createTextNode("0"));
            Body.appendChild(Align);

            Element Html = document.createElement("Html");
            Html.appendChild(document.createTextNode(htmlText));
            Body.appendChild(Html);

            Element FontName = document.createElement("FontName");
            FontName.appendChild(document.createTextNode(fontFamily));
            Body.appendChild(FontName);

            Element BackColor = document.createElement("BackColor");
            BackColor.appendChild(document.createTextNode(backColor));
            Body.appendChild(BackColor);

            Element FontColor = document.createElement("FontColor");
            FontColor.appendChild(document.createTextNode(fontColor));
            Body.appendChild(FontColor);

            Element FontSize = document.createElement("FontSize");
            FontSize.appendChild(document.createTextNode(fontSize));
            Body.appendChild(FontSize);

            Element BR = document.createElement("BR");
            BR.appendChild(document.createTextNode(textContent));
            Body.appendChild(BR);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            java.io.Writer wr = new java.io.OutputStreamWriter(new java.io.FileOutputStream(path), "UTF-8");
            PrintWriter pw = new PrintWriter(wr);
            StreamResult result = new StreamResult(pw);
            transformer.transform(source, result);
            log.info("生成字幕XML文件成功!");
            wr.close();
            pw.close();
            return sysDir + File.separator + textFileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> node(NodeList list) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            NodeList childNodes = node.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                if (childNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    map.put(childNodes.item(j).getNodeName() + "", childNodes.item(j).getFirstChild().getNodeValue());
                }
            }
        }
        return map;
    }
}