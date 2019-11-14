package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "task_record_terminal")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TaskRecordTerminal implements java.io.Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_record_id")
    private TaskRecord taskRecord;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id")
    private Terminal terminal;
    private String oprNo;
    private Short status;
}