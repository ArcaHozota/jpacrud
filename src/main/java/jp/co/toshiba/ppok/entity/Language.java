package jp.co.toshiba.ppok.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jp.co.toshiba.ppok.utils.LanguageId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 言語テーブルWORLD_LANGUAGEのエンティティ
 *
 * @author ArcaHozota
 * @since 1.02
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@IdClass(LanguageId.class)
@Table(name = "language")
public final class Language implements Serializable {

	private static final long serialVersionUID = -2909406519851156194L;

	/**
	 * This field corresponds to the database column COUNTRY_CODE
	 */
	@Id
	private String countryCode;

	/**
	 * This field corresponds to the database column LANGUAGE
	 */
	@Id
	private String language;

	/**
	 * This field corresponds to the database column IS_OFFICIAL
	 */
	@Column(nullable = false)
	private String isOfficial;

	/**
	 * This field corresponds to the database column PERCENTAGE
	 */
	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal percentage;

	/**
	 * This field corresponds to the database column DELETE_FLG
	 */
	@Column(nullable = false)
	private String deleteFlg;
}
