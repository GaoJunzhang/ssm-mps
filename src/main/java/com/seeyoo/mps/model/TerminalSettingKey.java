package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TerminalSettingKey implements Serializable {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "terminal_id")
	private Terminal terminal;
	@Enumerated
	private TerminalSettingEnum type;
}
