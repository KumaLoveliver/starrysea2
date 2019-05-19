package top.starrysea.service;

import org.springframework.web.multipart.MultipartFile;
import top.starrysea.common.ServiceResult;
import top.starrysea.object.dto.User;

public interface IUserService {

	ServiceResult registerService(User user);

	ServiceResult checkUserAvailabilityService(User user);

	ServiceResult userLogin(User user);

	ServiceResult activateService(String redisKey);
	
	ServiceResult getUserInfoService(String userId);
	
	ServiceResult editUserInfoService(User user);

	ServiceResult changeUserPasswordService(User user, String newPassword);

	ServiceResult changeAvatarService(MultipartFile multipartFile, User user);
}
