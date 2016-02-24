package com.king.tratt.spi;


public interface ApiConfiguratorProvider<E extends Event> {
    
    ApiConfigurator<E> getApiConfigurator();

}
