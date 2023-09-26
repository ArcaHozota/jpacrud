package jp.co.toshiba.ppok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.toshiba.ppok.entity.Language;
import jp.co.toshiba.ppok.utils.LanguageId;

/**
 * 言語リポジトリ
 *
 * @author Administrator
 */
@Repository
public interface LanguageRepository extends JpaRepository<Language, LanguageId>, JpaSpecificationExecutor<Language> {

	/**
	 * 国名によって公用語を取得する
	 *
	 * @param nationName 国名
	 * @return 公用語
	 */
	@Query(value = "SELECT DISTINCT CASE WHEN (UM.OFPERCENTAGE - COALESCE(OM.OTPERCENTAGE, 0)) >= 35 THEN UM.OFLANGUAGE ELSE NVL(OM.OTLANGUAGE, UM.OFLANGUAGE) END AS LANGUAGE "
			+ "FROM WORLD_LANGUAGE WL INNER JOIN WORLD_COUNTRY WCY ON WCY.CODE = WL.COUNTRY_CODE "
			+ "LEFT JOIN (SELECT WL.COUNTRY_CODE, WL.LANGUAGE AS OTLANGUAGE, WL.PERCENTAGE AS OTPERCENTAGE, "
			+ "ROW_NUMBER() OVER (PARTITION BY COUNTRY_CODE ORDER BY PERCENTAGE DESC) AS RNK FROM WORLD_LANGUAGE WL "
			+ "WHERE WL.DELETE_FLG = 'visible' AND WL.IS_OFFICIAL = 'True') OM ON OM.COUNTRY_CODE = WL.COUNTRY_CODE AND OM.RNK = 1 "
			+ "LEFT JOIN (SELECT WL.COUNTRY_CODE, WL.LANGUAGE AS OFLANGUAGE, WL.PERCENTAGE AS OFPERCENTAGE, "
			+ "ROW_NUMBER() OVER (PARTITION BY COUNTRY_CODE ORDER BY PERCENTAGE DESC) AS RNK FROM WORLD_LANGUAGE WL "
			+ "WHERE WL.DELETE_FLG = 'visible' AND WL.IS_OFFICIAL = 'False') UM ON UM.COUNTRY_CODE = WL.COUNTRY_CODE "
			+ "AND UM.RNK = 1 WHERE WCY.NAME =:nation", nativeQuery = true)
	String findLanguageByCountryName(@Param("nation") String nationName);
}
