package com.seeyoo.mps.dao;

import com.seeyoo.mps.model.TerminalDuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

public interface TerminalDurationRepository extends JpaRepository<TerminalDuration, Long>,BatchEntityRepository {

	@Transactional
	@Modifying(clearAutomatically = true)
	@Query(value = "insert into terminal_duration (terminal_id, date, duration) values (:terminalId, :date, :duration) on duplicate key update duration = duration + :duration", nativeQuery = true)
	int updateTerminalDuration(@Param("terminalId") Long terminalId, @Param("date") Date date, @Param("duration") Integer duration);


}