package jp.co.toshiba.ppok.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
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
import jp.co.toshiba.ppok.entity.Language;
import jp.co.toshiba.ppok.repository.CityRepository;
import jp.co.toshiba.ppok.repository.CityViewRepository;
import jp.co.toshiba.ppok.repository.CountryRepository;
import jp.co.toshiba.ppok.repository.LanguageRepository;
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

	/**
	 * 言語リポジトリ
	 */
	private final LanguageRepository languageRepository;

	@Override
	public CityDto getCityInfoById(final Long id) {
		final CityView cityView = this.cityViewRepository.findById(id).orElseGet(CityView::new);
		final CityDto cityInfoDto = new CityDto();
		BeanUtils.copyProperties(cityView, cityInfoDto);
		final String language = this.findLanguageByCty(cityInfoDto.getNation());
		cityInfoDto.setLanguage(language);
		return cityInfoDto;
	}

	@Override
	public Pagination<CityDto> getPageInfo(final Integer pageNum, final String keyword) {
		final int jpaPageNum = pageNum - 1;
		// ページングコンストラクタを宣言する；
		final PageRequest pageRequest = PageRequest.of(jpaPageNum, PAGE_SIZE, Sort.by(Direction.ASC, "id"));
		// キーワードの属性を判断する；
		if (StringUtils.isNotEmpty(keyword)) {
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
				// 人口数量昇順で最初の15個都市の情報を吹き出します；
				final List<CityDto> minimumRanks = this.cityViewRepository.findMinimumRanks(sort).stream().map(item -> {
					final CityDto cityDto = new CityDto();
					BeanUtils.copyProperties(item, cityDto);
					final String language = this.findLanguageByCty(item.getNation());
					cityDto.setLanguage(language);
					return cityDto;
				}).collect(Collectors.toList());
				if (pageMax >= sort) {
					return Pagination.of(minimumRanks.subList(pageMin, sort), minimumRanks.size(), pageNum);
				}
				return Pagination.of(minimumRanks.subList(pageMin, pageMax), minimumRanks.size(), pageNum);
			}
			if (hankakuKeyword.startsWith("max(pop)")) {
				final int indexOf = hankakuKeyword.indexOf(")");
				final String keisan = hankakuKeyword.substring(indexOf + 1);
				if (StringUtils.isNotEmpty(keisan)) {
					sort = Integer.parseInt(keisan);
				}
				// 人口数量降順で最初の15個都市の情報を吹き出します；
				final List<CityDto> maximumRanks = this.cityViewRepository.findMaximumRanks(sort).stream().map(item -> {
					final CityDto cityDto = new CityDto();
					BeanUtils.copyProperties(item, cityDto);
					final String language = this.findLanguageByCty(item.getNation());
					cityDto.setLanguage(language);
					return cityDto;
				}).collect(Collectors.toList());
				if (pageMax >= sort) {
					return Pagination.of(maximumRanks.subList(pageMin, sort), maximumRanks.size(), pageNum);
				}
				return Pagination.of(maximumRanks.subList(pageMin, pageMax), maximumRanks.size(), pageNum);
			}
			// ページング検索；
			final CityView cityView = new CityView();
			cityView.setNation(hankakuKeyword);
			final Example<CityView> example = Example.of(cityView, ExampleMatcher.matching());
			final List<CityView> findByNations = this.cityViewRepository.findAll(example);
			if (!findByNations.isEmpty()) {
				final Page<CityView> pages = this.cityViewRepository.findAll(example, pageRequest);
				final List<CityDto> pagesByNation = pages.getContent().stream().map(item -> {
					final CityDto cityDto = new CityDto();
					BeanUtils.copyProperties(item, cityDto);
					final String language = this.findLanguageByCty(item.getNation());
					cityDto.setLanguage(language);
					return cityDto;
				}).collect(Collectors.toList());
				return Pagination.of(pagesByNation, pages.getTotalElements(), pageNum);
			}
			cityView.setNation(null);
			cityView.setName(hankakuKeyword);
			final ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("name",
					GenericPropertyMatchers.contains());
			final Example<CityView> example2 = Example.of(cityView, exampleMatcher);
			final Page<CityView> pages = this.cityViewRepository.findAll(example2, pageRequest);
			final List<CityDto> pagesByName = pages.getContent().stream().map(item -> {
				final CityDto cityDto = new CityDto();
				BeanUtils.copyProperties(item, cityDto);
				final String language = this.findLanguageByCty(item.getNation());
				cityDto.setLanguage(language);
				return cityDto;
			}).collect(Collectors.toList());
			return Pagination.of(pagesByName, pages.getTotalElements(), pageNum);
		}
		// ページング検索；
		final Page<CityView> pages = this.cityViewRepository.findAll(pageRequest);
		final List<CityDto> pageInfos = pages.getContent().stream().map(item -> {
			final CityDto cityDto = new CityDto();
			BeanUtils.copyProperties(item, cityDto);
			final String language = this.findLanguageByCty(item.getNation());
			cityDto.setLanguage(language);
			return cityDto;
		}).collect(Collectors.toList());
		return Pagination.of(pageInfos, pages.getTotalElements(), pageNum);
	}

	@Override
	public List<String> findNationsByCityId(final Long id) {
		final List<String> list = Lists.newArrayList();
		final CityView cityView = this.cityViewRepository.findById(id).orElseGet(CityView::new);
		final String nationName = cityView.getNation();
		list.add(nationName);
		final List<String> nations = this.countryRepository.findNationsByCnt(cityView.getContinent());
		final List<String> collect = nations.stream().filter(item -> StringUtils.isNotEqual(item, nationName))
				.collect(Collectors.toList());
		list.addAll(collect);
		return list;
	}

	@Override
	public void updateById(final CityDto cityDto) {
		final City city = new City();
		BeanUtils.copyProperties(cityDto, city, "continent", "nation", "language");
		final String nationName = cityDto.getNation();
		final String nationCode = this.countryRepository.findNationCode(nationName);
		city.setCountryCode(nationCode);
		this.cityRepository.save(city);
	}

	@Override
	public void saveById(final CityDto cityDto) {
		final City city = new City();
		BeanUtils.copyProperties(cityDto, city, "continent", "nation", "language");
		final Long saiban = this.cityRepository.saiban();
		final String nationCode = this.countryRepository.findNationCode(cityDto.getNation());
		city.setId(saiban);
		city.setCountryCode(nationCode);
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
		final List<Language> languages = this.languageRepository.getLanguagesByCountryName(nationVal);
		if (languages.size() == 1) {
			return languages.get(0).getName();
		}
		final List<Language> officialLanguages = languages.stream()
				.filter(al -> StringUtils.isEqual("True", al.getIsOfficial())).collect(Collectors.toList());
		final List<Language> typicalLanguages = languages.stream()
				.filter(al -> StringUtils.isEqual("False", al.getIsOfficial())).collect(Collectors.toList());
		if (officialLanguages.isEmpty() && !typicalLanguages.isEmpty()) {
			return typicalLanguages.get(0).getName();
		}
		if (!officialLanguages.isEmpty() && typicalLanguages.isEmpty()) {
			return officialLanguages.get(0).getName();
		}
		final Language language1 = officialLanguages.get(0);
		final Language language2 = typicalLanguages.get(0);
		if (language2.getPercentage().subtract(language1.getPercentage()).compareTo(BigDecimal.valueOf(35L)) <= 0) {
			return language1.getName();
		}
		return language2.getName();
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
