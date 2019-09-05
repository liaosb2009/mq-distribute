package com.study.transaction.dispatch;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DispatchApplication {

	@Bean
	public Queue orderQueue() {
		return new Queue("orderQueue");
	}

	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder(DispatchApplication.class).web(WebApplicationType.SERVLET).run(args);
		System.out.println("#分配外卖小哥的运单系统启动完毕，你可以通过web接口访问我啦");
	}
}
