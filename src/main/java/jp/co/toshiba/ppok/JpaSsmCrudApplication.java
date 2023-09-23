package jp.co.toshiba.ppok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import jp.co.toshiba.ppok.utils.Messages;
import lombok.extern.log4j.Log4j2;

/**
 * JpaCrudアプリケーション
 *
 * @author ArcaHozota
 * @since 1.00beta
 */
@Log4j2
@SpringBootApplication
@ServletComponentScan
public class JpaSsmCrudApplication {
	public static void main(final String[] args) {
		SpringApplication.run(JpaSsmCrudApplication.class, args);
		log.info(Messages.MSG003);
	}
}
