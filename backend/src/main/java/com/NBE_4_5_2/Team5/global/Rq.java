package com.NBE_4_5_2.Team5.global;

import java.util.Optional;

public interface Rq {
	Optional<String> getRefreshToken();

	String getValueFromCookie(String accessToken);

	void addCookie(String accessToken, String s);

	String getHeader(String authorization);

	void removeCookie(String accessToken);
}
