package jp.co.toshiba.ppok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

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
public class JpaCrudApplication {
	public static void main(final String[] args) {
		SpringApplication.run(JpaCrudApplication.class, args);
		log.info("本アプリは正常的に起動されました。");
	}
}
