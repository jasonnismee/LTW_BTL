package com.restaurant.util;

public final class AppConstants {
  private AppConstants() {}

  public static final class SessionKeys {
    private SessionKeys() {}
    public static final String CART = "CART";
  }

  public static final class Routes {
    private Routes() {}
    public static final String AUTH_LOGIN = "/auth/login";
    public static final String AUTH_REGISTER = "/auth/register";
    public static final String AUTH_LOGOUT = "/auth/logout";

    public static final String ADMIN_DASHBOARD = "/admin/dashboard";
    public static final String STAFF_POS = "/staff/pos";
    public static final String CUSTOMER_HOME = "/customer/home";
  }
}

