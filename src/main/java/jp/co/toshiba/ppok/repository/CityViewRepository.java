package jp.co.toshiba.ppok.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.toshiba.ppok.entity.CityView;

/**
 * 都市情報リポジトリ
 *
 * @author Administrator
 */
@Repository
public interface CityViewRepository extends JpaRepository<CityView, Long>, JpaSpecificationExecutor<CityView> {

	/**
	 * 人口数量降順で都市情報を検索する
	 *
	 * @return List<CityView>
	 */
	@Query(value = "select cv.id, cv.name, cv.continent, cv.nation, cv.district, cv.population, cv.language "
			+ "from city_info as cv order by cv.population desc limit :sortNumber", nativeQuery = true)
	List<CityView> findMaximumRanks(@Param("sortNumber") Integer sort);

	/**
	 * 人口数量昇順で都市情報を検索する
	 *
	 * @return List<CityView>
	 */
	@Query(value = "select cv.id, cv.name, cv.continent, cv.nation, cv.district, cv.population, cv.language "
			+ "from city_info as cv order by cv.population asc limit :sortNumber", nativeQuery = true)
	List<CityView> findMinimumRanks(@Param("sortNumber") Integer sort);

	/**
	 * 国名によって公用語を取得する
	 *
	 * @param nationVal 国名
	 * @return String
	 */
	String getLanguage(@Param("nation") String nationVal);
}