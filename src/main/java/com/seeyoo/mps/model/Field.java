package com.seeyoo.mps.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by user on 2019/11/1.
 */
@Entity
@Table(name = "field")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "update field set is_delete=1 where id=?")
@Where(clause = "is_delete=0")
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "snowflake")
    @GenericGenerator(name = "snowflake", strategy = "com.seeyoo.mps.tool.IdUtil")
    private Long id;
    private String fieldName;
    @Column(updatable = false)
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private Short isDelete;
    private Integer sort;
    private String enName;
}
