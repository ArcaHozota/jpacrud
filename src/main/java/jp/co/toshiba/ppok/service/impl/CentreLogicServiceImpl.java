package jp.co.toshiba.ppok.service.impl;

import static jp.co.toshiba.ppok.jooq.Tables.CITY;
import static jp.co.toshiba.ppok.jooq.Tables.CITY_INFO;
import static jp.co.toshiba.ppok.jooq.Tables.COUNTRY;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import jp.co.toshiba.ppok.dto.CityDto;
import jp.co.toshiba.ppok.jooq.tables.records.CityInfoRecord;
import jp.co.toshiba.ppok.jooq.tables.records.CityRecord;
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
	private final DSLContext dslContext;

	@Override
	public Boolean checkDuplicatedNames(final String cityName) {
		final List<CityRecord> cityRecords = this.dslContext.selectFrom(CITY).where(CITY.DELETE_FLG.eq(Messages.MSG007))
				.and(CITY.NAME.eq(cityName)).fetchInto(CityRecord.class);
		return cityRecords.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public List<String> findAllContinents() {
		return this.dslContext.selectDistinct(COUNTRY.CONTINENT).from(COUNTRY)
				.where(COUNTRY.DELETE_FLG.eq(Messages.MSG007)).orderBy(COUNTRY.CONTINENT.asc()).fetchInto(String.class);
	}

	@Override
	public String findLanguageByCty(final String nationVal) {
		return this.dslContext.selectFrom(CITY_INFO).where(CITY_INFO.NATION.eq(nationVal)).fetchSingle().getLanguage();
	}

	@Override
	public List<String> findNationsByCnt(final String continentVal) {
		final List<String> list = Lists.newArrayList();
		if (StringUtils.isDigital(continentVal)) {
			final CityInfoRecord cityInfoRecord = this.dslContext.selectFrom(CITY_INFO)
					.where(CITY_INFO.ID.eq(Integer.parseInt(continentVal))).fetchSingle();
			final String nationName = cityInfoRecord.getNation();
			list.add(nationName);
			final List<String> nations = this.dslContext.selectDistinct(COUNTRY.NAME).from(COUNTRY)
					.where(COUNTRY.DELETE_FLG.eq(Messages.MSG007))
					.and(COUNTRY.CONTINENT.eq(cityInfoRecord.getContinent())).orderBy(COUNTRY.NAME.asc())
					.fetchInto(String.class);
			list.addAll(nations);
			return list.stream().distinct().toList();
		}
		return this.dslContext.selectDistinct(COUNTRY.NAME).from(COUNTRY).where(COUNTRY.DELETE_FLG.eq(Messages.MSG007))
				.and(COUNTRY.CONTINENT.eq(continentVal)).orderBy(COUNTRY.NAME.asc()).fetchInto(String.class);
	}

	@Override
	public CityDto getCityInfoById(final Integer id) {
		final CityInfoRecord cityInfoRecord = this.dslContext.selectFrom(CITY_INFO).where(CITY_INFO.ID.eq(id))
				.fetchSingle();
		return new CityDto(cityInfoRecord.getId(), cityInfoRecord.getName(), cityInfoRecord.getContinent(),
				cityInfoRecord.getNation(), cityInfoRecord.getDistrict(), cityInfoRecord.getPopulation(),
				cityInfoRecord.getLanguage());
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
			final List<CityInfoRecord> totalRecords = this.dslContext.selectFrom(CITY_INFO)
					.where(CITY_INFO.NAME.like(hankakuKeyword)).or(CITY_INFO.NATION.like(hankakuKeyword))
					.fetchInto(CityInfoRecord.class);
			if (totalRecords.isEmpty()) {
				return Pagination.of(Lists.newArrayList(), 0, 1, PAGE_SIZE, NAVIGATION_PAGES);
			}
			final List<CityInfoRecord> cityInfoRecords = this.dslContext.selectFrom(CITY_INFO)
					.where(CITY_INFO.NAME.like(hankakuKeyword)).or(CITY_INFO.NATION.like(hankakuKeyword))
					.orderBy(CITY_INFO.ID).limit(PAGE_SIZE).offset(offset).fetchInto(CityInfoRecord.class);
			final List<CityDto> pageInfos = cityInfoRecords.stream()
					.map(item -> new CityDto(item.getId(), item.getName(), item.getContinent(), item.getNation(),
							item.getDistrict(), item.getPopulation(), item.getLanguage()))
					.toList();
			return Pagination.of(pageInfos, totalRecords.size(), pageNum, PAGE_SIZE, NAVIGATION_PAGES);
		}
		// ページング検索；
		final List<CityInfoRecord> totalRecords = this.dslContext.selectFrom(CITY_INFO).fetchInto(CityInfoRecord.class);
		if (totalRecords.isEmpty()) {
			return Pagination.of(Lists.newArrayList(), 0, 1, PAGE_SIZE, NAVIGATION_PAGES);
		}
		final List<CityInfoRecord> cityInfoRecords = this.dslContext.selectFrom(CITY_INFO).orderBy(CITY_INFO.ID)
				.limit(PAGE_SIZE).offset(offset).fetchInto(CityInfoRecord.class);
		final List<CityDto> pageInfos = cityInfoRecords.stream().map(item -> new CityDto(item.getId(), item.getName(),
				item.getContinent(), item.getNation(), item.getDistrict(), item.getPopulation(), item.getLanguage()))
				.toList();
		return Pagination.of(pageInfos, totalRecords.size(), pageNum, PAGE_SIZE, NAVIGATION_PAGES);
	}

	@Override
	public RestMsg removeById(final Integer id) {
//		this.cityRepository.removeById(id);
		return RestMsg.success(Messages.MSG013);
	}

	@Override
	public RestMsg saveById(final CityDto cityDto) {
//		final City city = new City();
//		final Integer saiban = this.cityRepository.saiban();
//		final String countryCode = this.getCountryCode(cityDto.nation());
//		SecondBeanUtils.copyNullableProperties(cityDto, city);
//		city.setId(saiban);
//		city.setCountryCode(countryCode);
//		city.setDeleteFlg(Messages.MSG007);
//		try {
//			this.cityRepository.saveAndFlush(city);
//		} catch (final Exception e) {
//			return RestMsg.failure().add(ERROR_MSG, Messages.MSG009);
//		}
		return RestMsg.success(Messages.MSG011);
	}

	@Override
	public RestMsg updateById(final CityDto cityDto) {
//		final City city = this.cityRepository.findById(cityDto.id()).orElse(null);
//		if (city == null) {
//			return RestMsg.failure().add(ERROR_MSG, Messages.MSG009);
//		}
//		final City original = new City();
//		SecondBeanUtils.copyNullableProperties(city, original);
//		final String countryCode = this.getCountryCode(cityDto.nation());
//		city.setCountryCode(countryCode);
//		SecondBeanUtils.copyNullableProperties(cityDto, city);
//		if (original.equals(city)) {
//			return RestMsg.failure().add(ERROR_MSG, Messages.MSG012);
//		}
//		try {
//			this.cityRepository.saveAndFlush(city);
//		} catch (final Exception e) {
//			return RestMsg.failure().add(ERROR_MSG, Messages.MSG009);
//		}
		return RestMsg.success(Messages.MSG010);
	}
}
