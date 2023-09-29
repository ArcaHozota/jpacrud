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
import jp.co.toshiba.ppok.entity.City;
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
 * @author ArcaHozota
 * @since 1.11
 */
@RestController
@RequestMapping("/public/sshcrud")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CentreController {

	/**
	 * 中央処理サービスインターフェス
	 */
	private final CentreLogicService centreLogicService;

	/**
	 * 都市情報をページング検索する
	 *
	 * @return page(JSON)
	 */
	@GetMapping(value = "/city")
	public RestMsg getCities(@RequestParam(value = "pageNum", defaultValue = "1") final Integer pageNum,
			@RequestParam(value = "keyword", defaultValue = StringUtils.EMPTY_STRING) final String keyword) {
		final Pagination<CityDto> cityInfos = this.centreLogicService.getPageInfo(pageNum, keyword);
		return RestMsg.success().add("pageInfo", cityInfos);
	}

	/**
	 * 選択された都市情報を取得する
	 *
	 * @param id 都市ID
	 * @return RestMsg.success().add(data)
	 */
	@GetMapping(value = "/city/{id}")
	public RestMsg getCityInfo(@PathVariable("id") final Long id) {
		final CityDto cityInfo = this.centreLogicService.getCityInfoById(id);
		return RestMsg.success().add("citySelected", cityInfo);
	}

	/**
	 * 都市情報を保存する
	 *
	 * @param cityDto 都市情報DTO
	 * @return RestMsg.success()
	 */
	@PostMapping(value = "/city")
	public RestMsg saveCityInfo(@RequestBody final CityDto cityDto) {
		this.centreLogicService.saveById(cityDto);
		return RestMsg.success();
	}

	/**
	 * 都市情報を更新する
	 *
	 * @param cityDto 都市情報DTO
	 * @return RestMsg.success()
	 */
	@PutMapping(value = "/city/{id}")
	public RestMsg updateCityDto(@RequestBody final CityDto cityDto) {
		this.centreLogicService.updateById(cityDto);
		return RestMsg.success();
	}

	/**
	 * 都市IDによって都市情報を削除する(論理削除)
	 *
	 * @param id 都市ID
	 * @return RestMsg.success()
	 */
	@DeleteMapping(value = "/city/{id}")
	public RestMsg deleteCityDto(@PathVariable("id") final Long id) {
		this.centreLogicService.removeById(id);
		return RestMsg.success();
	}

	/**
	 * 都市名を重複するかどうかを確認する
	 *
	 * @param cityName 都市名
	 * @return RestMsg.success()
	 */
	@GetMapping(value = "/checklist")
	public RestMsg checkCityName(@RequestParam("cityName") final String cityName) {
		if (!cityName.matches(Messages.MSG006)) {
			return RestMsg.failure().add("validatedMsg", Messages.MSG005);
		}
		final List<City> duplicatedNames = this.centreLogicService.checkDuplicatedNames(cityName);
		if (!duplicatedNames.isEmpty()) {
			return RestMsg.failure().add("validatedMsg", Messages.MSG004);
		}
		return RestMsg.success();
	}

	/**
	 * 大陸リストを検索する
	 *
	 * @return RestMsg.success().add(data)
	 */
	@GetMapping(value = "/continents")
	public RestMsg getListOfContinents() {
		final List<String> cnList = this.centreLogicService.findAllContinents();
		return RestMsg.success().add("continents", cnList);
	}

	/**
	 * 大陸によってすべての国を検索する
	 *
	 * @return RestMsg.success().add(data)
	 */
	@GetMapping(value = "/countries")
	public RestMsg getListOfNations(@RequestParam("continentVal") final String continent) {
		final List<String> nationList = this.centreLogicService.findNationsByCnt(continent);
		return RestMsg.success().add("nations", nationList);
	}

	/**
	 * 選択された都市の大陸によってすべての国を検索する
	 *
	 * @return RestMsg.success().add(data)
	 */
	@GetMapping(value = "/countries/{id}")
	public RestMsg getListOfNationsById(@PathVariable("id") final Long id) {
		final List<String> nationList = this.centreLogicService.findNationsByCityId(id);
		return RestMsg.success().add("nationsByName", nationList);
	}

	/**
	 * 国名によって公用語を取得する
	 *
	 * @return RestMsg.success().add(data)
	 */
	@GetMapping(value = "/language")
	public RestMsg getLanguages(@RequestParam("nationVal") final String nation) {
		final String language = this.centreLogicService.findLanguageByCty(nation);
		return RestMsg.success().add("languages", language);
	}
}