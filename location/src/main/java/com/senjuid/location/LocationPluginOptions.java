package com.senjuid.location;

public class LocationPluginOptions {
    public String data;
    public String message1;
    public String message2;

    private LocationPluginOptions(Builder builder) {
        this.data = builder.data;
        this.message1 = builder.message1;
        this.message2 = builder.message2;
    }

    public static class Builder {
        private String data;
        private String message1;
        private String message2;

        public Builder setData(String data) {
            this.data = data;
            return this;
        }

        public Builder setMessage(String message1, String message2) {
            this.message1 = message1;
            this.message2 = message2;
            return this;
        }

        public LocationPluginOptions build() {
            return new LocationPluginOptions(this);
        }
    }
}
