package jp.co.toshiba.ppok.service;

import java.util.List;

import jp.co.toshiba.ppok.dto.CityDto;
import jp.co.toshiba.ppok.entity.City;
import jp.co.toshiba.ppok.utils.Pagination;

/**
 * 中央処理サービスインターフェス
 *
 * @author Administrator
 */
public interface CentreLogicService {

	/**
	 * 入力した都市名の重複するかどうかを検証する
	 *
	 * @param cityName 都市名
	 * @return List<City>
	 */
	List<City> checkDuplicatedNames(String cityName);

	/**
	 * 大陸情報を取得する
	 *
	 * @return List<String>
	 */
	List<String> findAllContinents();

	/**
	 * 指定された国の公用語を取得する
	 *
	 * @param nationVal 国名
	 * @return List<String>
	 */
	String findLanguageByCty(String nationVal);

	/**
	 * 指定された大陸に位置するすべての国を取得する
	 *
	 * @param continentVal 大陸名称或いは都市ID
	 * @return List<String>
	 */
	List<String> findNationsByCnt(String continentVal);

	/**
	 * 都市IDによって情報を抽出する
	 *
	 * @param id 都市ID
	 * @return CityInfoDto
	 */
	CityDto getCityInfoById(Long id);

	/**
	 * パージング情報を抽出する
	 *
	 * @param pageNum ページングナンバー
	 * @param keyword 検索キーワード
	 * @return Pagination<CityDto>
	 */
	Pagination<CityDto> getPageInfo(Integer pageNum, String keyword);

	/**
	 * 都市IDによって情報を削除する
	 *
	 * @param id 都市ID
	 */
	void removeById(Long id);

	/**
	 * 入力した都市情報を保存する
	 *
	 * @param cityDto 都市情報
	 */
	void saveById(CityDto cityDto);

	/**
	 * 入力した都市情報を更新する
	 *
	 * @param cityDto 都市情報
	 */
	void updateById(CityDto cityDto);
}
