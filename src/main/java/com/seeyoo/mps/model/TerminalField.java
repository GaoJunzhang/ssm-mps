package com.seeyoo.mps.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by user on 2019/11/1.
 */
@Entity
@Table(name = "terminal_field")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TerminalField {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "snowflake")
    @GenericGenerator(name = "snowflake", strategy = "com.seeyoo.mps.tool.IdUtil")
    private Long id;
    @ManyToOne()
    @JoinColumn(name = "terminal_id")
    private Terminal terminal;
    @ManyToOne()
    @JoinColumn(name = "field_id")
    private Field field;
    private String fieldContent;
    @Column(updatable = false)
    @CreationTimestamp
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;
}
