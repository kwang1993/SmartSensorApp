package com.smartsensor.www.com.kodart.httpzoid;

/**
 * (c) Artur Sharipov
 */
public interface HttpSerializer {
    public String getContentType();
    public String serialize(Object object);
    public Object deserialize(String value, Class type);
}
