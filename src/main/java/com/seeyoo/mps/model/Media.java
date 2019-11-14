package com.seeyoo.mps.model;

import lombok.*;

import java.sql.Timestamp;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.*;

@Entity
@Table(name = "media")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Media  implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mgroup_id", nullable = false)
	private Mgroup mgroup;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	@Column(name = "name", length = 64)
	private String name;
	@Column(name = "size")
	private Integer size;
	@Column(name = "width")
	private Integer width;
	@Column(name = "height")
	private Integer height;
	@Column(name = "path", length = 128)
	private String path;
	@Column(name = "md5", length = 64)
	private String md5;
	@Column(name = "type")
	private Short type;
	@Column(name = "audit")
	private Short audit;
	@Column(name = "is_default")
	private Short isDefault;
	@Column(name = "remark", length = 512)
	private String remark;
	@Column(name = "create_time", length = 19)
	private Timestamp createTime;
	@Column(name = "is_read")
	private Integer isRead;
	@Column(name = "is_delete")
	private Short isDelete;
	@Column(name = "ad_count")
	private Integer adCount;
	@Column(name = "duration")
	private Integer duration;
	@Column(name = "local_path")
	private String localPath;

}
