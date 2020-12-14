package org.mydotey.artemis.web.filter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.mydotey.java.ObjectExtension;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public class CustomFilterConfig implements FilterConfig {

    private ServletContext _context;
    private String _filterName;
    private Map<String, String> _initParams;

    public CustomFilterConfig(ServletContext context, String filterName, Map<String, String> initParams) {
        ObjectExtension.requireNonNull(context, "context");
        ObjectExtension.requireNonBlank(filterName, "filterName");
        ObjectExtension.requireNonNull(initParams, "initParams");

        _context = context;
        _filterName = filterName;
        _initParams = initParams;
    }

    @Override
    public String getFilterName() {
        return _filterName;
    }

    @Override
    public ServletContext getServletContext() {
        return _context;
    }

    @Override
    public String getInitParameter(String name) {
        return _initParams.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(_initParams.keySet());
    }

}
