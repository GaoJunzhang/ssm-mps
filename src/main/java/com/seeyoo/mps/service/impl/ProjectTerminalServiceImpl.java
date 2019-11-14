package com.seeyoo.mps.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Joiner;
import com.seeyoo.mps.bean.ProjectBean;
import com.seeyoo.mps.controller.request.AuditProjectRequest;
import com.seeyoo.mps.dao.*;
import com.seeyoo.mps.integration.mq.RabbitMqManager;
import com.seeyoo.mps.model.*;
import com.seeyoo.mps.service.*;
import com.seeyoo.mps.tool.ExcelUtil;
import com.seeyoo.mps.tool.OSSClientUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * 方案接口实现
 *
 * @author Wangj
 */
@Slf4j
@Service
@Transactional
public class ProjectTerminalServiceImpl implements ProjectTerminalService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectTerminalRepository projectTerminalRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TgroupService tgroupService;

    @Override
    public ProjectTerminalRepository getRepository() {
        return projectTerminalRepository;
    }

    @Value("${file.dir}")
    private String fileDir;

    @Override
    public Result exportVacancy(Long userId, Long tgroupId, String start, String end, int count) {
        if (StrUtil.isEmpty(start) || StrUtil.isEmpty(end)) {
            return new ResultUtil<>().setErrorMsg("无效的时间段");
        }
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(DateUtil.parse(start, "yyyy-MM-dd"));
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(DateUtil.parse(end, "yyyy-MM-dd"));

        Calendar day = Calendar.getInstance();
        day.setTime(startCalendar.getTime());
        int dateCnt = 0;
        HashMap<Long, List<Long>> projectTerminalsMap = new HashMap<>();
        HashMap<Long, Integer> okTerminlas = new HashMap<>();
        Set<Long> failTerminals = new HashSet<>();
        while (day.compareTo(endCalendar) <= 0 && dateCnt < 100) {
            List<Project> projects = projectRepository.projectsByDate(day.getTime());
            HashMap<Long, Integer> dateTerminalCount = new HashMap<>();
            for (Project project : projects) {
                List<Long> terminals = projectTerminalsMap.get(project.getId());
                if (terminals == null) {
                    List<ProjectTerminal> projectTerminals = projectTerminalRepository.findAllByProjectId(project.getId());
                    terminals = new ArrayList<>();
                    for (ProjectTerminal projectTerminal : projectTerminals) {
                        terminals.add(projectTerminal.getTerminal().getId());
                    }
                    projectTerminalsMap.put(project.getId(), terminals);
                }

                for (Long id : terminals) {
                    if (failTerminals.contains(id)) {
                        continue;
                    }
                    Integer c = dateTerminalCount.get(id);
                    if (c == null)
                        c = 0;
                    c += project.getAdCount();
                    if (c.intValue() > count) {
                        failTerminals.add(id);
                        okTerminlas.remove(id);
                        dateTerminalCount.remove(id);
                        continue;
                    }

                    Integer max = okTerminlas.get(id);
                    if (max == null) max = 0;
                    if (max < c) {
                        okTerminlas.put(id, c);
                    }
                    dateTerminalCount.put(id, c);
                }
            }
            day.add(Calendar.DATE, 1);
            dateCnt++;
        }

        List<String> titles = new ArrayList<>();
        titles.add("终端名称");
        titles.add("MAC");
        titles.add("终端组");
        titles.add("总刊位数");
        titles.add("已用刊位数");

        int page = 1;
        int totalPage = 0;
        List<String[]> terminalList = new ArrayList<>();
        do {
            Page<Terminal> terminals = terminalsByNameAndMac(userId, tgroupId, "", "", 1, 100);
            for (Terminal terminal : terminals) {
                if (failTerminals.contains(terminal.getId()))
                    continue;
                String[] content = new String[titles.size()];
                content[0] = terminal.getName() + "";
                content[1] = terminal.getMac() + "";
                content[2] = terminal.getTgroup().getName();
                content[3] = terminal.getAdCount() == null ? "0" : terminal.getAdCount() + "";
                content[4] = okTerminlas.get(terminal.getId()) == null ? "0" : "" + okTerminlas.get(terminal.getId());
                terminalList.add(content);
            }
            totalPage = terminals.getTotalPages();
            page++;
        } while (page < totalPage);
        try {
            HSSFWorkbook wb = getHSSFWorkbook("空位表", titles, terminalList, null);
            String dir = "export";
            String name = "Vacancy_" + System.currentTimeMillis() + ".xls";
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

    public HSSFWorkbook getHSSFWorkbook(String sheetName, List<String> title, List<String[]> values, HSSFWorkbook wb) {
        if (wb == null) {
            wb = new HSSFWorkbook();
        }
        HSSFSheet sheet = wb.createSheet(sheetName);
        HSSFCellStyle rowTitleStyle = ExcelUtil.createRowTitleStyle(wb);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, title.size() - 1));

        HSSFRow infoRow = sheet.createRow(0);
        HSSFCell infoCell = infoRow.createCell(0);

        infoCell.setCellValue("导出时间:" + DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss") + ", 共"
                + values.size() + "台终端");
        infoCell.setCellStyle(ExcelUtil.createInfoTitleStyle(wb));

        HSSFCellStyle rowContentStyle = ExcelUtil.createStyle(wb, true);
        HSSFCell cell = null;
        HSSFRow row = sheet.createRow(1);
        for (int i = 0; i < title.size(); i++) {
            cell = row.createCell(i);
            sheet.setColumnWidth((short) i, (short) 5000);

            cell.setCellValue(title.get(i));
            cell.setCellStyle(rowTitleStyle);
        }
        CellRangeAddress c = new CellRangeAddress(1, 1, 0, title.size() - 1);
        sheet.setAutoFilter(c);
        for (int i = 0; i < values.size(); i++) {
            row = sheet.createRow(i + 2);
            for (int j = 0; j < values.get(i).length; j++) {
                cell = row.createCell(j);
                cell.setCellValue(values.get(i)[j]);
                cell.setCellStyle(rowContentStyle);
            }
        }

        return wb;
    }

    @Override
    public Result projectSchedulingList(Long userId, Long tgroupId, String mac, String name, String start, String end, Integer page, Integer size) {
        if (StrUtil.isEmpty(start) || StrUtil.isEmpty(end)) {
            return new ResultUtil<>().setErrorMsg("无效的时间段");
        }
        Page<Terminal> terminals = terminalsByNameAndMac(userId, tgroupId, name, mac, page, size);
        List<HashMap<String, Object>> terminalSchedulingList = new ArrayList<>();

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(DateUtil.parse(start, "yyyy-MM-dd"));
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(DateUtil.parse(end, "yyyy-MM-dd"));

        int maxAdcount = 0;
        for (Terminal terminal : terminals) {
            HashMap<String, Object> t = new HashMap<>();
            t.put("id", terminal.getId());
            t.put("name", terminal.getName());
            t.put("mac", terminal.getMac());

            maxAdcount = terminal.getAdCount().intValue() > maxAdcount ? terminal.getAdCount().intValue() : maxAdcount;

            List<ProjectTerminal> projectTerminals = projectTerminalRepository.findByTerminalAndDate(terminal.getId(), startCalendar.getTime(), endCalendar.getTime());

            int dateCnt = 0;
            Calendar tCalendar = Calendar.getInstance();
            tCalendar.setTime(startCalendar.getTime());
            while (tCalendar.compareTo(endCalendar) <= 0 && dateCnt < 100) {
                String tString = DateUtil.format(tCalendar.getTime(), "yyyy-MM-dd");
                int adCnt = 0;
                int lockCnt = 0;
                int unlockCnt = 0;
                for (ProjectTerminal projectTerminal : projectTerminals) {
                    ProjectBean project = projectService.project(projectTerminal.getProject().getId());
                    if (project == null)
                        continue;

                    if (project.getValidStart() == null || project.getValidEnd() == null)
                        continue;
                    Calendar validStart = Calendar.getInstance();
                    validStart.setTime(project.getValidStart());

                    Calendar validEnd = Calendar.getInstance();
                    validEnd.setTime(project.getValidEnd());

                    if (tCalendar.compareTo(validStart) >= 0 && tCalendar.compareTo(validEnd) <= 0) {
                        for (int i = 0; i < project.getAdCount(); i++) {
                            if (project.getAudit() == ProjectAuditEnum.AUDIT_WAIT) {
                                t.put(tString + "_" + adCnt, project.getName() + "(w)");
                                lockCnt++;
                            } else if (project.getAudit() == ProjectAuditEnum.AUDIT_OK) {
                                t.put(tString + "_" + adCnt, project.getName());
                                unlockCnt++;
                            }
                            adCnt++;
                        }
                    }
                }

                tCalendar.add(Calendar.DATE, 1);
                t.put(tString + "_adCnt", adCnt + "/" + unlockCnt + "/" + lockCnt);
            }
            terminalSchedulingList.add(t);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", page);
        map.put("totalCount", terminals.getTotalElements());
        map.put("data", terminalSchedulingList);
        map.put("adCount", maxAdcount);
        return new ResultUtil<>().setData(map);
    }

    private Page<Terminal> terminalsByNameAndMac(Long userId, Long tgroupId, String name, String mac, Integer page, Integer size) {
        List<String> userTgroupCode = userService.userTgroupCodes(userId);
        final String tgroupCode = (tgroupId == null) ? "" : tgroupService.tgroupCode(tgroupId);
        Specification specification = new Specification<Terminal>() {
            @Override
            public Predicate toPredicate(Root<Terminal> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                if (tgroupId == null) {
                    Predicate[] userMgroupPredicates = new Predicate[userTgroupCode.size()];
                    for (int i = 0; i < userTgroupCode.size(); i++) {
                        userMgroupPredicates[i] = cb.like(r.get("tgroup").get("code"), userTgroupCode.get(i) + "%");
                    }
                    predicate.getExpressions().add(cb.or(userMgroupPredicates));
                } else {
                    predicate.getExpressions().add(cb.like(r.get("tgroup").get("code"), tgroupCode + "%"));
                }
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));

                if (!StrUtil.isEmpty(name)) {
                    predicate.getExpressions().add(cb.like(r.get("name"), "%" + name + "%"));
                }
                if (!StrUtil.isEmpty(mac)) {
                    predicate.getExpressions().add(cb.like(r.get("mac"), "%" + mac + "%"));
                }
                return predicate;
            }
        };
        return terminalRepository.findAll(specification, PageRequest.of(page > 0 ? page - 1 : page, size, Sort.Direction.ASC, "name"));
    }

    @Override
    public Result projectTerminals(Long userId, Long id) {
        List<ProjectTerminal> projectTerminals = projectTerminalRepository.findAllByProjectId(id);

        List<HashMap<String, Object>> terminals = new ArrayList<>();
        try {
            for (ProjectTerminal projectTerminal : projectTerminals) {
                HashMap<String, Object> terminal = new HashMap<>();
                Terminal t = projectTerminal.getTerminal();
                terminal.put("id", t.getId());
                terminal.put("mac", t.getMac());
                terminal.put("name", t.getName());
                terminal.put("tgroupName", t.getTgroup().getName());
                terminal.put("adCount", t.getAdCount());
                terminal.put("adUseCount", t.getAdUseCount());
                terminals.add(terminal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResultUtil<>().setData(terminals);
    }

    @Override
    public Result uploadTerminals(Long userId, InputStream inputStream) {
        Set<String> macs = new HashSet<>();
        int total = parseTerminalExcel(inputStream, macs);
        List<String> macList = new ArrayList<>(macs);
        List<Object[]> terminalList = new ArrayList<>();
        if (macs.size() > 0) {
            int page = 1000;
            for (int i = 0; i <= macList.size() / page; i++) {
                int end = (i + 1) * page;
                if ((i + 1) * page > macList.size())
                    end = macList.size();
                List<String> subList = macList.subList(i * page, end);
                terminalList.addAll(terminalRepository.getTerminalAdInfoByMac(Joiner.on(",").join(subList)));
            }
        }
        List<String> userTgroupCode = userService.userTgroupCodes(userId);
        List<HashMap<String, Object>> rows = new ArrayList<>();
        for (Object[] ter : terminalList) {
            String tgroupCode = "" + ter[6];
            boolean ok = false;
            for (String code : userTgroupCode) {
                if (tgroupCode.startsWith(code)) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                continue;
            }
            HashMap<String, Object> t = new HashMap<>();
            t.put("id", ter[0]);
            t.put("mac", ter[1]);
            t.put("name", ter[2]);
            t.put("tgroupName", ter[3]);
            t.put("adCount", ter[4]);
            t.put("adUseCount", ter[5]);
//            t.put("tgroupId", ter[6]);
            rows.add(t);
        }
        log.info("upload project terminal:{}", total);
        return new ResultUtil<>().setData(rows);
    }

    public int parseTerminalExcel(InputStream inputStream, Set<String> macs) {
        int macCnt = 0;
        try {
            Workbook wb = null;
            try {
                wb = new XSSFWorkbook(inputStream);
            } catch (Exception ex) {
                wb = new HSSFWorkbook(inputStream);
            }

            if (wb.getNumberOfSheets() <= 0)
                return macCnt;

            Sheet xSheet = wb.getSheetAt(0);
            if (xSheet == null) {
                return macCnt;
            }
            int cellIdx = -1;

            for (int rowNum = 0; rowNum <= xSheet.getLastRowNum() && rowNum < 30000; rowNum++) {
                Row xRow = xSheet.getRow(rowNum);
                if (xRow == null) {
                    continue;
                }
                if (xRow.getZeroHeight()) {
                    continue;
                }
                if (cellIdx == -1) {
                    for (int cellNum = 0; cellNum <= xRow.getLastCellNum() && cellNum < 50; cellNum++) {
                        Cell xCell = xRow.getCell(cellNum);
                        if (getStringValue(xCell).toUpperCase().equals("MAC")) {
                            cellIdx = cellNum;
                            break;
                        }
                    }
                } else if (xRow.getLastCellNum() > cellIdx) {
                    Cell xCell = xRow.getCell(cellIdx);
                    String mac = getStringValue(xCell);
                    if (!StrUtil.isEmpty(mac)) {
                        macs.add(mac);
                    }
                    macCnt++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return macCnt;
    }

    private String getStringValue(Cell xCell) {
        if (xCell == null)
            return "";

        if (xCell.getCellType() == CellType.STRING) {
            return xCell.getStringCellValue();
        }
        return "";
    }

}