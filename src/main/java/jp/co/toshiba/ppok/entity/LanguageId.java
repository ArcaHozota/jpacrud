package jp.co.toshiba.ppok.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 言語テーブル複数プライマリーキーの永続化するクラス
 *
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LanguageId implements Serializable {

    private static final long serialVersionUID = 4470395347286329942L;

    /**
     * This field corresponds to the database column COUNTRY_CODE
     */
    private String countryCode;

    /**
     * This field corresponds to the database column LANGUAGE
     */
    private String name;
}
