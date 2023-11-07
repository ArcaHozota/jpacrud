package jp.co.toshiba.ppok.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 都市情報ビューWORLD_CITY_VIEWのエンティティ
 *
 * @author Administrator
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "city_info")
@NamedQuery(name = "CityView.getLanguage", query = "select max(cv.language) from CityView as cv where cv.nation =:nation group by cv.nation")
public final class CityInfo implements Serializable {

	private static final long serialVersionUID = -5318717623213325302L;

	/**
	 * This field corresponds to the database column ID
	 */
	@Id
	private Integer id;

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
	 * This field corresponds to the database column NATION
	 */
	@Column(nullable = false)
	private String nation;

	/**
	 * This field corresponds to the database column DISTRICT
	 */
	@Column(nullable = false)
	private String district;

	/**
	 * This field corresponds to the database column POPULATION
	 */
	@Column(nullable = false)
	private Integer population;

	/**
	 * This field corresponds to the database column LANGUAGE
	 */
	private String language;
}
