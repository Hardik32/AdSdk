package com.ads.sdk.helper.banner.param;

public interface IAdsParam {

    public static final class None implements IAdsParam {
        public static final None INSTANCE = new None();

        private None() {
        }
    }
}