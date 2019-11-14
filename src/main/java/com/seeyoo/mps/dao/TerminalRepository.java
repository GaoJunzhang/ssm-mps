package com.seeyoo.mps.dao;

import com.seeyoo.mps.model.Terminal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

public interface TerminalRepository extends JpaRepository<Terminal, Long>, BatchEntityRepository {

    @Query(value = "call GetTerminalByMac(?)", nativeQuery = true)
    List<Object[]> terminals(@Param("macs") String macs);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update terminal t set t.connect_status = 1,t.imd_update_time = NOW(),t.connect_time = NOW() where t.id = :id", nativeQuery = true)
    int updateTerminalOnline(@Param("id") Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update terminal t set t.connect_status = 1,t.imd_update_time = NOW(),t.connect_time = NOW() where t.id in (:ids)", nativeQuery = true)
    int updateTerminalOnlines(@Param("ids") Collection<Long> ids);

    @Query(value = "call GetTerminalAdInfoByMac(?)", nativeQuery = true)
    List<Object[]> getTerminalAdInfoByMac(@Param("macs") String macs);

    @Query(value = "select t.id, t.mac, t.name from Terminal t where t.isDelete=0 and t.tgroup.code like :code%")
    List<Object[]> terminalsByTgroupCode(@Param("code") String code);

    @Query(value = "select t.id, t.mac, t.name from Terminal t where t.isDelete=0 and t.id in :ids")
    List<Object[]> terminalsByIds(@Param("ids") Long[] ids);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update terminal t set t.server_ip = :serverIp, t.server_mac = :serverMac, t.app_version = :appVersion, t.system_version = :systemVersion where t.id = :id", nativeQuery = true)
    int updateTerminalInitInfo(@Param("id") Long id, @Param("serverIp") String serverIp, @Param("serverMac") String serverMac,
                               @Param("appVersion") String appVersion, @Param("systemVersion") String systemVersion);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update terminal t set t.connect_status = 0, t.disconnect_time = :disconnectTime where t.id = :id", nativeQuery = true)
    int updateTerminalDisconnectTime(@Param("id") Long id, @Param("disconnectTime") Timestamp disconnectTime);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update terminal t set t.connect_status = 0, t.disconnect_time = NOW() where t.id in (:ids)", nativeQuery = true)
    int updateTerminalDisconnectTimes(@Param("ids") Collection<Long> ids);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update terminal t set t.dev_state = :devState, t.dl_file_size = :dlFileSize, t.useable_space = :useableSpace, t.disk_space = :diskSpace, t.task_name = :taskName, t.play_content = :playContent, t.imd_update_time = NOW() where t.id = :id", nativeQuery = true)
    int updateTerminalImdStat(@Param("devState") String devState, @Param("dlFileSize") String dlFileSize, @Param("useableSpace") String useableSpace, @Param("diskSpace") String diskSpace, @Param("taskName") String taskName, @Param("playContent") String playContent, @Param("id") Long id);

    Page<Terminal> findAll(Specification<Terminal> specification, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "update Terminal set name=:name where id=:id")
    int updateNameById(@Param("name") String name, @Param("id") long id);

    @Modifying
    @Transactional
    @Query(value = "update terminal set tgroup_id=:tgroupId where id=:id", nativeQuery = true)
    int updateTgroupById(@Param("tgroupId") long tgroupId, @Param("id") long id);


}