package jp.co.toshiba.ppok.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Administrator
 */
@Controller
public class IndexController {

	@GetMapping("/index")
	public void index(final HttpServletResponse response) throws IOException {
		response.sendRedirect("/public/cities.html");
	}
}
