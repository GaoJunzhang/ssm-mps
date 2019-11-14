package com.seeyoo.mps.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.dao.MediaRepository;
import com.seeyoo.mps.model.Media;
import com.seeyoo.mps.service.MediaService;
import com.seeyoo.mps.service.MgroupService;
import com.seeyoo.mps.service.UserService;
import com.seeyoo.mps.tool.OSSClientUtil;
import lombok.extern.slf4j.Slf4j;
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
import java.util.*;

/**
 * 媒体接口实现
 *
 * @author GaoJunZhang
 */
@Slf4j
@Service
@Transactional
public class MediaServiceImpl implements MediaService {

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MgroupService mgroupService;

    @Override
    public MediaRepository getRepository() {
        return mediaRepository;
    }

    @Override
    public Map<String, Object> pageMediaData(Long mgroupId, String name, Short audit, String[] types, long userId, Pageable pageable) {
        List<String> userMgroupCode = userService.userMgroupCodes(userId);
        final String mgroupCode = (mgroupId == null) ? "" : mgroupService.mgroupCode(mgroupId);
        Specification specification = new Specification<Media>() {
            @Override
            public Predicate toPredicate(Root<Media> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));

                if (mgroupId == null) {
                    Predicate[] userMgroupPredicates = new Predicate[userMgroupCode.size()];
                    for (int i = 0; i < userMgroupCode.size(); i++) {
                        userMgroupPredicates[i] = cb.like(r.get("mgroup").get("code"), userMgroupCode.get(i) + "%");
                    }
                    predicate.getExpressions().add(cb.or(userMgroupPredicates));
                } else {
                    predicate.getExpressions().add(cb.like(r.get("mgroup").get("code"), mgroupCode + "%"));
                }
                if (!StrUtil.isEmpty(name)) {
                    predicate.getExpressions().add(cb.like(r.get("name"), "%" + name + "%"));
                }
                if (audit != null) {
                    predicate.getExpressions().add(cb.equal(r.get("audit"), audit));
                }
                if (types != null) {
                    Predicate[] typesPredicates = new Predicate[types.length];
                    for (int j = 0; j < types.length; j++) {
                        typesPredicates[j] = cb.equal(r.get("type"), types[j]);
                    }
                    predicate.getExpressions().add(cb.or(typesPredicates));
                }
                return predicate;
            }
        };
        Page<Media> mediaPage = mediaRepository.findAll(specification, pageable);
        List<Map<String, Object>> mediasMap = new ArrayList<>();
        for (Media mediaObj : mediaPage) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", mediaObj.getId());
            u.put("name", mediaObj.getName());
            u.put("userName", mediaObj.getUser().getName());
            u.put("mGroupName", mediaObj.getMgroup().getName());
            u.put("mGroupId", mediaObj.getMgroup().getId());
            u.put("type", mediaObj.getType());
            u.put("size", mediaObj.getSize());
            u.put("width", mediaObj.getWidth());
            u.put("height", mediaObj.getHeight());
            u.put("path", OSSClientUtil.getAccessUrl() + "/" + mediaObj.getPath());
            u.put("audit", mediaObj.getAudit());
            u.put("remark", mediaObj.getRemark());
            u.put("duration", mediaObj.getDuration());
            u.put("createTime", DateUtil.format(mediaObj.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            u.put("localPath", mediaObj.getLocalPath());
            mediasMap.add(u);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", pageable.getPageNumber());
        map.put("pageSize", pageable.getPageSize());
        map.put("totalPage", mediaPage.getTotalPages());
        map.put("totalCount", mediaPage.getTotalElements());
        map.put("data", mediasMap);
        return map;
    }

    public List<Media> findAllByMd5(String md5) {
        return mediaRepository.findAllByMd5(md5);
    }

    public int updateAuditByIds(short audit, String[] ids) {
        Collection<Long> collection = new ArrayList<>();
        for (String id : ids) {
            collection.add(Long.parseLong(id));
        }
        return mediaRepository.updateAuditByIds(audit, collection);
    }
}