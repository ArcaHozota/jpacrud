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
	@Query(value = "SELECT DISTINCT CASE WHEN LF.MAX_PERCENTAGE - LT.MAX_PERCENTAGE >= 35 THEN LF.LANGUAGE ELSE COALESCE(LT.LANGUAGE, LF.LANGUAGE) END AS LANGUAGE "
			+ "FROM WORLD_COUNTRY WTY INNER JOIN WORLD_LANGUAGE WL ON WL.COUNTRY_CODE = WTY.CODE "
			+ "LEFT JOIN (SELECT WL.COUNTRY_CODE, WL.IS_OFFICIAL, WL.LANGUAGE, MP.MAX_PERCENTAGE "
			+ "FROM WORLD_LANGUAGE WL INNER JOIN (SELECT WL.COUNTRY_CODE, WL.IS_OFFICIAL, MAX( WL.PERCENTAGE ) AS MAX_PERCENTAGE "
			+ "FROM WORLD_LANGUAGE WL WHERE WL.DELETE_FLG = 'visible' GROUP BY WL.COUNTRY_CODE, WL.IS_OFFICIAL) MP "
			+ "ON MP.COUNTRY_CODE = WL.COUNTRY_CODE AND MP.IS_OFFICIAL = WL.IS_OFFICIAL AND MP.MAX_PERCENTAGE = WL.PERCENTAGE) LF "
			+ "ON LF.COUNTRY_CODE = WL.COUNTRY_CODE AND LF.IS_OFFICIAL = 'False' LEFT JOIN (SELECT WL.COUNTRY_CODE, WL.IS_OFFICIAL, WL.LANGUAGE, MP.MAX_PERCENTAGE "
			+ "FROM WORLD_LANGUAGE WL INNER JOIN (SELECT WL.COUNTRY_CODE, WL.IS_OFFICIAL, MAX( WL.PERCENTAGE ) AS MAX_PERCENTAGE "
			+ "FROM WORLD_LANGUAGE WL WHERE WL.DELETE_FLG = 'visible' GROUP BY WL.COUNTRY_CODE, WL.IS_OFFICIAL) MP "
			+ "ON MP.COUNTRY_CODE = WL.COUNTRY_CODE AND MP.IS_OFFICIAL = WL.IS_OFFICIAL AND MP.MAX_PERCENTAGE = WL.PERCENTAGE) LT "
			+ "ON LT.COUNTRY_CODE = WL.COUNTRY_CODE AND LT.IS_OFFICIAL = 'True' WHERE WTY.NAME =:nation", nativeQuery = true)
	String findLanguageByCountryName(@Param("nation") String nationName);
}
