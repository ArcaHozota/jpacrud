package jp.co.toshiba.ppok.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 言語テーブル複数プライマリーキーの永続化するクラス
 *
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LanguageId implements Serializable {

	private static final long serialVersionUID = 2572743589577511386L;

	/**
	 * This field corresponds to the database column COUNTRY_CODE
	 */
	private String countryCode;

	/**
	 * This field corresponds to the database column LANGUAGE
	 */
	private String name;
}
