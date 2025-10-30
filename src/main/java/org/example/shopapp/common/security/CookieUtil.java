package org.example.shopapp.common.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;

public class CookieUtil {

	public static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
	public static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";

	public static void addHttpOnlyCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds, boolean secure, String sameSite, String path, String domain) {
		Cookie cookie = new Cookie(name, value);
		cookie.setHttpOnly(true);
		cookie.setSecure(secure);
		cookie.setPath(path != null ? path : "/");
		cookie.setMaxAge(maxAgeSeconds);
		if (domain != null && !domain.isEmpty()) {
			cookie.setDomain(domain);
		}
		response.addHeader("Set-Cookie", buildSetCookieHeader(cookie, sameSite));
	}

	public static void clearCookie(HttpServletResponse response, String name, boolean secure, String sameSite, String path, String domain) {
		addHttpOnlyCookie(response, name, "", 0, secure, sameSite, path, domain);
	}

	public static Optional<String> getCookie(HttpServletRequest request, String name) {
		if (request.getCookies() == null) return Optional.empty();
		return Arrays.stream(request.getCookies())
				.filter(c -> c.getName().equals(name))
				.map(Cookie::getValue)
				.findFirst();
	}

    private static String buildSetCookieHeader(Cookie cookie, String sameSite) {
		StringBuilder sb = new StringBuilder();
		sb.append(cookie.getName()).append("=").append(cookie.getValue() == null ? "" : cookie.getValue());
		sb.append("; Path=").append(cookie.getPath());
		if (cookie.getDomain() != null) {
			sb.append("; Domain=").append(cookie.getDomain());
		}
		sb.append("; Max-Age=").append(cookie.getMaxAge());
		if (cookie.getSecure()) sb.append("; Secure");
		sb.append("; HttpOnly");
		if (sameSite != null && !sameSite.isEmpty()) {
			sb.append("; SameSite=").append(sameSite);
		}
		return sb.toString();
	}

    public static String buildSetCookieHeader(String name, String value, int maxAgeSeconds, boolean secure, String sameSite, String path, String domain) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath(path != null ? path : "/");
        cookie.setMaxAge(maxAgeSeconds);
        if (domain != null && !domain.isEmpty()) {
            cookie.setDomain(domain);
        }
        return buildSetCookieHeader(cookie, sameSite);
    }
}


