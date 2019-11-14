package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.UserWeather;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户天气数据处理层
 *
 * @author Wangj
 */
public interface UserWeatherRepository extends BaseRepository<UserWeather, Long> {

    List<UserWeather> findAllByUserIdAndWeatherId(Long userId, Long weatherId);

    @Modifying
    @Transactional
    @Query("from UserWeather uw where uw.user.code like ?1% and uw.weather.id = ?2")
    List<UserWeather> findAllByUserCodeAndWeatherId(String userCode, Long weatherId);
}