package jp.co.toshiba.ppok.service.impl;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import jp.co.toshiba.ppok.dto.CityDto;
import jp.co.toshiba.ppok.entity.CityView;
import jp.co.toshiba.ppok.service.CentreLogicService;
import jp.co.toshiba.ppok.utils.Messages;
import jp.co.toshiba.ppok.utils.Pagination;
import jp.co.toshiba.ppok.utils.RestMsg;
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
	private final JdbcTemplate jdbcTemplate;

	@Override
	public Boolean checkDuplicatedNames(final String cityName) {
		final Integer cityNameCount = this.jdbcTemplate
				.queryForObject("SELECT COUNT(1) FROM WORLD_CITY_VIEW WCV WHERE WCV.NAME = " + cityName, Integer.class);
		return cityNameCount > 0 ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public List<String> findAllContinents() {
		return this.jdbcTemplate.queryForList(
				"SELECT DISTINCT WCY.CONTINENT FROM WORLD_COUNTRY WCY ORDER BY WCY.CONTINENT ASC", String.class);
	}

	@Override
	public String findLanguageByCty(final String nationVal) {
		return this.jdbcTemplate.queryForObject(
				"SELECT WCV.LANGUAGE FROM WORLD_CITY_VIEW WCV WHERE WCV.NATION = " + nationVal, String.class);
	}

	@Override
	public List<String> findNationsByCnt(final String continentVal) {
		final List<String> list = Lists.newArrayList();
		if (StringUtils.isDigital(continentVal)) {
			final CityView cityView = this.jdbcTemplate.queryForObject(
					"SELECT WCV.* FROM WORLD_CITY_VIEW WCV WHERE WCV.ID = " + continentVal, CityView.class);
			list.add(cityView.getNation());
			final List<String> nations = this.jdbcTemplate.queryForList(
					"SELECT DISTINCT WCY.NAME FROM WORLD_COUNTRY WCY WHERE WCY.DEL_FLG = 'visible' AND WCY.CONTINENT = "
							+ cityView.getContinent() + "ORDER BY WCY.NAME",
					String.class);
			list.addAll(nations);
			return list.stream().distinct().toList();
		}
		return this.jdbcTemplate.queryForList(
				"SELECT DISTINCT WCY.NAME FROM WORLD_COUNTRY WCY WHERE WCY.DEL_FLG = 'visible' AND WCY.CONTINENT = "
						+ continentVal + "ORDER BY WCY.NAME",
				String.class);
	}

	@Override
	public CityDto getCityInfoById(final Integer id) {
		final CityView cityView = this.jdbcTemplate
				.queryForObject("SELECT WCV.* FROM WORLD_CITY_VIEW WCV WHERE WCV.ID = " + id, CityView.class);
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
				final List<CityInfoRecord> cityInfoRecords = this.dslContext.selectFrom(CITY_INFO)
						.orderBy(CITY_INFO.POPULATION.asc()).limit(sort).fetchInto(CityInfoRecord.class);
				final List<CityDto> minimumRanks = cityInfoRecords
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
				final List<CityInfoRecord> cityInfoRecords = this.dslContext.selectFrom(CITY_INFO)
						.orderBy(CITY_INFO.POPULATION.desc()).limit(sort).fetchInto(CityInfoRecord.class);
				final List<CityDto> maximumRanks = cityInfoRecords
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
			final Integer totalRecords = this.dslContext.selectCount().from(CITY_INFO)
					.where(CITY_INFO.NAME.like(hankakuKeyword)).or(CITY_INFO.NATION.like(hankakuKeyword)).fetchSingle()
					.into(Integer.class);
			if (totalRecords == 0) {
				return Pagination.of(Lists.newArrayList(), 0, 1, PAGE_SIZE, NAVIGATION_PAGES);
			}
			final List<CityInfoRecord> cityInfoRecords = this.dslContext.selectFrom(CITY_INFO)
					.where(CITY_INFO.NAME.like(hankakuKeyword).or(CITY_INFO.NATION.like(hankakuKeyword)))
					.orderBy(CITY_INFO.ID).limit(PAGE_SIZE).offset(offset).fetchInto(CityInfoRecord.class);
			final List<CityDto> pageInfos = cityInfoRecords.stream()
					.map(item -> new CityDto(item.getId(), item.getName(), item.getContinent(), item.getNation(),
							item.getDistrict(), item.getPopulation(), item.getLanguage()))
					.toList();
			return Pagination.of(pageInfos, totalRecords, pageNum, PAGE_SIZE, NAVIGATION_PAGES);
		}
		// ページング検索；
		final Integer totalRecords = this.dslContext.selectCount().from(CITY_INFO).fetchSingle().into(Integer.class);
		if (totalRecords == 0) {
			return Pagination.of(Lists.newArrayList(), 0, 1, PAGE_SIZE, NAVIGATION_PAGES);
		}
		final List<CityInfoRecord> cityInfoRecords = this.dslContext.selectFrom(CITY_INFO).orderBy(CITY_INFO.ID)
				.limit(PAGE_SIZE).offset(offset).fetchInto(CityInfoRecord.class);
		final List<CityDto> pageInfos = cityInfoRecords.stream().map(item -> new CityDto(item.getId(), item.getName(),
				item.getContinent(), item.getNation(), item.getDistrict(), item.getPopulation(), item.getLanguage()))
				.toList();
		return Pagination.of(pageInfos, totalRecords, pageNum, PAGE_SIZE, NAVIGATION_PAGES);
	}

	@Override
	public RestMsg removeById(final Integer id) {
		this.dslContext.update(CITY).set(CITY.DELETE_FLG, Messages.MSG008).where(CITY.ID.eq(id)).execute();
		this.dslContext.query("refresh materialized view city_info;").execute();
		return RestMsg.success(Messages.MSG013);
	}

	@Override
	public RestMsg saveById(final CityDto cityDto) {
		final Integer totalRecords = this.dslContext.selectCount().from(CITY).fetchOne().into(Integer.class);
		final String code = this.dslContext.selectDistinct(COUNTRY.CODE).from(COUNTRY)
				.where(COUNTRY.DELETE_FLG.eq(Messages.MSG007)).and(COUNTRY.NAME.eq(cityDto.nation())).fetchSingle()
				.into(String.class);
		final CityRecord cityRecord = this.dslContext.newRecord(CITY);
		cityRecord.setId(totalRecords + 1);
		cityRecord.setName(cityDto.name());
		cityRecord.setCountryCode(code);
		cityRecord.setDistrict(cityDto.district());
		cityRecord.setPopulation(cityDto.population());
		cityRecord.setDeleteFlg(Messages.MSG007);
		cityRecord.insert();
		this.dslContext.query("refresh materialized view city_info;").execute();
		return RestMsg.success(Messages.MSG011);
	}

	@Override
	public RestMsg updateById(final CityDto cityDto) {
		final CityInfoRecord cityInfoRecord = this.dslContext.selectFrom(CITY_INFO).where(CITY_INFO.ID.eq(cityDto.id()))
				.fetchSingle().into(CityInfoRecord.class);
		final CityDto aCityDto = new CityDto(cityInfoRecord.getId(), cityInfoRecord.getName(),
				cityInfoRecord.getContinent(), cityInfoRecord.getNation(), cityInfoRecord.getDistrict(),
				cityInfoRecord.getPopulation(), null);
		if (aCityDto.equals(cityDto)) {
			return RestMsg.failure().add("errorMsg", Messages.MSG012);
		}
		final String code = this.dslContext.selectDistinct(COUNTRY.CODE).from(COUNTRY)
				.where(COUNTRY.DELETE_FLG.eq(Messages.MSG007)).and(COUNTRY.NAME.eq(cityDto.nation())).fetchSingle()
				.into(String.class);
		final CityRecord cityRecord = this.dslContext.newRecord(CITY);
		cityRecord.setId(cityDto.id());
		cityRecord.setName(cityDto.name());
		cityRecord.setCountryCode(code);
		cityRecord.setDistrict(cityDto.district());
		cityRecord.setPopulation(cityDto.population());
		cityRecord.setDeleteFlg(Messages.MSG007);
		this.dslContext.update(CITY).set(cityRecord).where(CITY.DELETE_FLG.eq(Messages.MSG007))
				.and(CITY.ID.eq(cityRecord.getId())).execute();
		this.dslContext.query("refresh materialized view city_info;").execute();
		return RestMsg.success(Messages.MSG010);
	}
}
