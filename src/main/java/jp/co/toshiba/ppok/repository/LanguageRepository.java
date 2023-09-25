package jp.co.toshiba.ppok.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
	 * 国名によってすべての言語を検索する
	 *
	 * @param nationVal 国名
	 * @return List<String>
	 */
	List<Language> getLanguagesByCountryName(@Param("countryName") String nationVal);
}
