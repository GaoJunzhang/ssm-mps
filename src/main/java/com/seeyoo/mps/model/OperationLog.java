package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;

/**
 * Created by user on 2019/11/7.
 */
@Entity
@Table(name = "operation_log")
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog extends BaseEntity implements java.io.Serializable {
    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User user;
    private String action;
    private Integer status;
    @Lob
    private String request;
    @Lob
    private String response;
}
