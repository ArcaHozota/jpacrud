package jp.co.toshiba.ppok.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;

import jp.co.toshiba.ppok.utils.LanguageId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 言語テーブルWORLD_LANGUAGEのエンティティ
 *
 * @author Administrator
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "WORLD_LANGUAGE")
@Proxy(lazy = false)
@IdClass(LanguageId.class)
@NamedQuery(name = "Language.getLanguagesByCountryName", query = "select nl from Language as nl "
		+ "where nl.deleteFlg = 'visible' and nl.country.name =:countryName order by nl.percentage")
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
	@Column(name = "LANGUAGE")
	private String name;

	/**
	 * This field corresponds to the database column IS_OFFICIAL
	 */
	@Column(nullable = false)
	private String isOfficial;

	/**
	 * This field corresponds to the database column PERCENTAGE
	 */
	@Column(nullable = false)
	private BigDecimal percentage;

	/**
	 * This field corresponds to the database column DELETE_FLG
	 */
	@Column(nullable = false)
	private String deleteFlg;

	/**
	 * This field corresponds to the database table WORLD_COUNTRY
	 */
	@ManyToOne
	@JoinColumn(name = "countryCode", insertable = false, updatable = false)
	private Country country;
}
