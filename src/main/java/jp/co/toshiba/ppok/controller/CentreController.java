package jp.co.toshiba.ppok.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.co.toshiba.ppok.dto.CityDto;
import jp.co.toshiba.ppok.service.CentreLogicService;
import jp.co.toshiba.ppok.utils.Messages;
import jp.co.toshiba.ppok.utils.Pagination;
import jp.co.toshiba.ppok.utils.RestMsg;
import jp.co.toshiba.ppok.utils.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 中央処理コントローラ
 *
 * @author Arcahozota
 * @since 1.11
 */
@RestController
@RequestMapping("/public/grssmcrud")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CentreController {

	/**
	 * ページングサイズ
	 */
	private static final Integer PAGE_SIZE = 17;

	/**
	 * 中央処理サービスインターフェス
	 */
	private final CentreLogicService centreLogicService;

	/**
	 * Retrieve the city data.
	 *
	 * @return page(JSON)
	 */
	@GetMapping(value = "/city")
	public RestMsg getCities(@RequestParam(value = "pageNum", defaultValue = "1") final Integer pageNum,
			@RequestParam(value = "keyword", defaultValue = StringUtils.EMPTY_STRING) final String keyword) {
		final Pagination<CityDto> cityInfos = this.centreLogicService.findByKeywords(pageNum, PAGE_SIZE, keyword);
		return RestMsg.success().add("pageInfo", cityInfos);
	}

	/**
	 * Search the selected city's name.
	 *
	 * @param id the ID of city
	 * @return RestMsg.success().add(data)
	 */
	@GetMapping(value = "/city/{id}")
	public RestMsg getCityInfo(@PathVariable("id") final Integer id) {
		final CityDto cityInfo = this.centreLogicService.getCityInfo(id);
		return RestMsg.success().add("citySelected", cityInfo);
	}

	/**
	 * Save input city info.
	 *
	 * @param cityDto the input message of cities
	 * @return RestMsg.success()
	 */
	@PostMapping(value = "/city")
	public RestMsg saveCityInfo(@RequestBody final CityDto cityDto) {
		this.centreLogicService.save(cityDto);
		return RestMsg.success();
	}

	/**
	 * Update city info.
	 *
	 * @param cityDto the input message of cities
	 * @return RestMsg.success()
	 */
	@PutMapping(value = "/city/{id}")
	public RestMsg updateCityDto(@RequestBody final CityDto cityDto) {
		this.centreLogicService.update(cityDto);
		return RestMsg.success();
	}

	/**
	 * Delete the selected city info.
	 *
	 * @param id the ID of city
	 * @return RestMsg.success()
	 */
	@DeleteMapping(value = "/city/{id}")
	public RestMsg deleteCityDto(@PathVariable("id") final Integer id) {
		this.centreLogicService.removeById(id);
		return RestMsg.success();
	}

	/**
	 * Check the input city name already existed or not.
	 *
	 * @param cityName the input name
	 * @return RestMsg.success()
	 */
	@GetMapping(value = "/checklist")
	public RestMsg checkCityName(@RequestParam("cityName") final String cityName) {
		if (!cityName.matches(Messages.MSG006)) {
			return RestMsg.failure().add("validatedMsg", Messages.MSG005);
		}
		final Boolean duplicated = this.centreLogicService.checkDuplicated(cityName);
		if (Boolean.TRUE.equals(duplicated)) {
			return RestMsg.failure().add("validatedMsg", Messages.MSG004);
		}
		return RestMsg.success();
	}

	/**
	 * Get list of continents.
	 *
	 * @return RestMsg.success().add(data)
	 */
	@GetMapping(value = "/continents")
	public RestMsg getListOfContinents() {
		final List<String> cnList = this.centreLogicService.findAllContinents();
		return RestMsg.success().add("continents", cnList);
	}

	/**
	 * Get list of nations.
	 *
	 * @return RestMsg.success().add(data)
	 */
	@GetMapping(value = "/countries")
	public RestMsg getListOfNations(@RequestParam("continentVal") final String continent) {
		final List<String> nationList = this.centreLogicService.findNationsByCnt(continent);
		return RestMsg.success().add("nations", nationList);
	}

	/**
	 * Get list of nations.
	 *
	 * @return RestMsg.success().add(data)
	 */
	@GetMapping(value = "/countries/{id}")
	public RestMsg getListOfNationsById(@PathVariable("id") final Integer id) {
		final List<String> nationList = this.centreLogicService.findNationsByCityId(id);
		return RestMsg.success().add("nationsByName", nationList);
	}

	/**
	 * Get language by nation.
	 *
	 * @return RestMsg.success().add(data)
	 */
	@GetMapping(value = "/language")
	public RestMsg getLanguages(@RequestParam("nationVal") final String nation) {
		final String language = this.centreLogicService.findLanguageByCty(nation);
		return RestMsg.success().add("languages", language);
	}
}