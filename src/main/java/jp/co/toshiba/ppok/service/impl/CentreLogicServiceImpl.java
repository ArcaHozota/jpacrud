package jp.co.toshiba.ppok.service.impl;

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
import jp.co.toshiba.ppok.repository.CountryRepository;
import jp.co.toshiba.ppok.service.CentreLogicService;
import jp.co.toshiba.ppok.utils.Messages;
import jp.co.toshiba.ppok.utils.Pagination;
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
	 * ページサイズ
	 */
	private static final Integer PAGE_SIZE = 12;

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

	/**
	 * 国家リポジトリ
	 */
	private final CountryRepository countryRepository;

	@Override
	public CityDto getCityInfoById(final Long id) {
		final CityView cityView = this.cityViewRepository.findById(id).orElseGet(CityView::new);
		return new CityDto(cityView.getId(), cityView.getName(), cityView.getContinent(), cityView.getNation(),
				cityView.getDistrict(), cityView.getPopulation(), cityView.getLanguage());
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
					return Pagination.of(minimumRanks.subList(pageMin, sort), minimumRanks.size(), pageNum, PAGE_SIZE);
				}
				return Pagination.of(minimumRanks.subList(pageMin, pageMax), minimumRanks.size(), pageNum, PAGE_SIZE);
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
					return Pagination.of(maximumRanks.subList(pageMin, sort), maximumRanks.size(), pageNum, PAGE_SIZE);
				}
				return Pagination.of(maximumRanks.subList(pageMin, pageMax), maximumRanks.size(), pageNum, PAGE_SIZE);
			}
			// ページング検索；
			final String nationCode = this.countryRepository.findNationCode(hankakuKeyword);
			if (StringUtils.isNotEmpty(nationCode)) {
				cityView.setNation(hankakuKeyword);
				final Example<CityView> example = Example.of(cityView, ExampleMatcher.matching());
				final Page<CityView> pages = this.cityViewRepository.findAll(example, pageRequest);
				final List<CityDto> pagesByNation = pages.getContent().stream()
						.map(item -> new CityDto(item.getId(), item.getName(), item.getContinent(), item.getNation(),
								item.getDistrict(), item.getPopulation(), item.getLanguage()))
						.toList();
				return Pagination.of(pagesByNation, pages.getTotalElements(), pageNum, PAGE_SIZE);
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
			return Pagination.of(pagesByName, pages.getTotalElements(), pageNum, PAGE_SIZE);
		}
		// ページング検索；
		final Page<CityView> pages = this.cityViewRepository.findAll(pageRequest);
		final List<CityDto> pageInfos = pages.getContent().stream()
				.map(item -> new CityDto(item.getId(), item.getName(), item.getContinent(), item.getNation(),
						item.getDistrict(), item.getPopulation(), item.getLanguage()))
				.toList();
		return Pagination.of(pageInfos, pages.getTotalElements(), pageNum, PAGE_SIZE);
	}

	@Override
	public List<String> findNationsByCityId(final Long id) {
		final List<String> list = Lists.newArrayList();
		final CityView cityView = this.cityViewRepository.findById(id).orElseGet(CityView::new);
		final String nationName = cityView.getNation();
		list.add(nationName);
		final List<String> nations = this.countryRepository.findNationsByCnt(cityView.getContinent());
		final List<String> collect = nations.stream().filter(item -> StringUtils.isNotEqual(item, nationName)).toList();
		list.addAll(collect);
		return list;
	}

	@Override
	public void updateById(final CityDto cityDto) {
		final String nationCode = this.countryRepository.findNationCode(cityDto.nation());
		final City city = this.cityRepository.findById(cityDto.id()).orElseGet(City::new);
		city.setName(cityDto.name());
		city.setCountryCode(nationCode);
		city.setDistrict(cityDto.district());
		city.setPopulation(cityDto.population());
		this.cityRepository.save(city);
	}

	@Override
	public void saveById(final CityDto cityDto) {
		final String nationCode = this.countryRepository.findNationCode(cityDto.nation());
		final Long saiban = this.cityRepository.saiban();
		final City city = new City();
		city.setId(saiban);
		city.setName(cityDto.name());
		city.setCountryCode(nationCode);
		city.setDistrict(cityDto.district());
		city.setPopulation(cityDto.population());
		city.setDeleteFlg(Messages.MSG007);
		this.cityRepository.save(city);
	}

	@Override
	public void removeById(final Long id) {
		this.cityRepository.removeById(id);
	}

	@Override
	public List<String> findAllContinents() {
		return this.countryRepository.findAllContinents();
	}

	@Override
	public List<String> findNationsByCnt(final String continentVal) {
		final String hankaku = StringUtils.toHankaku(continentVal);
		return this.countryRepository.findNationsByCnt(hankaku);
	}

	@Override
	public String findLanguageByCty(final String nationVal) {
		final CityView cityView = new CityView();
		cityView.setNation(nationVal);
		final Example<CityView> example = Example.of(cityView, ExampleMatcher.matching());
		return this.cityViewRepository.findAll(example).get(0).getLanguage();
	}

	@Override
	public List<City> checkDuplicatedNames(final String cityName) {
		final City city = new City();
		city.setName(StringUtils.toHankaku(cityName));
		city.setDeleteFlg(Messages.MSG007);
		final Example<City> example = Example.of(city, ExampleMatcher.matchingAll());
		return this.cityRepository.findAll(example);
	}
}
