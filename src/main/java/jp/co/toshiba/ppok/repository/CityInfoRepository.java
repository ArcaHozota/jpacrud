package jp.co.toshiba.ppok.repository;

import java.util.List;

import org.postgresql.util.PSQLException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import jp.co.toshiba.ppok.entity.CityInfo;

/**
 * 都市情報リポジトリ
 *
 * @author Administrator
 */
public interface CityInfoRepository extends JpaRepository<CityInfo, Integer>, JpaSpecificationExecutor<CityInfo> {

	/**
	 * 人口数量降順で都市情報を検索する
	 *
	 * @return List<CityView>
	 */
	@Query(value = "select cv.id, cv.name, cv.continent, cv.nation, cv.district, cv.population, cv.language "
			+ "from city_info as cv order by cv.population desc limit :sortNumber", nativeQuery = true)
	List<CityInfo> findMaximumRanks(@Param("sortNumber") Integer sort);

	/**
	 * 人口数量昇順で都市情報を検索する
	 *
	 * @return List<CityView>
	 */
	@Query(value = "select cv.id, cv.name, cv.continent, cv.nation, cv.district, cv.population, cv.language "
			+ "from city_info as cv order by cv.population asc limit :sortNumber", nativeQuery = true)
	List<CityInfo> findMinimumRanks(@Param("sortNumber") Integer sort);

	/**
	 * 国名によって公用語を取得する
	 *
	 * @param nationVal 国名
	 * @return String
	 */
	String getLanguage(@Param("nation") String nationVal);

	/**
	 * リフレッシュ
	 */
	@Modifying
	@Transactional(rollbackFor = PSQLException.class)
	@Query(value = "refresh materialized view city_info", nativeQuery = true)
	void refresh();
}