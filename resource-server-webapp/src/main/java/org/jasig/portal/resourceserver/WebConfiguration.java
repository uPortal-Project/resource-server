package org.jasig.portal.resourceserver;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {

    /**
     * Makes static resources within webjars (i.e. JavaScript) available from a browser.  Solution
     * based on this article:  http://www.baeldung.com/maven-webjars.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry
                .addResourceHandler("/rs/**")
                .addResourceLocations("/rs/");
        registry
                .addResourceHandler("/webjars/**")
                .addResourceLocations("/webjars/");
    }

}
