package jp.co.toshiba.ppok.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jp.co.toshiba.ppok.entity.City;
import jp.co.toshiba.ppok.entity.CityView;
import jp.co.toshiba.ppok.entity.Language;
import jp.co.toshiba.ppok.repository.CityRepository;
import jp.co.toshiba.ppok.repository.CityViewRepository;
import jp.co.toshiba.ppok.repository.CountryRepository;
import jp.co.toshiba.ppok.repository.LanguageRepository;
import jp.co.toshiba.ppok.service.CentreLogicService;
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
	private static final Integer PAGE_SIZE = 17;

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
	public CityInfoDto getCityInfoById(final Long id) {
		final CityView cityView = this.cityViewRepository.getById(id);
		final CityInfoDto cityInfoDto = new CityInfoDto();
		BeanUtils.copyProperties(cityView, cityInfoDto);
		final String language = this.findLanguageByCty(cityInfoDto.getNation());
		cityInfoDto.setLanguage(language);
		return cityInfoDto;
	}

	@Override
	public Page<CityInfoDto> getPageInfo(final Integer pageNum, final String keyword) {
		// ページングコンストラクタを宣言する；
		final PageRequest pageRequest = PageRequest.of(pageNum - 1, PAGE_SIZE, Sort.by(Direction.ASC, "id"));
		// キーワードの属性を判断する；
		if (StringUtils.isNotEmpty(keyword)) {
			final String hankakuKeyword = StringUtils.toHankaku(keyword);
			final int pageMin = PAGE_SIZE * (pageNum - 1);
			final int pageMax = PAGE_SIZE * pageNum;
			int sort = SORT_NUMBER;
			if (hankakuKeyword.startsWith("min(pop)")) {
				final int indexOf = hankakuKeyword.indexOf(")");
				final String keisan = hankakuKeyword.substring(indexOf + 1);
				if (StringUtils.isNotEmpty(keisan)) {
					sort = Integer.parseInt(keisan);
				}
				// 人口数量昇順で最初の15個都市の情報を吹き出します；
				final List<CityInfoDto> minimumRanks = this.cityViewRepository.findMinimumRanks(sort).stream()
						.map(item -> {
							final CityInfoDto cityInfoDto = new CityInfoDto();
							BeanUtils.copyProperties(item, cityInfoDto);
							final String language = this.findLanguageByCty(item.getNation());
							cityInfoDto.setLanguage(language);
							return cityInfoDto;
						}).collect(Collectors.toList());
				if (pageMax >= sort) {
					return new PageImpl<>(minimumRanks.subList(pageMin, sort), pageRequest, minimumRanks.size());
				}
				return new PageImpl<>(minimumRanks.subList(pageMin, pageMax), pageRequest, minimumRanks.size());
			}
			if (hankakuKeyword.startsWith("max(pop)")) {
				final int indexOf = hankakuKeyword.indexOf(")");
				final String keisan = hankakuKeyword.substring(indexOf + 1);
				if (StringUtils.isNotEmpty(keisan)) {
					sort = Integer.parseInt(keisan);
				}
				// 人口数量降順で最初の15個都市の情報を吹き出します；
				final List<CityInfoDto> maximumRanks = this.cityViewRepository.findMaximumRanks(sort).stream()
						.map(item -> {
							final CityInfoDto cityInfoDto = new CityInfoDto();
							BeanUtils.copyProperties(item, cityInfoDto);
							final String language = this.findLanguageByCty(item.getNation());
							cityInfoDto.setLanguage(language);
							return cityInfoDto;
						}).collect(Collectors.toList());
				if (pageMax >= sort) {
					return new PageImpl<>(maximumRanks.subList(pageMin, sort), pageRequest, maximumRanks.size());
				}
				return new PageImpl<>(maximumRanks.subList(pageMin, pageMax), pageRequest, maximumRanks.size());
			}
			// ページング検索；
			final CityView cityView = new CityView();
			cityView.setNation(hankakuKeyword);
			final ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("nation",
					GenericPropertyMatchers.exact());
			final Example<CityView> example = Example.of(cityView, matcher);
			final List<CityView> findByNations = this.cityViewRepository.findAll(example);
			if (!findByNations.isEmpty()) {
				final Page<CityView> pages = this.cityViewRepository.findAll(example, pageRequest);
				return this.getCityInfoDtos(pages, pageRequest, pages.getTotalElements());
			}
			cityView.setNation(null);
			cityView.setName(hankakuKeyword);
			final ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("name",
					GenericPropertyMatchers.contains());
			final Example<CityView> example2 = Example.of(cityView, exampleMatcher);
			final Page<CityView> pages = this.cityViewRepository.findAll(example2, pageRequest);
			return this.getCityInfoDtos(pages, pageRequest, pages.getTotalElements());
		}
		// ページング検索；
		final Page<CityView> pages = this.cityViewRepository.findAll(pageRequest);
		return this.getCityInfoDtos(pages, pageRequest, pages.getTotalElements());
	}

	@Override
	public List<String> getListOfNationsById(final Long id) {
		final List<String> list = new ArrayList<>();
		final CityView cityView = this.cityViewRepository.getById(id);
		final List<String> nations = this.countryRepository.findNationsByCnt(cityView.getContinent());
		final String nationName = cityView.getNation();
		list.add(nationName);
		final List<String> collect = nations.stream().filter(item -> StringUtils.isNotEqual(item, nationName))
				.collect(Collectors.toList());
		list.addAll(collect);
		return list;
	}

	@Override
	public void update(final CityInfoDto cityInfoDto) {
		final City city = new City();
		BeanUtils.copyProperties(cityInfoDto, city, "continent", "nation", "language");
		final String nationName = cityInfoDto.getNation();
		final String nationCode = this.countryRepository.findNationCode(nationName);
		city.setCountryCode(nationCode);
		this.cityRepository.save(city);
	}

	@Override
	public void save(final CityInfoDto cityInfoDto) {
		final City city = new City();
		BeanUtils.copyProperties(cityInfoDto, city, "continent", "nation", "language");
		final Long saiban = this.cityRepository.saiban();
		final String nationCode = this.countryRepository.findNationCode(cityInfoDto.getNation());
		city.setId(saiban);
		city.setCountryCode(nationCode);
		city.setLogicDeleteFlg("visible");
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
		final String hankaku = StringUtils.toHankaku(nationVal);
		final String nationCode = this.countryRepository.findNationCode(hankaku);
		final Specification<Language> specification1 = (root, query, criteriaBuilder) -> criteriaBuilder
				.equal(root.get("countryCode"), nationCode);
		final Specification<Language> specification2 = (root, query, criteriaBuilder) -> {
			query.orderBy(criteriaBuilder.desc(root.get("percentage")));
			return criteriaBuilder.equal(root.get("logicDeleteFlg"), "visible");
		};
		final Specification<Language> languageSpecification = Specification.where(specification1).and(specification2);
		final List<Language> languages = this.languageRepository.findAll(languageSpecification);
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
	public List<City> checkDuplicate(final String cityName) {
		final City city = new City();
		city.setName(StringUtils.toHankaku(cityName));
		final ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("name", GenericPropertyMatchers.exact());
		final Example<City> example = Example.of(city, matcher);
		return this.cityRepository.findAll(example);
	}

	private Page<CityInfoDto> getCityInfoDtos(final Page<CityView> pages, final Pageable pageable, final Long total) {
		final List<CityInfoDto> cityInfoDtos = pages.getContent().stream().map(item -> {
			final CityInfoDto cityInfoDto = new CityInfoDto();
			BeanUtils.copyProperties(item, cityInfoDto);
			final String language = this.findLanguageByCty(item.getNation());
			cityInfoDto.setLanguage(language);
			return cityInfoDto;
		}).collect(Collectors.toList());
		return new PageImpl<>(cityInfoDtos, pageable, total);
	}
}
