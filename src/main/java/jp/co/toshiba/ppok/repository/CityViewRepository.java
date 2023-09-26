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
	 * 人口数量昇順で都市情報を検索する
	 *
	 * @return List<CityView>
	 */
	@Query(value = "SELECT WCV.ID, WCV.CONTINENT, WCV.NATION, WCV.NAME, WCV.DISTRICT, WCV.POPULATION, WCV.LANGUAGE "
			+ "FROM WORLD_CITY_VIEW WCV ORDER BY WCV.POPULATION ASC FETCH FIRST :sortNumber ROWS ONLY", nativeQuery = true)
	List<CityView> findMinimumRanks(@Param("sortNumber") Integer sort);

	/**
	 * 人口数量降順で都市情報を検索する
	 *
	 * @return List<CityView>
	 */
	@Query(value = "SELECT WCV.ID, WCV.CONTINENT, WCV.NATION, WCV.NAME, WCV.DISTRICT, WCV.POPULATION, WCV.LANGUAGE "
			+ "FROM WORLD_CITY_VIEW WCV ORDER BY WCV.POPULATION DESC FETCH FIRST :sortNumber ROWS ONLY", nativeQuery = true)
	List<CityView> findMaximumRanks(@Param("sortNumber") Integer sort);
}