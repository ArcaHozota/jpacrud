package jp.co.toshiba.ppok.service.impl;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import jp.co.toshiba.ppok.dto.CityDto;
import jp.co.toshiba.ppok.entity.City;
import jp.co.toshiba.ppok.entity.CityView;
import jp.co.toshiba.ppok.repository.CityRepository;
import jp.co.toshiba.ppok.repository.CityViewRepository;
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
	 * 都市リポジトリ
	 */
	private final CityRepository cityRepository;

	/**
	 * 都市情報リポジトリ
	 */
	private final CityViewRepository cityViewRepository;

	@Override
	public List<City> checkDuplicatedNames(final String cityName) {
		final City city = new City();
		city.setName(StringUtils.toHankaku(cityName));
		city.setDeleteFlg(Messages.MSG007);
		final Example<City> example = Example.of(city, ExampleMatcher.matchingAll());
		return this.cityRepository.findAll(example);
	}

	@Override
	public List<String> findAllContinents() {
		return this.cityViewRepository.findContinents();
	}

	@Override
	public String findLanguageByCty(final String nationVal) {
		return this.cityViewRepository.getLanguage(nationVal);
	}

	@Override
	public List<String> findNationsByCnt(final String continentVal) {
		if (StringUtils.isDigital(continentVal)) {
			final Integer id = Integer.parseInt(continentVal);
			final List<String> list = Lists.newArrayList();
			final CityView cityView = this.cityViewRepository.findById(id).orElseGet(CityView::new);
			final String nationName = cityView.getNation();
			list.add(nationName);
			final List<String> nations = this.cityViewRepository.findNationsByCnt(cityView.getContinent());
			final List<String> collect = nations.stream().filter(item -> StringUtils.isNotEqual(item, nationName))
					.sorted(Comparator.naturalOrder()).toList();
			list.addAll(collect);
			return list;
		}
		return this.cityViewRepository.findNationsByCnt(continentVal).stream().sorted(Comparator.naturalOrder())
				.toList();
	}

	@Override
	public CityDto getCityInfoById(final Integer id) {
		final CityView cityInfo = this.cityViewRepository.findById(id).orElseGet(CityView::new);
		return new CityDto(cityInfo.getId(), cityInfo.getName(), cityInfo.getContinent(), cityInfo.getNation(),
				cityInfo.getDistrict(), cityInfo.getPopulation(), cityInfo.getLanguage());
	}

	private String getCountryCode(final String nationName) {
		final CityView cityView = new CityView();
		cityView.setNation(nationName);
		final Example<CityView> example = Example.of(cityView, ExampleMatcher.matching());
		final List<Integer> ids = this.cityViewRepository.findAll(example).stream().map(CityView::getId).toList();
		final List<City> cities = this.cityRepository.findAllById(ids);
		return cities.get(0).getCountryCode();
	}

	@Override
	public Pagination<CityDto> getPageInfo(final Integer pageNum, final String keyword) {
		final int jpaPageNum = pageNum - 1;
		// ページングコンストラクタを宣言する；
		final PageRequest pageRequest = PageRequest.of(jpaPageNum, PAGE_SIZE, Sort.by(Direction.ASC, "id"));
		// キーワードの属性を判断する；
		if (StringUtils.isNotEmpty(keyword)) {
			// エンティティを宣言する；
			final CityView cityView = new CityView();
			final String hankakuKeyword = StringUtils.toHankaku(keyword);
			final int pageMin = PAGE_SIZE * jpaPageNum;
			final int pageMax = PAGE_SIZE * pageNum;
			int sort = SORT_NUMBER;
			if (hankakuKeyword.startsWith("min(pop)")) {
				final int indexOf = hankakuKeyword.indexOf(")");
				final String keisan = hankakuKeyword.substring(indexOf + 1);
				if (StringUtils.isNotEmpty(keisan)) {
					sort = Integer.parseInt(keisan);
				}
				final List<CityDto> minimumRanks = this.cityViewRepository.findMinimumRanks(sort).stream()
						.map(item -> new CityDto(item.getId(), item.getName(), item.getContinent(), item.getNation(),
								item.getDistrict(), item.getPopulation(), item.getLanguage()))
						.toList();
				if (pageMax >= sort) {
					return Pagination.of(minimumRanks.subList(pageMin, sort), minimumRanks.size(), pageNum, PAGE_SIZE,
							NAVIGATION_PAGES);
				}
				return Pagination.of(minimumRanks.subList(pageMin, pageMax), minimumRanks.size(), pageNum, PAGE_SIZE,
						NAVIGATION_PAGES);
			}
			if (hankakuKeyword.startsWith("max(pop)")) {
				final int indexOf = hankakuKeyword.indexOf(")");
				final String keisan = hankakuKeyword.substring(indexOf + 1);
				if (StringUtils.isNotEmpty(keisan)) {
					sort = Integer.parseInt(keisan);
				}
				final List<CityDto> maximumRanks = this.cityViewRepository.findMaximumRanks(sort).stream()
						.map(item -> new CityDto(item.getId(), item.getName(), item.getContinent(), item.getNation(),
								item.getDistrict(), item.getPopulation(), item.getLanguage()))
						.toList();
				if (pageMax >= sort) {
					return Pagination.of(maximumRanks.subList(pageMin, sort), maximumRanks.size(), pageNum, PAGE_SIZE,
							NAVIGATION_PAGES);
				}
				return Pagination.of(maximumRanks.subList(pageMin, pageMax), maximumRanks.size(), pageNum, PAGE_SIZE,
						NAVIGATION_PAGES);
			}
			// ページング検索；
			final Integer nationCnt = this.cityViewRepository.countByNations(hankakuKeyword);
			if (nationCnt >= 1) {
				cityView.setNation(hankakuKeyword);
				final Example<CityView> example = Example.of(cityView, ExampleMatcher.matching());
				final Page<CityView> pages = this.cityViewRepository.findAll(example, pageRequest);
				final List<CityDto> pagesByNation = pages.getContent().stream()
						.map(item -> new CityDto(item.getId(), item.getName(), item.getContinent(), item.getNation(),
								item.getDistrict(), item.getPopulation(), item.getLanguage()))
						.toList();
				return Pagination.of(pagesByNation, pages.getTotalElements(), pageNum, PAGE_SIZE, NAVIGATION_PAGES);
			}
			cityView.setName(hankakuKeyword);
			final ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("name",
					GenericPropertyMatchers.contains());
			final Example<CityView> example = Example.of(cityView, matcher);
			final Page<CityView> pages = this.cityViewRepository.findAll(example, pageRequest);
			final List<CityDto> pagesByName = pages.getContent().stream()
					.map(item -> new CityDto(item.getId(), item.getName(), item.getContinent(), item.getNation(),
							item.getDistrict(), item.getPopulation(), item.getLanguage()))
					.toList();
			return Pagination.of(pagesByName, pages.getTotalElements(), pageNum, PAGE_SIZE, NAVIGATION_PAGES);
		}
		// ページング検索；
		final Page<CityView> pages = this.cityViewRepository.findAll(pageRequest);
		final List<CityDto> pageInfos = pages.getContent().stream()
				.map(item -> new CityDto(item.getId(), item.getName(), item.getContinent(), item.getNation(),
						item.getDistrict(), item.getPopulation(), item.getLanguage()))
				.toList();
		return Pagination.of(pageInfos, pages.getTotalElements(), pageNum, PAGE_SIZE, NAVIGATION_PAGES);
	}

	@Override
	public RestMsg removeById(final Integer id) {
		this.cityRepository.removeById(id);
		return RestMsg.success(Messages.MSG013);
	}

	@Override
	public RestMsg saveById(final CityDto cityDto) {
		final City city = new City();
		SecondBeanUtils.copyNullableProperties(cityDto, city);
		final Integer saiban = this.cityRepository.saiban();
		final String countryCode = this.getCountryCode(cityDto.nation());
		city.setId(saiban);
		city.setCountryCode(countryCode);
		city.setDeleteFlg(Messages.MSG007);
		try {
			this.cityRepository.saveAndFlush(city);
		} catch (final Exception e) {
			return RestMsg.failure().add("errorMsg", Messages.MSG009);
		}
		return RestMsg.success(Messages.MSG011);
	}

	@Override
	public RestMsg updateById(final CityDto cityDto) {
		final City city = this.cityRepository.findById(cityDto.id()).orElse(null);
		if (city == null) {
			return RestMsg.failure().add("errorMsg", Messages.MSG009);
		}
		final City original = new City();
		SecondBeanUtils.copyNullableProperties(city, original);
		final String countryCode = this.getCountryCode(cityDto.nation());
		city.setCountryCode(countryCode);
		SecondBeanUtils.copyNullableProperties(cityDto, city);
		if (original.equals(city)) {
			return RestMsg.failure().add("errorMsg", Messages.MSG012);
		}
		this.cityRepository.saveAndFlush(city);
		return RestMsg.success(Messages.MSG010);
	}
}
