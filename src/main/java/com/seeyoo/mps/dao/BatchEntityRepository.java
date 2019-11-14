package com.seeyoo.mps.dao;


import java.util.List;

public interface BatchEntityRepository {
   void batchUpdate(String sql, List<Object[]> params);
}