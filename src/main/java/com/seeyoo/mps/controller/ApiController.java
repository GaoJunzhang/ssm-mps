package com.seeyoo.mps.controller;

import com.seeyoo.mps.generator.base.BaseController;
import com.seeyoo.mps.model.Weather;
import com.seeyoo.mps.service.WeatherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.*;

/**
 * @author Wangj
 */
@Slf4j
@RestController
@Api(description = "API接口")
@RequestMapping("/api/*")
@Transactional
public class ApiController {

    @RequestMapping(value = "/uploadScreen")
    @ApiOperation(value = "截屏")
    public Object weatherCityData(@RequestParam("image") MultipartFile image) {
//        if (!image.isEmpty()) {
//
//            try {
//                String name = image.getOriginalFilename();
//                File saveFile = new File(resourceDir + "/screen/" + name);
//                if (saveFile.exists()) {
//                    saveFile.delete();
//                }
//                if (!saveFile.getParentFile().exists()) {
//                    saveFile.getParentFile().mkdirs();
//                }
//                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(saveFile));
//                out.write(image.getBytes());
//                out.flush();
//                out.close();
//
//                return "success";
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        return "error";
    }

}
