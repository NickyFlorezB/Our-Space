package com.nicky.ourspace.domain.port;

import java.util.Map;

public interface AuthenticationPort {

    Map<String, String> login(String username, String password);
}
