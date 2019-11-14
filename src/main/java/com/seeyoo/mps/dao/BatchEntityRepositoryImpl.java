package com.seeyoo.mps.dao;


import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Slf4j
public class BatchEntityRepositoryImpl implements BatchEntityRepository {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void batchUpdate(String sql, List<Object[]> params) {
        try {
            Query query = em.createNativeQuery(sql);
            for (Object[] param : params) {
                int idx = 1;
                for (Object p : param) {
                    query.setParameter(idx++, p);
                }
                query.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}