package org.multibit.hd.error_reporting.servlets;

/**
 * <p>Servlet filter to provide the following to application:</p>
 * <ul>
 * <li>Detect repeat requests from same origin and discard them</li>
 * </ul>
 */

import org.multibit.hd.error_reporting.caches.AddressThrottlingCache;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AddressThrottlingFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Do nothing
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    String remoteAddress = request.getRemoteAddr();

    if (AddressThrottlingCache.INSTANCE.put(remoteAddress)) {
      // This is new so proceed with request
      chain.doFilter(request, response);
    }

    // If not new then not calling the chain will block the request from proceeding
    if (response instanceof HttpServletResponse) {

      HttpServletResponse httpResponse = (HttpServletResponse) response;

      httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);

    }

  }

  @Override
  public void destroy() {
    // Do nothing
  }

}

