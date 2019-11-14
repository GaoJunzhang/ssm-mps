package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "task_content")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TaskContent implements java.io.Serializable {

    @Id
    private Long taskId;
    private String content;
}