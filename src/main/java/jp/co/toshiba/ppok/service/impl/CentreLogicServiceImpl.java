package jp.co.toshiba.ppok.service.impl;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import jp.co.toshiba.ppok.dto.CityDto;
import jp.co.toshiba.ppok.entity.City;
import jp.co.toshiba.ppok.entity.CityView;
import jp.co.toshiba.ppok.service.CentreLogicService;
import jp.co.toshiba.ppok.utils.Messages;
import jp.co.toshiba.ppok.utils.Pagination;
import jp.co.toshiba.ppok.utils.RestMsg;
import jp.co.toshiba.ppok.utils.SecondBeanUtils;
import jp.co.toshiba.ppok.utils.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 中央処理サービス実装クラス
 *
 * @author Administrator
 */
@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CentreLogicServiceImpl implements CentreLogicService {

	/**
	 * ページングナヴィゲーション
	 */
	private static final Integer NAVIGATION_PAGES = 7;

	/**
	 * ページサイズ
	 */
	private static final Integer PAGE_SIZE = 8;

	/**
	 * デフォルトソート値
	 */
	private static final Integer SORT_NUMBER = 100;

	/**
	 * 共通リポジトリ
	 */
	private final JdbcClient jdbcClient;

	@Override
	public Boolean checkDuplicatedNames(final String cityName) {
		final Integer cityNameCount = this.jdbcClient.sql("SELECT COUNT(1) FROM WORLD_CITY_VIEW WCV WHERE WCV.NAME = ?")
				.param(cityName).query(Integer.class).single();
		return cityNameCount > 0 ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public List<String> findAllContinents() {
		return this.jdbcClient.sql("SELECT DISTINCT WCY.CONTINENT FROM WORLD_COUNTRY WCY ORDER BY WCY.CONTINENT ASC")
				.query(String.class).list();
	}

	@Override
	public String findLanguageByCty(final String nationVal) {
		return this.jdbcClient.sql("SELECT DISTINCT WCV.LANGUAGE FROM WORLD_CITY_VIEW WCV WHERE WCV.NATION = ?")
				.param(nationVal).query(String.class).single();
	}

	@Override
	public List<String> findNationsByCnt(final String continentVal) {
		final List<String> list = Lists.newArrayList();
		if (StringUtils.isDigital(continentVal)) {
			final CityView cityView = this.jdbcClient.sql("SELECT WCV.* FROM WORLD_CITY_VIEW WCV WHERE WCV.ID = ?")
					.param(continentVal).query(CityView.class).single();
			list.add(cityView.getNation());
			final List<String> nations = this.jdbcClient.sql(
					"SELECT DISTINCT WCY.NAME FROM WORLD_COUNTRY WCY WHERE WCY.DELETE_FLG = ? AND WCY.CONTINENT = ? ORDER BY WCY.NAME")
					.params(Messages.MSG007, cityView.getContinent()).query(String.class).list();
			list.addAll(nations);
			return list.stream().distinct().toList();
		}
		return this.jdbcClient.sql(
				"SELECT DISTINCT WCY.NAME FROM WORLD_COUNTRY WCY WHERE WCY.DELETE_FLG = ? AND WCY.CONTINENT = ? ORDER BY WCY.NAME")
				.params(Messages.MSG007, continentVal).query(String.class).list();
	}

	@Override
	public CityDto getCityInfoById(final Integer id) {
		final CityView cityView = this.jdbcClient.sql("SELECT WCV.* FROM WORLD_CITY_VIEW WCV WHERE WCV.ID = ?")
				.param(id).query(CityView.class).single();
		return new CityDto(cityView.getId(), cityView.getName(), cityView.getContinent(), cityView.getNation(),
				cityView.getDistrict(), cityView.getPopulation(), cityView.getLanguage());
	}

	@Override
	public Pagination<CityDto> getPageInfo(final Integer pageNum, final String keyword) {
		final int offset = PAGE_SIZE * (pageNum - 1);
		if (StringUtils.isNotEmpty(keyword)) {
			// エンティティを宣言する；
			final String hankakuKeyword = StringUtils.toHankaku(keyword);
			final int pageMax = PAGE_SIZE * pageNum;
			int sort = SORT_NUMBER;
			if (hankakuKeyword.startsWith("min(pop)")) {
				final int indexOf = hankakuKeyword.indexOf(")");
				final String keisan = hankakuKeyword.substring(indexOf + 1);
				if (StringUtils.isNotEmpty(keisan)) {
					sort = Integer.parseInt(keisan);
				}
				final List<CityView> cityViews = this.jdbcClient
						.sql("SELECT WCV.* FROM WORLD_CITY_VIEW WCV ORDER BY WCV.POPULATION ASC").query(CityView.class)
						.list();
				final List<CityDto> minimumRanks = cityViews
						.stream().map(item -> new CityDto(item.getId(), item.getName(), item.getContinent(),
								item.getNation(), item.getDistrict(), item.getPopulation(), item.getLanguage()))
						.toList();
				if (pageMax >= sort) {
					return Pagination.of(minimumRanks.subList(offset, sort), minimumRanks.size(), pageNum, PAGE_SIZE,
							NAVIGATION_PAGES);
				}
				return Pagination.of(minimumRanks.subList(offset, pageMax), minimumRanks.size(), pageNum, PAGE_SIZE,
						NAVIGATION_PAGES);
			}
			if (hankakuKeyword.startsWith("max(pop)")) {
				final int indexOf = hankakuKeyword.indexOf(")");
				final String keisan = hankakuKeyword.substring(indexOf + 1);
				if (StringUtils.isNotEmpty(keisan)) {
					sort = Integer.parseInt(keisan);
				}
				final List<CityView> cityViews = this.jdbcClient
						.sql("SELECT WCV.* FROM WORLD_CITY_VIEW WCV ORDER BY WCV.POPULATION DESC").query(CityView.class)
						.list();
				final List<CityDto> maximumRanks = cityViews
						.stream().map(item -> new CityDto(item.getId(), item.getName(), item.getContinent(),
								item.getNation(), item.getDistrict(), item.getPopulation(), item.getLanguage()))
						.toList();
				if (pageMax >= sort) {
					return Pagination.of(maximumRanks.subList(offset, sort), maximumRanks.size(), pageNum, PAGE_SIZE,
							NAVIGATION_PAGES);
				}
				return Pagination.of(maximumRanks.subList(offset, pageMax), maximumRanks.size(), pageNum, PAGE_SIZE,
						NAVIGATION_PAGES);
			}
			// ページング検索；
			final String searchStr = "%" + hankakuKeyword + "%";
			final Integer totalRecords = this.jdbcClient
					.sql("SELECT COUNT(1) FROM WORLD_CITY_VIEW WCV WHERE WCV.NAME LIKE ? OR WCV.NATION LIKE ?")
					.param(searchStr).query(Integer.class).single();
			if (totalRecords == 0) {
				return Pagination.of(Lists.newArrayList(), 0, 1, PAGE_SIZE, NAVIGATION_PAGES);
			}
			final List<CityView> cityViews = this.jdbcClient
					.sql("SELECT WCV.* FROM WORLD_CITY_VIEW WCV WHERE WCV.NAME LIKE ? OR WCV.NATION LIKE ? "
							+ "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY")
					.params(searchStr, searchStr, offset, PAGE_SIZE).query(CityView.class).list();
			final List<CityDto> pageInfos = cityViews.stream()
					.map(item -> new CityDto(item.getId(), item.getName(), item.getContinent(), item.getNation(),
							item.getDistrict(), item.getPopulation(), item.getLanguage()))
					.toList();
			return Pagination.of(pageInfos, totalRecords, pageNum, PAGE_SIZE, NAVIGATION_PAGES);
		}
		// ページング検索；
		final Integer totalRecords = this.jdbcClient.sql("SELECT COUNT(1) FROM WORLD_CITY_VIEW WCV")
				.query(Integer.class).single();
		if (totalRecords == 0) {
			return Pagination.of(Lists.newArrayList(), 0, 1, PAGE_SIZE, NAVIGATION_PAGES);
		}
		final List<CityView> cityInfoRecords = this.jdbcClient
				.sql("SELECT COUNT(1) FROM WORLD_CITY_VIEW WCV OFFSET ? ROWS FETCH NEXT ? ROWS ONLY")
				.params(offset, PAGE_SIZE).query(CityView.class).list();
		final List<CityDto> pageInfos = cityInfoRecords.stream().map(item -> new CityDto(item.getId(), item.getName(),
				item.getContinent(), item.getNation(), item.getDistrict(), item.getPopulation(), item.getLanguage()))
				.toList();
		return Pagination.of(pageInfos, totalRecords, pageNum, PAGE_SIZE, NAVIGATION_PAGES);
	}

	@Override
	public RestMsg removeById(final Integer id) {
		this.jdbcClient.sql("UPDATE WORLD_CITY WC SET WC.DELETE_FLG = ? WHERE WC.ID = ?").params(Messages.MSG008, id)
				.update();
		return RestMsg.success(Messages.MSG013);
	}

	@Override
	public RestMsg saveById(final CityDto cityDto) {
		final Integer totalRecords = this.jdbcClient.sql("SELECT COUNT(WC.ID) FROM WORLD_CITY WC").query(Integer.class)
				.single();
		final String countryCode = this.jdbcClient
				.sql("SELECT DISTINCT WCY.CODE FROM WORLD_COUNTRY WCY WHERE WCY.DELETE_FLG = ? AND WCY.NAME = ?")
				.params(Messages.MSG008, cityDto.nation()).query(String.class).single();
		final City city = new City();
		SecondBeanUtils.copyNullableProperties(cityDto, city);
		city.setId(totalRecords + 1);
		city.setCountryCode(countryCode);
		city.setDeleteFlg(Messages.MSG007);
		this.jdbcClient.sql(
				"INSERT INTO WORLD_CITY WC (WC.ID, WC.NAME, WC.COUNTRY_CODE, WC.DISTRICT, WC.POPULATION, WC.DELETE_FLG) "
						+ "VALUES (:id, :name, :countryCode, :district, :population, :deleteFlg)")
				.param(city).update();
		return RestMsg.success(Messages.MSG011);
	}

	@Override
	public RestMsg updateById(final CityDto cityDto) {
		final CityView cityView = this.jdbcClient.sql("SELECT WCV.* FROM WORLD_CITY_VIEW WCV WHERE WCV.ID = ?")
				.param(cityDto.id()).query(CityView.class).single();
		final CityView originalEntity = new CityView();
		SecondBeanUtils.copyNullableProperties(cityView, originalEntity);
		SecondBeanUtils.copyNullableProperties(cityDto, cityView);
		if (originalEntity.equals(cityView)) {
			return RestMsg.failure().add("errorMsg", Messages.MSG012);
		}
		final String countryCode = this.jdbcClient
				.sql("SELECT DISTINCT WCY.CODE FROM WORLD_COUNTRY WCY WHERE WCY.DELETE_FLG = ? AND WCY.NAME = ?")
				.params(Messages.MSG008, cityDto.nation()).query(String.class).single();
		final City city = new City();
		SecondBeanUtils.copyNullableProperties(cityDto, city);
		city.setCountryCode(countryCode);
		city.setDeleteFlg(Messages.MSG007);
		this.jdbcClient.sql("UPDATE WORLD_CITY WC SET WC = ? WHERE WC.ID = ?").params(city, city.getId()).update();
		return RestMsg.success(Messages.MSG010);
	}
}
