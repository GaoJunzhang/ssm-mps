package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.Weather;

import java.util.List;

/**
 * 天气数据处理层
 *
 * @author Wangj
 */
public interface WeatherRepository extends BaseRepository<Weather, Long> {
    List<Weather> findAllByWeatherId(Integer id);
}