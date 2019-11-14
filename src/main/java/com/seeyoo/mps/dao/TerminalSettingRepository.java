package com.seeyoo.mps.dao;

import com.seeyoo.mps.model.TerminalSetting;
import com.seeyoo.mps.model.TerminalSettingKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminalSettingRepository extends JpaRepository<TerminalSetting, TerminalSettingKey> {



}