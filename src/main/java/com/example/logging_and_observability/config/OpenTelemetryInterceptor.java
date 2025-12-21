package com.example.logging_and_observability.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class OpenTelemetryInterceptor implements HandlerInterceptor {

    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;

    public OpenTelemetryInterceptor(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer("product-management-backend");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Extraire le contexte du header traceparent
        Context extractedContext = openTelemetry.getPropagators().getTextMapPropagator()
                .extract(Context.current(), request, new HttpServletRequestGetter());

        // Créer un span pour cette requête
        Span span = tracer.spanBuilder(request.getMethod() + " " + request.getRequestURI())
                .setParent(extractedContext)
                .startSpan();

        // Ajouter des attributs
        span.setAttribute("http.method", request.getMethod());
        span.setAttribute("http.url", request.getRequestURL().toString());
        span.setAttribute("http.target", request.getRequestURI());
        span.setAttribute("user.name", request.getHeader("X-User-Name"));
        span.setAttribute("user.email", request.getHeader("X-User-Email"));

        // Stocker le span dans la requête
        request.setAttribute("otel.span", span);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Span span = (Span) request.getAttribute("otel.span");
        if (span != null) {
            span.setAttribute("http.status_code", response.getStatus());

            if (ex != null) {
                span.recordException(ex);
            }

            span.end();
        }
    }

    // Getter pour extraire les headers HTTP
    private static class HttpServletRequestGetter implements TextMapGetter<HttpServletRequest> {
        @Override
        public Iterable<String> keys(HttpServletRequest carrier) {
            return java.util.Collections.list(carrier.getHeaderNames());
        }

        @Override
        public String get(HttpServletRequest carrier, String key) {
            return carrier.getHeader(key);
        }
    }
}