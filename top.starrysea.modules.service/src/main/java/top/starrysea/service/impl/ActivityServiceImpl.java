package top.starrysea.service.impl;

import static top.starrysea.common.Const.FUNDING_FACTOR;
import static top.starrysea.common.ResultKey.ACTIVITY;
import static top.starrysea.common.ResultKey.DOUBLE;
import static top.starrysea.common.ResultKey.FUNDING;
import static top.starrysea.common.ResultKey.LIST_1;
import static top.starrysea.common.ServiceResult.SUCCESS_SERVICE_RESULT;
import static top.starrysea.dao.impl.ActivityDaoImpl.PAGE_LIMIT;
import static top.starrysea.common.Const.ORIGINAL_FUNDING_QUEUE;
import static top.starrysea.common.Const.CANCEL_FUNDING_QUEUE;
import static top.starrysea.common.Const.FUNDING_EXCHANGE;
import static top.starrysea.common.Const.QUEUE_TIMEOUT;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import top.starrysea.common.Common;
import top.starrysea.common.Condition;
import top.starrysea.common.ServiceResult;
import top.starrysea.dao.IActivityDao;
import top.starrysea.dao.IActivityImageDao;
import top.starrysea.dao.IFundingDao;
import top.starrysea.exception.UpdateException;
import top.starrysea.file.FileCondition;
import top.starrysea.file.FileType;
import top.starrysea.file.FileUtil;
import top.starrysea.mq.MessageSender;
import top.starrysea.object.dto.Activity;
import top.starrysea.object.dto.ActivityImage;
import top.starrysea.object.dto.Funding;
import top.starrysea.service.IActivityService;

