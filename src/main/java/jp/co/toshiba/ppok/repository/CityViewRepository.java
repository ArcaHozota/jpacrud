package jp.co.toshiba.ppok.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.toshiba.ppok.entity.CityView;

/**
 * 都市情報リポジトリ
 *
 * @author ArkamaHozota
 * @since 1.04
 */
public interface CityViewRepository extends JpaRepository<CityView, Integer>, JpaSpecificationExecutor<CityView> {

	/**
	 * 国名によって情報の数を取得する
	 *
	 * @param nation 国名
	 * @return Integer
	 */
	Integer countByNations(@Param("nation") String nation);

	/**
	 * すべての大陸名称を取得する
	 *
	 * @return List<String>
	 */
	List<String> findContinents();

	/**
	 * 人口数量降順で都市情報を検索する
	 *
	 * @return List<CityView>
	 */
	@Query(value = "SELECT WCV.* FROM WORLD_CITY_VIEW WCV ORDER BY cv.population DESC FETCH FIRST :sortNumber ROWS ONLY", nativeQuery = true)
	List<CityView> findMaximumRanks(@Param("sortNumber") Integer sort);

	/**
	 * 人口数量昇順で都市情報を検索する
	 *
	 * @return List<CityView>
	 */
	@Query(value = "SELECT WCV.* FROM WORLD_CITY_VIEW WCV ORDER BY cv.population ASC FETCH FIRST :sortNumber ROWS ONLY", nativeQuery = true)
	List<CityView> findMinimumRanks(@Param("sortNumber") Integer sort);

	/**
	 * 大陸名称によってすべての国名を取得する
	 *
	 * @param continent 大陸名称
	 * @return List<String>
	 */
	List<String> findNationsByCnt(@Param("continent") String continent);

	/**
	 * 国名によって公用語を取得する
	 *
	 * @param nationVal 国名
	 * @return String
	 */
	String getLanguage(@Param("nation") String nationVal);
}