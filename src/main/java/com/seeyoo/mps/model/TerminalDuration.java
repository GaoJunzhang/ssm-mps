package com.seeyoo.mps.model;

		import lombok.AllArgsConstructor;
		import lombok.Builder;
		import lombok.Data;
		import lombok.EqualsAndHashCode;
		import lombok.NoArgsConstructor;
		import lombok.ToString;
		import org.hibernate.annotations.Type;

		import javax.persistence.*;
		import java.io.Serializable;
		import java.util.Date;

@Entity
@Table(name = "terminal_duration")
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TerminalDuration extends BaseEntity implements Serializable {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "terminal_id")
	private Terminal terminal;
	private Date date;
	private Integer duration;
}