@Service("activityService")
public class ActivityServiceImpl implements IActivityService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private IActivityDao activityDao;
	@Autowired
	private IActivityImageDao activityImageDao;
	@Autowired
	private IFundingDao fundingDao;
	@Autowired
	private FileUtil fileUtil;
	@Autowired
	private MessageSender messageSender;

	@Override
	// 查询所有众筹活动
	public ServiceResult queryAllActivityService(Condition condition, Activity activity) {
		Activity a = activityDao.getNewestActivityDao();
		List<Activity> activitylist = activityDao.getAllActivityDao(condition, activity);
		int totalPage = 0;
		int count = activityDao.getActivityCountDao(condition, activity);
		if (count % PAGE_LIMIT == 0)
			totalPage = count / PAGE_LIMIT;
		else
			totalPage = (count / PAGE_LIMIT) + 1;

		return ServiceResult.of(true).setResult(LIST_1, activitylist).setResult(ACTIVITY, a)
				.setNowPage(condition.getPage()).setTotalPage(totalPage);
	}

	@Override
	// 查询一个众筹活动的详情页
	public ServiceResult queryActivityService(Activity activity) {
		ServiceResult result = ServiceResult.of();
		Activity a = activityDao.getActivityDao(activity);
		result.setSuccessed(true).setResult(ACTIVITY, a);
		List<Funding> fundings = fundingDao
				.getAllFundingDao(new Funding.Builder().activity(activity).fundingStatus((short) 2).build());
		double fundingMoneySum = fundings.stream().collect(Collectors.summingDouble(Funding::getFundingMoney));
		double richThreshold = fundingMoneySum * FUNDING_FACTOR;
		List<Funding> richFundings = new ArrayList<>();
		List<Funding> normalFundings = new ArrayList<>();
		for (Funding funding : fundings) {
			if (funding.getFundingMoney() > richThreshold) {
				richFundings.add(funding);
			} else {
				normalFundings.add(funding);
			}
		}
		richFundings.sort((funding1, funding2) -> funding2.getFundingMoney().compareTo(funding1.getFundingMoney()));
		richFundings.addAll(normalFundings);
		result.setResult(LIST_1, richFundings).setResult(DOUBLE, richThreshold);
		return result;
	}

	@Override
	// 添加一个众筹活动
	@Transactional
	public ServiceResult addActivityService(MultipartFile coverFile, Activity activity,
			List<ActivityImage> activityImages) {
		try {
			String originCoverFileName = fileUtil.saveFile(coverFile, FileCondition.of(FileType.IMG, 1, "activity_"));
			activity.setActivityCover(originCoverFileName);
			activity.setActivityStatus((short) 1);
			activity.setActivityId(activityDao.saveActivityDao(activity));
			for (ActivityImage activityImage : activityImages) {
				activityImage.setActivity(activity);
			}
			activityImageDao.saveActivityImageDao(activityImages);
			return SUCCESS_SERVICE_RESULT;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new UpdateException(e);
		}
	}

	@Override
	// 修改一个众筹活动的状态
	public ServiceResult modifyActivityService(Activity activity) {
		if (activity.getActivityStatus() == 3) {
			activity.setActivityEndtime(Common.getNowDate());
		}
		activityDao.updateActivityDao(activity);
		return SUCCESS_SERVICE_RESULT;
	}

	@Override
	// 删除一个众筹活动
	public ServiceResult removeActivityService(Activity activity) {
		activityDao.deleteActivityDao(activity);
		return SUCCESS_SERVICE_RESULT;
	}

	@Override
	@Transactional
	public ServiceResult addFundingService(List<Funding> fundings) {
		try {
			fundings.forEach(funding -> {
				funding.setFundingTime(Common.getNowDate());
				funding.setFundingNum(Common.getCharId(20));
				funding.setFundingStatus((short) 2);
			});
			fundingDao.saveFundingDao(fundings);
			List<Activity> activitys = new ArrayList<>();
			for (Funding funding : fundings) {
				Activity activity = new Activity.Builder().activityMoney(funding.getFundingMoney())
						.activityId(funding.getActivity().getActivityId()).build();
				activitys.add(activity);
			}
			activityDao.updateAddActivityMoneyDao(activitys);
			return SUCCESS_SERVICE_RESULT;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new UpdateException(e);
		}
	}

	@Override
	@Transactional
	public ServiceResult removeFundingService(Funding funding) {
		try {
			fundingDao.deleteFundingDao(funding);
			Activity activity = new Activity.Builder().activityMoney(funding.getFundingMoney())
					.activityId(funding.getActivity().getActivityId()).build();
			activityDao.updateReduceActivityMoneyDao(activity);
			return SUCCESS_SERVICE_RESULT;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new UpdateException(e);
		}

	}

	@Override
	@Transactional
	public ServiceResult participateFundingService(Funding funding) {
		try {
			funding.setFundingTime(Common.getNowDate());
			funding.setFundingNum(Common.getCharId(20));
			funding.setFundingStatus((short) 1);
			funding.setFundingName(funding.getUser().getUsername());
			List<Funding> fundings = new ArrayList<>();
			fundings.add(funding);
			fundingDao.saveFundingDao(fundings);
			messageSender.sendMessage(FUNDING_EXCHANGE, ORIGINAL_FUNDING_QUEUE, Common.toJson(funding), QUEUE_TIMEOUT);
			return ServiceResult.of(true).setResult(FUNDING, funding);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new UpdateException(e);
		}
	}

	@Override
	@Transactional
	public ServiceResult notifyParticipateFundingService(Funding funding) {
		Funding f = fundingDao.getFundingDao(funding);
		if (f.getFundingStatus() == 1) {
			f.setFundingStatus((short) 2);
			fundingDao.updateFundingStatusDao(f);
			Activity activity = new Activity.Builder().activityMoney(f.getFundingMoney())
					.activityId(f.getActivity().getActivityId()).build();
			List<Activity> activities = new ArrayList<>();
			activities.add(activity);
			activityDao.updateAddActivityMoneyDao(activities);
		}
		return ServiceResult.SUCCESS_SERVICE_RESULT;
	}

	@Override
	@RabbitListener(queues = CANCEL_FUNDING_QUEUE)
	public void cancelParticipateFundingService(String message) {
		logger.info("收到取消参与众筹消息:" + message);
		Funding funding = Common.toObject(message, Funding.class);
		Funding f = fundingDao.getFundingDao(funding);
		if (f.getFundingStatus() != 2) {
			f.setFundingStatus((short) 3);
			fundingDao.updateFundingStatusDao(funding);
		}
	}
}
