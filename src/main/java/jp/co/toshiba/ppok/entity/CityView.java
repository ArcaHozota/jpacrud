package jp.co.toshiba.ppok.entity;

import java.io.Serializable;

import lombok.Data;


/**
 * 都市情報ビューWORLD_CITY_VIEWのエンティティ
 *
 * @author Administrator
 */
@Data
public final class CityView implements Serializable {

	private static final long serialVersionUID = -5318717623213325302L;

	/**
	 * This field corresponds to the database column ID
	 */
	private Integer id;

	/**
	 * This field corresponds to the database column NAME
	 */
	private String name;

	/**
	 * This field corresponds to the database column CONTINENT
	 */
	private String continent;

	/**
	 * This field corresponds to the database column NATION
	 */
	private String nation;

	/**
	 * This field corresponds to the database column DISTRICT
	 */
	private String district;

	/**
	 * This field corresponds to the database column POPULATION
	 */
	private Integer population;

	/**
	 * This field corresponds to the database column LANGUAGE
	 */
	private String language;
}
