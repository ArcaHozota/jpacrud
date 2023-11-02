package jp.co.toshiba.ppok.dto;

/**
 * 都市情報DTO
 *
 * @author ArcaHozota
 * @since 1.07
 */
public record CityDto(Integer id, String name, String continent, String nation, String district, Integer population,
		String language) {
}