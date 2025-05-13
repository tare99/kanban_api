package com.nsoft.integrations.vibra.kanban_api.api.filter;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

  private final ConcurrentMap<Object, Object> cache =
      Caffeine.newBuilder()
          .expireAfterAccess(Duration.ofMinutes(10))
          .maximumSize(10_000)
          .build()
          .asMap();

  private Bucket createNewBucket() {
    return Bucket.builder()
        .addLimit(
            Bandwidth.builder().capacity(100).refillGreedy(100, Duration.ofMinutes(1)).build())
        .build();
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String ip = getClientIP(request);
    Bucket bucket = (Bucket) cache.computeIfAbsent(ip, _ -> createNewBucket());
    log.info("IP: {}, tokens left: {}", ip, bucket.getAvailableTokens());

    if (bucket.tryConsume(1)) {
      filterChain.doFilter(request, response);
    } else {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.getWriter().write("Rate limit exceeded. Try again later.");
    }
  }

  private String getClientIP(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    return forwarded != null ? forwarded.split(",")[0] : request.getRemoteAddr();
  }
}
