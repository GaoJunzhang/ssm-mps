package com.seeyoo.mps.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.bean.TerminalExpandBean;
import com.seeyoo.mps.dao.TerminalExpandRepository;
import com.seeyoo.mps.dao.TerminalFieldRepository;
import com.seeyoo.mps.dao.TerminalRepository;
import com.seeyoo.mps.model.Field;
import com.seeyoo.mps.model.Terminal;
import com.seeyoo.mps.model.TerminalExpand;
import com.seeyoo.mps.model.TerminalField;
import com.seeyoo.mps.service.TerminalExpandService;
import com.seeyoo.mps.vo.SearchVo;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * 终端扩展接口实现
 *
 * @author GaoJunZhang
 */
@Slf4j
@Service
@Transactional
public class TerminalExpandServiceImpl implements TerminalExpandService {

    @Autowired
    private TerminalExpandRepository terminalExpandRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private TerminalFieldRepository terminalFieldRepository;

    @Override
    public TerminalExpandRepository getRepository() {
        return terminalExpandRepository;
    }

    @Override
    public Page<TerminalExpand> findByCondition(TerminalExpand terminalExpand, SearchVo searchVo, Pageable pageable) {

        return terminalExpandRepository.findAll(new Specification<TerminalExpand>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<TerminalExpand> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {

                // 可添加你的其他搜索过滤条件 默认已有创建时间过滤
                Path<Date> createTimeField = root.get("createTime");

                List<Predicate> list = new ArrayList<Predicate>();

                //创建时间
                if (StrUtil.isNotBlank(searchVo.getStartDate()) && StrUtil.isNotBlank(searchVo.getEndDate())) {
                    Date start = DateUtil.parse(searchVo.getStartDate());
                    Date end = DateUtil.parse(searchVo.getEndDate());
                    list.add(cb.between(createTimeField, start, DateUtil.endOfDay(end)));
                }

                Predicate[] arr = new Predicate[list.size()];
                cq.where(list.toArray(arr));
                return null;
            }
        }, pageable);
    }

    public TerminalExpandBean terminalExpandByTid(Long id) {
        TerminalExpand terminalExpands = terminalExpandRepository.findByTerminalId(id);
        TerminalExpandBean terminalExpandBean = new TerminalExpandBean();
        if (terminalExpands == null) {
            return null;
        }
        terminalExpandBean.setTerminalId(id);
        terminalExpandBean.setId(terminalExpands.getId());
        terminalExpandBean.setDeviceName(terminalExpands.getDeviceName());
        terminalExpandBean.setDeviceNo(terminalExpands.getDeviceNo());
        terminalExpandBean.setKeyNo(terminalExpands.getKeyNo());
        terminalExpandBean.setSimNo(terminalExpands.getSimNo());
        terminalExpandBean.setDeviceType(terminalExpands.getDeviceType());
        terminalExpandBean.setMacAddr(terminalExpands.getMacAddr());
        terminalExpandBean.setDateOfManufacture(DateUtil.format(terminalExpands.getDateOfManufacture(), "yyyy-MM-dd HH:mm:ss"));
        terminalExpandBean.setIndustry(terminalExpands.getIndustry());
        terminalExpandBean.setAddress(terminalExpands.getAddress());
        terminalExpandBean.setRequirement(terminalExpands.getRequirement());
        terminalExpandBean.setBulidingDescription(terminalExpands.getBulidingDescription());
        terminalExpandBean.setHeight(terminalExpands.getHeight());
        terminalExpandBean.setArea(terminalExpands.getArea());
        terminalExpandBean.setPopularity(terminalExpands.getPopularity());
        terminalExpandBean.setIsActivity(terminalExpands.getIsActivity());
        terminalExpandBean.setActivityType(terminalExpands.getActivityType());
        terminalExpandBean.setRegisteredCapital(terminalExpands.getRegisteredCapital());
        terminalExpandBean.setMonthWages(terminalExpands.getMonthWages());
        terminalExpandBean.setIndustryRank(terminalExpands.getIndustryRank());
        terminalExpandBean.setMarketDes(terminalExpands.getMarketDes());
        terminalExpandBean.setMarketValue(terminalExpands.getMarketValue());
        terminalExpandBean.setAnnualTurnover(terminalExpands.getAnnualTurnover());
        terminalExpandBean.setOfficeCost(terminalExpands.getOfficeCost());
        terminalExpandBean.setEmployees(terminalExpands.getEmployees());
        terminalExpandBean.setPlayTaboos(terminalExpands.getPlayTaboos());
        terminalExpandBean.setCompetitionName(terminalExpands.getCompetitionName());
        terminalExpandBean.setType(terminalExpands.getType());
        terminalExpandBean.setLevel(terminalExpands.getLevel());
        terminalExpandBean.setBestCompany(terminalExpands.getBestCompany());
        terminalExpandBean.setInstallTime(DateUtil.format(terminalExpands.getInstallTime(), "yyyy-MM-dd HH:mm:ss"));
        terminalExpandBean.setScreen(terminalExpands.getScreen());
        terminalExpandBean.setCharger(terminalExpands.getCharger());
        terminalExpandBean.setChargerMobile(terminalExpands.getChargerMobile());
        terminalExpandBean.setContractor(terminalExpands.getContractor());
        terminalExpandBean.setContractorMobile(terminalExpands.getContractorMobile());
        terminalExpandBean.setSimInfo(terminalExpands.getSimInfo());
        terminalExpandBean.setInstallAddr(terminalExpands.getInstallAddr());
        return terminalExpandBean;
    }

    public String saveTerminalExpand(TerminalExpandBean terminalExpandBean, String fieldStr) {
        TerminalExpand terminalExpand = new TerminalExpand();
        if (terminalExpandBean.getId() == null) {
            terminalExpand = new TerminalExpand();
        } else {
            terminalExpand = terminalExpandRepository.getOne(terminalExpandBean.getId());
        }
        Terminal terminal = terminalRepository.getOne(terminalExpandBean.getTerminalId());
        terminal.setAdCount(terminalExpandBean.getAdCount());
        terminalRepository.save(terminal);
        terminalExpand.setTerminal(terminal);
        terminalExpand.setDeviceName(terminalExpandBean.getDeviceName());
        terminalExpand.setDeviceNo(terminalExpandBean.getDeviceNo());
        terminalExpand.setKeyNo(terminalExpandBean.getKeyNo());
        terminalExpand.setSimNo(terminalExpandBean.getSimNo());
        terminalExpand.setDeviceType(terminalExpandBean.getDeviceType());
        terminalExpand.setMacAddr(terminalExpandBean.getMacAddr());
        if (terminalExpandBean.getDateOfManufacture() != null) {
            terminalExpand.setDateOfManufacture(Timestamp.valueOf(terminalExpandBean.getDateOfManufacture()));
        } else {
            terminalExpand.setDateOfManufacture(null);
        }
        terminalExpand.setIndustry(terminalExpandBean.getIndustry());
        terminalExpand.setAddress(terminalExpandBean.getAddress());
        terminalExpand.setRequirement(terminalExpandBean.getRequirement());
        terminalExpand.setBulidingDescription(terminalExpandBean.getBulidingDescription());
        terminalExpand.setHeight(terminalExpandBean.getHeight());
        terminalExpand.setArea(terminalExpandBean.getArea());
        terminalExpand.setPopularity(terminalExpandBean.getPopularity());
        terminalExpand.setIsActivity(terminalExpandBean.getIsActivity());
        terminalExpand.setRegisteredCapital(terminalExpandBean.getRegisteredCapital());
        terminalExpand.setMonthWages(terminalExpandBean.getMonthWages());
        terminalExpand.setIndustryRank(terminalExpandBean.getIndustryRank());
        terminalExpand.setMarketDes(terminalExpandBean.getMarketDes());
        terminalExpand.setMarketValue(terminalExpandBean.getMarketValue());
        terminalExpand.setAnnualTurnover(terminalExpandBean.getAnnualTurnover());
        terminalExpand.setOfficeCost(terminalExpandBean.getOfficeCost());
        terminalExpand.setEmployees(terminalExpandBean.getEmployees());
        terminalExpand.setPlayTaboos(terminalExpandBean.getPlayTaboos());
        terminalExpand.setCompetitionName(terminalExpandBean.getCompetitionName());
        terminalExpand.setType(terminalExpandBean.getType());
        terminalExpand.setLevel(terminalExpandBean.getLevel());
        terminalExpand.setBestCompany(terminalExpandBean.getBestCompany());
        if (terminalExpandBean.getInstallTime() != null) {
            terminalExpand.setInstallTime(Timestamp.valueOf(terminalExpandBean.getInstallTime()));
        } else {
            terminalExpand.setInstallTime(null);
        }
        terminalExpand.setScreen(terminalExpandBean.getScreen());
        terminalExpand.setCharger(terminalExpandBean.getCharger());
        terminalExpand.setChargerMobile(terminalExpandBean.getChargerMobile());
        terminalExpand.setContractor(terminalExpandBean.getContractor());
        terminalExpand.setContractorMobile(terminalExpandBean.getContractorMobile());
        terminalExpand.setSimInfo(terminalExpandBean.getSimInfo());
        terminalExpand.setInstallAddr(terminalExpandBean.getInstallAddr());
        terminalExpand.setActivityType(terminalExpandBean.getActivityType());
        terminalExpandRepository.save(terminalExpand);
        //保存其他字段信息
        JSONArray json = JSONArray.fromObject(fieldStr);
        List<TerminalField> terminalFields = new ArrayList<>();
        if (json.size() > 0) {
            for (int i = 0; i < json.size(); i++) {
                TerminalField terminalField = new TerminalField();
                JSONObject job = json.getJSONObject(i);  // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                if (!"".equals(job.get("id"))) {
                    terminalField.setId(Long.parseLong(job.get("id") + ""));
                }
                terminalField.setTerminal(terminal);
                Field field = new Field();
                field.setId(Long.parseLong(job.get("fieldId") + ""));
                terminalField.setField(field);
                terminalField.setFieldContent(job.get("value") + "");
                terminalFields.add(terminalField);
            }
        }
        terminalFieldRepository.saveAll(terminalFields);
        return "操作成功";
    }
}