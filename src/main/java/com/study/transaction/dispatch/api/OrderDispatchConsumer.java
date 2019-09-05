package com.study.transaction.dispatch.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.study.transaction.dispatch.service.DispatchService;

/**
 * 消费者，取调度队列
 *
 */
@Component
public class OrderDispatchConsumer {
	private final Logger logger = LoggerFactory.getLogger(OrderDispatchConsumer.class);

	@Autowired
	DispatchService dispatchService;

	// spring集成
	@RabbitListener(queues = "orderQueue")
	public void messageConsumer(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
		try {
			// mq里面的数据转为json对象
			JSONObject orderInfo = JSONObject.parseObject(message);
			logger.warn("收到MQ里面的消息：" + orderInfo.toJSONString());
			Thread.sleep(5000L);

			// 执行业务操作，同一个数据不能处理两次，根据业务情况去重，保证幂等性。 （拓展：redis记录处理情况）
			String orderId = orderInfo.getString("orderId");
			// 这里就是一个分配外卖小哥...业务处理
			dispatchService.dispatch(orderId);
			// ack - 告诉MQ，我已经收到啦
			channel.basicAck(tag, false);
		} catch (Exception e) {
			// 处理消息异常了，让MQ重发这条消息，尝试再次处理 。但一定要有个重试次数限制，防止死循环。
			// 【注意】 我们一定要记录重发次数，重试多次不成功则记录该条异常消息，并通知人工处理（可本地记录，也可发到MQ的指定队列）。
			// 记到哪里： 本地库中定义一个异常消息表 或 发到异常信息队列中，或其他
			channel.basicNack(tag, false, false);
		}
		// 如果不给回复，就等这个consumer断开链接后，mq-server会继续推送

	}
}