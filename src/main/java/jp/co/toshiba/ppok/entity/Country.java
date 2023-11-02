package jp.co.toshiba.ppok.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 国家テーブルWORLD_COUNTRYのエンティティ
 *
 * @author ArcaHozota
 * @since 1.02
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "country")
@NamedQuery(name = "Country.findNationCode", query = "select cty.code from Country as cty where cty.deleteFlg = 'visible' and cty.name =:name")
@NamedQuery(name = "Country.findAllContinents", query = "select max(cty.continent) from Country as cty where cty.deleteFlg = 'visible' group by cty.continent order by cty.continent asc")
@NamedQuery(name = "Country.findNationsByCnt", query = "select max(cty.name) from Country as cty where cty.deleteFlg = 'visible' and cty.continent =:continent group by cty.name order by cty.name asc")
public final class Country implements Serializable {

	private static final long serialVersionUID = 8200490748915737257L;

	/**
	 * This field corresponds to the database column CODE
	 */
	@Id
	private String code;

	/**
	 * This field corresponds to the database column NAME
	 */
	@Column(nullable = false)
	private String name;

	/**
	 * This field corresponds to the database column CONTINENT
	 */
	@Column(nullable = false)
	private String continent;

	/**
	 * This field corresponds to the database column REGION
	 */
	@Column(nullable = false)
	private String region;

	/**
	 * This field corresponds to the database column SURFACE_AREA
	 */
	@Column(nullable = false, precision = 23, scale = 5)
	private BigDecimal surfaceArea;

	/**
	 * This field corresponds to the database column INDEPENDENCE_YEAR
	 */
	private Short independenceYear;

	/**
	 * This field corresponds to the database column POPULATION
	 */
	@Column(nullable = false)
	private Integer population;

	/**
	 * This field corresponds to the database column LIFE_EXPECTANCY
	 */
	@Column(precision = 5, scale = 2)
	private BigDecimal lifeExpectancy;

	/**
	 * This field corresponds to the database column GNP_USD
	 */
	@Column(precision = 23, scale = 5)
	private BigDecimal gnpUsd;

	/**
	 * This field corresponds to the database column GNP_USD_OLD
	 */
	@Column(precision = 23, scale = 5)
	private BigDecimal gnpUsdOld;

	/**
	 * This field corresponds to the database column LOCAL_NAME
	 */
	@Column(nullable = false)
	private String localName;

	/**
	 * This field corresponds to the database column GOVERNMENT_FORM
	 */
	@Column(nullable = false)
	private String governmentForm;

	/**
	 * This field corresponds to the database column HEAD_OF_STATE
	 */
	private String headOfState;

	/**
	 * This field corresponds to the database column CAPITAL
	 */
	private Integer capital;

	/**
	 * This field corresponds to the database column CODE2
	 */
	@Column(nullable = false)
	private String code2;

	/**
	 * This field corresponds to the database column DELETE_FLG
	 */
	@Column(nullable = false)
	private String deleteFlg;
}
