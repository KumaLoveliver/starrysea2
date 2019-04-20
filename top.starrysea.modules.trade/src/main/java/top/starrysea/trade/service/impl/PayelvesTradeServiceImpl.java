package top.starrysea.trade.service.impl;

import static top.starrysea.common.ResultKey.STRING;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import top.starrysea.common.Common;
import top.starrysea.common.ServiceResult;
import top.starrysea.trade.PayelvesPayNotify;
import top.starrysea.trade.PayelvesPayRequest;
import top.starrysea.trade.service.ITradeService;

@Service("payelvesTradeService")
public class PayelvesTradeServiceImpl implements ITradeService {
	@Value("${payelves.openId}")
	private String openId;
	@Value("${payelves.token}")
	private String token;
	@Value("${payelves.uuid}")
	private String uuid;
	@Value("${payelves.clientVersion}")
	private String clientVersion;
	@Value("${payelves.gateway}")
	private String gateway;
	@Value("${payelves.appKey}")
	private String appKey;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	/***
	 * 组装支付url
	 */
	public ServiceResult createPaymentRequestRouteService(PayelvesPayRequest payRequestBo) {
		StringBuffer paramBuffer = new StringBuffer();
		try {
			// appKey
			paramBuffer.append("appKey=" + appKey + "&");
			// backParam
			String backParam = URLEncoder.encode(payRequestBo.getBackPara(), "UTF-8");
			paramBuffer.append("backPara=" + backParam + "&");
			// 商品名称
			paramBuffer.append("body=" + URLEncoder.encode(payRequestBo.getBody(), "UTF-8") + "&");
			// 通道
			paramBuffer.append("channel=" + payRequestBo.getChannel() + "&");
			// 版本号
			paramBuffer.append("clientVersion=" + clientVersion + "&");
			// 订单时间
			paramBuffer.append("dateTime=" + Common.getNowTime() + "&");
			// 开发者ID
			paramBuffer.append("openId=" + openId + "&");
			// 订单ID
			paramBuffer.append("orderId=" + payRequestBo.getOrderId() + "&");
			// 支付类型1：支付宝 2：微信
			paramBuffer.append("payType=" + payRequestBo.getPayType() + "&");
			// 价格
			paramBuffer.append("price=" + payRequestBo.getPrice().intValue() + "&");
			// 商品名称
			paramBuffer.append("subject=" + URLEncoder.encode(payRequestBo.getSubject(), "UTF-8") + "&");
			// 用户ID
			paramBuffer.append("userId=" + payRequestBo.getUserId() + "&");
			// uuid
			paramBuffer.append("uuid=" + uuid + "&");
			paramBuffer.append("sign=" + createSignForRequest(payRequestBo));
			// 网关
			paramBuffer.insert(0, gateway + "?");
		} catch (UnsupportedEncodingException e) {
			return ServiceResult.of(false);
		}
		return ServiceResult.of(true).setResult(STRING, paramBuffer.toString());
	}

	/***
	 * 签名
	 * 
	 * @param payRequestBo
	 * @return
	 */
	private String createSignForRequest(PayelvesPayRequest payRequestBo) {
		StringBuffer paramBuffer = new StringBuffer();
		// appKey
		paramBuffer.append("appKey=" + appKey + "&");
		// backParam
		paramBuffer.append("backPara=" + payRequestBo.getBackPara() + "&");
		// 商品名称
		paramBuffer.append("body=" + payRequestBo.getBody() + "&");
		// 通道
		paramBuffer.append("channel=" + payRequestBo.getChannel() + "&");
		// 版本号
		paramBuffer.append("clientVersion=" + clientVersion + "&");
		// 订单时间
		paramBuffer.append("dateTime=" + Common.getNowTime() + "&");
		// 开发者ID
		paramBuffer.append("openId=" + openId + "&");
		// 订单ID
		paramBuffer.append("orderId=" + payRequestBo.getOrderId() + "&");
		// 支付类型1：支付宝 2：微信
		paramBuffer.append("payType=" + payRequestBo.getPayType() + "&");
		// 价格
		paramBuffer.append("price=" + payRequestBo.getPrice().intValue() + "&");
		// 商品名称
		paramBuffer.append("subject=" + payRequestBo.getSubject() + "&");
		// 用户ID
		paramBuffer.append("userId=" + payRequestBo.getUserId() + "&");
		// uuid
		paramBuffer.append("uuid=" + uuid);
		paramBuffer.append(token);
		return Common.md5(paramBuffer.toString());
	}

	@Override
	public ServiceResult validateNotifyParamService(PayelvesPayNotify payNotify) {
		StringBuilder paramBuilder = new StringBuilder();
		paramBuilder.append("amount=" + payNotify.getAmount() + "&");
		paramBuilder.append("appKey=" + payNotify.getAppKey() + "&");
		paramBuilder.append("backPara=" + payNotify.getBackPara() + "&");
		paramBuilder.append("dateTime=" + payNotify.getDateTime() + "&");
		paramBuilder.append("openId=" + payNotify.getOpenId() + "&");
		paramBuilder.append("orderId=" + payNotify.getOrderId() + "&");
		paramBuilder.append("outTradeNo=" + payNotify.getOutTradeNo() + "&");
		paramBuilder.append("payType=" + payNotify.getPayType() + "&");
		paramBuilder.append("payUserId=" + payNotify.getPayUserId() + "&");
		paramBuilder.append("status=" + payNotify.getStatus() + "&");
		paramBuilder.append("version=" + payNotify.getVersion() + token);
		logger.info("正在对回调信息" + Common.toJson(payNotify) + "验证签名");
		logger.info("签名原文为：" + paramBuilder);
		logger.info("得到签名为：" + Common.md5(paramBuilder.toString()) + ",与原签名" + payNotify.getSign() + "进行对比");
		return payNotify.getSign().equals(Common.md5(paramBuilder.toString())) ? ServiceResult.SUCCESS_SERVICE_RESULT
				: ServiceResult.FAIL_SERVICE_RESULT;
	}

}