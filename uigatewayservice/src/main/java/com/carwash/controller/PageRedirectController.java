package com.carwash.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;


@Controller
public class PageRedirectController {

    private final WebHandler webHandler;

    public PageRedirectController(WebHandler webHandler) {
        this.webHandler = webHandler;
    }

    private Mono<Void> forward(ServerWebExchange exchange, String toPath) {
        // ✅ Change only the request path, keep same host/port, no redirect, URL stays pretty
        ServerHttpRequest newRequest = exchange.getRequest()
                .mutate()
                .path(toPath)
                .build();

        ServerWebExchange newExchange = exchange.mutate()
                .request(newRequest)
                .build();

        return webHandler.handle(newExchange);
    }

    @GetMapping("/")                 public Mono<Void> root(ServerWebExchange ex){ return forward(ex, "/dashboard.html"); }
    @GetMapping("/aspcare")          public Mono<Void> aspcare(ServerWebExchange ex){ return forward(ex, "/dashboard.html"); }

    @GetMapping("/mainpage")         public Mono<Void> mainpage(ServerWebExchange ex){ return forward(ex, "/mainpage.html"); }
    @GetMapping("/admin")            public Mono<Void> admin(ServerWebExchange ex){ return forward(ex, "/admin.html"); }
    @GetMapping("/login")            public Mono<Void> login(ServerWebExchange ex){ return forward(ex, "/login.html"); }
    @GetMapping("/SignuLogin")       public Mono<Void> signuLogin(ServerWebExchange ex){ return forward(ex, "/SignupLogin.html"); }

    @GetMapping("/selfdrive")        public Mono<Void> selfdrive(ServerWebExchange ex){ return forward(ex, "/selfdrive.html"); }
    @GetMapping("/homewash")         public Mono<Void> homewash(ServerWebExchange ex){ return forward(ex, "/home.html"); }
    @GetMapping("/ASPcareservice")   public Mono<Void> asp(ServerWebExchange ex){ return forward(ex, "/ASPcare.html"); }

    @GetMapping("/selfdrivereview")  public Mono<Void> sdr(ServerWebExchange ex){ return forward(ex, "/selfdrivereview.html"); }
    @GetMapping("/homewashreview")   public Mono<Void> hwr(ServerWebExchange ex){ return forward(ex, "/review.html"); }

    @GetMapping("/payment")          public Mono<Void> payment(ServerWebExchange ex){ return forward(ex, "/payment.html"); }
    @GetMapping("/orders")           public Mono<Void> orders(ServerWebExchange ex){ return forward(ex, "/orders.html"); }

    @GetMapping("/support-chat")     public Mono<Void> supportChat(ServerWebExchange ex){ return forward(ex, "/chatbox.html"); }
    @GetMapping("/admin-chat")       public Mono<Void> adminChat(ServerWebExchange ex){ return forward(ex, "/admin-chat.html"); }

    @GetMapping("/edit-profile")     public Mono<Void> editProfile(ServerWebExchange ex){ return forward(ex, "/edit-profile.html"); }
    @GetMapping("/membership")       public Mono<Void> membership(ServerWebExchange ex){ return forward(ex, "/membership.html"); }

    @GetMapping("/aboutUs")          public Mono<Void> aboutUs(ServerWebExchange ex){ return forward(ex, "/about.html"); }
    @GetMapping("/privacypolicy")    public Mono<Void> privacy(ServerWebExchange ex){ return forward(ex, "/privacypolicy.html"); }
    @GetMapping("/refundpolicy")     public Mono<Void> refund(ServerWebExchange ex){ return forward(ex, "/refundpolicy.html"); }
}

//package com.carwash.controller;
//
//import java.net.URI;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import reactor.core.publisher.Mono;
//
//@Controller
//public class PageRedirectController {
//
//    private Mono<Void> redirect(ServerHttpResponse response, String to) {
//        response.setStatusCode(HttpStatus.FOUND); // 302
//        response.getHeaders().setLocation(URI.create(to));
//        return response.setComplete();
//    }
//
//    @GetMapping("/")                 public Mono<Void> root(ServerHttpResponse r){ return redirect(r, "/dashboard.html"); }
//    @GetMapping("/aspcare")          public Mono<Void> aspcare(ServerHttpResponse r){ return redirect(r, "/dashboard.html"); }
//    @GetMapping("/admin")            public Mono<Void> admin(ServerHttpResponse r){ return redirect(r, "/admin.html"); }
//    @GetMapping("/login")            public Mono<Void> login(ServerHttpResponse r){ return redirect(r, "/login.html"); }
//    @GetMapping("/selfdrive")        public Mono<Void> selfdrive(ServerHttpResponse r){ return redirect(r, "/selfdrive.html"); }
//    @GetMapping("/homewash")         public Mono<Void> homewash(ServerHttpResponse r){ return redirect(r, "/home.html"); }
//    @GetMapping("/orders")           public Mono<Void> orders(ServerHttpResponse r){ return redirect(r, "/orders.html"); }
//    @GetMapping("/support-chat")     public Mono<Void> supportChat(ServerHttpResponse r){ return redirect(r, "/chatbox.html"); }
//    @GetMapping("/admin-chat")       public Mono<Void> adminChat(ServerHttpResponse r){ return redirect(r, "/admin-chat.html"); }
//    @GetMapping("/selfdrivereview")  public Mono<Void> selfdrivereview(ServerHttpResponse r){ return redirect(r, "/selfdrivereview.html"); }
//    @GetMapping("/payment")          public Mono<Void> payment(ServerHttpResponse r){ return redirect(r, "/payment.html"); }
//    @GetMapping("/homewashreview")   public Mono<Void> homewashreview(ServerHttpResponse r){ return redirect(r, "/review.html"); }
//    @GetMapping("/edit-profile")     public Mono<Void> editProfile(ServerHttpResponse r){ return redirect(r, "/edit-profile.html"); }
//    @GetMapping("/ASPcareservice")   public Mono<Void> ASPcareservice(ServerHttpResponse r){ return redirect(r, "/ASPcare.html"); }
//    @GetMapping("/mainpage")         public Mono<Void> mainpage(ServerHttpResponse r){ return redirect(r, "/mainpage.html"); }
//    @GetMapping("/SignuLogin")       public Mono<Void> signuLogin(ServerHttpResponse r){ return redirect(r, "/SignupLogin.html"); }
//    @GetMapping("/aboutUs")          public Mono<Void> aboutUs(ServerHttpResponse r){ return redirect(r, "/about.html"); }
//    @GetMapping("/privacypolicy")    public Mono<Void> privacy(ServerHttpResponse r){ return redirect(r, "/privacypolicy.html"); }
//    @GetMapping("/refundpolicy")     public Mono<Void> refund(ServerHttpResponse r){ return redirect(r, "/refundpolicy.html"); }
//    @GetMapping("/membership")       public Mono<Void> membership(ServerHttpResponse r){ return redirect(r, "/membership.html"); }
//}
