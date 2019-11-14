package com.seeyoo.mps.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.bean.TerminalBean;
import com.seeyoo.mps.dao.TerminalRepository;
import com.seeyoo.mps.model.ProjectTerminal;
import com.seeyoo.mps.model.Terminal;
import com.seeyoo.mps.service.TerminalService;
import com.seeyoo.mps.tool.OSSClientUtil;
import com.seeyoo.mps.tool.PageUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.PageVo;
import com.seeyoo.mps.vo.Result;
import com.seeyoo.mps.vo.SearchVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 终端接口实现
 *
 * @author GaoJunZhang
 */
@Slf4j
@Service
@Transactional
public class TerminalServiceImpl implements TerminalService {

    @Autowired
    private TerminalRepository terminalRepository;

    @Value("${file.dir}")
    private String fileDir;

    @Override
    public Map<String, Object> terminalList(TerminalBean terminalBean, Pageable pageable) {
        Page<Terminal> terminals = terminalPage(terminalBean, pageable);
        List<Map<String, Object>> terminalMap = new ArrayList<>();
        for (Terminal terminal : terminals) {
            Map<String, Object> t = new HashMap<>();
            t.put("id", terminal.getId());
            t.put("name", terminal.getName());
            t.put("tgroupName", terminal.getTgroup().getName());
            t.put("mac", terminal.getMac());
            t.put("devState", terminal.getDevState());
            t.put("dlFileSize", terminal.getDlFileSize());
            t.put("useableSpace", terminal.getUseableSpace());
            t.put("diskSpace", terminal.getDiskSpace());
            t.put("playContent", terminal.getPlayContent());
            t.put("imdUpdateTime", terminal.getImdUpdateTime());
            t.put("serverIp", terminal.getServerIp());
            t.put("serverMac", terminal.getServerMac());
            t.put("systemVersion", terminal.getSystemVersion());
            t.put("appVersion", terminal.getAppVersion());
            t.put("connectStatus", terminal.getConnectStatus());
            t.put("connectTime", DateUtil.format(terminal.getConnectTime(), "yyyy-MM-dd HH:mm:ss"));
            t.put("disconnectTime", DateUtil.format(terminal.getDisconnectTime(), "yyyy-MM-dd HH:mm:ss"));
            t.put("deleteTime", DateUtil.format(terminal.getDeleteTime(), "yyyy-MM-dd HH:mm:ss"));
            t.put("adCount", terminal.getAdCount());
            t.put("adUseCount", terminal.getAdUseCount());
            t.put("adTaskName", terminal.getTaskName());
            t.put("sendTaskName", StringUtils.EMPTY);
            terminalMap.add(t);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", pageable.getPageNumber());
        map.put("totalCount", terminals.getTotalElements());
        map.put("data", terminalMap);
        return map;
    }

    public int rename(Long id, String name) {
        return terminalRepository.updateNameById(name, id);
    }

    public int updateTgroupById(Long tgroupId, Long id) {
        return terminalRepository.updateTgroupById(tgroupId, id);
    }

    public Page<Terminal> terminalPage(TerminalBean terminalBean, Pageable pageable) {
        Specification specification = new Specification<Terminal>() {
            @Override
            public Predicate toPredicate(Root<Terminal> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));

                if (terminalBean.getTgroupId() != null) {
                    predicate.getExpressions().add(cb.equal(r.get("tgroup").get("id"), terminalBean.getTgroupId()));
                }
                if (!StrUtil.isEmpty(terminalBean.getName())) {
                    predicate.getExpressions().add(cb.like(r.get("name"), "%" + terminalBean.getName() + "%"));
                }
                if (!StrUtil.isEmpty(terminalBean.getTgroupName())) {
                    predicate.getExpressions().add(cb.like(r.get("tgroup").get("name"), "%" + terminalBean.getTgroupName() + "%"));
                }
                if (!StrUtil.isEmpty(terminalBean.getMac())) {
                    predicate.getExpressions().add(cb.like(r.get("mac"), "%" + terminalBean.getMac() + "%"));
                }
                return predicate;
            }
        };
        return terminalRepository.findAll(specification, pageable);
    }

    public Result exportVacancy(TerminalBean terminalBean) {
        int page = 1;
        int totalPage = 0;
        List<String[]> terminalList = new ArrayList<>();
        PageVo pageVo = new PageVo();
        pageVo.setPageNo(1);
        pageVo.setPageSize(100);
        List<String> titles = new ArrayList<>();
        titles.add("终端组");
        titles.add("终端名称");
        titles.add("MAC");
        titles.add("状态");
        titles.add("设备状态");
        titles.add("磁盘空间");
        titles.add("终端任务");
        titles.add("刊位状态");
        titles.add("上线时间");
        titles.add("离线时间");
        titles.add("应用版本");
        titles.add("系统版本");
        do {
            Page<Terminal> terminals = terminalPage(terminalBean, PageUtil.initPage(pageVo));
            for (Terminal terminal : terminals) {
                String[] content = new String[titles.size()];
                content[0] = terminal.getTgroup().getName();
                content[1] = terminal.getName() + "";
                content[2] = terminal.getMac() + "";
                if ("1".equals(terminal.getConnectStatus())) {
                    content[3] = "在线";
                } else {
                    if (terminal.getDisconnectTime() != null) {
                        Timestamp nowTime = new Timestamp(System.currentTimeMillis());
                        System.out.println("离线时间");
                        System.out.println(nowTime.getTime() - terminal.getDisconnectTime().getTime());
                        if (nowTime.getTime() - terminal.getDisconnectTime().getTime() < 3 * 24 * 60 * 60 * 1000) {
                            content[3] = "离线";
                        } else {
                            content[3] = "多日离线";
                        }
                    }
                }
                content[4] = "1".equals(terminal.getDevState()) ? "播放" : "暂停";
                content[5] = terminal.getDiskSpace();
                content[6] = terminal.getTaskName();
                content[7] = terminal.getAdCount() == null ? "0" : terminal.getAdCount() + "";
                content[8] = terminal.getConnectTime() == null ? "" : DateUtil.format(terminal.getConnectTime(), "yyyy-MM-dd HH:mm:ss");
                content[9] = terminal.getDisconnectTime() == null ? "" : DateUtil.format(terminal.getDisconnectTime(), "yyyy-MM-dd HH:mm:ss");
                content[10] = terminal.getAppVersion();
                content[11] = terminal.getSystemVersion();
                terminalList.add(content);
            }
            totalPage = terminals.getTotalPages();
            page++;
        } while (page < totalPage);
        try {
            ProjectTerminalServiceImpl projectTerminalService = new ProjectTerminalServiceImpl();
            HSSFWorkbook wb = projectTerminalService.getHSSFWorkbook("终端表", titles, terminalList, null);
            String dir = "export";
            String name = "Terminal_" + System.currentTimeMillis() + ".xls";
            File file = new File(fileDir + File.separator + OSSClientUtil.getOssDir() + File.separator + dir + File.separator + name);
            FileUtil.mkParentDirs(file);
            FileOutputStream fos = new FileOutputStream(file);
            wb.write(fos);
            fos.flush();
            fos.close();
            String result = OSSClientUtil.uploadFile2OSS(file, dir + "/" + name);
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("url", OSSClientUtil.getAccessUrl() + "/" + result);
            return new ResultUtil<>().setData(map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResultUtil<>().setErrorMsg("无法导出");
    }
}