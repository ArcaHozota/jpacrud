package jp.co.toshiba.ppok.dto;

import jp.co.toshiba.ppok.entity.CityView;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CityDto extends CityView {

	private static final long serialVersionUID = 4831225402305424520L;

	private String language;
}
